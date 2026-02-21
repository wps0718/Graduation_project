### 评价模块 - 任务规划

**防重复评价**：
- review 表有唯一索引：`idx_order_reviewer(order_id, reviewer_id)`
- 同一用户对同一订单只能评价一次

---

### 步骤 1：创建 Review 实体类

**文件**：`src/main/java/com/qingyuan/secondhand/entity/Review.java`

**字段定义**：
```java
@TableId(type = IdType.AUTO)
private Long id;

private Long orderId;          // 订单ID
private Long reviewerId;       // 评价人ID
private Long targetId;         // 被评价人ID

private Integer scoreDesc;     // 商品描述相符 1-5分
private Integer scoreAttitude; // 沟通态度 1-5分
private Integer scoreExperience; // 交易体验 1-5分

@Size(max = 200, message = "评价内容不能超过200字")
private String content;        // 评价内容（可选）

private Integer isAuto;        // 是否自动评价 0-否 1-是

@TableField(fill = FieldFill.INSERT)
private LocalDateTime createTime; // 评价时间
```

**注解**：
- @Data
- @TableName("review")
- @TableId(type = IdType.AUTO)
- @TableField(fill = FieldFill.INSERT)

**说明**：
- scoreDesc、scoreAttitude、scoreExperience 范围 1-5
- content 可选，最大 200 字
- isAuto 用于标识是否为定时任务自动生成的好评（F25 定时任务使用）

---

### 步骤 2：创建 ReviewSubmitDTO

**文件**：`src/main/java/com/qingyuan/secondhand/dto/ReviewSubmitDTO.java`

**字段定义**：
```java
@NotNull(message = "订单ID不能为空")
private Long orderId;

@NotNull(message = "商品描述评分不能为空")
@Min(value = 1, message = "评分最低为1分")
@Max(value = 5, message = "评分最高为5分")
private Integer scoreDesc;

@NotNull(message = "沟通态度评分不能为空")
@Min(value = 1, message = "评分最低为1分")
@Max(value = 5, message = "评分最高为5分")
private Integer scoreAttitude;

@NotNull(message = "交易体验评分不能为空")
@Min(value = 1, message = "评分最低为1分")
@Max(value = 5, message = "评分最高为5分")
private Integer scoreExperience;

@Size(max = 200, message = "评价内容不能超过200字")
private String content;  // 可选
```

**注解**：
- @Data
- 参数校验注解

---

### 步骤 3：创建 ReviewDetailVO

**文件**：`src/main/java/com/qingyuan/secondhand/vo/ReviewDetailVO.java`

**字段定义**：
```java
// 我的评价
private Long myReviewId;
private Integer myScoreDesc;
private Integer myScoreAttitude;
private Integer myScoreExperience;
private String myContent;
private LocalDateTime myCreateTime;

// 对方的评价
private Long otherReviewId;
private Integer otherScoreDesc;
private Integer otherScoreAttitude;
private Integer otherScoreExperience;
private String otherContent;
private LocalDateTime otherCreateTime;

// 订单状态
private Integer orderStatus;  // 3-已完成 4-已评价
```

**注解**：
- @Data

**说明**：
- 订单 status=3 时，只返回自己的评价（对方字段为 null）
- 订单 status=4 时，返回双方的评价

---
### 步骤 4：创建 ReviewMapper 接口

**文件**：`src/main/java/com/qingyuan/secondhand/mapper/ReviewMapper.java`

**接口定义**：
```java
public interface ReviewMapper extends BaseMapper<Review> {
    /**
     * 查询订单的所有评价
     */
    List<Review> selectByOrderId(@Param("orderId") Long orderId);
    
    /**
     * 查询用户收到的所有评价（用于计算综合评分）
     */
    List<Review> selectByTargetId(@Param("targetId") Long targetId);
}
```

**注解**：
- @Mapper

**说明**：
- 继承 BaseMapper<Review>
- 简单查询使用 MyBatis-Plus 内置方法
- 复杂查询可在 ReviewMapper.xml 中编写 SQL

---

### 步骤 5：在 ReviewService 中定义方法签名

**文件**：`src/main/java/com/qingyuan/secondhand/service/ReviewService.java`

**接口定义**：
```java
public interface ReviewService extends IService<Review> {
    /**
     * 提交评价
     */
    void submitReview(ReviewSubmitDTO dto);
    
    /**
     * 查看订单评价详情
     */
    ReviewDetailVO getReviewDetail(Long orderId);
}
```

**注解**：
- 继承 IService<Review>

---

### 步骤 6：实现 ReviewServiceImpl

