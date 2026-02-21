## Feature F05：卖家主页与账号管理

### 任务规划

该功能依赖 F01 已完成的 User 实体、UserMapper、UserService 等基础代码，以及 F04 的统计查询方法。实现卖家主页查询、账号注销申请和恢复功能。

#### 步骤 1：创建 UserProfileVO（vo/UserProfileVO.java）

字段定义：
- id (Long) - 用户ID
- nickName (String) - 昵称
- avatarUrl (String) - 头像URL
- authStatus (Integer) - 认证状态
- score (BigDecimal) - 综合评分
- onSaleCount (Integer) - 在售商品数
- soldCount (Integer) - 成交数
- products (Page<ProductSimpleVO>) - 在售商品分页数据

注意：
- products 字段类型为 MyBatis-Plus 的 Page<ProductSimpleVO>
- ProductSimpleVO 需要新建，包含商品基本信息（id, title, price, images, createTime）

#### 步骤 2：创建 ProductSimpleVO（vo/ProductSimpleVO.java）

字段定义：
- id (Long) - 商品ID
- title (String) - 商品标题
- price (BigDecimal) - 价格
- images (List<String>) - 商品图片（JSON 数组反序列化）
- createTime (LocalDateTime) - 发布时间

注意：
- images 字段需要从 JSON 字符串反序列化为 List<String>

#### 步骤 3：在 UserService 中添加三个方法

UserService.java：
```java
UserProfileVO getUserProfile(Long userId, Integer page, Integer pageSize);
void deactivateAccount();
void restoreAccount();
```

#### 步骤 4：在 UserServiceImpl 中实现 getUserProfile 方法

业务逻辑：
- 查询目标用户信息，不存在抛出 BusinessException("用户不存在")
- 统计在售数与成交数
- 分页查询在售商品：user_id={userId} AND status=1 AND is_deleted=0，按 create_time 倒序
- images 字段从 JSON 反序列化为 List<String>
- 组装并返回 UserProfileVO

#### 步骤 5：在 UserServiceImpl 中实现 deactivateAccount 方法

业务逻辑：
- 获取当前 userId
- 校验用户存在、状态非注销中/封禁
- 检查进行中订单：trade_order.status=1，存在则抛出 BusinessException("有进行中的订单，无法注销")
- 更新用户 status=2 并设置 deactivate_time
- 批量下架所有在售商品：product.status 从 1 更新为 2
- 清除 user:info:{userId} 和 user:stats:{userId} 缓存

#### 步骤 6：在 UserServiceImpl 中实现 restoreAccount 方法

业务逻辑：
- 获取当前 userId
- 校验用户存在且 status=2，否则抛出 BusinessException("账号未在注销中")
- 更新用户 status=1 并清空 deactivate_time
- 清除 user:info:{userId} 和 user:stats:{userId} 缓存

#### 步骤 7：在 UserMapper 中添加查询方法

```sql
SELECT COUNT(1) FROM trade_order WHERE (buyer_id = #{userId} OR seller_id = #{userId}) AND status = 1
UPDATE product SET status = 2 WHERE user_id = #{userId} AND status = 1 AND is_deleted = 0
```

#### 步骤 8：在 MiniUserController 中添加三个接口

```java
@GetMapping("/profile/{id}")
public Result<UserProfileVO> getUserProfile(@PathVariable Long id,
                                            @RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer pageSize)

@PostMapping("/deactivate")
public Result<Void> deactivateAccount()

@PostMapping("/restore")
public Result<Void> restoreAccount()
```

#### 步骤 9：编写测试用例（UserServiceImplTest.java 中追加）

覆盖场景：
- 卖家主页查询成功/用户不存在
- 注销：无进行中订单成功/有进行中订单失败/已注销中/封禁
- 恢复：成功/非注销中失败

#### 步骤 10：运行测试并生成证据包

命令：
- mvn test -Dtest=UserServiceImplTest

输出保存：
- run-folder/F05-卖家主页与账号管理/test_output.log

