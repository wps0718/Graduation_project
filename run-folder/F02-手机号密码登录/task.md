# Feature F02：手机号密码登录 - 任务步骤（证据包）

## 步骤

1. 引入 spring-security-crypto 依赖（pom.xml）
2. 创建 AccountLoginDTO（dto/AccountLoginDTO.java）
3. 在 UserService 增加 accountLogin（service/UserService.java）
4. 在 UserServiceImpl 实现 accountLogin（BCrypt 校验 + Redis 失败计数/锁定 + JWT）
5. 创建 BCryptPasswordEncoder Bean（config/SecurityCryptoConfig.java）
6. 创建 MiniUserController 接口（POST /mini/user/login）
7. 编写 UserServiceImplTest 覆盖登录场景
8. 运行测试并保存输出到 test_output.log
9. 在项目根目录创建 .ready-for-review
