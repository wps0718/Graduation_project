# Feature F11：商品发布与编辑

## 功能概述
小程序端商品发布、编辑、修改价格，含图片 JSON 存储与权限校验。

## 实现步骤
1. 创建 Product 实体与基础架构
2. 创建 ProductPublishDTO、ProductUpdateDTO 校验规则
3. 实现 ProductService 与 ProductServiceImpl 业务逻辑
4. 创建 MiniProductController 接口
5. 编写 ProductServiceImplTest 覆盖核心场景
6. 运行 mvn test 输出到 run-folder/F11-商品发布与编辑/test_output.log

## 验收标准
- 参数校验：标题1-50字、价格>0、图片1-9张、描述1-500字
- 发布商品默认 status=0、is_deleted=0
- auto_off_time 设置为当前时间+90天
- images 字段存储为 JSON 数组字符串
- 编辑商品时校验是否是自己的商品
- 编辑后状态重置为待审核、清空驳回原因、重新设置 auto_off_time
- 修改价格只更新价格不重新审核
- 编写 Service 层单元测试覆盖核心场景
