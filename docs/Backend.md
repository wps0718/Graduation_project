

# 全部 26 个 Feature 标准操作提示词

---

## 使用说明

每个 Feature 包含 **4 个标准提示词**：

| 编号 | 提示词 | 在哪个 IDE 中使用 | 何时使用 |
|------|--------|-----------------|---------|
| A | 监督者规划任务 | Kiro | 开始新功能时 |
| B | 执行者开始编码 | Trae | 监督者规划完成后 |
| C | 监督者执行审查 | Kiro | 执行者提交 .ready-for-review 后 |
| D | 执行者修正驳回 | Trae | 监督者驳回后（仅在驳回时使用） |

**流程**：A → B → C →（通过则下一个 Feature 的 A / 驳回则 D → C）

**SQL语句**：后端SQL语句已经写好了（路径：G:\Code\Graduation_project\sql\init.sql）。生成的字段以init.sql中相同表的字段为准。

**任务文件规则**：
- 任务文件采用 tasksNN.md 命名（NN 为两位递增数字），读取与写入都以最新编号文件为准
- 当前最新任务文件为 tasks02.md
- 当最新任务文件超过 2000 行或 200KB 时，立即创建下一编号文件（如 tasks03.md）并转移到新文件继续记录
- 旧的 tasks 文件仅作为历史记录，禁止继续追加

---

## F01：微信登录

### F01-A：Kiro 规划任务

```
请开始你的监督者工作，规划 Feature F01：微信登录。

1. 读取 feature_list.json，找到 id 为 "F01" 的功能
2. 分析该功能需要涉及的所有文件：
   - entity/User.java
   - dto/WxLoginDTO.java
   - vo/LoginVO.java
   - mapper/UserMapper.java
   - service/UserService.java
   - service/impl/UserServiceImpl.java
   - controller/mini/MiniUserController.java
   - config/WxConfig.java
3. 在 tasks.md 中规划详细的开发步骤，包括：
   - User 实体类的所有字段定义（参考数据库 user 表结构）
   - WxLoginDTO 的字段：code
   - LoginVO 的字段：token, userId, isNew, authStatus, nickName, avatarUrl
   - WxConfig 需要从 application.yml 读取 appId 和 appSecret
   - 微信 API 调用地址：https://api.weixin.qq.com/sns/jscode2session，使用 RestTemplate
   - POST /mini/user/wx-login 接口的完整定义
   - 业务逻辑：code换openId → 查用户 → 存在则更新/不存在则新建 → 检查封禁/注销状态 → 生成JWT → 返回LoginVO
   - 测试用例要求：覆盖正常登录、新用户注册、封禁用户、注销中用户四种场景
4. 步骤要足够具体，让执行者可以直接按步骤编写代码
5. 明确标注这是项目的第一个功能，需要同时创建 User 实体和基础的 Mapper

规划完成后，保存 tasks.md，然后告诉我你规划的内容摘要。
```

### F01-B：Trae 开始编码

```
请开始你的执行者工作，实现 Feature F01：微信登录。

1. 读取 tasks.md，找到 F01 微信登录的任务步骤
2. 读取 feature_list.json 中 id="F01" 的 acceptance_criteria
3. 按照 tasks.md 中的步骤，逐步编写以下代码：
   - entity/User.java（包含所有数据库字段的实体类）
   - dto/WxLoginDTO.java
   - vo/LoginVO.java
   - mapper/UserMapper.java
   - service/UserService.java + service/impl/UserServiceImpl.java
   - controller/mini/MiniUserController.java
   - config/WxConfig.java
4. 每完成一个文件，在 tasks.md 中追加 [执行者] 前缀的记录
5. 编写测试用例 UserServiceImplTest.java，覆盖以下场景：
   - 正常微信登录（已有用户）
   - 新用户首次登录自动注册
   - 封禁用户登录抛异常
   - 注销中用户登录返回特殊标识
6. 在终端运行 mvn test -Dtest=UserServiceImplTest，将输出保存到 run-folder/F01-微信登录/test_output.log
7. 生成 run-folder/F01-微信登录/ 证据包（task.md + run.sh + test_output.log）
8. 创建 .ready-for-review 信号文件
9. 完成后告诉我结果摘要

注意：
- 这是项目的第一个功能，User 实体需要完整创建
- 微信 API 调用使用 RestTemplate
- BCrypt 使用 Spring Security 的 BCryptPasswordEncoder
- application.yml 中需要添加微信小程序配置项（wx.appId, wx.appSecret）
```

### F01-C：Kiro 执行审查

```
执行者已提交 F01 微信登录的审查请求。请执行完整的审查流程：

1. 读取 .ready-for-review 文件内容
2. 读取 tasks.md 中执行者关于 F01 的 [执行者] 工作记录
3. 逐一审查以下文件：
   - entity/User.java — 字段是否与数据库表结构一致
   - dto/WxLoginDTO.java — 是否只有 code 字段
   - vo/LoginVO.java — 是否包含 token, userId, isNew, authStatus, nickName, avatarUrl
   - mapper/UserMapper.java — 是否定义了根据 openId 查询用户的方法
   - service/impl/UserServiceImpl.java — 核心审查：
     a. 微信 API 调用逻辑是否正确（RestTemplate）
     b. 新用户是否设置默认昵称"微信用户"和默认头像
     c. 已有用户是否更新 session_key 和 last_login_time
     d. 封禁状态是否抛出 BusinessException
     e. 注销状态是否返回特殊标识
     f. JWT Token 生成是否正确
   - controller/mini/MiniUserController.java — 是否只有参数接收和 Service 调用
   - config/WxConfig.java — 是否从 application.yml 读取配置
4. 审查测试文件：
   - 测试是否覆盖4个场景（正常登录、新用户、封禁、注销中）
   - 断言是否有实际意义（非 assertTrue(true)）
   - Mock 是否正确配置
5. 审查 run-folder/F01-微信登录/ 证据包完整性
6. 在终端运行 mvn test -Dtest=UserServiceImplTest 独立复跑验证
7. 对比运行结果与执行者的 test_output.log
8. 根据审查结果执行通过或驳回流程
9. 告诉我审查结果

重点检查：
- User 实体不得直接返回前端（必须转为 LoginVO）
- 密码字段不得出现在 VO 中
- openId 查询是否有唯一性保证
```

### F01-D：Trae 修正驳回（仅驳回时使用）

```
监督者已驳回 F01 微信登录的审查。请：
1. 读取 tasks.md 中监督者关于 F01 的 [监督者] 反馈
2. 逐一理解每个驳回问题
3. 按照反馈修正对应的代码文件
4. 重新运行 mvn test -Dtest=UserServiceImplTest
5. 更新 run-folder/F01-微信登录/ 证据包
6. 重新创建 .ready-for-review 信号文件
7. 在 tasks.md 中记录 [执行者] 修正内容和重试结果
```

---

## F02：手机号密码登录

### F02-A：Kiro 规划任务

```
请继续你的监督者工作，规划 Feature F02：手机号密码登录。

1. 读取 feature_list.json，找到 id 为 "F02" 的功能
2. 该功能依赖 F01 已完成的 User 实体、UserMapper、UserService 等基础代码
3. 分析需要新增/修改的文件：
   - 新增：dto/AccountLoginDTO.java（字段：phone, password）
   - 修改：service/UserService.java — 添加 accountLogin 方法
   - 修改：service/impl/UserServiceImpl.java — 实现 accountLogin
   - 修改：controller/mini/MiniUserController.java — 添加 POST /mini/user/login 接口
4. 在 tasks.md 中规划详细步骤，包括：
   - AccountLoginDTO 字段定义
   - Redis 登录失败次数限制逻辑：key=login:fail:{phone}，5次锁定15分钟
   - BCryptPasswordEncoder 密码验证
   - 封禁和注销状态检查
   - 测试用例要求：覆盖正常登录、密码错误、账号锁定、账号不存在、封禁状态场景
5. 步骤要足够具体，让执行者可以直接编写代码

规划完成后，保存 tasks.md，然后告诉我你规划的内容摘要。
```

### F02-B：Trae 开始编码

```
请开始你的执行者工作，实现 Feature F02：手机号密码登录。

1. 读取 tasks.md，找到 F02 手机号密码登录的任务步骤
2. 读取 feature_list.json 中 id="F02" 的 acceptance_criteria
3. 按步骤编写代码：
   - 创建 dto/AccountLoginDTO.java
   - 在 UserService 中添加 accountLogin 方法
   - 在 UserServiceImpl 中实现登录逻辑：
     a. 根据 phone 查询用户
     b. BCrypt 验证密码
     c. Redis 记录失败次数和锁定机制
     d. 检查封禁/注销状态
     e. 生成 JWT Token
   - 在 MiniUserController 中添加 POST /mini/user/login 接口
4. 每完成一个步骤，在 tasks.md 中追加 [执行者] 记录
5. 编写测试 UserServiceImplTest 中的 accountLogin 相关测试方法，覆盖：
   - 正常登录成功
   - 密码错误（Redis 计数）
   - 账号锁定（5次失败后）
   - 账号不存在
   - 封禁状态
6. 运行 mvn test，保存输出到 run-folder/F02-手机号密码登录/test_output.log
7. 生成 run-folder/F02-手机号密码登录/ 证据包
8. 创建 .ready-for-review 信号文件
9. 完成后告诉我结果摘要

注意：
- 不要修改 F01 已通过审查的代码逻辑，只做新增
- Redis 操作使用 StringRedisTemplate
- 密码验证使用 BCryptPasswordEncoder.matches()
```

### F02-C：Kiro 执行审查

```
执行者已提交 F02 手机号密码登录的审查请求。请执行完整的审查流程：

1. 读取 .ready-for-review 文件内容
2. 读取 tasks.md 中执行者关于 F02 的工作记录
3. 逐一审查：
   - dto/AccountLoginDTO.java — 字段是否正确，是否有参数校验注解
   - service/impl/UserServiceImpl.java 的 accountLogin 方法：
     a. 用户不存在是否抛出明确异常
     b. BCrypt 是否使用 matches() 方法而非 equals()
     c. Redis 失败计数 key 格式是否正确（login:fail:{phone}）
     d. 锁定逻辑是否正确（5次后TTL=15分钟）
     e. 密码正确后是否清除失败计数
     f. 封禁/注销检查是否完整
   - controller 是否只做参数接收和 Service 调用
4. 审查测试：断言是否有实际意义，场景是否完整
5. 审查证据包完整性
6. 在终端运行 mvn test 独立复跑
7. 根据结果执行通过或驳回

重点检查：
- 密码是否有泄露风险（日志中不得打印密码）
- Redis key 的 TTL 设置是否正确
- F01 已有代码是否被意外修改
```

### F02-D：Trae 修正驳回

```
监督者已驳回 F02 手机号密码登录的审查。请：
1. 读取 tasks.md 中监督者关于 F02 的 [监督者] 反馈
2. 逐一修正代码
3. 重新运行 mvn test
4. 更新 run-folder/F02-手机号密码登录/ 证据包
5. 重新创建 .ready-for-review
6. 在 tasks.md 中记录修正内容
```

---

## F03：短信验证码登录

### F03-A：Kiro 规划任务

