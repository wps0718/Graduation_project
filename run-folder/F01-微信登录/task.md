# Feature F01：微信登录 - 任务步骤（证据包）

## 步骤

1. 创建 User 实体类（entity/User.java）
2. 创建 UserMapper 接口（mapper/UserMapper.java）
3. 创建 DTO 和 VO（WxLoginDTO / LoginVO）
4. 创建 WxConfig 配置类（读取 wx.appId / wx.appSecret）
5. 创建 UserService 接口和实现类（wxLogin：调用微信 jscode2session；自动注册；更新登录时间；封禁抛异常；注销中返回标识；生成JWT）
6. 创建 MiniUserController（POST /mini/user/wx-login）
7. 编写 UserServiceImplTest 覆盖四场景
8. 运行测试并保存输出到 test_output.log
9. 在项目根目录创建 .ready-for-review