**文件**：`src/main/java/com/qingyuan/secondhand/service/impl/ReviewServiceImpl.java`

**依赖注入**：
```java
@Autowired
private ReviewMapper reviewMapper;

@Autowired
private TradeOrderMapper tradeOrderMapper;

@Autowired
private UserMapper userMapper;

@Autowired
private NotificationService notificationService;  // 预留通知服务
```

**实现要点**：

#### 6.1 submitReview - 提交评价

**业务逻辑**：
1. 获取当前用户ID：`Long currentUserId = UserContext.getCurrentUserId()`
2. 查询订单并校验存在：`tradeOrderMapper.selectById(dto.getOrderId())`
3. 校验权限：
   - `currentUserId != order.getBuyerId() && currentUserId != order.getSellerId()` 抛出异常"无权评价该订单"
4. 校验订单状态：
   - `order.getStatus() != 3 && order.getStatus() != 4` 抛出异常"订单状态不正确"
5. 校验评价窗口期：
   - `LocalDateTime deadline = order.getCompleteTime().plusDays(7)`
   - `LocalDateTime.now().isAfter(deadline)` 抛出异常"评价窗口期已过"
6. 防重复评价：
   - 使用 LambdaQueryWrapper 查询：`reviewMapper.selectOne(new LambdaQueryWrapper<Review>().eq(Review::getOrderId, dto.getOrderId()).eq(Review::getReviewerId, currentUserId))`
   - 如果已存在，抛出异常"您已评价过该订单"
7. 确定被评价人：
   - `Long targetId = currentUserId.equals(order.getBuyerId()) ? order.getSellerId() : order.getBuyerId()`
8. 创建评价记录：
   ```java
   Review review = new Review();
   review.setOrderId(dto.getOrderId());
   review.setReviewerId(currentUserId);
   review.setTargetId(targetId);
   review.setScoreDesc(dto.getScoreDesc());
   review.setScoreAttitude(dto.getScoreAttitude());
   review.setScoreExperience(dto.getScoreExperience());
   review.setContent(dto.getContent());
   review.setIsAuto(0);
   reviewMapper.insert(review);
   ```
9. 检查双方是否都评价了：
   - 查询该订单的所有评价：`List<Review> reviews = reviewMapper.selectByOrderId(dto.getOrderId())`
   - 如果 `reviews.size() == 2`，更新订单 status=4(已评价)
10. 重新计算被评价人的综合评分：
    - 查询被评价人收到的所有评价：`List<Review> targetReviews = reviewMapper.selectByTargetId(targetId)`
    - 计算平均分：
      ```java
      double totalScore = 0.0;
      for (Review r : targetReviews) {
          double avgScore = (r.getScoreDesc() + r.getScoreAttitude() + r.getScoreExperience()) / 3.0;
          totalScore += avgScore;
      }
      BigDecimal newScore = BigDecimal.valueOf(totalScore / targetReviews.size())
          .setScale(1, RoundingMode.HALF_UP);
      ```
    - 更新用户表：
      ```java
      User targetUser = userMapper.selectById(targetId);
      targetUser.setScore(newScore);
      userMapper.updateById(targetUser);
      ```
11. 预留通知调用：
    ```java
    // TODO: 通知对方
    // notificationService.sendNotification(targetId, 
    //     NotificationType.REVIEW_RECEIVED, 
    //     "您收到了新的评价", 
    //     dto.getOrderId());
    ```

**注解**：
- @Transactional（涉及 review、trade_order、user 三张表的更新）

**注意事项**：
- 评价窗口期为 complete_time + 7 天
- 防重复评价使用数据库唯一索引 + 代码校验双重保证
- 综合评分保留 1 位小数，使用 BigDecimal 避免精度问题
- 双方都评价后才更新订单 status=4

---

#### 6.2 getReviewDetail - 查看订单评价详情

**业务逻辑**：
1. 获取当前用户ID
2. 查询订单并校验存在
3. 校验权限：`currentUserId != order.getBuyerId() && currentUserId != order.getSellerId()` 抛出异常"无权查看该订单评价"
4. 查询该订单的所有评价：`List<Review> reviews = reviewMapper.selectByOrderId(orderId)`
5. 构建 ReviewDetailVO：
   ```java
   ReviewDetailVO vo = new ReviewDetailVO();
   vo.setOrderStatus(order.getStatus());
   
   for (Review review : reviews) {
       if (review.getReviewerId().equals(currentUserId)) {
           // 我的评价
           vo.setMyReviewId(review.getId());
           vo.setMyScoreDesc(review.getScoreDesc());
           vo.setMyScoreAttitude(review.getScoreAttitude());
           vo.setMyScoreExperience(review.getScoreExperience());
           vo.setMyContent(review.getContent());
           vo.setMyCreateTime(review.getCreateTime());
       } else {
           // 对方的评价
           vo.setOtherReviewId(review.getId());
           vo.setOtherScoreDesc(review.getScoreDesc());
           vo.setOtherScoreAttitude(review.getScoreAttitude());
           vo.setOtherScoreExperience(review.getScoreExperience());
           vo.setOtherContent(review.getContent());
           vo.setOtherCreateTime(review.getCreateTime());
       }
   }
   ```