```
请继续你的监督者工作，规划 Feature F03：短信验证码登录。

1. 读取 feature_list.json，找到 id 为 "F03" 的功能
2. 该功能依赖 F01 的 User 基础代码，包含两个接口：发送验证码 + 验证码登录
3. 分析需要新增的文件：
   - dto/SmsSendDTO.java（字段：phone）
   - dto/SmsLoginDTO.java（字段：phone, smsCode）
   - UserService 新增 sendSms、smsLogin 方法
4. 在 tasks.md 中规划详细步骤，包括：
   - Redis key 设计：
     - sms:code:{phone} — 验证码存储，TTL=5分钟
     - sms:limit:{phone} — 60秒频率限制
     - sms:daily:{phone} — 每日10次上限，TTL=24小时
   - 手机号正则校验规则
   - 6位随机数字验证码生成
   - V1.0 短信发送用 log.info 打印
   - 验证码登录：验证→删除验证码→查用户→不存在自动注册→生成Token
   - 测试用例要求：覆盖发送频率限制、每日上限、验证码正确/错误/过期、新用户自动注册场景

规划完成后，保存 tasks.md，然后告诉我你规划的内容摘要。
```

### F03-B：Trae 开始编码

```
请开始你的执行者工作，实现 Feature F03：短信验证码登录。

1. 读取 tasks.md，找到 F03 短信验证码登录的任务步骤
2. 读取 feature_list.json 中 id="F03" 的 acceptance_criteria
3. 按步骤编写代码：
   - 创建 dto/SmsSendDTO.java 和 dto/SmsLoginDTO.java
   - 在 UserService 中添加 sendSmsCode 和 smsLogin 方法
   - 在 UserServiceImpl 中实现：
     a. 发送验证码：手机号格式校验 → 频率限制检查 → 每日上限检查 → 生成验证码 → 存Redis → 打印日志
     b. 验证码登录：取验证码 → 比对 → 删除验证码 → 查/建用户 → 生成Token
   - 在 MiniUserController 中添加两个接口
4. 每完成一个步骤，在 tasks.md 中追加 [执行者] 记录
5. 编写测试覆盖所有 acceptance_criteria
6. 运行 mvn test，保存输出到 run-folder/F03-短信验证码登录/test_output.log
7. 生成证据包，创建 .ready-for-review
8. 完成后告诉我结果摘要

注意：
- 验证码用完必须从 Redis 删除（防止重复使用）
- V1.0 不实际发送短信，用 log.info("发送验证码到{}：{}", phone, code)
- Redis 操作使用 StringRedisTemplate
```

### F03-C：Kiro 执行审查

```
执行者已提交 F03 短信验证码登录的审查请求。请执行完整的审查流程：

1. 读取 .ready-for-review 文件内容
2. 读取 tasks.md 中执行者关于 F03 的工作记录
3. 逐一审查：
   - SmsSendDTO 和 SmsLoginDTO 字段和校验注解
   - 发送验证码逻辑：
     a. 手机号正则校验是否正确
     b. Redis 频率限制 key 和 TTL 是否正确（sms:limit:{phone}, 60s）
     c. 每日上限 key 和 TTL 是否正确（sms:daily:{phone}, 24h）
     d. 验证码是否为6位随机数字
     e. 验证码存储 key 和 TTL 是否正确（sms:code:{phone}, 5分钟）
   - 验证码登录逻辑：
     a. 过期/错误/正确三种情况处理
     b. 验证通过后是否删除 Redis 中的验证码
     c. 用户不存在是否自动创建
4. 审查测试场景完整性和断言有效性
5. 审查证据包，终端独立复跑 mvn test
6. 根据结果执行通过或驳回

重点检查：
- 验证码是否在验证后删除（防重放攻击）
- 频率限制是否可被绕过
- 日志中是否打印了验证码（V1.0 允许，但需确认是 info 级别）
```

### F03-D：Trae 修正驳回

```
监督者已驳回 F03 短信验证码登录的审查。请：
1. 读取 tasks.md 中监督者关于 F03 的 [监督者] 反馈
2. 逐一修正代码
3. 重新运行 mvn test
4. 更新 run-folder/F03-短信验证码登录/ 证据包
5. 重新创建 .ready-for-review
6. 在 tasks.md 中记录修正内容
```

---

## F04：用户信息管理

### F04-A：Kiro 规划任务

```
请继续你的监督者工作，规划 Feature F04：用户信息管理。

1. 读取 feature_list.json，找到 id 为 "F04" 的功能
2. 该功能包含三个接口：获取用户信息、更新用户信息、获取用户统计数据
3. 分析需要新增的文件：
   - dto/UserUpdateDTO.java（字段：nickName, avatarUrl, gender）
   - vo/UserInfoVO.java（字段：id, nickName, avatarUrl, phone(脱敏), gender, campusId, campusName, authStatus, score, status）
   - vo/UserStatsVO.java（字段：onSaleCount, soldCount, favoriteCount）
   - common/util 中新增手机号脱敏方法
4. 在 tasks.md 中规划详细步骤，包括：
   - 手机号脱敏逻辑：138****8888 格式
   - 关联查询校区名称（需要 campus 表，但 campus 模块未开发，此处 UserInfoVO 的 campusName 可先返回 null 或通过简单 SQL 关联）
   - 统计数据 Redis 缓存设计：key=user:stats:{userId}，TTL=10分钟
   - 统计查询 SQL：在售商品数、成交数、收藏数
   - 更新用户信息后清除缓存
   - 测试用例要求

规划完成后，保存 tasks.md，然后告诉我你规划的内容摘要。
```

### F04-B：Trae 开始编码

```
请开始你的执行者工作，实现 Feature F04：用户信息管理。

1. 读取 tasks.md，找到 F04 用户信息管理的任务步骤
2. 读取 feature_list.json 中 id="F04" 的 acceptance_criteria
3. 按步骤编写代码：
   - 创建 dto/UserUpdateDTO.java
   - 创建 vo/UserInfoVO.java 和 vo/UserStatsVO.java
   - 在 common/util 中编写手机号脱敏工具方法
   - 在 UserService 中添加 getUserInfo、updateUserInfo、getUserStats 方法
   - 在 UserServiceImpl 中实现：
     a. getUserInfo：从 UserContext 获取 userId → 查询用户 → 关联校区名 → 手机号脱敏 → 返回 UserInfoVO
     b. updateUserInfo：校验参数 → 更新 → 清除缓存
     c. getUserStats：先查 Redis 缓存 → 未命中查数据库 → 存入 Redis
   - 在 MiniUserController 中添加三个接口
4. 每完成一个步骤，在 tasks.md 中追加 [执行者] 记录
5. 编写测试覆盖所有 acceptance_criteria
6. 运行 mvn test，保存输出到 run-folder/F04-用户信息管理/test_output.log
7. 生成证据包，创建 .ready-for-review
8. 完成后告诉我结果摘要

注意：
- UserContext.getCurrentId() 获取当前用户ID
- 统计查询中涉及的 product、trade_order、favorite 表可能还没有对应的 Mapper，可以先在 UserMapper 中写关联查询 SQL，或者先用占位数据
- 脱敏方法需要处理 phone 为 null 的情况
```

### F04-C：Kiro 执行审查

```
执行者已提交 F04 用户信息管理的审查请求。请执行完整的审查流程：

1. 读取 .ready-for-review 文件内容
2. 读取 tasks.md 中执行者关于 F04 的工作记录
3. 逐一审查：
   - UserInfoVO 是否包含所有必需字段，手机号是否脱敏
   - UserStatsVO 字段是否完整
   - 手机号脱敏工具方法：是否处理 null/空字符串/长度不足的情况
   - getUserInfo：是否从 UserContext 获取 userId，是否关联查询校区名
   - updateUserInfo：是否校验参数，是否清除缓存
   - getUserStats：Redis 缓存逻辑是否正确（key格式、TTL、缓存穿透处理）
   - Controller 是否只做参数接收和 Service 调用
4. 审查测试场景和断言
5. 审查证据包，终端独立复跑
6. 根据结果执行通过或驳回

重点检查：
- 手机号脱敏方法是否健壮（边界情况）
- Redis 缓存是否有穿透风险（用户ID不存在时）
- User 实体的 password 字段是否出现在 VO 中
```

### F04-D：Trae 修正驳回

```
监督者已驳回 F04 用户信息管理的审查。请：
1. 读取 tasks.md 中监督者关于 F04 的 [监督者] 反馈
2. 逐一修正代码
3. 重新运行 mvn test
4. 更新 run-folder/F04-用户信息管理/ 证据包
5. 重新创建 .ready-for-review
6. 在 tasks.md 中记录修正内容
```

---

## F05：卖家主页与账号管理

### F05-A：Kiro 规划任务

```
请继续你的监督者工作，规划 Feature F05：卖家主页与账号管理。

1. 读取 feature_list.json，找到 id 为 "F05" 的功能
2. 该功能包含三个接口：卖家主页、申请注销、恢复账号
3. 分析需要新增的文件：
   - vo/UserProfileVO.java（字段：id, nickName, avatarUrl, authStatus, score, onSaleCount, soldCount, products 分页数据）
4. 在 tasks.md 中规划详细步骤，包括：
   - 卖家主页：查询目标用户信息 + 在售商品分页列表 + 统计数据
   - 注销申请：检查进行中订单 → 更新 status=2 → 设置 deactivate_time → 下架所有在售商品
   - 恢复账号：检查 status=2 → 更新 status=1 → 清空 deactivate_time
   - 测试用例要求

规划完成后，保存 tasks.md，然后告诉我你规划的内容摘要。
```

### F05-B：Trae 开始编码

```
请开始你的执行者工作，实现 Feature F05：卖家主页与账号管理。

1. 读取 tasks.md，找到 F05 卖家主页与账号管理的任务步骤
2. 读取 feature_list.json 中 id="F05" 的 acceptance_criteria
3. 按步骤编写代码：
   - 创建 vo/UserProfileVO.java
   - 在 UserService 中添加 getUserProfile、deactivateAccount、restoreAccount 方法
   - 在 UserServiceImpl 中实现：
     a. 卖家主页：查询用户 → 查询在售商品分页 → 统计在售数和成交数
     b. 注销：检查进行中订单 → 更新状态 → 下架商品
     c. 恢复：检查状态 → 更新
   - 在 MiniUserController 中添加三个接口
4. 每完成一个步骤，在 tasks.md 中追加 [执行者] 记录
5. 编写测试覆盖所有 acceptance_criteria
6. 运行 mvn test，保存输出到 run-folder/F05-卖家主页与账号管理/test_output.log
7. 生成证据包，创建 .ready-for-review
8. 完成后告诉我结果摘要

注意：
- 商品相关的表和 Mapper 可能还未创建，可以在 UserMapper 中先写关联查询或使用占位逻辑
- 注销时"下架所有在售商品"的 SQL 需要批量更新 product 表
```

### F05-C：Kiro 执行审查

```
执行者已提交 F05 卖家主页与账号管理的审查请求。请执行完整的审查流程：

1. 读取 .ready-for-review 文件内容
2. 读取 tasks.md 中执行者关于 F05 的工作记录
3. 逐一审查：
   - UserProfileVO 字段是否完整
   - 卖家主页：是否正确查询在售商品（status=1, is_deleted=0）、分页是否正确
   - 注销申请：是否检查进行中订单、状态更新是否正确、是否下架所有商品
   - 恢复账号：是否校验当前状态为注销中(status=2)
   - 各接口的异常场景是否处理
4. 审查测试和证据包
5. 终端独立复跑 mvn test
6. 根据结果执行通过或驳回

重点检查：
- 注销时是否有事务保证（更新用户状态 + 下架商品应在同一事务中）
- 卖家主页是否泄露敏感信息（如手机号、密码）
```

### F05-D：Trae 修正驳回

