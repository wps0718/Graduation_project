## Feature F25：定时任务

### 任务规划

**[监督者] 2026-02-22 规划任务：**

该功能包含 5 个定时任务类，用于自动化处理订单超时、自动确认收货、自动好评、商品自动下架和用户注销清理等业务场景。

#### 依赖关系
- 依赖 F01（微信登录）：User 实体、UserMapper
- 依赖 F05（卖家主页与账号管理）：用户注销逻辑
- 依赖 F11（商品发布与编辑）：Product 实体、ProductMapper
- 依赖 F15（订单创建与查询）：TradeOrder 实体、TradeOrderMapper
- 依赖 F16（订单状态管理）：订单状态流转逻辑
- 依赖 F17（评价模块）：Review 实体、ReviewMapper、评分计算逻辑

#### 定时任务概览
| 任务类 | Cron 表达式 | 执行频率 | 业务场景 |
|--------|------------|---------|---------|
| OrderExpireTask | 0 */5 * * * ? | 每5分钟 | 订单超时自动取消 |
| OrderAutoConfirmTask | 0 0 2 * * ? | 每天凌晨2点 | 订单自动确认收货 |
| ReviewAutoTask | 0 0 3 * * ? | 每天凌晨3点 | 订单自动好评 |
| ProductAutoOffTask | 0 0 4 * * ? | 每天凌晨4点 | 商品90天自动下架 |
| UserDeactivateTask | 0 0 5 * * ? | 每天凌晨5点 | 注销账号30天清理 |

---

### 步骤 1：配置定时任务开关

**文件**：`src/main/resources/application.yml`

**新增配置**：
```yaml
# 定时任务开关配置
task:
  enabled:
    order-expire: true          # 订单超时取消任务
    order-auto-confirm: true    # 订单自动确认任务
    review-auto: true           # 自动好评任务
    product-auto-off: true      # 商品自动下架任务
    user-deactivate: true       # 用户注销清理任务
```

**说明**：
- 每个定时任务都有独立的开关，方便在不同环境中灵活控制
- 开发环境可以关闭某些任务，生产环境全部开启
- 任务类中通过 `@ConditionalOnProperty` 注解读取配置

---

### 步骤 2：启动类添加 @EnableScheduling 注解

**文件**：`src/main/java/com/qingyuan/secondhand/SecondhandApplication.java`

**修改内容**：
```java
@SpringBootApplication
@EnableScheduling  // 新增：启用定时任务
@EnableAsync       // 已有：启用异步任务
@MapperScan("com.qingyuan.secondhand.mapper")
public class SecondhandApplication {
    public static void main(String[] args) {
        SpringApplication.run(SecondhandApplication.class, args);
    }
}
```

**说明**：
- `@EnableScheduling` 是 Spring 定时任务的必需注解
- 与 `@EnableAsync` 配合使用，支持异步通知发送

---

### 步骤 3：创建 OrderExpireTask - 订单超时取消任务

**文件**：`src/main/java/com/qingyuan/secondhand/task/OrderExpireTask.java`

**执行频率**：每 5 分钟执行一次

**Cron 表达式**：`0 */5 * * * ?`

**业务逻辑**：
1. 查询条件：`expire_time < NOW() AND status = 1`（待面交状态且已超时）
2. 批量处理：
   - 更新订单 status = 5（已取消）
   - 更新订单 cancel_by = 0（系统取消）
   - 更新订单 cancel_reason = "订单超时未面交，系统自动取消"
   - 恢复商品 status = 1（在售）
3. 通知双方：
   - 通知买家："您的订单已超时，系统已自动取消"
   - 通知卖家："买家订单已超时，系统已自动取消"

