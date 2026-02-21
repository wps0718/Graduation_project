# Feature F10：学院管理模块

## 功能概述
管理端学院的增删改查功能，删除学院前需要检查是否存在认证记录引用。

## 实现步骤
1. 创建 dto/CollegeDTO.java
2. 在 CollegeService 中新增 add/update/delete 方法
3. 在 CollegeServiceImpl 中实现新增、更新、删除与缓存清除，并校验认证引用
4. 创建 controller/admin/AdminCollegeController.java
5. 编写 CollegeServiceImplTest 覆盖 6 个场景
6. 运行 mvn test 输出到 run-folder/F10-学院管理模块/test_output.log

## 验收标准
- 学院列表查询返回所有学院
- 删除学院前检查是否有认证记录使用该学院，有则抛异常禁止删除
- 添加/更新学院后清除学院列表 Redis 缓存
- 编写 Service 层单元测试，覆盖删除有认证引用的学院场景