```
监督者已驳回 F05 卖家主页与账号管理的审查。请：
1. 读取 tasks.md 中监督者关于 F05 的 [监督者] 反馈
2. 逐一修正代码
3. 重新运行 mvn test
4. 更新 run-folder/F05-卖家主页与账号管理/ 证据包
5. 重新创建 .ready-for-review
6. 在 tasks.md 中记录修正内容
```

---

## F06：校园认证-小程序端

### F06-A：Kiro 规划任务

```
请继续你的监督者工作，规划 Feature F06：校园认证-小程序端。

1. 读取 feature_list.json，找到 id 为 "F06" 的功能
2. 该功能包含三个接口：提交认证、查询认证状态、获取学院列表
3. 需要新建多个文件（两个实体、两个 Mapper、两个 Service、DTO/VO、两个 Controller）
4. 在 tasks.md 中规划详细步骤，包括：
   - CampusAuth 实体字段（参考 campus_auth 表）
   - College 实体字段（参考 college 表）
   - AuthSubmitDTO 字段：collegeId, studentNo, className, certImage
   - AuthStatusVO 字段：status, collegeName, studentNo, className, certImage, rejectReason, reviewTime
   - CollegeVO 字段：id, name
   - 提交认证逻辑：审核中不允许重复提交 + 学号唯一性 + 已驳回可更新 + 更新 auth_status=1
   - 学院列表：status=1 + sort 排序 + Redis 缓存
   - 测试用例要求

规划完成后，保存 tasks.md，然后告诉我你规划的内容摘要。
```

### F06-B：Trae 开始编码

```
请开始你的执行者工作，实现 Feature F06：校园认证-小程序端。

1. 读取 tasks.md，找到 F06 校园认证-小程序端的任务步骤
2. 读取 feature_list.json 中 id="F06" 的 acceptance_criteria
3. 按步骤创建以下文件：
   - entity/CampusAuth.java
   - entity/College.java
   - dto/AuthSubmitDTO.java
   - vo/AuthStatusVO.java
   - vo/CollegeVO.java
   - mapper/CampusAuthMapper.java
   - mapper/CollegeMapper.java
   - service/CampusAuthService.java + impl
   - service/CollegeService.java + impl
   - controller/mini/MiniAuthController.java
   - controller/mini/MiniCollegeController.java
4. 实现业务逻辑：
   - 提交认证：审核中状态校验 → 学号唯一性校验 → 保存/更新记录 → 更新用户 auth_status
   - 查询认证状态：关联学院名称
   - 学院列表：Redis 缓存 + 按 sort 排序
5. 编写测试覆盖所有场景
6. 运行 mvn test，保存输出到 run-folder/F06-校园认证小程序端/
7. 生成证据包，创建 .ready-for-review
8. 完成后告诉我结果摘要
```

### F06-C：Kiro 执行审查

```
执行者已提交 F06 校园认证-小程序端的审查请求。请执行完整的审查流程：

1. 读取 .ready-for-review 和 tasks.md 中 F06 的记录
2. 逐一审查所有新建文件：
   - 两个实体类字段是否与数据库表结构一致
   - DTO 参数校验注解是否完整
   - 提交认证逻辑：审核中重复提交、学号唯一性、驳回后更新（非新建）
   - 认证提交后是否正确更新用户表 auth_status=1
   - 学院列表是否有 Redis 缓存，缓存 key 和 TTL 是否合理
   - Controller 是否分了 MiniAuthController 和 MiniCollegeController
3. 审查测试和证据包
4. 终端独立复跑 mvn test
5. 根据结果执行通过或驳回

重点检查：
- 学号唯一性校验是否正确（不同用户同一学号）
- 驳回后重新提交是更新已有记录还是新建（应为更新）
```

### F06-D：Trae 修正驳回

```
监督者已驳回 F06 校园认证-小程序端的审查。请：
1. 读取 tasks.md 中监督者关于 F06 的 [监督者] 反馈
2. 逐一修正代码
3. 重新运行 mvn test
4. 更新 run-folder/F06-校园认证小程序端/ 证据包
5. 重新创建 .ready-for-review
6. 在 tasks.md 中记录修正内容
```

---

## F07：校园认证-管理端审核

### F07-A：Kiro 规划任务

```
请继续你的监督者工作，规划 Feature F07：校园认证-管理端审核。

1. 读取 feature_list.json，找到 id 为 "F07" 的功能
2. 该功能依赖 F06 已完成的 CampusAuth 和 College 实体及 Service
3. 需要新增的文件：
   - vo/AuthPageVO.java
   - controller/admin/AdminAuthController.java
4. 在 tasks.md 中规划详细步骤，包括：
   - 分页查询：支持 status 和 collegeId 筛选，关联用户信息和学院名称
   - 认证详情查询
   - 通过操作：更新认证 status=1 + 用户 auth_status=2 + 预留通知调用
   - 驳回操作：更新认证 status=2 + 用户 auth_status=3 + 记录 rejectReason + 预留通知调用
   - 测试用例要求

规划完成后，保存 tasks.md，然后告诉我你规划的内容摘要。
```

### F07-B：Trae 开始编码

```
请开始你的执行者工作，实现 Feature F07：校园认证-管理端审核。

1. 读取 tasks.md，找到 F07 校园认证-管理端审核的任务步骤
2. 读取 feature_list.json 中 id="F07" 的 acceptance_criteria
3. 按步骤编写代码：
   - 创建 vo/AuthPageVO.java
   - 在 CampusAuthService 中添加管理端方法：page、detail、approve、reject
   - 在 CampusAuthServiceImpl 中实现：
     a. 分页查询关联用户信息和学院名称（可能需要 XML 写关联查询）
     b. 通过：更新认证状态 + 用户 auth_status + 预留 NotificationService.send 调用（先注释或打桩）
     c. 驳回：同上 + 记录 rejectReason
   - 创建 controller/admin/AdminAuthController.java
4. 编写测试覆盖通过和驳回场景
5. 运行 mvn test，保存输出到 run-folder/F07-校园认证管理端审核/
6. 生成证据包，创建 .ready-for-review
7. 完成后告诉我结果摘要

注意：
- NotificationService 尚未开发，通知调用先用 // TODO 注释标记或写空方法打桩
- 审核操作需要事务保证（更新认证状态 + 更新用户状态）
```

### F07-C：Kiro 执行审查

```
执行者已提交 F07 校园认证-管理端审核的审查请求。请执行完整的审查流程：

1. 读取 .ready-for-review 和 tasks.md 中 F07 的记录
2. 逐一审查：
   - AuthPageVO 字段是否完整（含用户信息和学院名称）
   - 分页查询 SQL 是否正确关联了三张表
   - 通过/驳回操作是否同时更新认证表和用户表
   - 是否有 @Transactional 事务保证
   - 通知调用是否预留了（TODO 注释或打桩）
   - AdminAuthController 路径前缀是否为 /admin/auth
3. 审查测试和证据包
4. 终端独立复跑 mvn test
5. 根据结果执行通过或驳回

重点检查：
- 管理端接口是否在 admin 包下
- 事务是否正确配置
```

### F07-D：Trae 修正驳回

```
监督者已驳回 F07 校园认证-管理端审核的审查。请：
1. 读取 tasks.md 中监督者关于 F07 的 [监督者] 反馈
2. 逐一修正代码
3. 重新运行 mvn test
4. 更新 run-folder/F07-校园认证管理端审核/ 证据包
5. 重新创建 .ready-for-review
6. 在 tasks.md 中记录修正内容
```

---

## F08：分类模块

### F08-A：Kiro 规划任务

```
请继续你的监督者工作，规划 Feature F08：分类模块。

1. 读取 feature_list.json，找到 id 为 "F08" 的功能
2. 该功能是独立模块，无前置依赖，包含小程序端1个接口 + 管理端5个接口
3. 需要新建的完整文件列表（entity、mapper、service+impl、dto、vo、controller 双端）
4. 在 tasks.md 中规划详细步骤，包括：
   - Category 实体字段（参考 category 表）
   - 小程序端：GET /mini/category/list，公开接口，Redis 缓存1小时
   - 管理端：page 分页（支持名称搜索）、list 全量、add、update、delete
   - 删除前检查是否有商品使用该分类（关联 product 表）
   - 添加/更新后清除 Redis 缓存
   - 测试用例要求

规划完成后，保存 tasks.md，然后告诉我你规划的内容摘要。
```

### F08-B：Trae 开始编码

```
请开始你的执行者工作，实现 Feature F08：分类模块。

1. 读取 tasks.md，找到 F08 分类模块的任务步骤
2. 读取 feature_list.json 中 id="F08" 的 acceptance_criteria
3. 按步骤创建所有文件：
   - entity/Category.java
   - mapper/CategoryMapper.java
   - service/CategoryService.java + service/impl/CategoryServiceImpl.java
   - dto/CategoryDTO.java
   - vo/CategoryVO.java
   - controller/mini/MiniCategoryController.java
   - controller/admin/AdminCategoryController.java
4. 实现业务逻辑：
   - 小程序端列表：Redis 缓存1小时
   - 管理端 CRUD
   - 删除前检查商品引用
   - 增删改后清除缓存
5. 编写测试覆盖缓存命中/未命中、删除有商品引用的分类等场景
6. 运行 mvn test，保存输出到 run-folder/F08-分类模块/
7. 生成证据包，创建 .ready-for-review
8. 完成后告诉我结果摘要
```

### F08-C：Kiro 执行审查

```
执行者已提交 F08 分类模块的审查请求。请执行完整的审查流程：

1. 读取 .ready-for-review 和 tasks.md 中 F08 的记录
2. 逐一审查所有文件：
   - 实体类字段与表结构一致性
   - 小程序端接口是否公开（无需登录）
   - Redis 缓存 key 和 TTL 是否正确（1小时）
   - 增删改操作后是否清除了缓存
   - 删除时是否检查了商品引用
   - 管理端分页是否支持名称搜索
   - Controller 是否分了 mini 和 admin 两个
3. 审查测试和证据包
4. 终端独立复跑 mvn test
5. 根据结果执行通过或驳回
```

### F08-D：Trae 修正驳回

```
监督者已驳回 F08 分类模块的审查。请：
1. 读取 tasks.md 中监督者关于 F08 的 [监督者] 反馈
2. 逐一修正代码
3. 重新运行 mvn test
4. 更新 run-folder/F08-分类模块/ 证据包
5. 重新创建 .ready-for-review
6. 在 tasks.md 中记录修正内容
```

---

## F09：校区与面交地点模块

### F09-A：Kiro 规划任务

```
请继续你的监督者工作，规划 Feature F09：校区与面交地点模块。

1. 读取 feature_list.json，找到 id 为 "F09" 的功能
2. 该模块包含两个实体（Campus、MeetingPoint），小程序端2个接口 + 管理端7个接口
3. 在 tasks.md 中规划详细步骤，包括：
   - Campus 和 MeetingPoint 实体字段
   - 小程序端：校区列表（公开，Redis 缓存）、按 campusId 查面交地点
   - 管理端：校区增改查 + 面交地点增删改查
   - 所有需要创建的文件清单
   - 测试用例要求

规划完成后，保存 tasks.md，然后告诉我你规划的内容摘要。
```

### F09-B：Trae 开始编码