**代码结构**：
```java
@Component
@Slf4j
@ConditionalOnProperty(name = "task.enabled.order-expire", havingValue = "true", matchIfMissing = true)
public class OrderExpireTask {
    
    @Autowired
    private TradeOrderMapper tradeOrderMapper;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private NotificationService notificationService;
    
    @Scheduled(cron = "0 */5 * * * ?")
    public void execute() {
        log.info("[订单超时取消任务] 开始执行，时间：{}", LocalDateTime.now());
        long startTime = System.currentTimeMillis();
        int processedCount = 0;
        
        try {
            // 1. 查询超时订单
            LambdaQueryWrapper<TradeOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.lt(TradeOrder::getExpireTime, LocalDateTime.now())
                   .eq(TradeOrder::getStatus, 1);
            List<TradeOrder> expiredOrders = tradeOrderMapper.selectList(wrapper);
            
            if (expiredOrders.isEmpty()) {
                log.info("[订单超时取消任务] 无超时订单");
                return;
            }
            
            // 2. 逐条处理（单条失败不影响其他）
            for (TradeOrder order : expiredOrders) {
                try {
                    // 更新订单状态
                    order.setStatus(5);
                    order.setCancelBy(0L);
                    order.setCancelReason("订单超时未面交，系统自动取消");
                    tradeOrderMapper.updateById(order);
                    
                    // 恢复商品在售状态
                    Product product = productMapper.selectById(order.getProductId());
                    if (product != null) {
                        product.setStatus(1);
                        productMapper.updateById(product);
                    }
                    
                    // 通知买家和卖家
                    notificationService.sendNotification(
                        order.getBuyerId(), 
                        1, 
                        "订单已取消", 
                        "您的订单已超时，系统已自动取消", 
                        order.getId(), 
                        "order", 
                        1
                    );
                    notificationService.sendNotification(
                        order.getSellerId(), 
                        1, 
                        "订单已取消", 
                        "买家订单已超时，系统已自动取消", 
                        order.getId(), 
                        "order", 
                        1
                    );
                    
                    processedCount++;
                } catch (Exception e) {
                    log.error("[订单超时取消任务] 处理订单失败，订单ID：{}，错误：{}", order.getId(), e.getMessage(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("[订单超时取消任务] 执行失败：{}", e.getMessage(), e);
        } finally {
            long endTime = System.currentTimeMillis();
            log.info("[订单超时取消任务] 执行完成，处理条数：{}，耗时：{}ms", processedCount, endTime - startTime);
        }
    }
}
```

**关键点**：
- 使用 `@ConditionalOnProperty` 支持配置开关
- 单条记录处理失败不影响其他记录（try-catch 在循环内部）
- 完善的日志记录（开始时间、处理条数、耗时）
- 使用 MyBatis-Plus 的 LambdaQueryWrapper 构建查询条件

---

### 步骤 4：创建 OrderAutoConfirmTask - 订单自动确认收货任务

**文件**：`src/main/java/com/qingyuan/secondhand/task/OrderAutoConfirmTask.java`

**执行频率**：每天凌晨 2 点执行

**Cron 表达式**：`0 0 2 * * ?`

**业务逻辑**：
1. 查询条件：`confirm_deadline < NOW() AND status = 1`（待面交状态且已超过确认期限）
   - 注意：使用 `confirm_deadline` 字段，而非 `create_time + 7天`
   - `confirm_deadline` 在订单创建时已设置为 `create_time + 7天`
2. 批量处理：
   - 更新订单 status = 3（已完成）
   - 更新订单 complete_time = NOW()
   - 更新商品 status = 3（已售出）
3. 通知双方：
   - 通知买家："订单已超过确认期限，系统已自动确认收货"
   - 通知卖家："订单已超过确认期限，系统已自动确认收货"