6. 返回 ReviewDetailVO

**注意事项**：
- 订单 status=3 时，只有一条评价，对方字段为 null
- 订单 status=4 时，有两条评价，双方字段都有值
- 不需要事务

---
### 步骤 7：创建 MiniReviewController

**文件**：`src/main/java/com/qingyuan/secondhand/controller/mini/MiniReviewController.java`

**注解**：
- @RestController
- @RequestMapping("/mini/review")
- @RequiredArgsConstructor

**接口定义**：

#### 7.1 提交评价
```java
@PostMapping("/submit")
public Result<Void> submitReview(@Valid @RequestBody ReviewSubmitDTO dto) {
    reviewService.submitReview(dto);
    return Result.success();
}
```

#### 7.2 查看订单评价详情
```java
@GetMapping("/detail/{orderId}")
public Result<ReviewDetailVO> getReviewDetail(@PathVariable Long orderId) {
    ReviewDetailVO detail = reviewService.getReviewDetail(orderId);
    return Result.success(detail);
}
```

**参数说明**：
- 提交评价：接收 ReviewSubmitDTO（包含 orderId、三个评分、content）
- 查看评价：接收 orderId 路径参数

---

### 步骤 8：编写单元测试

**文件**：`src/test/java/com/qingyuan/secondhand/service/impl/ReviewServiceImplTest.java`

**测试场景**（共 12 个）：

#### 8.1 testSubmitReview_Success_FirstReview - 首次评价成功（买家评价卖家）
- Mock UserContext.getCurrentUserId() 返回 10001L（买家）
- Mock tradeOrderMapper.selectById() 返回订单（buyerId=10001L, sellerId=10002L, status=3, completeTime=3天前）
- Mock reviewMapper.selectOne() 返回 null（未评价过）
- Mock reviewMapper.selectByOrderId() 返回空列表（对方也未评价）
- 调用 submitReview(dto)
- 验证评价记录创建：reviewerId=10001L, targetId=10002L, isAuto=0
- 验证订单状态未变（仍为 status=3）
- 验证 reviewMapper.insert() 被调用
- 验证 tradeOrderMapper.updateById() 未被调用（因为只有一方评价）

#### 8.2 testSubmitReview_Success_SecondReview - 第二次评价成功（卖家评价买家，双方都评价后订单status→4）
- Mock UserContext.getCurrentUserId() 返回 10002L（卖家）
- Mock tradeOrderMapper.selectById() 返回订单（buyerId=10001L, sellerId=10002L, status=3）
- Mock reviewMapper.selectOne() 返回 null（卖家未评价过）
- Mock reviewMapper.selectByOrderId() 返回 1 条评价（买家已评价）
- 第二次调用 selectByOrderId() 返回 2 条评价（模拟插入后的状态）
- 调用 submitReview(dto)
- 验证评价记录创建：reviewerId=10002L, targetId=10001L
- 验证订单状态更新：status=4
- 验证 tradeOrderMapper.updateById() 被调用

#### 8.3 testSubmitReview_Unauthorized - 无权评价该订单
- Mock UserContext.getCurrentUserId() 返回 10003L（第三方用户）
- Mock tradeOrderMapper.selectById() 返回订单（buyerId=10001L, sellerId=10002L）
- 调用 submitReview(dto)
- 断言抛出 BusinessException("无权评价该订单")

#### 8.4 testSubmitReview_WrongStatus - 订单状态不正确
- Mock UserContext.getCurrentUserId() 返回 10001L（买家）
- Mock tradeOrderMapper.selectById() 返回订单（status=1, 待面交）
- 调用 submitReview(dto)
- 断言抛出 BusinessException("订单状态不正确")

#### 8.5 testSubmitReview_WindowExpired - 评价窗口期已过
- Mock UserContext.getCurrentUserId() 返回 10001L（买家）
- Mock tradeOrderMapper.selectById() 返回订单（status=3, completeTime=8天前）
- 调用 submitReview(dto)
- 断言抛出 BusinessException("评价窗口期已过")