```
请开始你的执行者工作，实现 Feature F09：校区与面交地点模块。

1. 读取 tasks.md，找到 F09 校区与面交地点模块的任务步骤
2. 读取 feature_list.json 中 id="F09" 的 acceptance_criteria
3. 按步骤创建所有文件：
   - entity/Campus.java, entity/MeetingPoint.java
   - mapper/CampusMapper.java, mapper/MeetingPointMapper.java
   - service/CampusService.java + impl, service/MeetingPointService.java + impl
   - dto/CampusDTO.java, dto/MeetingPointDTO.java
   - vo/CampusVO.java, vo/MeetingPointVO.java
   - controller/mini/MiniCampusController.java
   - controller/admin/AdminCampusController.java
4. 实现业务逻辑
5. 编写测试
6. 运行 mvn test，保存输出到 run-folder/F09-校区与面交地点模块/
7. 生成证据包，创建 .ready-for-review
8. 完成后告诉我结果摘要
```

### F09-C：Kiro 执行审查

```
执行者已提交 F09 校区与面交地点模块的审查请求。请执行完整的审查流程：

1. 读取 .ready-for-review 和 tasks.md 中 F09 的记录
2. 审查所有文件：实体、Mapper、Service、DTO、VO、Controller
3. 重点检查：
   - 小程序端接口是否公开
   - Redis 缓存是否正确
   - 面交地点是否按 campusId 正确关联
   - Controller 是否正确分包
4. 审查测试和证据包
5. 终端独立复跑 mvn test
6. 根据结果执行通过或驳回
```

### F09-D：Trae 修正驳回

```
监督者已驳回 F09 校区与面交地点模块的审查。请：
1. 读取 tasks.md 中监督者关于 F09 的 [监督者] 反馈
2. 逐一修正代码
3. 重新运行 mvn test
4. 更新 run-folder/F09-校区与面交地点模块/ 证据包
5. 重新创建 .ready-for-review
6. 在 tasks.md 中记录修正内容
```

---

## F10：学院管理模块

### F10-A：Kiro 规划任务

```
请继续你的监督者工作，规划 Feature F10：学院管理模块。

1. 读取 feature_list.json，找到 id 为 "F10" 的功能
2. 该功能依赖 F06 已创建的 College 实体和 CollegeService，只需新增管理端功能
3. 需要新增：
   - dto/CollegeDTO.java
   - controller/admin/AdminCollegeController.java
   - CollegeService 中补充 add、update、delete 方法
4. 在 tasks.md 中规划详细步骤，包括：
   - 管理端4个接口：list、add、update、delete
   - 删除前检查是否有认证记录使用该学院
   - 增删改后清除学院列表 Redis 缓存
   - 测试用例要求

规划完成后，保存 tasks.md，然后告诉我你规划的内容摘要。
```

### F10-B：Trae 开始编码

```
请开始你的执行者工作，实现 Feature F10：学院管理模块。

1. 读取 tasks.md，找到 F10 学院管理模块的任务步骤
2. 读取 feature_list.json 中 id="F10" 的 acceptance_criteria
3. 按步骤编写代码：
   - 创建 dto/CollegeDTO.java
   - 在 CollegeService 中添加 add、update、delete 方法
   - 在 CollegeServiceImpl 中实现（删除前检查认证引用 + 缓存清除）
   - 创建 controller/admin/AdminCollegeController.java
4. 编写测试，特别覆盖删除有认证引用的学院场景
5. 运行 mvn test，保存输出到 run-folder/F10-学院管理模块/
6. 生成证据包，创建 .ready-for-review
7. 完成后告诉我结果摘要

注意：
- College 实体和 CollegeMapper 在 F06 中已创建，不要重复创建
- 只新增管理端功能
```

### F10-C：Kiro 执行审查

```
执行者已提交 F10 学院管理模块的审查请求。请执行完整的审查流程：

1. 读取 .ready-for-review 和 tasks.md 中 F10 的记录
2. 审查新增文件和修改的文件
3. 重点检查：
   - 是否复用了 F06 已有的 College 实体和 CollegeMapper（而非重复创建）
   - 删除前是否检查了认证记录引用
   - 增删改后是否清除了 Redis 缓存
   - F06 已通过的代码是否被意外修改
4. 审查测试和证据包
5. 终端独立复跑 mvn test
6. 根据结果执行通过或驳回
```

### F10-D：Trae 修正驳回

```
监督者已驳回 F10 学院管理模块的审查。请：
1. 读取 tasks.md 中监督者关于 F10 的 [监督者] 反馈
2. 逐一修正代码
3. 重新运行 mvn test
4. 更新 run-folder/F10-学院管理模块/ 证据包
5. 重新创建 .ready-for-review
6. 在 tasks.md 中记录修正内容
```

---

## F11：商品发布与编辑

### F11-A：Kiro 规划任务

```
请继续你的监督者工作，规划 Feature F11：商品发布与编辑。

1. 读取 feature_list.json，找到 id 为 "F11" 的功能
2. 这是商品模块的第一个功能，需要创建 Product 实体和基础架构
3. 包含三个接口：发布商品、编辑商品、修改价格
4. 在 tasks.md 中规划详细步骤，包括：
   - Product 实体所有字段（参考 product 表结构）
   - ProductPublishDTO 字段：title, description, price, originalPrice, categoryId, conditionLevel, campusId, meetingPointId, meetingPointText, images(List<String>)
   - ProductUpdateDTO 字段：同 publish + productId
   - 参数校验规则：标题1-50字、价格>0、图片1-9张、描述1-500字
   - 发布逻辑：校验 → 保存（status=0, is_deleted=0, auto_off_time=now+90天, images存JSON字符串）
   - 编辑逻辑：校验是自己的商品 → 更新 → 状态重置为待审核 → 重设 auto_off_time
   - 修改价格：校验是自己的商品 → 只更新价格（不重新审核）
   - 测试用例要求

规划完成后，保存 tasks.md，然后告诉我你规划的内容摘要。
```

### F11-B：Trae 开始编码

```
请开始你的执行者工作，实现 Feature F11：商品发布与编辑。

1. 读取 tasks.md，找到 F11 商品发布与编辑的任务步骤
2. 读取 feature_list.json 中 id="F11" 的 acceptance_criteria
3. 按步骤创建以下文件：
   - entity/Product.java
   - dto/ProductPublishDTO.java
   - dto/ProductUpdateDTO.java
   - mapper/ProductMapper.java
   - service/ProductService.java + service/impl/ProductServiceImpl.java
   - controller/mini/MiniProductController.java
4. 实现三个接口的业务逻辑
5. 编写测试覆盖：正常发布、参数校验失败、编辑非自己商品、修改价格
6. 运行 mvn test，保存输出到 run-folder/F11-商品发布与编辑/
7. 生成证据包，创建 .ready-for-review
8. 完成后告诉我结果摘要

注意：
- images 字段在数据库中为 VARCHAR/TEXT，存储 JSON 数组字符串，Java 端使用 List<String> 转换
- auto_off_time 计算：LocalDateTime.now().plusDays(90)
- 校验"是自己的商品"需要从 UserContext 获取当前用户ID与商品 userId 比对
```

### F11-C：Kiro 执行审查

```
执行者已提交 F11 商品发布与编辑的审查请求。请执行完整的审查流程：

1. 读取 .ready-for-review 和 tasks.md 中 F11 的记录
2. 逐一审查：
   - Product 实体字段是否与数据库表一致
   - DTO 参数校验注解是否完整（标题长度、价格范围、图片数量等）
   - images 的 JSON 序列化/反序列化是否正确
   - 发布时 status=0, is_deleted=0, auto_off_time 是否正确设置
   - 编辑时是否校验商品归属、状态重置、auto_off_time 重设
   - 修改价格是否不触发重新审核
   - Controller 路径是否在 /mini/product 下
3. 审查测试和证据包
4. 终端独立复跑 mvn test
5. 根据结果执行通过或驳回

重点检查：
- images 存储为 JSON 字符串的实现方式是否安全
- 价格字段的精度处理（BigDecimal vs Double）
- 是否校验了分类、校区、面交地点的存在性
```

### F11-D：Trae 修正驳回

```
监督者已驳回 F11 商品发布与编辑的审查。请：
1. 读取 tasks.md 中监督者关于 F11 的 [监督者] 反馈
2. 逐一修正代码
3. 重新运行 mvn test
4. 更新 run-folder/F11-商品发布与编辑/ 证据包
5. 重新创建 .ready-for-review
6. 在 tasks.md 中记录修正内容
```

---

## F12：商品查询与浏览

### F12-A：Kiro 规划任务

```
请继续你的监督者工作，规划 Feature F12：商品查询与浏览。

1. 读取 feature_list.json，找到 id 为 "F12" 的功能
2. 包含三个接口：商品详情、商品列表（多条件筛选）、我的发布列表
3. 这是最复杂的查询功能，需要 MyBatis XML 写关联查询
4. 在 tasks.md 中规划详细步骤，包括：
   - ProductDetailVO 字段（含卖家信息、分类名、校区名、面交地点名、isFavorited、isOwner、hasActiveOrder）
   - ProductListVO 字段（含卖家基本信息）
   - 商品列表查询参数：page, pageSize, campusId, categoryId, keyword, sortBy, minPrice, maxPrice
   - ProductMapper.xml 中需要写的关联查询 SQL
   - 浏览量 Redis 去重逻辑：key=product:view:{productId}:{userId}，TTL=24h
   - 浏览量异步更新数据库：@Async
   - 搜索关键词异步记录：search_keyword 表 search_count+1
   - 测试用例要求

规划完成后，保存 tasks.md，然后告诉我你规划的内容摘要。
```

### F12-B：Trae 开始编码

```
请开始你的执行者工作，实现 Feature F12：商品查询与浏览。

1. 读取 tasks.md，找到 F12 商品查询与浏览的任务步骤
2. 读取 feature_list.json 中 id="F12" 的 acceptance_criteria
3. 按步骤创建以下文件：
   - vo/ProductDetailVO.java
   - vo/ProductListVO.java
   - resources/mapper/ProductMapper.xml（关联查询 SQL）
4. 在 ProductService 和 ProductServiceImpl 中添加：
   - getProductDetail：关联查询 + 浏览量去重 + 异步更新
   - getProductList：多条件筛选 + 排序 + 分页 + 关联卖家信息
   - getMyProductList：按当前用户和状态查询
5. 在 MiniProductController 中添加三个 GET 接口
6. 编写测试覆盖多条件筛选、排序、浏览量去重等场景
7. 运行 mvn test，保存输出到 run-folder/F12-商品查询与浏览/
8. 生成证据包，创建 .ready-for-review
9. 完成后告诉我结果摘要

注意：
- 启动类或配置类需要 @EnableAsync 注解
- 商品列表只查 status=1 且 is_deleted=0
- keyword 模糊搜索使用 LIKE '%keyword%'（注意 SQL 注入防护，使用 #{} 而非 ${}）
- 关联查询建议使用 MyBatis XML 编写
```

### F12-C：Kiro 执行审查

```
执行者已提交 F12 商品查询与浏览的审查请求。请执行完整的审查流程：

1. 读取 .ready-for-review 和 tasks.md 中 F12 的记录
2. **重点审查 ProductMapper.xml**：
   - SQL 是否正确关联了 product、user、category、campus、meeting_point 表
   - 是否使用 #{} 而非 ${}
   - 动态 SQL（<if>、<where>）逻辑是否正确
   - 分页是否正确
3. 审查业务逻辑：
   - 商品详情的 isFavorited、isOwner、hasActiveOrder 判断逻辑
   - 浏览量 Redis 去重 key 和 TTL
   - @Async 异步更新浏览量是否正确配置
   - 搜索关键词记录是否异步
   - 商品列表排序是否支持 latest/price_asc/price_desc
4. 审查测试和证据包
5. 终端独立复跑 mvn test
6. 根据结果执行通过或驳回

重点检查：
- XML 中有无 SQL 注入风险（${}）
- 关联查询性能（是否有必要索引）
- @Async 是否配置了线程池（或使用默认）
```