**代码结构**：
```java
@Component
@Slf4j
@ConditionalOnProperty(name = "task.enabled.order-auto-confirm", havingValue = "true", matchIfMissing = true)
public class OrderAutoConfirmTask {
    
    @Autowired
    private TradeOrderMapper tradeOrderMapper;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private NotificationService notificationService;
    
    @Scheduled(cron = "0 0 2 * * ?")
    public void execute() {
        log.info("[订单自动确认任务] 开始执行，时间：{}", LocalDateTime.now());
        long startTime = System.currentTimeMillis();
        int processedCount = 0;
        
        try {
            // 1. 查询需要自动确认的订单
            LambdaQueryWrapper<TradeOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.lt(TradeOrder::getConfirmDeadline, LocalDateTime.now())
                   .eq(TradeOrder::getStatus, 1);
            List<TradeOrder> orders = tradeOrderMapper.selectList(wrapper);
            
            if (orders.isEmpty()) {
                log.info("[订单自动确认任务] 无需自动确认的订单");
                return;
            }
            
            // 2. 逐条处理
            for (TradeOrder order : orders) {
                try {
                    // 更新订单状态
                    order.setStatus(3);
                    order.setCompleteTime(LocalDateTime.now());
                    tradeOrderMapper.updateById(order);
                    
                    // 更新商品状态为已售出
                    Product product = productMapper.selectById(order.getProductId());
                    if (product != null) {
                        product.setStatus(3);
                        productMapper.updateById(product);
                    }
                    
                    // 通知买家和卖家
                    notificationService.sendNotification(
                        order.getBuyerId(), 
                        1, 
                        "订单已确认", 
                        "订单已超过确认期限，系统已自动确认收货", 
                        order.getId(), 
                        "order", 
                        1
                    );
                    notificationService.sendNotification(
                        order.getSellerId(), 
                        1, 
                        "订单已确认", 
                        "订单已超过确认期限，系统已自动确认收货", 
                        order.getId(), 
                        "order", 
                        1
                    );
                    
                    processedCount++;
                } catch (Exception e) {
                    log.error("[订单自动确认任务] 处理订单失败，订单ID：{}，错误：{}", order.getId(), e.getMessage(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("[订单自动确认任务] 执行失败：{}", e.getMessage(), e);
        } finally {
            long endTime = System.currentTimeMillis();
            log.info("[订单自动确认任务] 执行完成，处理条数：{}，耗时：{}ms", processedCount, endTime - startTime);
        }
    }
}
```

**关键点**：
- 查询条件使用 `confirm_deadline` 字段（订单创建时已设置）
- 自动确认后订单状态变为"已完成"，商品状态变为"已售出"
- 通知双方用户

---

### 步骤 5：创建 ReviewAutoTask - 自动好评任务

**文件**：`src/main/java/com/qingyuan/secondhand/task/ReviewAutoTask.java`

**执行频率**：每天凌晨 3 点执行

**Cron 表达式**：`0 0 3 * * ?`

**业务逻辑**：
1. 查询条件：`status = 3 AND complete_time + 7天 < NOW()`（已完成但未评价且超过评价窗口期）
2. 对每个订单：
   - 查询该订单的评价记录（review 表）
   - 判断买家和卖家是否已评价
   - 为未评价方生成默认好评：
     - scoreDesc = 5
     - scoreAttitude = 5
     - scoreExperience = 5
     - content = "系统自动好评"
     - isAuto = 1
3. 如果双方都已评价（包括自动生成的），更新订单 status = 4（已评价）
4. 重新计算被评价人的综合评分
5. 通知收到自动好评的用户

