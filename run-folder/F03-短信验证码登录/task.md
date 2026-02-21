# Feature F03：短信验证码登录 - 任务步骤（证据包）

## 步骤

1. 创建 SmsSendDTO（dto/SmsSendDTO.java）
2. 创建 SmsLoginDTO（dto/SmsLoginDTO.java）
3. 在 UserService 增加 sendSmsCode 和 smsLogin（service/UserService.java）
4. 在 UserServiceImpl 实现 sendSmsCode（频率限制/每日上限/验证码生成与存储/日志）
5. 在 UserServiceImpl 实现 smsLogin（验证码校验/验证后删除/查或建用户/生成JWT）
6. 在 MiniUserController 增加接口（POST /mini/user/sms/send、POST /mini/user/sms-login）
7. 更新 WebMvcConfig 放行短信接口
8. 编写 UserServiceImplTest 覆盖所有验收场景
9. 运行测试并保存输出到 test_output.log
10. 在项目根目录创建 .ready-for-review