### F12-D：Trae 修正驳回

```
监督者已驳回 F12 商品查询与浏览的审查。请：
1. 读取 tasks.md 中监督者关于 F12 的 [监督者] 反馈
2. 逐一修正代码（特别注意 MyBatis XML 的修改）
3. 重新运行 mvn test
4. 更新 run-folder/F12-商品查询与浏览/ 证据包
5. 重新创建 .ready-for-review
6. 在 tasks.md 中记录修正内容
```

---

## F13：商品状态管理

### F13-A：Kiro 规划任务

```
请继续你的监督者工作，规划 Feature F13：商品状态管理。

1. 读取 feature_list.json，找到 id 为 "F13" 的功能
2. 包含小程序端3个接口（下架、上架、删除）+ 管理端6个接口（分页、详情、通过、驳回、批量通过、强制下架）
3. 在 tasks.md 中规划详细步骤，包括：
   - 各状态流转规则：0待审核→1在售→2已下架→3已售出→4已驳回
   - 小程序端操作的权限校验（是否是自己的商品）
   - 管理端审核操作的通知预留
   - 批量通过接口的参数格式
   - 管理端分页 VO 和 Controller
   - 测试用例要求

规划完成后，保存 tasks.md，然后告诉我你规划的内容摘要。
```

### F13-B：Trae 开始编码

```
请开始你的执行者工作，实现 Feature F13：商品状态管理。

1. 读取 tasks.md，找到 F13 商品状态管理的任务步骤
2. 读取 feature_list.json 中 id="F13" 的 acceptance_criteria
3. 按步骤编写代码：
   - 创建 vo/AdminProductPageVO.java
   - 创建 controller/admin/AdminProductController.java
   - 在 ProductService 中添加所有状态管理方法
   - 实现小程序端：下架（校验权限+无进行中订单）、上架（重新审核）、删除（逻辑删除）
   - 实现管理端：分页、详情、审核通过、审核驳回、批量通过、强制下架
   - 审核操作预留 NotificationService 调用
4. 编写测试覆盖各状态流转和权限校验场景
5. 运行 mvn test，保存输出到 run-folder/F13-商品状态管理/
6. 生成证据包，创建 .ready-for-review
7. 完成后告诉我结果摘要
```

### F13-C：Kiro 执行审查

```
执行者已提交 F13 商品状态管理的审查请求。请执行完整的审查流程：

1. 读取 .ready-for-review 和 tasks.md 中 F13 的记录
2. 逐一审查：
   - 状态流转是否正确（不允许非法状态跳转）
   - 权限校验是否完整（自己的商品才能操作）
   - 下架和删除前是否检查了进行中订单
   - 管理端接口是否在 admin 包下
   - 批量通过是否正确处理了多个商品ID
   - 审核操作是否预留了通知调用
   - 强制下架是否正确更新状态
3. 审查测试和证据包
4. 终端独立复跑 mvn test
5. 根据结果执行通过或驳回
```

### F13-D：Trae 修正驳回

```
监督者已驳回 F13 商品状态管理的审查。请：
1. 读取 tasks.md 中监督者关于 F13 的 [监督者] 反馈
2. 逐一修正代码
3. 重新运行 mvn test
4. 更新 run-folder/F13-商品状态管理/ 证据包
5. 重新创建 .ready-for-review
6. 在 tasks.md 中记录修正内容
```

---

## F14：收藏模块

### F14-A：Kiro 规划任务

```
请继续你的监督者工作，规划 Feature F14：收藏模块。

1. 读取 feature_list.json，找到 id 为 "F14" 的功能
2. 包含4个接口：收藏、取消收藏、收藏列表、查询是否收藏
3. 在 tasks.md 中规划详细步骤，包括：
   - Favorite 实体字段（参考 favorite 表）
   - 收藏逻辑：校验商品存在 + 未收藏过 + 插入记录 + product.favorite_count+1 + 预留通知
   - 取消收藏：删除记录 + favorite_count-1
   - 收藏列表：关联商品信息 + 分页 + 按收藏时间倒序
   - 所有需要创建的文件
   - 测试用例要求

规划完成后，保存 tasks.md，然后告诉我你规划的内容摘要。
```

### F14-B：Trae 开始编码

```
请开始你的执行者工作，实现 Feature F14：收藏模块。

1. 读取 tasks.md，找到 F14 收藏模块的任务步骤
2. 读取 feature_list.json 中 id="F14" 的 acceptance_criteria
3. 按步骤创建所有文件：entity、mapper、service+impl、dto、vo、controller
4. 实现四个接口的业务逻辑
5. 编写测试覆盖：收藏、重复收藏、取消收藏、列表查询、是否收藏查询
6. 运行 mvn test，保存输出到 run-folder/F14-收藏模块/
7. 生成证据包，创建 .ready-for-review
8. 完成后告诉我结果摘要

注意：
- 收藏时 product.favorite_count+1 需要更新 product 表
- 重复收藏应该抛异常而非静默忽略
```

### F14-C：Kiro 执行审查

```
执行者已提交 F14 收藏模块的审查请求。请执行完整的审查流程：

1. 读取 .ready-for-review 和 tasks.md 中 F14 的记录
2. 审查所有文件
3. 重点检查：
   - 防止重复收藏的逻辑（数据库唯一索引 or 代码校验）
   - favorite_count 的增减是否正确
   - 取消收藏时 favorite_count 是否防止为负数
   - 收藏列表关联的商品信息是否完整
   - 通知预留是否正确
4. 审查测试和证据包
5. 终端独立复跑 mvn test
6. 根据结果执行通过或驳回
```

### F14-D：Trae 修正驳回

```
监督者已驳回 F14 收藏模块的审查。请：
1. 读取 tasks03.md 中监督者关于 F14 的 [监督者] 反馈
2. 逐一修正代码
3. 重新运行 mvn test
4. 更新 run-folder/F14-收藏模块/ 证据包
5. 重新创建 .ready-for-review
6. 在 tasks03.md 中记录修正内容
```

---

## F15：订单创建与查询

### F15-A：Kiro 规划任务

```
请继续你的监督者工作，规划 Feature F15：订单创建与查询。

1. 读取 feature_list.json，找到 id 为 "F15" 的功能
2. 这是订单模块的第一个功能，需要创建 TradeOrder 实体和基础架构
3. 包含三个接口：创建订单、订单列表、订单详情
4. 在 tasks.md 中规划详细步骤，包括：
   - TradeOrder 实体所有字段（参考 trade_order 表）
   - OrderCreateDTO 字段：productId, price, campusId, meetingPoint
   - OrderCreateVO 字段：orderId, orderNo, expireTime
   - OrderListVO 字段（含商品信息和对方用户信息）
   - OrderDetailVO 字段（完整订单+商品+双方用户信息）
   - OrderNoUtil 工具类：TD + yyyyMMddHHmmss + 4位随机数
   - Redis 分布式锁逻辑：key=product:lock:{productId}，30s
   - 创建订单核心逻辑：校验 → 加锁 → 检查无进行中订单 → 生成订单号 → 创建订单 → 释放锁 → 通知卖家
   - 创建订单时同时设置：
     - expire_time = NOW() + 72小时
     - confirm_deadline = NOW() + 7天
   - 订单列表关联查询（MyBatis XML）
   - 测试用例要求
   

规划完成后，保存 tasks.md，然后告诉我你规划的内容摘要。
```

### F15-B：Trae 开始编码

```
请开始你的执行者工作，实现 Feature F15：订单创建与查询。

1. 读取 tasks02.md，找到 F15 订单创建与查询的任务步骤
2. 读取 feature_list.json 中 id="F15" 的 acceptance_criteria
3. 按步骤创建所有文件：
   - entity/TradeOrder.java
   - mapper/TradeOrderMapper.java + resources/mapper/TradeOrderMapper.xml
   - service/TradeOrderService.java + impl
   - dto/OrderCreateDTO.java
   - vo/OrderCreateVO.java, OrderListVO.java, OrderDetailVO.java
   - common/util/OrderNoUtil.java
   - controller/mini/MiniOrderController.java
4. 实现核心业务逻辑：
   - 创建订单：Redis 分布式锁 + 并发检查 + 订单号生成
   - 订单列表：支持 role 和 status 筛选 + 关联查询
   - 订单详情：权限校验 + 完整信息
5. 编写测试覆盖：正常下单、购买自己商品、并发下单、商品已有订单
6. 运行 mvn test，保存输出到 run-folder/F15-订单创建与查询/
7. 生成证据包，创建 .ready-for-review
8. 完成后告诉我结果摘要

注意：
- 分布式锁使用 StringRedisTemplate.opsForValue().setIfAbsent(key, value, timeout, unit)
- 必须在 finally 中释放锁
- 订单号需要保证唯一性
```

### F15-C：Kiro 执行审查

```
执行者已提交 F15 订单创建与查询的审查请求。请执行完整的审查流程：

1. 读取 .ready-for-review 和 tasks.md 中 F15 的记录
2. **重点审查**：
   - Redis 分布式锁的实现：
     a. 是否使用 setIfAbsent（而非 set）
     b. 是否设置了过期时间（30s）
     c. 是否在 finally 中释放锁
     d. 释放锁时是否校验了是自己加的锁（防误删）
   - 订单号生成逻辑是否正确且唯一
   - 创建订单的事务性：校验、创建订单、设置过期时间
   - 防止购买自己商品的校验
   - TradeOrderMapper.xml 关联查询 SQL
   - 订单列表的 role/status 筛选逻辑
   - 订单详情的权限校验
3. 审查测试和证据包
4. 终端独立复跑 mvn test
5. 根据结果执行通过或驳回

重点检查：
- 分布式锁的正确性（这是核心防并发逻辑）
- SQL 注入防护
- 订单号是否有碰撞风险
```

### F15-D：Trae 修正驳回

```
监督者已驳回 F15 订单创建与查询的审查。请：
1. 读取 tasks.md 中监督者关于 F15 的 [监督者] 反馈
2. 逐一修正代码（特别注意分布式锁相关问题）
3. 重新运行 mvn test
4. 更新 run-folder/F15-订单创建与查询/ 证据包
5. 重新创建 .ready-for-review
6. 在 tasks.md 中记录修正内容
```

---

## F16：订单状态管理

### F16-A：Kiro 规划任务

```
请继续你的监督者工作，规划 Feature F16：订单状态管理。

1. 读取 feature_list.json，找到 id 为 "F16" 的功能
2. 包含小程序端3个接口（确认收货、取消、删除）+ 管理端2个接口（分页、详情）
3. 在 tasks.md 中规划详细步骤，包括：
   - 确认收货：买家操作 + status→3 + complete_time + 商品status→3 + 通知
   - 取消订单：买家或卖家 + status→5 + cancel_by + 商品恢复在售 + 通知
   - 删除订单：仅已评价(4)或已取消(5)才可删除 + 按角色逻辑删除
   - 管理端分页和详情
   - 测试用例要求

规划完成后，保存 tasks.md，然后告诉我你规划的内容摘要。
```

### F16-B：Trae 开始编码