**代码结构**：
```java
@Component
@Slf4j
@ConditionalOnProperty(name = "task.enabled.review-auto", havingValue = "true", matchIfMissing = true)
public class ReviewAutoTask {
    
    @Autowired
    private TradeOrderMapper tradeOrderMapper;
    
    @Autowired
    private ReviewMapper reviewMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private NotificationService notificationService;
    
    @Scheduled(cron = "0 0 3 * * ?")
    public void execute() {
        log.info("[自动好评任务] 开始执行，时间：{}", LocalDateTime.now());
        long startTime = System.currentTimeMillis();
        int processedCount = 0;
        
        try {
            // 1. 查询已完成且超过评价窗口期的订单
            LambdaQueryWrapper<TradeOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TradeOrder::getStatus, 3)
                   .lt(TradeOrder::getCompleteTime, LocalDateTime.now().minusDays(7));
            List<TradeOrder> orders = tradeOrderMapper.selectList(wrapper);
            
            if (orders.isEmpty()) {
                log.info("[自动好评任务] 无需自动好评的订单");
                return;
            }
            
            // 2. 逐条处理
            for (TradeOrder order : orders) {
                try {
                    List<Review> reviews = reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                        .eq(Review::getOrderId, order.getId()));
                    
                    boolean buyerReviewed = reviews.stream().anyMatch(r -> r.getReviewerId().equals(order.getBuyerId()));
                    boolean sellerReviewed = reviews.stream().anyMatch(r -> r.getReviewerId().equals(order.getSellerId()));
                    
                    if (!buyerReviewed) {
                        Review review = new Review();
                        review.setOrderId(order.getId());
                        review.setReviewerId(order.getBuyerId());
                        review.setTargetId(order.getSellerId());
                        review.setScoreDesc(5);
                        review.setScoreAttitude(5);
                        review.setScoreExperience(5);
                        review.setContent("系统自动好评");
                        review.setIsAuto(1);
                        reviewMapper.insert(review);
                        notificationService.send(
                            order.getSellerId(),
                            10,
                            "收到评价",
                            "您收到了一条系统自动好评",
                            order.getId(),
                            2,
                            1
                        );
                    }
                    
                    if (!sellerReviewed) {
                        Review review = new Review();
                        review.setOrderId(order.getId());
                        review.setReviewerId(order.getSellerId());
                        review.setTargetId(order.getBuyerId());
                        review.setScoreDesc(5);
                        review.setScoreAttitude(5);
                        review.setScoreExperience(5);
                        review.setContent("系统自动好评");
                        review.setIsAuto(1);
                        reviewMapper.insert(review);
                        notificationService.send(
                            order.getBuyerId(),
                            10,
                            "收到评价",
                            "您收到了一条系统自动好评",
                            order.getId(),
                            2,
                            1
                        );
                    }
                    
                    // 更新订单状态
                    Long reviewCount = reviewMapper.selectCount(new LambdaQueryWrapper<Review>()
                        .eq(Review::getOrderId, order.getId()));
                    if (reviewCount != null && reviewCount >= 2) {
                        TradeOrder updateOrder = new TradeOrder();
                        updateOrder.setId(order.getId());
                        updateOrder.setStatus(4);
                        tradeOrderMapper.updateById(updateOrder);
                    }
                    
                    // 重新计算综合评分
                    Long targetId = order.getSellerId();
                    BigDecimal newScore = calculateUserScore(targetId);
                    User updateUser = new User();
                    updateUser.setId(targetId);
                    updateUser.setScore(newScore);
                    userMapper.updateById(updateUser);
                    
                    processedCount++;
                } catch (Exception e) {
                    log.error("[自动好评任务] 处理订单失败，订单ID：{}，错误：{}", order.getId(), e.getMessage(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("[自动好评任务] 执行失败：{}", e.getMessage(), e);
        } finally {
            long endTime = System.currentTimeMillis();
            log.info("[自动好评任务] 执行完成，处理条数：{}，耗时：{}ms", processedCount, endTime - startTime);
        }
    }
}
```

**关键点**：
- 评价窗口期为订单完成后 7 天
- 自动好评评分全部为 5 分，`isAuto = 1`
- 生成评价后需要重新计算被评价人的综合评分
- 双方都评价后订单状态变为"已评价"

---

### 步骤 6：创建 ProductAutoOffTask - 商品自动下架任务

**文件**：`src/main/java/com/qingyuan/secondhand/task/ProductAutoOffTask.java`

**执行频率**：每天凌晨 4 点执行

**Cron 表达式**：`0 0 4 * * ?`

**业务逻辑**：
1. 查询条件：`auto_off_time < NOW() AND status = 1 AND is_deleted = 0`（在售商品且已到自动下架时间）
2. 批量处理：
   - 更新商品 status = 2（已下架）
3. 通知卖家："您的商品已发布超过90天，系统已自动下架"