#### 8.6 testSubmitReview_Duplicate - 重复评价
- Mock UserContext.getCurrentUserId() 返回 10001L（买家）
- Mock tradeOrderMapper.selectById() 返回订单（status=3）
- Mock reviewMapper.selectOne() 返回已有评价记录
- 调用 submitReview(dto)
- 断言抛出 BusinessException("您已评价过该订单")

#### 8.7 testSubmitReview_ScoreCalculation - 评分计算正确性
- Mock UserContext.getCurrentUserId() 返回 10001L（买家）
- Mock tradeOrderMapper.selectById() 返回订单（buyerId=10001L, sellerId=10002L, status=3）
- Mock reviewMapper.selectOne() 返回 null
- Mock reviewMapper.selectByTargetId(10002L) 返回 2 条历史评价：
  - 评价1：scoreDesc=5, scoreAttitude=4, scoreExperience=5（平均 4.67）
  - 评价2：scoreDesc=4, scoreAttitude=4, scoreExperience=4（平均 4.0）
- 调用 submitReview(dto)，dto 评分为 5/5/5（平均 5.0）
- 验证用户综合评分更新为：(4.67 + 4.0 + 5.0) / 3 = 4.6（保留 1 位小数）
- 验证 userMapper.updateById() 被调用，参数 score=4.6

#### 8.8 testSubmitReview_FirstReviewDefaultScore - 首次收到评价时评分计算
- Mock reviewMapper.selectByTargetId(10002L) 返回 1 条评价（当前评价）
- 评分为 4/5/4（平均 4.33）
- 验证用户综合评分更新为 4.3（保留 1 位小数）

#### 8.9 testGetReviewDetail_OnlyMyReview - 查看评价（只有自己评价了）
- Mock UserContext.getCurrentUserId() 返回 10001L（买家）
- Mock tradeOrderMapper.selectById() 返回订单（buyerId=10001L, sellerId=10002L, status=3）
- Mock reviewMapper.selectByOrderId() 返回 1 条评价（reviewerId=10001L）
- 调用 getReviewDetail(orderId)
- 验证返回 VO：
  - orderStatus=3
  - myReviewId 不为空
  - otherReviewId 为 null

#### 8.10 testGetReviewDetail_BothReviewed - 查看评价（双方都评价了）
- Mock UserContext.getCurrentUserId() 返回 10001L（买家）
- Mock tradeOrderMapper.selectById() 返回订单（status=4）
- Mock reviewMapper.selectByOrderId() 返回 2 条评价：
  - 评价1：reviewerId=10001L（我的评价）
  - 评价2：reviewerId=10002L（对方的评价）
- 调用 getReviewDetail(orderId)
- 验证返回 VO：
  - orderStatus=4
  - myReviewId 不为空
  - otherReviewId 不为空

#### 8.11 testGetReviewDetail_Unauthorized - 无权查看评价
- Mock UserContext.getCurrentUserId() 返回 10003L（第三方用户）
- Mock tradeOrderMapper.selectById() 返回订单（buyerId=10001L, sellerId=10002L）
- 调用 getReviewDetail(orderId)
- 断言抛出 BusinessException("无权查看该订单评价")

#### 8.12 testGetReviewDetail_NoReview - 查看评价（双方都未评价）
- Mock UserContext.getCurrentUserId() 返回 10001L（买家）
- Mock tradeOrderMapper.selectById() 返回订单（status=3）
- Mock reviewMapper.selectByOrderId() 返回空列表
- 调用 getReviewDetail(orderId)
- 验证返回 VO：
  - orderStatus=3
  - myReviewId 为 null
  - otherReviewId 为 null

**Mock 对象**：
- @Mock ReviewMapper reviewMapper
- @Mock TradeOrderMapper tradeOrderMapper
- @Mock UserMapper userMapper
- @Mock NotificationService notificationService
- @InjectMocks ReviewServiceImpl reviewService

**注意事项**：
- 需要 mock UserContext 静态方法
- 需要 mock 订单、评价、用户的查询结果
- 需要验证 insert() 和 updateById() 方法的调用次数和参数
- 评分计算使用 BigDecimal，测试时需要精确比较
- 通知服务的调用可以先注释掉（TODO）

---

### 步骤 9：运行测试并生成证据包

**操作**：
1. 在终端运行：`mvn test -Dtest=ReviewServiceImplTest`
2. 将输出保存到：`run-folder/F17-评价模块/test_output.log`
3. 创建 `run-folder/F17-评价模块/run.sh`，内容：
   ```bash
   #!/bin/bash
   mvn test -Dtest=ReviewServiceImplTest
   ```
