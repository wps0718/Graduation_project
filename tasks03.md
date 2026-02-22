# 轻院二手 - 开发任务记录（第三期）

## 项目信息
- 项目名称：二手交易平台（毕业设计）
- 项目路径：G:\Code\Graduation_project
- 根包名：com.qingyuan.secondhand
- 技术栈：Spring Boot 3.x + MyBatis-Plus + MySQL 5.7 + Redis
- 构建工具：Maven

---

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
            // 1. 查询需要自动好评的订单（已完成且超过评价窗口期）
            LocalDateTime deadline = LocalDateTime.now().minusDays(7);
            LambdaQueryWrapper<TradeOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TradeOrder::getStatus, 3)
                   .lt(TradeOrder::getCompleteTime, deadline);
            List<TradeOrder> orders = tradeOrderMapper.selectList(wrapper);
            
            if (orders.isEmpty()) {
                log.info("[自动好评任务] 无需自动好评的订单");
                return;
            }
            
            // 2. 逐条处理
            for (TradeOrder order : orders) {
                try {
                    // 查询该订单的评价记录
                    LambdaQueryWrapper<Review> reviewWrapper = new LambdaQueryWrapper<>();
                    reviewWrapper.eq(Review::getOrderId, order.getId());
                    List<Review> reviews = reviewMapper.selectList(reviewWrapper);
                    
                    // 判断买家和卖家是否已评价
                    boolean buyerReviewed = reviews.stream()
                        .anyMatch(r -> r.getReviewerId().equals(order.getBuyerId()));
                    boolean sellerReviewed = reviews.stream()
                        .anyMatch(r -> r.getReviewerId().equals(order.getSellerId()));
                    
                    // 为未评价方生成默认好评
                    if (!buyerReviewed) {
                        createAutoReview(order, order.getBuyerId(), order.getSellerId());
                        processedCount++;
                    }
                    if (!sellerReviewed) {
                        createAutoReview(order, order.getSellerId(), order.getBuyerId());
                        processedCount++;
                    }
                    
                    // 如果双方都已评价（包括自动生成的），更新订单状态
                    if (!buyerReviewed || !sellerReviewed) {
                        order.setStatus(4);
                        tradeOrderMapper.updateById(order);
                    }
                    
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
    
    /**
     * 创建自动好评
     */
    private void createAutoReview(TradeOrder order, Long reviewerId, Long targetId) {
        // 1. 创建评价记录
        Review review = new Review();
        review.setOrderId(order.getId());
        review.setReviewerId(reviewerId);
        review.setTargetId(targetId);
        review.setScoreDesc(5);
        review.setScoreAttitude(5);
        review.setScoreExperience(5);
        review.setContent("系统自动好评");
        review.setIsAuto(1);
        reviewMapper.insert(review);
        
        // 2. 重新计算被评价人的综合评分
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getTargetId, targetId);
        List<Review> targetReviews = reviewMapper.selectList(wrapper);
        
        double totalScore = 0.0;
        for (Review r : targetReviews) {
            double avgScore = (r.getScoreDesc() + r.getScoreAttitude() + r.getScoreExperience()) / 3.0;
            totalScore += avgScore;
        }
        BigDecimal newScore = BigDecimal.valueOf(totalScore / targetReviews.size())
            .setScale(1, RoundingMode.HALF_UP);
        
        User targetUser = userMapper.selectById(targetId);
        if (targetUser != null) {
            targetUser.setScore(newScore);
            userMapper.updateById(targetUser);
        }
        
        // 3. 通知被评价人
        notificationService.sendNotification(
            targetId, 
            10, 
            "收到评价", 
            "您收到了一条系统自动好评", 
            order.getId(), 
            "order", 
            1
        );
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
14. **验证 updateById() 方法的调用次数和参数**
15. **确保 NotificationService 已在 F19 中实现并配置 @Async**

---

**规划完成时间**：2026-02-22
**规划人**：监督者（Kiro IDE）

---

### 开发进展

#### 2026-02-22 执行记录

- 完成 F25 定时任务全部实现与配置：OrderExpireTask、OrderAutoConfirmTask、ReviewAutoTask、ProductAutoOffTask、UserDeactivateTask
- 定时任务开关已配置在 application.yml，启动类已启用定时任务
- ReviewAutoTask 自动好评逻辑包含自动评价、订单状态更新、用户评分重算、通知发送
- ProductAutoOffTask 支持 90 天自动下架并通知卖家
- UserDeactivateTask 清理注销 30 天用户敏感信息并保留交易记录
- 新增 ScheduledTaskTest 覆盖 5 个定时任务核心逻辑
- 已运行命令：
  - mvn test -Dtest=ScheduledTaskTest 2>&1 | tee run-folder/F25-定时任务/test_output.log
  - mvn compile -q
- 测试结果：Tests run: 5, Failures: 0, Errors: 0, Skipped: 0

---

### 审查记录

（监督者在此追加审查意见）

---

---

**[监督者] 2026-02-22 14:57: ✅ 功能「F25 定时任务」验收通过。**

#### 多维度审查结果

##### 4.1 MyBatis-Plus 规范审查：✅
- ✅ 所有定时任务类正确使用 MyBatis-Plus 的 LambdaQueryWrapper 构建查询条件
- ✅ 使用 MyBatis-Plus 内置方法（selectList, selectById, updateById, insert）
- ✅ 条件查询使用类型安全的 Lambda 表达式（而非字符串拼接）
- ✅ 正确使用枚举类（OrderStatus, ProductStatus, UserStatus, NotificationCategory）

##### 4.2 功能正确性审查：✅
- ✅ **OrderExpireTask**：
  - Cron 表达式正确：`0 */5 * * * ?`（每5分钟）
  - 查询条件精确：`expire_time < NOW() AND status = 1`
  - 状态流转正确：订单 status→5（已取消），商品 status→1（在售）
  - cancel_by = 0L（系统取消）
  - cancel_reason = "订单超时未面交，系统自动取消"
  - 通知买家和卖家各1次

- ✅ **OrderAutoConfirmTask**：
  - Cron 表达式正确：`0 0 2 * * ?`（每天凌晨2点）
  - 查询条件精确：`confirm_deadline < NOW() AND status = 1`（使用 confirm_deadline 字段）
  - 状态流转正确：订单 status→3（已完成），商品 status→3（已售出）
  - 设置 completeTime = NOW()
  - 通知买家和卖家各1次

- ✅ **ReviewAutoTask**：
  - Cron 表达式正确：`0 0 3 * * ?`（每天凌晨3点）
  - 查询条件精确：`status = 3 AND complete_time < NOW() - 7天`
  - 自动好评评分：scoreDesc=5, scoreAttitude=5, scoreExperience=5
  - isAuto = 1（标识为自动评价）
  - content = "系统自动好评"
  - 判断买家和卖家是否已评价（避免重复生成）
  - 双方都评价后订单 status→4（已评价）
  - 重新计算被评价人综合评分（使用 BigDecimal，保留1位小数）
  - 通知被评价人

- ✅ **ProductAutoOffTask**：
  - Cron 表达式正确：`0 0 4 * * ?`（每天凌晨4点）
  - 查询条件精确：`auto_off_time < NOW() AND status = 1 AND is_deleted = 0`
  - 状态流转正确：商品 status→2（已下架）
  - 通知卖家，包含商品标题

- ✅ **UserDeactivateTask**：
  - Cron 表达式正确：`0 0 5 * * ?`（每天凌晨5点）
  - 查询条件精确：`status = 2 AND deactivate_time < NOW() - 30天`
  - 清理个人信息：nickName="已注销用户", avatarUrl="", phone=null, openId=null, sessionKey=null
  - 状态更新：status→0（封禁，防止再次登录）
  - deactivateTime = null
  - 保留用户记录（保留交易历史）
  - 不发送通知

##### 4.3 安全性审查：✅
- ✅ 所有查询使用 LambdaQueryWrapper，类型安全
- ✅ 无 SQL 注入风险
- ✅ 敏感信息清理正确（UserDeactivateTask）

##### 4.4 代码质量审查：✅
- ✅ 所有定时任务类使用 @Component 注解
- ✅ 使用 @ConditionalOnProperty 支持配置开关
- ✅ 使用 @RequiredArgsConstructor 进行依赖注入
- ✅ 使用 @Slf4j 进行日志记录
- ✅ 单条记录处理失败不影响其他（try-catch 在循环内部）
- ✅ 完善的日志记录：开始时间、处理条数、耗时、错误信息
- ✅ 命名规范，代码清晰
- ✅ 异常处理合理（外层 try-catch + 内层 try-catch）

##### 4.5 测试审查（反作弊）：✅
- ✅ 测试文件存在：ScheduledTaskTest.java
- ✅ 测试用例数量：5 个测试方法（覆盖所有定时任务）
- ✅ 断言有实际意义：
  - testOrderExpireTask：断言 status=5, cancelBy=0, cancelReason正确, 商品status=1, 通知2次
  - testOrderAutoConfirmTask：断言 status=3, completeTime不为空, 商品status=3, 通知2次
  - testReviewAutoTask：断言生成2条评价, 评分5/5/5, isAuto=1, 订单status=4, 用户评分更新2次, 通知2次
  - testProductAutoOffTask：断言 status=2, 通知1次
  - testUserDeactivateTask：断言所有字段清理正确, status=0, deactivateTime=null
- ✅ Mock 配置正确：
  - Mock 所有 Mapper 和 NotificationService
  - 使用 ArgumentCaptor 捕获更新参数
  - 验证方法调用次数和参数
- ✅ 测试覆盖了所有 acceptance_criteria

##### 4.6 数据库一致性审查：✅
- ✅ 查询条件与数据库字段一致
- ✅ 时间字段使用 LocalDateTime
- ✅ 枚举值使用正确（OrderStatus, ProductStatus, UserStatus）
- ✅ OrderAutoConfirmTask 使用 confirm_deadline 字段（而非 create_time + 7天）

##### 4.7 配置审查：✅
- ✅ 启动类添加了 @EnableScheduling 注解
- ✅ application.yml 中配置了定时任务开关：
  ```yaml
  task:
    enabled:
      order-expire: true
      order-auto-confirm: true
      review-auto: true
      product-auto-off: true
      user-deactivate: true
  ```

##### 4.8 证据包审查：✅
- ✅ `run-folder/F25-定时任务/` 目录完整
- ✅ `test_output.log` 包含 `BUILD SUCCESS`
- ✅ 测试结果：`Tests run: 5, Failures: 0, Errors: 0, Skipped: 0`
- ✅ `run.sh` 文件存在
- ✅ `task.md` 文件存在

##### 4.9 独立复跑验证：✅
- ✅ 在 Kiro 终端执行：`mvn test -Dtest=ScheduledTaskTest`
- ✅ 测试结果：`Tests run: 5, Failures: 0, Errors: 0, Skipped: 0`
- ✅ 构建状态：`BUILD SUCCESS`
- ✅ 执行时间：7.747s

#### 验收标准逐项检查（来自 feature_list.json）

1. ✅ 启动类添加 @EnableScheduling 注解
   - SecondhandApplication.java 中已添加 @EnableScheduling

2. ✅ application.yml 中添加定时任务开关配置（task.enabled.xxx=true/false）
   - 所有5个定时任务都有独立开关配置

3. ✅ OrderExpireTask（每5分钟）：查询 expire_time<NOW() 且 status=1 的订单，批量取消，恢复商品在售状态，通知买卖双方
   - 查询条件：`lt(TradeOrder::getExpireTime, LocalDateTime.now()).eq(TradeOrder::getStatus, 1)`
   - 订单 status→5, cancelBy=0, cancelReason="订单超时未面交，系统自动取消"
   - 商品 status→1（在售）
   - 通知买家和卖家各1次

4. ✅ OrderAutoConfirmTask（每天凌晨2点）：查询 confirm_deadline<NOW() 且 status=1 的订单，自动确认收货，商品标记已售出，通知双方
   - 查询条件：`lt(TradeOrder::getConfirmDeadline, LocalDateTime.now()).eq(TradeOrder::getStatus, 1)`
   - 订单 status→3, completeTime=NOW()
   - 商品 status→3（已售出）
   - 通知买家和卖家各1次

5. ✅ ReviewAutoTask（每天凌晨3点）：查询 status=3 且 complete_time+7天<NOW() 的订单，为未评价方生成默认好评（5/5/5分，is_auto=1），订单 status→4，重新计算综合评分
   - 查询条件：`eq(TradeOrder::getStatus, 3).lt(TradeOrder::getCompleteTime, LocalDateTime.now().minusDays(7))`
   - 判断买家和卖家是否已评价
   - 生成默认好评：scoreDesc=5, scoreAttitude=5, scoreExperience=5, content="系统自动好评", isAuto=1
   - 双方都评价后订单 status→4
   - 重新计算综合评分：使用 BigDecimal，保留1位小数

6. ✅ ProductAutoOffTask（每天凌晨4点）：查询 auto_off_time<NOW() 且 status=1 的商品，批量下架，通知卖家
   - 查询条件：`lt(Product::getAutoOffTime, LocalDateTime.now()).eq(Product::getStatus, 1).eq(Product::getIsDeleted, 0)`
   - 商品 status→2（已下架）
   - 通知卖家，包含商品标题

7. ✅ UserDeactivateTask（每天凌晨5点）：查询 status=2 且 deactivate_time+30天<NOW() 的用户，清理个人信息但保留交易记录
   - 查询条件：`eq(User::getStatus, 2).lt(User::getDeactivateTime, LocalDateTime.now().minusDays(30))`
   - 清理：nickName="已注销用户", avatarUrl="", phone=null, openId=null, sessionKey=null
   - status→0（封禁），deactivateTime=null
   - 保留用户记录

8. ✅ 每个定时任务包含完善的日志记录（开始时间、处理条数、结束时间）
   - 所有任务都有：log.info("[任务名] 开始执行，时间：{}", start)
   - 所有任务都有：log.info("[任务名] 执行完成，处理条数：{}，耗时：{}ms", processedCount, endTime - startTime)
   - 错误日志：log.error("[任务名] 处理XX失败，ID：{}，错误：{}", id, e.getMessage(), e)

9. ✅ 单条记录处理失败不影响其他记录（try-catch 在循环内部）
   - 所有任务都在 for 循环内部使用 try-catch
   - 单条失败只记录错误日志，不影响其他记录处理

10. ✅ 编写单元测试，验证各定时任务的业务逻辑正确性
    - ScheduledTaskTest.java 包含5个测试方法
    - 所有测试通过，无失败、无错误、无跳过

#### 特别说明

1. **Cron 表达式**：
   - 所有 Cron 表达式格式正确
   - 凌晨执行的任务时间错开（2点、3点、4点、5点），避免资源竞争

2. **查询条件**：
   - OrderAutoConfirmTask 正确使用 confirm_deadline 字段（而非 create_time + 7天）
   - 所有时间比较使用 LocalDateTime.now()，无时区问题
   - 查询条件精确，使用 LambdaQueryWrapper 类型安全

3. **状态流转**：
   - 所有状态流转逻辑正确
   - 使用枚举类而非硬编码数字

4. **异常处理**：
   - 外层 try-catch 捕获整个任务执行异常
   - 内层 try-catch 捕获单条记录处理异常
   - 单条失败不影响其他记录

5. **日志记录**：
   - 所有任务都有完善的日志
   - 包含开始时间、处理条数、耗时
   - 错误日志包含记录ID和错误信息

6. **通知发送**：
   - 使用 NotificationService.send() 方法
   - 该方法已配置 @Async，不会阻塞定时任务执行

7. **代码亮点**：
   - 使用 @ConditionalOnProperty 支持配置开关
   - 使用 @RequiredArgsConstructor 简化依赖注入
   - 使用枚举类提高代码可读性
   - ReviewAutoTask 的评分计算使用 BigDecimal 保证精度
   - UserDeactivateTask 清理敏感信息但保留交易记录

#### 审查结论

**✅ 通过验收**

该功能代码质量优秀，完全符合项目规范和验收标准。所有5个定时任务实现正确，Cron 表达式准确，查询条件精确，状态流转合理，异常处理完善，日志记录详细，测试覆盖全面。

---

**审查人**：监督者（Kiro IDE）  
**审查时间**：2026-02-22 14:57  
**独立复跑**：✅ 通过（5/5 测试通过）

---


---

**[监督者] 2026-02-22 最终评价：✅ F25 定时任务执行者完成情况评估**

#### 执行完成度检查

##### 1. 代码实现检查：✅ 全部完成
- ✅ OrderExpireTask.java - 订单超时取消任务（每5分钟）
- ✅ OrderAutoConfirmTask.java - 订单自动确认任务（每天凌晨2点）
- ✅ ReviewAutoTask.java - 自动好评任务（每天凌晨3点）
- ✅ ProductAutoOffTask.java - 商品自动下架任务（每天凌晨4点）
- ✅ UserDeactivateTask.java - 用户注销清理任务（每天凌晨5点）
- ✅ ScheduledTaskTest.java - 单元测试（5个测试方法）

##### 2. 配置检查：✅ 全部正确
- ✅ SecondhandApplication.java 已添加 @EnableScheduling 注解
- ✅ application.yml 已配置定时任务开关（task.enabled.*）

##### 3. 代码质量检查：✅ 优秀

**MyBatis-Plus 规范：**
- ✅ 所有任务类正确使用 LambdaQueryWrapper 构建查询条件
- ✅ 使用枚举类（OrderStatus, ProductStatus, UserStatus, NotificationCategory）
- ✅ 使用 MyBatis-Plus 内置方法（selectList, selectById, updateById, insert）
- ✅ 条件查询类型安全，无字符串拼接

**代码结构：**
- ✅ 使用 @Component + @Slf4j + @RequiredArgsConstructor 注解
- ✅ 使用 @ConditionalOnProperty 支持配置开关
- ✅ 使用 @Scheduled 注解配置 Cron 表达式

**异常处理：**
- ✅ 外层 try-catch 捕获整体异常
- ✅ 内层 try-catch 在循环内部，单条失败不影响其他
- ✅ 完善的日志记录（开始时间、处理条数、耗时）

**业务逻辑正确性：**
- ✅ OrderExpireTask：查询条件 `expire_time < NOW() AND status = 1`，状态流转正确
- ✅ OrderAutoConfirmTask：查询条件 `confirm_deadline < NOW() AND status = 1`（使用 confirm_deadline 字段）
- ✅ ReviewAutoTask：自动好评评分 5/5/5，isAuto=1，双方都评价后订单 status→4，重新计算用户评分
- ✅ ProductAutoOffTask：查询条件 `auto_off_time < NOW() AND status = 1 AND is_deleted = 0`
- ✅ UserDeactivateTask：清理敏感信息，保留用户记录，status→0（封禁）

##### 4. 测试检查：✅ 覆盖全面

**测试用例：**
- ✅ testOrderExpireTask - 验证订单超时取消逻辑
- ✅ testOrderAutoConfirmTask - 验证订单自动确认逻辑
- ✅ testReviewAutoTask - 验证自动好评逻辑（生成2条评价，订单状态更新，用户评分更新）
- ✅ testProductAutoOffTask - 验证商品自动下架逻辑
- ✅ testUserDeactivateTask - 验证用户注销清理逻辑

**测试质量：**
- ✅ 使用 Mockito 正确 mock 所有依赖
- ✅ 使用 ArgumentCaptor 捕获更新参数
- ✅ 断言有实际意义，验证具体业务数据
- ✅ 验证方法调用次数和参数
- ✅ 测试结果：Tests run: 5, Failures: 0, Errors: 0, Skipped: 0

##### 5. 证据包检查：✅ 完整
- ✅ run-folder/F25-定时任务/ 目录存在
- ✅ run.sh 脚本存在
- ✅ task.md 任务规划文档存在
- ✅ test_output.log 包含 BUILD SUCCESS

##### 6. 特别审查项

**ReviewAutoTask 自动好评逻辑：✅**
- ✅ 评分：scoreDesc=5, scoreAttitude=5, scoreExperience=5
- ✅ isAuto=1（标识为自动评价）
- ✅ content="系统自动好评"
- ✅ 判断买家和卖家是否已评价（避免重复生成）
- ✅ 双方都评价后订单 status→4（已评价）
- ✅ 重新计算被评价人综合评分（使用 BigDecimal，保留1位小数）
- ✅ 通知被评价人

**Cron 表达式检查：✅**
- ✅ OrderExpireTask: `0 */5 * * * ?`（每5分钟）
- ✅ OrderAutoConfirmTask: `0 0 2 * * ?`（每天凌晨2点）
- ✅ ReviewAutoTask: `0 0 3 * * ?`（每天凌晨3点）
- ✅ ProductAutoOffTask: `0 0 4 * * ?`（每天凌晨4点）
- ✅ UserDeactivateTask: `0 0 5 * * ?`（每天凌晨5点）

**时间字段使用：✅**
- ✅ OrderAutoConfirmTask 使用 confirm_deadline 字段（而非 create_time + 7天）
- ✅ 所有时间字段使用 LocalDateTime 类型
- ✅ 时间比较使用 lt() 方法

**通知发送：✅**
- ✅ 所有任务正确调用 notificationService.send()
- ✅ 通知参数正确（userId, type, title, content, relatedId, relatedType, category）
- ✅ UserDeactivateTask 不发送通知（用户已注销）

#### 验收标准逐项检查

- ✅ 启动类添加 @EnableScheduling 注解
- ✅ application.yml 中添加定时任务开关配置（task.enabled.xxx=true/false）
- ✅ OrderExpireTask（每5分钟）：查询 expire_time<NOW() 且 status=1 的订单，批量取消，恢复商品在售状态，通知买卖双方
- ✅ OrderAutoConfirmTask（每天凌晨2点）：查询 confirm_deadline<NOW() 且 status=1 的订单，自动确认收货，商品标记已售出，通知双方
- ✅ ReviewAutoTask（每天凌晨3点）：查询 status=3 且 complete_time+7天<NOW() 的订单，为未评价方生成默认好评（5/5/5分，is_auto=1），订单 status→4，重新计算综合评分
- ✅ ProductAutoOffTask（每天凌晨4点）：查询 auto_off_time<NOW() 且 status=1 的商品，批量下架，通知卖家
- ✅ UserDeactivateTask（每天凌晨5点）：查询 status=2 且 deactivate_time+30天<NOW() 的用户，清理个人信息但保留交易记录
- ✅ 每个定时任务包含完善的日志记录（开始时间、处理条数、结束时间）
- ✅ 单条记录处理失败不影响其他记录（try-catch 在循环内部）
- ✅ 编写单元测试，验证各定时任务的业务逻辑正确性

#### 总体评价

执行者完美完成了 F25 定时任务的所有要求：

1. **代码实现**：5个定时任务类全部实现，逻辑正确，代码质量高
2. **配置完整**：启动类注解、application.yml 配置全部到位
3. **测试覆盖**：5个测试用例全部通过，断言有实际意义
4. **证据包完整**：目录结构规范，测试输出包含 BUILD SUCCESS
5. **特别审查项**：ReviewAutoTask 自动好评逻辑完全符合要求，Cron 表达式全部正确

**无任何问题，可以直接通过验收。**

---


---

## Feature F26：OpenIM集成（最后一个功能）

### 任务规划

**[监督者] 2026-02-22 规划任务：**

该功能集成 OpenIM 实现即时通讯，包括用户注册 IM 账号和获取 IM Token。这是项目的最后一个功能。

#### 依赖关系
- 依赖 F01（微信登录）：User 实体、UserService、UserServiceImpl

#### 功能概述
- 配置 OpenIM 服务地址和管理员令牌
- 用户注册/首次登录时自动注册 IM 账号
- 提供获取 IM Token 的接口
- OpenIM API 调用失败时降级处理（不影响主业务）

---

### 前置准备：获取 OpenIM 配置

**重要说明**：在开始开发前，需要先获取 OpenIM 的配置信息。

#### 方案一：使用 OpenIM 官方服务（推荐用于开发测试）
1. 访问 OpenIM 官网：https://www.openim.io/
2. 注册开发者账号并创建应用
3. 获取配置：API 地址、Admin Token、App ID

#### 方案二：自建 OpenIM 服务（推荐用于生产环境）
1. 部署 OpenIM Server（Docker 或源码编译）
2. 配置数据库和 Redis
3. 生成 Admin Token

#### 方案三：使用测试配置（仅开发阶段）
使用占位符配置，后续替换为实际值。

---

### 步骤 1：配置 OpenIM 参数

**文件**：`src/main/resources/application.yml`

**新增配置**：
```yaml
# OpenIM 配置
openim:
  api-url: "http://localhost:10002"  # OpenIM API 地址
  admin-token: "your-admin-token-here"  # 管理员令牌
  secret: "your-secret-key"  # 密钥（可选）
  enabled: true  # 是否启用 OpenIM 集成
```

**说明**：
- `api-url`：OpenIM 服务的 API 地址
- `admin-token`：管理员令牌，用于调用 OpenIM 管理接口
- `secret`：密钥，用于签名验证（如需要）
- `enabled`：开关，方便在不同环境中启用/禁用 OpenIM 功能

---

### 步骤 2：创建 OpenImConfig 配置类

**文件**：`src/main/java/com/qingyuan/secondhand/config/OpenImConfig.java`

**功能**：从 application.yml 读取 OpenIM 配置

**代码结构**：
```java
@Configuration
@ConfigurationProperties(prefix = "openim")
@Data
public class OpenImConfig {
    private String apiUrl;
    private String adminToken;
    private String secret;
    private Boolean enabled = true;
}
```

**说明**：
- 使用 `@ConfigurationProperties` 自动绑定配置
- 提供 getter/setter 方法供其他类使用
- `enabled` 默认为 true

---

### 步骤 3：创建 ImService 接口

**文件**：`src/main/java/com/qingyuan/secondhand/service/ImService.java`

**方法定义**：
```java
public interface ImService {
    /**
     * 注册 IM 用户
     * @param userId 用户ID
     * @param nickname 昵称
     * @param faceURL 头像URL
     * @return 是否成功
     */
    boolean registerUser(Long userId, String nickname, String faceURL);
    
    /**
     * 获取用户 Token
     * @param userId 用户ID
     * @return IM Token
     */
    String getUserToken(Long userId);
}
```

**说明**：
- `registerUser`：注册 IM 用户，失败返回 false
- `getUserToken`：获取用户 IM Token，失败返回 null

---

### 步骤 4：实现 ImServiceImpl

**文件**：`src/main/java/com/qingyuan/secondhand/service/impl/ImServiceImpl.java`

**依赖**：
- OpenImConfig：读取配置
- RestTemplate：调用 OpenIM API

**核心逻辑**：

#### 4.1 registerUser 方法
1. 检查 OpenIM 是否启用（`openImConfig.getEnabled()`）
2. 构建请求参数：
   ```json
   {
     "userID": "用户ID（字符串）",
     "nickname": "昵称",
     "faceURL": "头像URL"
   }
   ```
3. 调用 OpenIM API：`POST {apiUrl}/user/user_register`
4. 请求头添加：`operationID`（UUID）、`token`（adminToken）
5. 处理响应：
   - 成功：返回 true
   - 失败：记录日志，返回 false（降级处理）

#### 4.2 getUserToken 方法
1. 检查 OpenIM 是否启用
2. 构建请求参数：
   ```json
   {
     "userID": "用户ID（字符串）",
     "platformID": 1
   }
   ```
3. 调用 OpenIM API：`POST {apiUrl}/auth/user_token`
4. 请求头添加：`operationID`、`token`
5. 处理响应：
   - 成功：返回 token 字段
   - 失败：记录日志，返回 null（降级处理）

**降级处理**：
- 所有 OpenIM API 调用失败时，记录错误日志
- 不抛出异常，不影响主业务流程
- 返回默认值（false 或 null）

**代码结构**：
```java
@Service
@Slf4j
@RequiredArgsConstructor
public class ImServiceImpl implements ImService {
    
    private final OpenImConfig openImConfig;
    private final RestTemplate restTemplate;
    
    @Override
    public boolean registerUser(Long userId, String nickname, String faceURL) {
        if (!Boolean.TRUE.equals(openImConfig.getEnabled())) {
            log.info("[OpenIM] 功能未启用，跳过用户注册");
            return false;
        }
        try {
            // 构建请求
            // 调用 API
            // 处理响应
            return true;
        } catch (Exception e) {
            log.error("[OpenIM] 注册用户失败，userId: {}, 错误: {}", userId, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public String getUserToken(Long userId) {
        if (!Boolean.TRUE.equals(openImConfig.getEnabled())) {
            log.info("[OpenIM] 功能未启用，跳过获取Token");
            return null;
        }
        try {
            // 构建请求
            // 调用 API
            // 处理响应
            return token;
        } catch (Exception e) {
            log.error("[OpenIM] 获取Token失败，userId: {}, 错误: {}", userId, e.getMessage(), e);
            return null;
        }
    }
}
```

---

### 步骤 5：修改 UserServiceImpl - 自动注册 IM 用户

**文件**：`src/main/java/com/qingyuan/secondhand/service/impl/UserServiceImpl.java`

**修改位置**：
1. 微信登录方法（`wxLogin`）
2. 短信验证码登录方法（`smsLogin`）

**修改逻辑**：
- 在用户首次注册时（新建用户记录后），调用 `imService.registerUser()`
- 传入参数：userId、nickName、avatarUrl
- IM 注册失败不影响主业务（降级处理）

**代码示例**：
```java
// 在 wxLogin 方法中，创建新用户后
if (isNewUser) {
    User newUser = new User();
    // ... 设置用户信息
    userMapper.insert(newUser);
    
    // 注册 IM 用户（降级处理，失败不影响主业务）
    try {
        boolean imRegistered = imService.registerUser(
            newUser.getId(), 
            newUser.getNickName(), 
            newUser.getAvatarUrl()
        );
        if (imRegistered) {
            log.info("[OpenIM] 用户 IM 注册成功，userId: {}", newUser.getId());
        } else {
            log.warn("[OpenIM] 用户 IM 注册失败，userId: {}", newUser.getId());
        }
    } catch (Exception e) {
        log.error("[OpenIM] 用户 IM 注册异常，userId: {}, 错误: {}", newUser.getId(), e.getMessage());
    }
}
```

**注意事项**：
- 使用 try-catch 包裹 IM 注册调用
- 失败时记录日志，但不抛出异常
- 不影响用户登录流程

---

### 步骤 6：创建 MiniImController

**文件**：`src/main/java/com/qingyuan/secondhand/controller/mini/MiniImController.java`

**接口**：`GET /mini/im/token`

**功能**：获取当前用户的 IM Token

**代码结构**：
```java
@RestController
@RequestMapping("/mini/im")
@RequiredArgsConstructor
public class MiniImController {
    
    private final ImService imService;
    
    @GetMapping("/token")
    public Result<String> getToken() {
        Long userId = UserContext.getUserId();
        String token = imService.getUserToken(userId);
        if (token == null) {
            throw new BusinessException("获取IM Token失败");
        }
        return Result.success(token);
    }
}
```

**说明**：
- 从 UserContext 获取当前用户ID
- 调用 ImService.getUserToken() 获取 Token
- Token 为 null 时抛出 BusinessException
- 成功返回 Token 字符串

---

### 步骤 7：配置 RestTemplate Bean

**文件**：`src/main/java/com/qingyuan/secondhand/config/OpenImConfig.java` 或新建 `RestTemplateConfig.java`

**说明**：ImService 需要使用 RestTemplate 调用 OpenIM API

**代码**：
```java
@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

**或者在 OpenImConfig 中添加**：
```java
@Configuration
@ConfigurationProperties(prefix = "openim")
@Data
public class OpenImConfig {
    private String apiUrl;
    private String adminToken;
    private String secret;
    private Boolean enabled = true;
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

---

### 步骤 8：编写单元测试

**文件**：`src/test/java/com/qingyuan/secondhand/service/ImServiceTest.java`

**测试场景**（共 4 个）：

#### 8.1 testRegisterUser_Success - 注册成功
- Mock OpenImConfig.getEnabled() 返回 true
- Mock RestTemplate.postForEntity() 返回成功响应
- 调用 imService.registerUser()
- 验证返回 true
- 验证 RestTemplate 被调用 1 次

#### 8.2 testRegisterUser_Disabled - OpenIM 未启用
- Mock OpenImConfig.getEnabled() 返回 false
- 调用 imService.registerUser()
- 验证返回 false
- 验证 RestTemplate 未被调用

#### 8.3 testGetUserToken_Success - 获取 Token 成功
- Mock OpenImConfig.getEnabled() 返回 true
- Mock RestTemplate.postForEntity() 返回包含 token 的响应
- 调用 imService.getUserToken()
- 验证返回的 token 不为 null
- 验证 RestTemplate 被调用 1 次

#### 8.4 testGetUserToken_ApiFailure - API 调用失败（降级处理）
- Mock OpenImConfig.getEnabled() 返回 true
- Mock RestTemplate.postForEntity() 抛出异常
- 调用 imService.getUserToken()
- 验证返回 null（降级处理）
- 验证日志记录了错误信息

**Mock 对象**：
- @Mock OpenImConfig openImConfig
- @Mock RestTemplate restTemplate
- @InjectMocks ImServiceImpl imService

**注意事项**：
- 测试降级处理逻辑（API 失败不影响主业务）
- 验证 OpenIM 启用/禁用开关
- 验证 RestTemplate 调用参数

---

### 步骤 9：编写 Controller 测试

**文件**：`src/test/java/com/qingyuan/secondhand/controller/mini/MiniImControllerTest.java`

**测试场景**（共 2 个）：

#### 9.1 testGetToken_Success - 获取 Token 成功
- Mock UserContext.getUserId() 返回 10001L
- Mock imService.getUserToken() 返回 "test-token-123"
- 调用 GET /mini/im/token
- 验证响应状态 200
- 验证返回的 token 为 "test-token-123"

#### 9.2 testGetToken_Failure - 获取 Token 失败
- Mock UserContext.getUserId() 返回 10001L
- Mock imService.getUserToken() 返回 null
- 调用 GET /mini/im/token
- 验证抛出 BusinessException
- 验证错误信息为 "获取IM Token失败"

**使用 MockMvc 测试**：
```java
@WebMvcTest(MiniImController.class)
class MiniImControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ImService imService;
    
    // 测试方法...
}
```

---

### 步骤 10：运行测试并生成证据包

**操作**：
1. 在终端运行：`mvn test -Dtest=ImServiceTest,MiniImControllerTest`
2. 将输出保存到：`run-folder/F26-OpenIM集成/test_output.log`
3. 创建 `run-folder/F26-OpenIM集成/run.sh`，内容：
   ```bash
   #!/bin/bash
   mvn test -Dtest=ImServiceTest,MiniImControllerTest
   ```
4. 复制本任务规划到：`run-folder/F26-OpenIM集成/task.md`

---

### 步骤 11：创建审查信号文件

**操作**：
- 在项目根目录创建 `.ready-for-review` 文件
- 文件内容：
  ```
  Feature: F26 OpenIM集成
  Status: 待审查
  Timestamp: [当前时间]
  ```

---

## OpenIM API 说明

### API 端点

#### 1. 注册用户
- **URL**：`POST {apiUrl}/user/user_register`
- **请求头**：
  ```
  Content-Type: application/json
  operationID: {UUID}
  token: {adminToken}
  ```
- **请求体**：
  ```json
  {
    "userID": "10001",
    "nickname": "用户昵称",
    "faceURL": "https://example.com/avatar.jpg"
  }
  ```
- **响应**：
  ```json
  {
    "errCode": 0,
    "errMsg": "success"
  }
  ```

#### 2. 获取用户 Token
- **URL**：`POST {apiUrl}/auth/user_token`
- **请求头**：
  ```
  Content-Type: application/json
  operationID: {UUID}
  token: {adminToken}
  ```
- **请求体**：
  ```json
  {
    "userID": "10001",
    "platformID": 1
  }
  ```
- **响应**：
  ```json
  {
    "errCode": 0,
    "errMsg": "success",
    "data": {
      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "expireTimeSeconds": 86400
    }
  }
  ```

### platformID 说明
- 1: iOS
- 2: Android
- 3: Windows
- 4: macOS
- 5: Web
- 6: 小程序
- 7: Linux

---

## 验收标准（来自 feature_list.json）

- [ ] OpenImConfig 从 application.yml 读取 OpenIM 服务地址、adminToken 等配置
- [ ] 用户注册/首次登录时自动调用 OpenIM API 注册 IM 用户（userId 作为 IM 的 userId）
- [ ] GET /mini/im/token 接口调用 OpenIM admin API 获取用户 UserToken 并返回
- [ ] OpenIM API 调用失败时记录日志但不影响主业务流程（降级处理）
- [ ] ImService 中的 OpenIM API 调用使用 RestTemplate
- [ ] 编写 Service 层单元测试，mock OpenIM API 调用

---

## 文件清单

### 需要创建的文件
1. `src/main/java/com/qingyuan/secondhand/config/OpenImConfig.java`
2. `src/main/java/com/qingyuan/secondhand/service/ImService.java`
3. `src/main/java/com/qingyuan/secondhand/service/impl/ImServiceImpl.java`
4. `src/main/java/com/qingyuan/secondhand/controller/mini/MiniImController.java`
5. `src/test/java/com/qingyuan/secondhand/service/ImServiceTest.java`
6. `src/test/java/com/qingyuan/secondhand/controller/mini/MiniImControllerTest.java`

### 需要修改的文件
1. `src/main/resources/application.yml` - 添加 OpenIM 配置
2. `src/main/java/com/qingyuan/secondhand/service/impl/UserServiceImpl.java` - 用户注册时调用 IM 注册

---

## 技术要点

### 1. RestTemplate 使用
- 使用 `postForEntity()` 方法调用 OpenIM API
- 设置请求头：Content-Type、operationID、token
- 处理响应：解析 JSON 获取结果

### 2. 降级处理
- 所有 OpenIM API 调用使用 try-catch 包裹
- 失败时记录日志，返回默认值（false 或 null）
- 不抛出异常，不影响主业务流程

### 3. 配置开关
- 使用 `openim.enabled` 配置项控制功能启用/禁用
- 未启用时跳过所有 OpenIM 操作

### 4. 用户ID 转换
- 数据库中用户ID为 Long 类型
- OpenIM API 要求 userID 为字符串类型
- 调用时需要转换：`String.valueOf(userId)`

### 5. operationID
- 每次 API 调用生成唯一的 operationID（UUID）
- 用于追踪和调试 API 调用

---

## 注意事项

1. **OpenIM 配置必须正确**：
   - api-url 必须是可访问的 OpenIM 服务地址
   - admin-token 必须是有效的管理员令牌
   - 配置错误会导致所有 API 调用失败

2. **降级处理必须完善**：
   - 所有 OpenIM API 调用失败不能影响主业务
   - 用户登录、获取 Token 等操作失败时要有明确的日志
   - 测试时要验证降级处理逻辑

3. **RestTemplate Bean 必须配置**：
   - 确保 Spring 容器中有 RestTemplate Bean
   - 可以在 OpenImConfig 或单独的配置类中定义

4. **UserServiceImpl 修改要谨慎**：
   - 只在新用户注册时调用 IM 注册
   - 使用 try-catch 包裹，失败不影响登录
   - 记录详细的日志

5. **测试要覆盖降级场景**：
   - 测试 OpenIM 未启用的情况
   - 测试 API 调用失败的情况
   - 验证日志记录是否正确

6. **platformID 选择**：
   - 小程序端建议使用 platformID = 6
   - 可以根据实际情况调整

---

**规划完成时间**：2026-02-22
**规划人**：监督者（Kiro IDE）

---

### 开发进展

（执行者在此追加进展记录，以 `[执行者]` 为前缀）

---

### 审查记录

（监督者在此追加审查意见，以 `[监督者]` 为前缀）

---
