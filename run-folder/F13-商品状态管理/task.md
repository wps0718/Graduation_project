# Feature F13：商品状态管理

## 功能概述
小程序端上下架与删除，管理端商品审核、分页、详情与强制下架，含状态流转与权限校验。

## 实现步骤
1. 扩展 ProductService 与 ProductServiceImpl 实现状态流转
2. 新增 AdminProductPageVO 与管理端/小程序端接口
3. 新增 ProductMapper XML 管理端分页查询
4. 补充 ProductServiceImplTest 覆盖状态流转与审核
5. 运行 mvn test 输出到 run-folder/F13-商品状态管理/test_output.log

## 验收标准
- 下架校验归属与无进行中订单，status→2
- 上架校验归属，status→0
- 删除校验归属与无进行中订单，is_deleted→1
- 管理端分页支持 status 筛选
- 审核通过 status→1，记录审核信息并通知（预留）
- 审核驳回 status→4，记录原因并通知（预留）
- 批量审核通过接收商品ID列表
- 强制下架 status→2 并通知（预留）
- Service 层测试覆盖状态流转与权限校验
