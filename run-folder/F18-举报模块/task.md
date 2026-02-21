## Feature F18：举报模块任务规划

### 范围
- 小程序端：提交举报
- 管理端：举报分页、举报详情、处理举报

### 依赖
- F01（微信登录）
- F11（商品发布与编辑）
- F15（订单创建与查询）

### 业务规则
- 不能举报自己
- 同一用户对同一目标只能举报一次
- 目标类型：1-商品，2-用户
- 处理动作：off_shelf / warn / ban / ignore
- ban 级联：封禁用户、下架在售商品、取消进行中订单、恢复订单商品在售状态

### 文件清单
- entity：Report
- dto：ReportSubmitDTO、ReportHandleDTO
- vo：ReportPageVO、ReportDetailVO
- mapper：ReportMapper、ReportMapper.xml
- service：ReportService、ReportServiceImpl
- controller：MiniReportController、AdminReportController
- test：ReportServiceImplTest

### 测试场景
- 提交举报成功
- 举报自己（商品/用户）失败
- 重复举报失败
- 目标不存在失败
- 处理动作：off_shelf / warn / ban / ignore
- 已处理举报再次处理失败