**代码结构**：
```java
@Component
@Slf4j
@ConditionalOnProperty(name = "task.enabled.product-auto-off", havingValue = "true", matchIfMissing = true)
public class ProductAutoOffTask {
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private NotificationService notificationService;
    
    @Scheduled(cron = "0 0 4 * * ?")
    public void execute() {
        log.info("[商品自动下架任务] 开始执行，时间：{}", LocalDateTime.now());
        long startTime = System.currentTimeMillis();
        int processedCount = 0;
        
        try {
            // 1. 查询需要自动下架的商品
            LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
            wrapper.lt(Product::getAutoOffTime, LocalDateTime.now())
                   .eq(Product::getStatus, 1)
                   .eq(Product::getIsDeleted, 0);
            List<Product> products = productMapper.selectList(wrapper);
            
            if (products.isEmpty()) {
                log.info("[商品自动下架任务] 无需自动下架的商品");
                return;
            }
            
            // 2. 逐条处理
            for (Product product : products) {
                try {
                    // 更新商品状态为已下架
                    product.setStatus(2);
                    productMapper.updateById(product);
                    
                    // 通知卖家
                    notificationService.sendNotification(
                        product.getUserId(), 
                        2, 
                        "商品已下架", 
                        "您的商品《" + product.getTitle() + "》已发布超过90天，系统已自动下架", 
                        product.getId(), 
                        "product", 
                        2
                    );
                    
                    processedCount++;
                } catch (Exception e) {
                    log.error("[商品自动下架任务] 处理商品失败，商品ID：{}，错误：{}", product.getId(), e.getMessage(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("[商品自动下架任务] 执行失败：{}", e.getMessage(), e);
        } finally {
            long endTime = System.currentTimeMillis();
            log.info("[商品自动下架任务] 执行完成，处理条数：{}，耗时：{}ms", processedCount, endTime - startTime);
        }
    }
}
```

**关键点**：
- 商品发布时 `auto_off_time` 已设置为 `create_time + 90天`
- 只下架在售状态（status=1）且未删除（is_deleted=0）的商品
- 通知卖家商品已自动下架

---

### 步骤 7：创建 UserDeactivateTask - 用户注销清理任务

**文件**：`src/main/java/com/qingyuan/secondhand/task/UserDeactivateTask.java`

**执行频率**：每天凌晨 5 点执行

**Cron 表达式**：`0 0 5 * * ?`

**业务逻辑**：
1. 查询条件：`status = 2 AND deactivate_time + 30天 < NOW()`（注销中状态且已超过30天）
2. 批量处理：
   - 清理个人信息：
     - nick_name = "已注销用户"
     - avatar_url = ""（空字符串）
     - phone = NULL
     - open_id = NULL
     - session_key = NULL
   - 保留交易记录（不删除 user 记录，只清理敏感信息）
   - 更新 status = 0（封禁状态，防止再次登录）
3. 不发送通知（用户已注销）

**代码结构**：
```java
@Component
@Slf4j
@ConditionalOnProperty(name = "task.enabled.user-deactivate", havingValue = "true", matchIfMissing = true)
public class UserDeactivateTask {
    
    @Autowired
    private UserMapper userMapper;
    
    @Scheduled(cron = "0 0 5 * * ?")
    public void execute() {
        log.info("[用户注销清理任务] 开始执行，时间：{}", LocalDateTime.now());
        long startTime = System.currentTimeMillis();
        int processedCount = 0;
        
        try {
            // 1. 查询需要清理的用户（注销中且超过30天）
            LocalDateTime deadline = LocalDateTime.now().minusDays(30);
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getStatus, 2)
                   .lt(User::getDeactivateTime, deadline);
            List<User> users = userMapper.selectList(wrapper);
            
            if (users.isEmpty()) {
                log.info("[用户注销清理任务] 无需清理的用户");
                return;
            }
            
            // 2. 逐条处理
            for (User user : users) {
                try {
                    // 清理个人信息
                    user.setNickName("已注销用户");
                    user.setAvatarUrl("");
                    user.setPhone(null);
                    user.setOpenId(null);
                    user.setSessionKey(null);
                    user.setStatus(0);  // 封禁状态，防止再次登录
                    user.setDeactivateTime(null);
                    userMapper.updateById(user);
                    
                    processedCount++;
                    log.info("[用户注销清理任务] 清理用户成功，用户ID：{}", user.getId());
                } catch (Exception e) {
                    log.error("[用户注销清理任务] 处理用户失败，用户ID：{}，错误：{}", user.getId(), e.getMessage(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("[用户注销清理任务] 执行失败：{}", e.getMessage(), e);
        } finally {
            long endTime = System.currentTimeMillis();
            log.info("[用户注销清理任务] 执行完成，处理条数：{}，耗时：{}ms", processedCount, endTime - startTime);
        }
    }
}
```

