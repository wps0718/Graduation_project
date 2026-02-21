## Feature F19：消息通知模块任务规划

### 范围
- 小程序端：通知列表、标记已读、全部已读、未读计数
- 通用：异步 send 方法供业务模块调用

### 依赖
- F01（微信登录）

### 业务规则
- 通知列表支持分页与 category 筛选
- 只能标记自己的通知已读
- 标记全部已读仅作用当前用户未读通知
- 未读计数仅统计当前用户未读通知
- send 方法异步写入通知记录

### 文件清单
- entity：Notification
- mapper：NotificationMapper
- service：NotificationService、NotificationServiceImpl
- vo：NotificationVO
- controller：MiniNotificationController
- test：NotificationServiceImplTest

### 测试场景
- 通知列表分页查询
- 按 category 筛选
- 标记单条已读成功/无权限/已读跳过
- 批量标记全部已读
- 未读数量统计
- send 发送成功
