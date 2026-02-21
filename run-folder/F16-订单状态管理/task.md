# Feature F16：订单状态管理

## 功能概述
小程序端确认收货/取消/删除订单，管理端订单分页与详情，含状态流转、商品联动与通知预留。

## 实现步骤
1. 新增 OrderCancelDTO 与 AdminOrderPageVO
2. 新增 AdminOrderController 接口
3. 扩展 TradeOrderService 与实现确认收货、取消、删除
4. 新增管理端分页查询 SQL
5. 小程序端新增确认收货/取消/删除接口
6. 编写 TradeOrderServiceImplTest 覆盖核心状态流转
7. 运行 mvn test 输出到 run-folder/F16-订单状态管理/test_output.log

## 验收标准
- 确认收货：校验当前用户是买家且 status=1
- 确认收货后 status→3(已完成)，设置 complete_time
- 确认收货后商品 status→3(已售出)，通知卖家
- 取消订单：校验当前用户是买家或卖家且 status=1
- 取消后 status→5(已取消)，记录 cancel_by=当前userId
- 取消后商品 status 恢复→1(在售)，通知对方
- 删除订单：校验 status=4 或 5（已评价或已取消才能删除）
- 根据当前用户角色设置 is_deleted_buyer=1 或 is_deleted_seller=1（逻辑删除）
- 管理端订单分页查询和详情查看
- Service 层单元测试覆盖确认收货、取消（买家/卖家）、删除（状态限制）场景