```
请开始你的执行者工作，实现 Feature F16：订单状态管理。

1. 读取 tasks.md，找到 F16 订单状态管理的任务步骤
2. 读取 feature_list.json 中 id="F16" 的 acceptance_criteria
3. 按步骤编写代码：
   - 创建 dto/OrderCancelDTO.java
   - 创建 vo/AdminOrderPageVO.java
   - 创建 controller/admin/AdminOrderController.java
   - 在 TradeOrderService 中添加 confirm、cancel、delete 方法
   - 实现所有业务逻辑
4. 编写测试覆盖：确认收货、取消（买家/卖家）、删除（状态限制）
5. 运行 mvn test，保存输出到 run-folder/F16-订单状态管理/
6. 生成证据包，创建 .ready-for-review
7. 完成后告诉我结果摘要

注意：
- 确认收货和取消订单涉及多表更新，需要 @Transactional
- 通知调用预留 NotificationService
```

### F16-C：Kiro 执行审查

```
执行者已提交 F16 订单状态管理的审查请求。请执行完整的审查流程：

1. 读取 .ready-for-review 和 tasks.md 中 F16 的记录
2. 审查所有状态管理逻辑：
   - 确认收货：是否校验买家身份和当前状态、商品状态更新、complete_time 设置
   - 取消订单：买家和卖家都能取消、cancel_by 记录、商品恢复在售
   - 删除订单：状态限制是否正确、逻辑删除字段是否按角色区分
   - 事务注解是否正确
   - 管理端接口路径和分包是否正确
3. 审查测试和证据包
4. 终端独立复跑 mvn test
5. 根据结果执行通过或驳回
```

### F16-D：Trae 修正驳回

```
监督者已驳回 F16 订单状态管理的审查。请：
1. 读取 tasks.md 中监督者关于 F16 的 [监督者] 反馈
2. 逐一修正代码
3. 重新运行 mvn test
4. 更新 run-folder/F16-订单状态管理/ 证据包
5. 重新创建 .ready-for-review
6. 在 tasks.md 中记录修正内容
```

---

## F17：评价模块

### F17-A：Kiro 规划任务

```
请继续你的监督者工作，规划 Feature F17：评价模块。

1. 读取 feature_list.json，找到 id 为 "F17" 的功能
2. 包含2个接口：提交评价、查看评价
3. 在 tasks.md 中规划详细步骤，包括：
   - Review 实体字段（参考 review 表）
   - ReviewSubmitDTO：orderId, scoreDesc(1-5), scoreAttitude(1-5), scoreExperience(1-5), content(max200)
   - 提交评价逻辑：身份校验 → 订单状态校验 → 防重复评价 → 窗口期校验 → 保存 → 检查双方是否都评了 → 计算综合评分
   - 评分计算公式：单次=(desc+attitude+experience)/3.0，综合=所有收到评价的平均值，保留1位小数
   - 查看评价：status=3只返回自己的，status=4返回双方的
   - 测试用例要求

规划完成后，保存 tasks.md，然后告诉我你规划的内容摘要。
```

### F17-B：Trae 开始编码

```
请开始你的执行者工作，实现 Feature F17：评价模块。

1. 读取 tasks.md，找到 F17 评价模块的任务步骤
2. 读取 feature_list.json 中 id="F17" 的 acceptance_criteria
3. 按步骤创建所有文件：
   - entity/Review.java
   - mapper/ReviewMapper.java
   - service/ReviewService.java + impl
   - dto/ReviewSubmitDTO.java
   - vo/ReviewDetailVO.java
   - controller/mini/MiniReviewController.java
4. 实现评价提交和查看逻辑
5. 编写测试覆盖：正常评价、重复评价、超窗口期、双方都评后状态变更、评分计算
6. 运行 mvn test，保存输出到 run-folder/F17-评价模块/
7. 生成证据包，创建 .ready-for-review
8. 完成后告诉我结果摘要

注意：
- 评分计算使用 BigDecimal 保证精度，保留一位小数
- 无评价的用户默认评分 5.0
- 双方都评价后订单 status 从 3 变为 4
```

### F17-C：Kiro 执行审查

```
执行者已提交 F17 评价模块的审查请求。请执行完整的审查流程：

1. 读取 .ready-for-review 和 tasks.md 中 F17 的记录
2. 重点审查：
   - 评分范围校验（1-5）
   - 防重复评价逻辑
   - 评价窗口期计算（complete_time + 7天）
   - 综合评分计算公式是否正确（平均值，保留1位小数）
   - 双方都评价后订单状态变更
   - 查看评价时的可见性规则（status=3 vs status=4）
3. 审查测试和证据包
4. 终端独立复跑 mvn test
5. 根据结果执行通过或驳回

重点检查：
- 评分精度问题（BigDecimal vs Double）
- 并发评价场景（双方同时提交）
```

### F17-D：Trae 修正驳回

```
监督者已驳回 F17 评价模块的审查。请：
1. 读取 tasks02.md 中监督者关于 F17 的 [监督者] 反馈
2. 逐一修正代码
3. 重新运行 mvn test
4. 更新 run-folder/F17-评价模块/ 证据包
5. 重新创建 .ready-for-review
6. 在 tasks02.md 中记录修正内容
```

---

## F18：举报模块

### F18-A：Kiro 规划任务

```
请继续你的监督者工作，规划 Feature F18：举报模块。

1. 读取 feature_list.json，找到 id 为 "F18" 的功能
2. 包含小程序端1个接口 + 管理端3个接口
3. 在 tasks.md 中规划详细步骤，包括：
   - Report 实体字段
   - 提交举报：不能举报自己 + 同一目标只能举报一次 + targetType(1商品/2用户)
   - 管理端处理：off_shelf/warn/ban/ignore 四种 action 的不同逻辑
   - ban 操作的级联影响：封禁用户 + 下架商品 + 取消订单
   - 所有文件清单
   - 测试用例要求

规划完成后，保存 tasks.md，然后告诉我你规划的内容摘要。
```

### F18-B：Trae 开始编码

```
请开始你的执行者工作，实现 Feature F18：举报模块。

1. 读取 tasks02.md，找到 F18 举报模块的任务步骤
2. 读取 feature_list.json 中 id="F18" 的 acceptance_criteria
3. 按步骤创建所有文件：entity、mapper、service+impl、dto、vo、controller(mini+admin)
4. 实现所有业务逻辑，特别是管理端 handle 接口的四种 action
5. 编写测试覆盖：举报自己、重复举报、各种处理 action
6. 运行 mvn test，保存输出到 run-folder/F18-举报模块/
7. 生成证据包，创建 .ready-for-review
8. 完成后告诉我结果摘要

注意：
- ban 操作涉及多表级联更新，需要 @Transactional
- 同一用户对同一目标的唯一性可通过数据库唯一索引或代码校验实现
```

### F18-C：Kiro 执行审查

```
执行者已提交 F18 举报模块的审查请求。请执行完整的审查流程：

1. 读取 .ready-for-review 和 tasks.md 中 F18 的记录
2. 重点审查：
   - 不能举报自己的校验
   - 唯一性校验（同一用户对同一目标）
   - ban 操作的级联影响是否完整（用户封禁+商品下架+订单取消）
   - ban 操作是否有事务保证
   - 各 action 的通知预留
3. 审查测试和证据包
4. 终端独立复跑 mvn test
5. 根据结果执行通过或驳回
```

### F18-D：Trae 修正驳回

```
监督者已驳回 F18 举报模块的审查。请：
1. 读取 tasks.md 中监督者关于 F18 的 [监督者] 反馈
2. 逐一修正代码
3. 重新运行 mvn test
4. 更新 run-folder/F18-举报模块/ 证据包
5. 重新创建 .ready-for-review
6. 在 tasks.md 中记录修正内容
```

---

## F19：消息通知模块

### F19-A：Kiro 规划任务

```
请继续你的监督者工作，规划 Feature F19：消息通知模块。

1. 读取 feature_list.json，找到 id 为 "F19" 的功能
2. 包含4个接口 + 一个供其他模块调用的 send 方法
3. 在 tasks.md 中规划详细步骤，包括：
   - Notification 实体字段（参考 notification 表）
   - 4个小程序端接口：list（分页+category筛选）、read、read-all、unread-count
   - send 方法签名：send(userId, type, title, content, relatedId, relatedType, category)
   - send 方法使用 @Async 异步
   - 启动类需要 @EnableAsync
   - 所有文件清单
   - 测试用例要求

规划完成后，保存 tasks.md，然后告诉我你规划的内容摘要。
```

### F19-B：Trae 开始编码

```
请开始你的执行者工作，实现 Feature F19：消息通知模块。

1. 读取 tasks02.md，找到 F19 消息通知模块的任务步骤
2. 读取 feature_list.json 中 id="F19" 的 acceptance_criteria
3. 按步骤创建所有文件：
   - entity/Notification.java
   - mapper/NotificationMapper.java
   - service/NotificationService.java + impl
   - vo/NotificationVO.java
   - controller/mini/MiniNotificationController.java
4. 实现：
   - 通知列表（分页+category筛选）
   - 标记已读（单条+全部）
   - 未读计数
   - send 方法（@Async 异步）
5. 确保启动类有 @EnableAsync 注解
6. 编写测试覆盖所有场景
7. 运行 mvn test，保存输出到 run-folder/F19-消息通知模块/
8. 生成证据包，创建 .ready-for-review
9. 完成后将结果按照要求写入tasks02.md并且告诉我结果摘要
```

### F19-C：Kiro 执行审查

```
执行者已提交 F19 消息通知模块的审查请求。请执行完整的审查流程：

1. 读取 .ready-for-review 和 tasks.md 中 F19 的记录
2. 审查所有文件
3. 重点检查：
   - send 方法是否有 @Async 注解
   - @EnableAsync 是否在启动类上配置
   - 标记全部已读的 SQL 是否正确（只更新当前用户的未读通知）
   - 未读计数的查询条件是否正确
   - send 方法的参数是否完整
4. 审查测试和证据包
5. 终端独立复跑 mvn test
6. 根据结果执行通过或驳回
```

### F19-D：Trae 修正驳回

```
监督者已驳回 F19 消息通知模块的审查。请：
1. 读取 tasks.md 中监督者关于 F19 的 [监督者] 反馈
2. 逐一修正代码
3. 重新运行 mvn test
4. 更新 run-folder/F19-消息通知模块/ 证据包
5. 重新创建 .ready-for-review
6. 在 tasks.md 中记录修正内容
```

---

## F20：回填通知调用

### F20-A：Kiro 规划任务

```
请继续你的监督者工作，规划 Feature F20：回填通知调用。

1. 读取 feature_list.json，找到 id 为 "F20" 的功能
2. 这个功能不创建新文件，而是修改已有的 ServiceImpl 文件
3. 在 tasks.md 中规划详细步骤，列出每个需要修改的文件和具体的修改位置：
   - CampusAuthServiceImpl.java — approve 和 reject 方法中添加 notificationService.send 调用
   - ProductServiceImpl.java — approve、reject、forceOff 方法中添加通知
   - FavoriteServiceImpl.java — add 方法中添加通知
   - TradeOrderServiceImpl.java — create、confirm、cancel 方法中添加通知
   - ReviewServiceImpl.java — submit 方法中添加通知
   - ReportServiceImpl.java — handle 方法中添加通知
4. 对每个通知调用，明确指定：
   - 通知接收人（userId）
   - 通知类型（type）
   - 通知标题（title）
   - 通知内容（content）
   - 关联ID和类型（relatedId, relatedType）
   - 通知分类（category）
5. 测试要求：集成测试验证通知是否被正确触发

规划完成后，保存 tasks.md，然后告诉我你规划的内容摘要。
```

### F20-B：Trae 开始编码