**关键点**：
- 注销后 30 天才清理个人信息
- 清理敏感信息但保留用户记录（保留交易历史）
- 清理后状态设为封禁（status=0），防止再次登录
- 不发送通知（用户已注销）

---

### 步骤 8：编写单元测试

**文件**：`src/test/java/com/qingyuan/secondhand/task/ScheduledTaskTest.java`

**测试场景**（共 5 个）：

#### 8.1 testOrderExpireTask - 订单超时取消任务测试
- Mock tradeOrderMapper.selectList() 返回超时订单列表
- Mock productMapper.selectById() 返回商品
- Mock notificationService.sendNotification()
- 调用 orderExpireTask.execute()
- 验证订单状态更新为 5（已取消）
- 验证 cancel_by = 0，cancel_reason 正确
- 验证商品状态恢复为 1（在售）
- 验证通知被调用 2 次（买家+卖家）

#### 8.2 testOrderAutoConfirmTask - 订单自动确认任务测试
- Mock tradeOrderMapper.selectList() 返回需要自动确认的订单
- Mock productMapper.selectById() 返回商品
- Mock notificationService.sendNotification()
- 调用 orderAutoConfirmTask.execute()
- 验证订单状态更新为 3（已完成）
- 验证 complete_time 不为空
- 验证商品状态更新为 3（已售出）
- 验证通知被调用 2 次

#### 8.3 testReviewAutoTask - 自动好评任务测试
- Mock tradeOrderMapper.selectList() 返回超过评价窗口期的订单
- Mock reviewMapper.selectList() 返回空列表（双方都未评价）
- Mock userMapper.selectById() 返回用户
- Mock notificationService.sendNotification()
- 调用 reviewAutoTask.execute()
- 验证生成了 2 条评价记录（买家+卖家）
- 验证评价 scoreDesc/scoreAttitude/scoreExperience 都为 5
- 验证 isAuto = 1
- 验证订单状态更新为 4（已评价）
- 验证用户综合评分被重新计算

#### 8.4 testProductAutoOffTask - 商品自动下架任务测试
- Mock productMapper.selectList() 返回需要下架的商品
- Mock notificationService.sendNotification()
- 调用 productAutoOffTask.execute()
- 验证商品状态更新为 2（已下架）
- 验证通知被调用

#### 8.5 testUserDeactivateTask - 用户注销清理任务测试
- Mock userMapper.selectList() 返回需要清理的用户
- 调用 userDeactivateTask.execute()
- 验证用户信息被清理：
  - nickName = "已注销用户"
  - avatarUrl = ""
  - phone = null
  - openId = null
  - sessionKey = null
  - status = 0
  - deactivateTime = null

**Mock 对象**：
- @Mock TradeOrderMapper tradeOrderMapper
- @Mock ProductMapper productMapper
- @Mock ReviewMapper reviewMapper
- @Mock UserMapper userMapper
- @Mock NotificationService notificationService
- @InjectMocks OrderExpireTask orderExpireTask
- @InjectMocks OrderAutoConfirmTask orderAutoConfirmTask
- @InjectMocks ReviewAutoTask reviewAutoTask
- @InjectMocks ProductAutoOffTask productAutoOffTask
- @InjectMocks UserDeactivateTask userDeactivateTask

**注意事项**：
- 测试时不需要真正等待定时任务触发，直接调用 execute() 方法
- 需要 mock 所有依赖的 Mapper 和 Service
- 验证业务逻辑的正确性，特别是状态流转和通知发送

---

### 步骤 9：运行测试并生成证据包

**操作**：
1. 在终端运行：`mvn test -Dtest=ScheduledTaskTest`
2. 将输出保存到：`run-folder/F25-定时任务/test_output.log`
3. 创建 `run-folder/F25-定时任务/run.sh`，内容：
   ```bash
   #!/bin/bash
   mvn test -Dtest=ScheduledTaskTest
   ```
4. 复制本任务规划到：`run-folder/F25-定时任务/task.md`

---

### 步骤 10：创建审查信号文件

**操作**：
- 在项目根目录创建 `.ready-for-review` 文件
- 文件内容：
  ```
  Feature: F25 定时任务
  Status: 待审查
  Timestamp: [当前时间]
  ```

