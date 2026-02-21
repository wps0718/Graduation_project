## Feature F06：校园认证-小程序端

### 验收标准
- 审核中状态不允许重复提交认证
- 同一学号不能被两个账号认证（学号唯一性校验）
- 已驳回的认证可以重新提交（更新已有记录而非新建）
- 提交认证后更新用户表 auth_status=1（审核中）
- 认证状态查询返回 AuthStatusVO 包含 status、collegeName、studentNo、className、certImage、rejectReason、reviewTime
- 学院列表查询 status=1 的学院，按 sort 排序，优先走 Redis 缓存
- 编写 Service 层单元测试，覆盖首次提交、重复提交、驳回后重新提交、学号重复场景

### 关键实现点
- 认证提交：从 UserContext 获取 userId；检查本用户已有认证状态；校验 studentNo 是否被其他 userId 使用；写入/更新 campus_auth；更新 user.auth_status=1
- 认证状态：按 userId 查 campus_auth；关联 college 获取学院名称
- 学院列表：Redis key=college:list，TTL=1小时；缓存未命中则按 status=1 且 sort 升序查询并写入缓存

