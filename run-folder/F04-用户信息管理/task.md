# Feature F04：用户信息管理

## 目标
- 实现用户信息查询 / 更新 / 统计数据接口
- 手机号脱敏展示
- 统计数据 Redis 缓存（10 分钟）

## 关键接口
- GET /mini/user/info
- POST /mini/user/update
- GET /mini/user/stats

## 关键实现点
- 从 UserContext 获取当前 userId
- 校区名通过 UserMapper 简单 SQL 查询
- updateUserInfo 更新后清理 Redis 缓存
- getUserStats 优先读缓存，未命中再查库并回写缓存

## 验证
- 运行：mvn test -Dtest=UserServiceImplTest
- 输出：test_output.log