---

## 核心业务规则

### 1. OrderExpireTask - 订单超时取消
- 查询条件：`expire_time < NOW() AND status = 1`
- 超时时间：订单创建后 72 小时（expire_time 在创建订单时已设置）
- 状态流转：1(待面交) → 5(已取消)
- 商品状态：恢复为 1(在售)
- 取消人：cancel_by = 0（系统）
- 取消原因：cancel_reason = "订单超时未面交，系统自动取消"
- 通知：买家 + 卖家

### 2. OrderAutoConfirmTask - 订单自动确认
- 查询条件：`confirm_deadline < NOW() AND status = 1`
- 确认期限：订单创建后 7 天（confirm_deadline 在创建订单时已设置）
- 状态流转：1(待面交) → 3(已完成)
- 商品状态：更新为 3(已售出)
- 完成时间：complete_time = NOW()
- 通知：买家 + 卖家

### 3. ReviewAutoTask - 自动好评
- 查询条件：`status = 3 AND complete_time + 7天 < NOW()`
- 评价窗口期：订单完成后 7 天
- 自动好评内容：
  - scoreDesc = 5
  - scoreAttitude = 5
  - scoreExperience = 5
  - content = "系统自动好评"
  - isAuto = 1
- 状态流转：3(已完成) → 4(已评价)（双方都评价后）
- 综合评分：重新计算被评价人的平均分，保留 1 位小数
- 通知：被评价人

### 4. ProductAutoOffTask - 商品自动下架
- 查询条件：`auto_off_time < NOW() AND status = 1 AND is_deleted = 0`
- 下架时间：商品发布后 90 天（auto_off_time 在发布时已设置）
- 状态流转：1(在售) → 2(已下架)
- 通知：卖家

### 5. UserDeactivateTask - 用户注销清理
- 查询条件：`status = 2 AND deactivate_time + 30天 < NOW()`
- 清理时间：申请注销后 30 天
- 清理内容：
  - nickName = "已注销用户"
  - avatarUrl = ""
  - phone = NULL
  - openId = NULL
  - sessionKey = NULL
  - status = 0（封禁，防止再次登录）
  - deactivateTime = NULL
- 保留：用户记录（保留交易历史）
- 通知：无（用户已注销）

---

## 数据库字段映射

### trade_order 表
| 数据库字段 | Java 类型 | 说明 |
|-----------|----------|------|
| expire_time | LocalDateTime | 订单超时时间（创建时+72h） |
| confirm_deadline | LocalDateTime | 自动确认期限（创建时+7天） |
| complete_time | LocalDateTime | 订单完成时间 |
| cancel_by | Long | 取消人ID，0=系统 |
| cancel_reason | String | 取消原因 |

### product 表
| 数据库字段 | Java 类型 | 说明 |
|-----------|----------|------|
| auto_off_time | LocalDateTime | 自动下架时间（发布时+90天） |

### review 表
| 数据库字段 | Java 类型 | 说明 |
|-----------|----------|------|
| is_auto | Integer | 是否自动评价：0-否 1-是 |

### user 表
| 数据库字段 | Java 类型 | 说明 |
|-----------|----------|------|
| deactivate_time | LocalDateTime | 申请注销时间 |
| status | Integer | 0-封禁 1-正常 2-注销中 |

---

## 验收标准（来自 feature_list.json）

- [ ] 启动类添加 @EnableScheduling 注解
- [ ] application.yml 中添加定时任务开关配置（task.enabled.xxx=true/false）
- [ ] OrderExpireTask（每5分钟）：查询 expire_time<NOW() 且 status=1 的订单，批量取消，恢复商品在售状态，通知买卖双方
- [ ] OrderAutoConfirmTask（每天凌晨2点）：查询 confirm_deadline<NOW() 且 status=1 的订单，自动确认收货，商品标记已售出，通知双方
- [ ] ReviewAutoTask（每天凌晨3点）：查询 status=3 且 complete_time+7天<NOW() 的订单，为未评价方生成默认好评（5/5/5分，is_auto=1），订单 status→4，重新计算综合评分
- [ ] ProductAutoOffTask（每天凌晨4点）：查询 auto_off_time<NOW() 且 status=1 的商品，批量下架，通知卖家
- [ ] UserDeactivateTask（每天凌晨5点）：查询 status=2 且 deactivate_time+30天<NOW() 的用户，清理个人信息但保留交易记录
- [ ] 每个定时任务包含完善的日志记录（开始时间、处理条数、结束时间）
- [ ] 单条记录处理失败不影响其他记录（try-catch 在循环内部）
- [ ] 编写单元测试，验证各定时任务的业务逻辑正确性