4. 复制本任务规划到：`run-folder/F17-评价模块/task.md`

---

### 步骤 10：创建审查信号文件

**操作**：
- 在项目根目录创建 `.ready-for-review` 文件
- 文件内容：
  ```
  Feature: F17 评价模块
  Status: 待审查
  Timestamp: [当前时间]
  ```

---
## 关键业务规则

1. **评价权限**：
   - 只有订单的买家或卖家可以评价
   - 第三方用户无权评价或查看评价

2. **评价窗口期**：
   - 订单确认收货后（status=3），双方有 7 天时间互评
   - 窗口期计算：complete_time + 7 天
   - 超过窗口期不允许评价（由定时任务自动好评）

3. **防重复评价**：
   - 数据库唯一索引：`idx_order_reviewer(order_id, reviewer_id)`
   - 代码校验：查询是否已存在评价记录
   - 双重保证防止重复评价

4. **订单状态流转**：
   - 一方评价：订单 status 保持为 3(已完成)
   - 双方都评价：订单 status 更新为 4(已评价)

5. **评分计算**：
   - 单次评价综合分 = (scoreDesc + scoreAttitude + scoreExperience) / 3.0
   - 用户综合评分 = 所有收到评价的平均值
   - 保留 1 位小数，使用 BigDecimal 避免精度问题
   - 无评价的用户默认评分 5.0

6. **查看评价规则**：
   - 订单 status=3（已完成）：只返回自己的评价，对方字段为 null
   - 订单 status=4（已评价）：返回双方的评价
   - 双方都未评价时：所有字段为 null

7. **事务保证**：
   - 提交评价涉及 review、trade_order、user 三张表的更新，必须使用 @Transactional
   - 查看评价只读操作，不需要事务

---

## 数据库字段映射

| 数据库字段 | Java 类型 | 说明 |
|-----------|----------|------|
| review.id | Long | 主键，自增 |
| review.order_id | Long | 订单ID |
| review.reviewer_id | Long | 评价人ID |
| review.target_id | Long | 被评价人ID |
| review.score_desc | Integer | 商品描述相符 1-5分 |
| review.score_attitude | Integer | 沟通态度 1-5分 |
| review.score_experience | Integer | 交易体验 1-5分 |
| review.content | String | 评价内容，最大200字，可选 |
| review.is_auto | Integer | 是否自动评价 0-否 1-是 |
| review.create_time | LocalDateTime | 评价时间 |
| user.score | BigDecimal | 用户综合评分，decimal(3,1) |

---

## 验收标准（来自 feature_list.json）

- [ ] 校验当前用户是该订单的买家或卖家
- [ ] 校验订单status=3(已完成)或status=4(已评价但自己未评)
- [ ] 校验该用户未评价过该订单（防重复评价）
- [ ] 校验评价窗口期（complete_time + 7天内）
- [ ] 评分字段scoreDesc、scoreAttitude、scoreExperience范围1-5
- [ ] content可选，最大200字
- [ ] 双方都评价后订单status→4(已评价)
- [ ] 评价后重新计算被评价人综合评分：(scoreDesc+scoreAttitude+scoreExperience)/3.0的所有评价平均值，保留一位小数
- [ ] 无评价的用户默认评分5.0
- [ ] 评价后通知对方（预留NotificationService调用）
- [ ] 查看评价：status=3时只返回自己的评价，status=4时返回双方评价
- [ ] 编写Service层单元测试，覆盖正常评价、重复评价、超过窗口期、双方都评价后状态变更、评分计算场景

---

## 文件清单

### 需要创建的文件
1. `src/main/java/com/qingyuan/secondhand/entity/Review.java`
2. `src/main/java/com/qingyuan/secondhand/mapper/ReviewMapper.java`
3. `src/main/java/com/qingyuan/secondhand/service/ReviewService.java`
4. `src/main/java/com/qingyuan/secondhand/service/impl/ReviewServiceImpl.java`
5. `src/main/java/com/qingyuan/secondhand/dto/ReviewSubmitDTO.java`
6. `src/main/java/com/qingyuan/secondhand/vo/ReviewDetailVO.java`
7. `src/main/java/com/qingyuan/secondhand/controller/mini/MiniReviewController.java`
8. `src/test/java/com/qingyuan/secondhand/service/impl/ReviewServiceImplTest.java`

### 需要修改的文件
无（本功能为独立模块，不需要修改已有文件）

---

## 技术要点

### 1. 评价窗口期校验
- 使用 LocalDateTime.plusDays(7) 计算截止时间
- 使用 LocalDateTime.now().isAfter(deadline) 判断是否超期