```
请开始你的执行者工作，实现 Feature F20：回填通知调用。

1. 读取 tasks02.md，找到 F20 回填通知调用的任务步骤
2. 读取 feature_list.json 中 id="F20" 的 acceptance_criteria
3. 逐一修改以下 ServiceImpl 文件，在对应方法中添加 NotificationService.send 调用：
   - CampusAuthServiceImpl — 认证通过/驳回
   - ProductServiceImpl — 审核通过/驳回/强制下架
   - FavoriteServiceImpl — 被收藏
   - TradeOrderServiceImpl — 订单创建/确认/取消
   - ReviewServiceImpl — 收到评价
   - ReportServiceImpl — 举报处理
4. 确保：
   - 所有 ServiceImpl 中注入了 NotificationService
   - 通知调用不影响主业务流程（@Async 已在 send 方法上配置）
   - 通知内容清晰、有意义
5. 每修改一个文件，在 tasks.md 中记录
6. 运行 mvn test（全量测试，确保不破坏已有功能）
7. 保存输出到 run-folder/F20-回填通知调用/
8. 生成证据包，创建 .ready-for-review
9. 完成后将结果按照要求写入tasks02.md并且告诉我结果摘要

注意：
- 不要修改已有的业务逻辑，只新增 notificationService.send 调用
- 注入 NotificationService 时注意不要引入循环依赖
```

### F20-C：Kiro 执行审查

```
执行者已提交 F20 回填通知调用的审查请求。请执行完整的审查流程：

1. 读取 .ready-for-review 和 tasks.md 中 F20 的记录
2. 逐一检查每个被修改的 ServiceImpl：
   - 是否正确注入了 NotificationService
   - send 调用的参数是否正确（接收人、类型、标题、内容等）
   - 是否只新增了通知调用，没有修改已有业务逻辑
   - 是否有循环依赖风险
3. 通过 git diff 检查所有变更文件，确认没有意外的修改
4. 运行全量 mvn test，确认所有已通过功能的测试仍然通过
5. 根据结果执行通过或驳回

重点检查：
- 是否所有 acceptance_criteria 中列出的通知点都已覆盖
- 通知内容是否对用户友好
- 是否有遗漏的通知调用
```

### F20-D：Trae 修正驳回

```
监督者已驳回 F20 回填通知调用的审查。请：
1. 读取 tasks.md 中监督者关于 F20 的 [监督者] 反馈
2. 逐一修正遗漏或错误的通知调用
3. 运行全量 mvn test（确保不破坏已有功能）
4. 更新 run-folder/F20-回填通知调用/ 证据包
5. 重新创建 .ready-for-review
6. 在 tasks.md 中记录修正内容
```

---

## F21：Banner与搜索热词

### F21-A：Kiro 规划任务

```
请继续你的监督者工作，规划 Feature F21：Banner与搜索热词。

1. 读取 feature_list.json，找到 id 为 "F21" 的功能
2. 包含两个子模块：Banner（5个接口）+ 搜索热词（1个接口）
3. 在 tasks.md 中规划详细步骤，包括：
   - Banner 实体字段（参考 banner 表）
   - SearchKeyword 实体字段（参考 search_keyword 表）
   - Banner：小程序端按 campusId 查询（Redis 缓存30分钟）+ 管理端 CRUD
   - 搜索热词：is_hot=1 或 search_count 最高的前10个（Redis 缓存1小时）
   - 所有需要创建的文件
   - 测试用例要求

规划完成后，保存 tasks.md，然后告诉我你规划的内容摘要。
```

### F21-B：Trae 开始编码

```
请开始你的执行者工作，实现 Feature F21：Banner与搜索热词。

1. 读取 tasks02.md，找到 F21 Banner与搜索热词的任务步骤
2. 读取 feature_list.json 中 id="F21" 的 acceptance_criteria
3. 按步骤创建所有文件：
   - entity/Banner.java, entity/SearchKeyword.java
   - mapper/BannerMapper.java, mapper/SearchKeywordMapper.java
   - service/BannerService.java + impl, service/SearchKeywordService.java + impl
   - dto/BannerDTO.java
   - vo/BannerVO.java, vo/HotKeywordVO.java
   - controller/mini/MiniBannerController.java, controller/mini/MiniSearchController.java
   - controller/admin/AdminBannerController.java
4. 实现所有业务逻辑
5. 编写测试
6. 运行 mvn test，保存输出到 run-folder/F21-Banner与搜索热词/
7. 生成证据包，创建 .ready-for-review
8. 完成后将结果按照要求写入tasks02.md并且告诉我结果摘要
```

### F21-C：Kiro 执行审查

```
执行者已提交 F21 Banner与搜索热词的审查请求。请执行完整的审查流程：

1. 读取 .ready-for-review 和 tasks.md 中 F21 的记录
2. 审查所有文件
3. 重点检查：
   - Banner Redis 缓存 key 是否按 campusId 区分
   - Banner 缓存 TTL 30分钟
   - 搜索热词查询逻辑（is_hot=1 OR top 10 by search_count）
   - 搜索热词缓存 TTL 1小时
   - Banner 增删改后是否清除缓存
4. 审查测试和证据包
5. 终端独立复跑 mvn test
6. 根据结果执行通过或驳回
```

### F21-D：Trae 修正驳回

```
监督者已驳回 F21 Banner与搜索热词的审查。请：
1. 读取 tasks.md 中监督者关于 F21 的 [监督者] 反馈
2. 逐一修正代码
3. 重新运行 mvn test
4. 更新 run-folder/F21-Banner与搜索热词/ 证据包
5. 重新创建 .ready-for-review
6. 在 tasks.md 中记录修正内容
```

---

## F22：公告模块

### F22-A：Kiro 规划任务

```
请继续你的监督者工作，规划 Feature F22：公告模块。

1. 读取 feature_list.json，找到 id 为 "F22" 的功能
2. 包含管理端4个接口，发布公告时需要异步批量推送通知
3. 在 tasks.md 中规划详细步骤，包括：
   - Notice 实体字段（参考 notice 表）
   - 管理端 CRUD：page、add、update、delete
   - 发布公告时的异步批量通知推送逻辑：
     a. 查询所有正常状态用户
     b. 分批（每批1000条）插入 notification 记录
     c. 使用 @Async 异步执行
   - 所有文件清单
   - 测试用例要求

规划完成后，保存 tasks.md，然后告诉我你规划的内容摘要。
```

### F22-B：Trae 开始编码

```
请开始你的执行者工作，实现 Feature F22：公告模块。

1. 读取 tasks03.md，找到 F22 公告模块的任务步骤
2. 读取 feature_list.json 中 id="F22" 的 acceptance_criteria
3. 按步骤创建所有文件：entity、mapper、service+impl、dto、vo、controller
4. 实现：
   - 管理端公告 CRUD
   - 发布公告时异步批量推送通知：查询所有正常用户 → 分批1000条 → 调用 NotificationMapper 批量插入
5. 编写测试
6. 运行 mvn test，保存输出到 run-folder/F22-公告模块/
7. 生成证据包，创建 .ready-for-review
8. 完成后将结果按照要求写入tasks03.md并且告诉我结果摘要

注意：
- 批量插入使用 MyBatis 的 <foreach> 或 MyBatis-Plus 的 saveBatch
- 分批处理避免单次 SQL 过大
```

### F22-C：Kiro 执行审查

```
执行者已提交 F22 公告模块的审查请求。请执行完整的审查流程：

1. 读取 .ready-for-review 和 tasks.md 中 F22 的记录
2. 重点审查：
   - 批量通知推送是否异步（@Async）
   - 是否分批处理（每批1000条）
   - 批量插入 SQL 是否正确
   - 是否只推送给正常状态用户（非封禁、非注销）
3. 审查测试和证据包
4. 终端独立复跑 mvn test
5. 根据结果执行通过或驳回
```

### F22-D：Trae 修正驳回

```
监督者已驳回 F22 公告模块的审查。请：
1. 读取 tasks.md 中监督者关于 F22 的 [监督者] 反馈
2. 逐一修正代码
3. 重新运行 mvn test
4. 更新 run-folder/F22-公告模块/ 证据包
5. 重新创建 .ready-for-review
6. 在 tasks.md 中记录修正内容
```

---

## F23：员工管理模块

### F23-A：Kiro 规划任务

```
请继续你的监督者工作，规划 Feature F23：员工管理模块。

1. 读取 feature_list.json，找到 id 为 "F23" 的功能
2. 包含6个管理端接口：登录、获取信息、分页、添加、更新、重置密码
3. 在 tasks.md 中规划详细步骤，包括：
   - Employee 实体字段（参考 employee 表）
   - 登录逻辑：BCrypt 验证密码 + 生成管理端 JWT Token（与小程序端区分）
   - 添加员工：仅超级管理员 + 默认密码 123456 的 BCrypt 加密值
   - 重置密码：恢复为 123456 的 BCrypt 值
   - 管理端 Token 与小程序端 Token 的区分方案
   - 所有文件清单
   - 测试用例要求

规划完成后，保存 tasks.md，然后告诉我你规划的内容摘要。
```

### F23-B：Trae 开始编码

```
请开始你的执行者工作，实现 Feature F23：员工管理模块。

1. 读取 tasks03.md，找到 F23 员工管理模块的任务步骤
2. 读取 feature_list.json 中 id="F23" 的 acceptance_criteria
3. 按步骤创建所有文件：
   - entity/Employee.java
   - mapper/EmployeeMapper.java
   - service/EmployeeService.java + impl
   - dto/EmployeeLoginDTO.java, dto/EmployeeDTO.java
   - vo/EmployeeLoginVO.java, vo/EmployeeVO.java
   - controller/admin/AdminEmployeeController.java
4. 实现所有业务逻辑
5. 编写测试覆盖：登录成功/失败、权限校验
6. 运行 mvn test，保存输出到 run-folder/F23-员工管理模块/
7. 生成证据包，创建 .ready-for-review
8. 完成后将结果按照要求写入tasks03.md并且告诉我结果摘要

注意：
- 默认密码 123456 的 BCrypt 值可以用 new BCryptPasswordEncoder().encode("123456") 生成
- 管理端 JWT 需要与小程序端区分（可用不同的签名密钥或 claim 中加入角色标识）
```

### F23-C：Kiro 执行审查

```
执行者已提交 F23 员工管理模块的审查请求。请执行完整的审查流程：

1. 读取 .ready-for-review 和 tasks.md 中 F23 的记录
2. 重点审查：
   - 密码是否使用 BCrypt 加密（非明文、非 MD5）
   - 管理端 Token 是否与小程序端 Token 有区分机制
   - 添加员工的权限校验（仅超级管理员）
   - 默认密码和重置密码是否正确使用 BCrypt
   - 登录日志中是否泄露密码
3. 审查测试和证据包
4. 终端独立复跑 mvn test
5. 根据结果执行通过或驳回
```

### F23-D：Trae 修正驳回

```
监督者已驳回 F23 员工管理模块的审查。请：
1. 读取 tasks.md 中监督者关于 F23 的 [监督者] 反馈
2. 逐一修正代码
3. 重新运行 mvn test
4. 更新 run-folder/F23-员工管理模块/ 证据包
5. 重新创建 .ready-for-review
6. 在 tasks.md 中记录修正内容
```

---

## F24：数据统计模块

### F24-A：Kiro 规划任务