---

## 文件清单

### 需要创建的文件
1. `src/main/java/com/qingyuan/secondhand/task/OrderExpireTask.java`
2. `src/main/java/com/qingyuan/secondhand/task/OrderAutoConfirmTask.java`
3. `src/main/java/com/qingyuan/secondhand/task/ReviewAutoTask.java`
4. `src/main/java/com/qingyuan/secondhand/task/ProductAutoOffTask.java`
5. `src/main/java/com/qingyuan/secondhand/task/UserDeactivateTask.java`
6. `src/test/java/com/qingyuan/secondhand/task/ScheduledTaskTest.java`

### 需要修改的文件
1. `src/main/java/com/qingyuan/secondhand/SecondhandApplication.java` - 添加 @EnableScheduling 注解
2. `src/main/resources/application.yml` - 添加定时任务开关配置

---

## 技术要点

### 1. Cron 表达式
- `0 */5 * * * ?` - 每 5 分钟执行一次
- `0 0 2 * * ?` - 每天凌晨 2 点执行
- `0 0 3 * * ?` - 每天凌晨 3 点执行
- `0 0 4 * * ?` - 每天凌晨 4 点执行
- `0 0 5 * * ?` - 每天凌晨 5 点执行

### 2. 配置开关
- 使用 `@ConditionalOnProperty` 注解
- 配置格式：`task.enabled.xxx=true/false`
- 默认值：`matchIfMissing = true`（配置不存在时默认启用）

### 3. 异常处理
- 外层 try-catch：捕获整个任务执行的异常
- 内层 try-catch：捕获单条记录处理的异常
- 单条失败不影响其他记录的处理

### 4. 日志记录
- 任务开始：记录开始时间
- 任务结束：记录处理条数和耗时
- 单条失败：记录错误日志（包含记录ID和错误信息）

### 5. 时间计算
- 使用 `LocalDateTime.now()` 获取当前时间
- 使用 `LocalDateTime.minusDays(n)` 计算 n 天前的时间
- 使用 `LocalDateTime.plusDays(n)` 计算 n 天后的时间（订单创建时使用）

### 6. 批量处理
- 使用 MyBatis-Plus 的 LambdaQueryWrapper 构建查询条件
- 使用 for 循环逐条处理（而非批量更新）
- 原因：需要对每条记录执行不同的业务逻辑（如通知、关联更新）

### 7. 通知发送
- 使用 NotificationService.sendNotification() 方法
- 该方法已配置 @Async，不会阻塞定时任务执行
- 通知失败不影响主业务逻辑

---

## 注意事项

1. **@EnableScheduling 注解必须添加到启动类上**
2. **Cron 表达式必须正确**（特别是凌晨执行的任务）
3. **查询条件必须精确**（时间比较、状态判断）
4. **单条记录处理失败不能影响其他记录**（try-catch 在循环内部）
5. **日志记录必须完善**（开始时间、处理条数、耗时、错误信息）
6. **通知发送不能阻塞主业务**（NotificationService 已配置 @Async）
7. **时间字段使用 LocalDateTime**（而非 Date）
8. **配置开关必须在 application.yml 中定义**
9. **测试时直接调用 execute() 方法**（不需要等待定时任务触发）
10. **OrderAutoConfirmTask 使用 confirm_deadline 字段**（而非 create_time + 7天）
11. **ReviewAutoTask 需要判断买家和卖家是否已评价**（避免重复生成）
12. **UserDeactivateTask 清理信息但保留用户记录**（保留交易历史）
13. **所有业务异常使用 BusinessException 抛出**（但定时任务中应 catch 住，记录日志）