```
请继续你的监督者工作，规划 Feature F24：数据统计模块。

1. 读取 feature_list.json，找到 id 为 "F24" 的功能
2. 包含4个管理端统计接口
3. 在 tasks.md 中规划详细步骤，包括：
   - StatsOverviewVO 字段：今日新增用户/商品/成交量/GMV + 累计数据 + 待处理事项（待审核商品数/待处理认证数/待处理举报数）
   - StatsTrendVO 字段：近N天趋势数据（日期、新增用户、新增商品、成交量、GMV）
   - StatsCampusVO 字段：校区名、商品数、交易数、用户数
   - StatsCategoryVO 字段：分类名、商品数、交易数
   - 所有统计使用 SQL 聚合查询，不使用缓存
   - 所有文件清单
   - 测试用例要求

规划完成后，保存 tasks.md，然后告诉我你规划的内容摘要。
```

### F24-B：Trae 开始编码

```
请开始你的执行者工作，实现 Feature F24：数据统计模块。

1. 读取 tasks.md，找到 F24 数据统计模块的任务步骤
2. 读取 feature_list.json 中 id="F24" 的 acceptance_criteria
3. 按步骤创建所有文件：
   - vo/StatsOverviewVO.java, StatsTrendVO.java, StatsCampusVO.java, StatsCategoryVO.java
   - service/StatsService.java + impl
   - controller/admin/AdminStatsController.java
4. 实现统计查询（使用 SQL 聚合，可能需要在各 Mapper 或新建 StatsMapper 中添加统计方法）
5. 编写测试验证 SQL 查询结果结构
6. 运行 mvn test，保存输出到 run-folder/F24-数据统计模块/
7. 生成证据包，创建 .ready-for-review
8. 完成后告诉我结果摘要

注意：
- trend 接口的 days 参数默认7天
- 统计 SQL 可能涉及多张表，注意性能
- 不使用 Redis 缓存（管理后台访问量小）
```

### F24-C：Kiro 执行审查

```
执行者已提交 F24 数据统计模块的审查请求。请执行完整的审查流程：

1. 读取 .ready-for-review 和 tasks.md 中 F24 的记录
2. 重点审查：
   - 统计 SQL 是否正确（聚合函数、GROUP BY、日期筛选）
   - overview 的待处理事项查询是否准确
   - trend 的日期范围计算是否正确
   - campus 和 category 统计的关联是否正确
   - 是否没有使用缓存（按要求不使用）
3. 审查测试和证据包
4. 终端独立复跑 mvn test
5. 根据结果执行通过或驳回
```

### F24-D：Trae 修正驳回

```
监督者已驳回 F24 数据统计模块的审查。请：
1. 读取 tasks.md 中监督者关于 F24 的 [监督者] 反馈
2. 逐一修正代码
3. 重新运行 mvn test
4. 更新 run-folder/F24-数据统计模块/ 证据包
5. 重新创建 .ready-for-review
6. 在 tasks.md 中记录修正内容
```

---

## F25：定时任务

### F25-A：Kiro 规划任务

```
请继续你的监督者工作，规划 Feature F25：定时任务。

1. 读取 feature_list.json，找到 id 为 "F25" 的功能
2. 包含5个定时任务类
3. 在 tasks.md 中规划详细步骤，包括：
   - 每个定时任务的 Cron 表达式、查询条件、处理逻辑、通知要求
   - OrderExpireTask（每5分钟）：超时取消
   - OrderAutoConfirmTask（每天凌晨2点）：自动确认（WHERE confirm_deadline < NOW() AND status = 1）
   - ReviewAutoTask（每天凌晨3点）：自动好评
   - ProductAutoOffTask（每天凌晨4点）：90天下架
   - UserDeactivateTask（每天凌晨5点）：30天注销清理
   - @EnableScheduling 配置
   - application.yml 定时任务开关配置
   - 日志记录要求
   - 异常处理要求（单条失败不影响其他）
   - 测试用例要求

规划完成后，保存 tasks.md，然后告诉我你规划的内容摘要。
```

### F25-B：Trae 开始编码

```
请开始你的执行者工作，实现 Feature F25：定时任务。

1. 读取 tasks.md，找到 F25 定时任务的任务步骤
2. 读取 feature_list.json 中 id="F25" 的 acceptance_criteria
3. 按步骤创建以下文件：
   - task/OrderExpireTask.java
   - task/OrderAutoConfirmTask.java
   - task/ReviewAutoTask.java
   - task/ProductAutoOffTask.java
   - task/UserDeactivateTask.java
4. 实现每个定时任务：
   - 查询条件精确
   - 批量处理
   - 单条记录 try-catch（失败不影响其他）
   - 完善的日志（开始时间、处理条数、结束时间、耗时）
   - 调用 NotificationService 通知相关用户
5. 确保启动类有 @EnableScheduling
6. 在 application.yml 中添加定时任务开关配置
7. 编写测试验证业务逻辑
8. 运行 mvn test，保存输出到 run-folder/F25-定时任务/
9. 生成证据包，创建 .ready-for-review
10. 完成后告诉我结果摘要

注意：
- ReviewAutoTask 生成默认好评时 is_auto=1，评分全5分
- UserDeactivateTask 清理个人信息但保留交易记录
- 每个任务开始时记录日志，结束时记录处理条数和耗时
```

### F25-C：Kiro 执行审查

```
执行者已提交 F25 定时任务的审查请求。请执行完整的审查流程：

1. 读取 .ready-for-review 和 tasks.md 中 F25 的记录
2. 逐一审查每个定时任务：
   - Cron 表达式是否正确
   - 查询条件是否精确（特别是时间比较）
   - 是否有 try-catch 在循环内部（单条失败不影响其他）
   - 状态更新是否正确（订单、商品、用户）
   - 通知是否调用
   - 日志是否完善
   - @EnableScheduling 是否配置
   - application.yml 开关配置是否存在
3. 特别审查 ReviewAutoTask：
   - 自动好评的评分是否为5/5/5
   - is_auto 字段是否设置为1
   - 综合评分是否重新计算
4. 审查测试和证据包
5. 终端独立复跑 mvn test
6. 根据结果执行通过或驳回

重点检查：
- Cron 表达式是否正确（特别是凌晨执行的任务）
- 时间比较的 SQL 是否有时区问题
- 批量更新是否有性能问题
```

### F25-D：Trae 修正驳回

```
监督者已驳回 F25 定时任务的审查。请：
1. 读取 tasks.md 中监督者关于 F25 的 [监督者] 反馈
2. 逐一修正代码
3. 重新运行 mvn test
4. 更新 run-folder/F25-定时任务/ 证据包
5. 重新创建 .ready-for-review
6. 在 tasks.md 中记录修正内容
```

---

## F26：OpenIM集成

### F26-A：Kiro 规划任务

```
请继续你的监督者工作，规划 Feature F26：OpenIM集成。

这是最后一个功能。

1. 读取 feature_list.json，找到 id 为 "F26" 的功能
2. 包含1个新接口 + 修改 UserServiceImpl
3. 在 tasks.md 中规划详细步骤，包括：
   - OpenImConfig：从 application.yml 读取 OpenIM 服务地址、adminToken 等
   - ImService.registerUser：调用 OpenIM API 注册用户
   - ImService.getUserToken：调用 OpenIM API 获取用户 Token
   - UserServiceImpl 修改：用户注册/首次登录时调用 imService.registerUser
   - GET /mini/im/token 接口
   - OpenIM API 调用失败的降级处理（记录日志但不影响主业务）
   - 测试用例要求（mock OpenIM API）
   - application.yml 中的 OpenIM 配置项

规划完成后，保存 tasks.md，然后告诉我你规划的内容摘要。
```

### F26-B：Trae 开始编码

```
请开始你的执行者工作，实现 Feature F26：OpenIM集成。

这是项目的最后一个功能！

1. 读取 tasks.md，找到 F26 OpenIM集成的任务步骤
2. 读取 feature_list.json 中 id="F26" 的 acceptance_criteria
3. 按步骤创建/修改文件：
   - 创建 config/OpenImConfig.java
   - 创建 service/ImService.java + service/impl/ImServiceImpl.java
   - 创建 controller/mini/MiniImController.java
   - 修改 service/impl/UserServiceImpl.java — 在用户注册/首次登录时调用 imService.registerUser
4. 实现：
   - OpenIM API 调用使用 RestTemplate
   - registerUser：调用 OpenIM 注册接口，系统 userId 作为 IM userId
   - getUserToken：调用 OpenIM admin API 获取 UserToken
   - 降级处理：API 调用失败时 try-catch，记录 error 日志但不抛异常
5. 编写测试（mock OpenIM API 调用）
6. 运行 mvn test（全量测试，确保不破坏任何已有功能）
7. 保存输出到 run-folder/F26-OpenIM集成/
8. 生成证据包，创建 .ready-for-review
9. 完成后告诉我结果摘要

注意：
- OpenIM API 调用失败不能影响用户正常登录
- application.yml 中添加 openim.apiUrl、openim.adminToken 等配置
- 这是最后一个功能，全量测试通过非常重要
```

### F26-C：Kiro 执行审查

```
执行者已提交 F26 OpenIM集成的审查请求。

这是最后一个功能的审查，请特别仔细！

1. 读取 .ready-for-review 和 tasks.md 中 F26 的记录
2. 审查所有新建和修改的文件：
   - OpenImConfig 是否从配置文件读取（非硬编码）
   - ImService 的 registerUser 和 getUserToken 逻辑
   - 降级处理：API 调用失败时是否 catch 异常并记录日志，而非向上抛出
   - UserServiceImpl 的修改：是否只在注册/首次登录时调用 registerUser
   - 是否影响了已有的登录逻辑
3. 审查测试（是否 mock 了外部 API 调用）
4. **运行全量 mvn test**（确保所有26个功能的测试都通过）
5. 如果全部通过：
   - 修改 feature_list.json 中 F26 的 passes 为 true
   - 检查 feature_list.json 中所有26个功能的 passes 是否都为 true
   - 如果全部为 true：
     ```
     - [监督者] YYYY-MM-DD HH:MM: 🎉 所有26个功能验收通过，项目开发完成！
     ```
     执行：
     ```bash
     git add .
     git commit -m "feat: F26 OpenIM集成 - 验收通过，项目全部功能完成"
     git tag v1.0.0-release
     ```

重点检查：
- 降级处理是否真正有效
- 是否影响已有的登录流程
- 全量测试是否全部通过
```

### F26-D：Trae 修正驳回

```
监督者已驳回 F26 OpenIM集成的审查。请：
1. 读取 tasks.md 中监督者关于 F26 的 [监督者] 反馈
2. 逐一修正代码
3. 运行全量 mvn test（这是最后一个功能，必须确保所有测试通过）
4. 更新 run-folder/F26-OpenIM集成/ 证据包
5. 重新创建 .ready-for-review
6. 在 tasks.md 中记录修正内容
```

---

## 项目完成检查提示词（最终使用）

当 F26 审查通过后，在 **Kiro** 中执行最终检查：

```
所有26个功能已完成。请执行项目最终检查：

1. 读取 feature_list.json，确认所有26个功能的 passes 均为 true
2. 运行全量 mvn test，确认所有测试通过
3. 运行 mvn compile，确认项目编译无错误
4. 检查 tasks.md，确认所有任务都有完整的记录
5. 检查 run-folder/ 下是否有26个功能的证据包
6. 执行 git log 查看提交历史是否完整
7. 生成项目完成报告，包括：
   - 总功能数：26
   - 总接口数：[统计]
   - 总文件数：[统计]
   - 开发周期：[从 tasks.md 第一条记录到最后一条]
   - 所有功能列表及通过状态

最终 Git 操作：
git tag v1.0.0-release
git push origin main --tags

🎉 项目开发完成！
```
