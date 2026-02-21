# Feature F14：收藏模块

## 功能概述
小程序端收藏/取消收藏/收藏列表/收藏状态查询，包含收藏数同步与通知预留。

## 实现步骤
1. 创建 Favorite 实体与 Mapper、XML 查询
2. 创建 FavoriteDTO 与 FavoriteListVO
3. 实现 FavoriteService 与 FavoriteServiceImpl 业务逻辑
4. 新增 MiniFavoriteController 接口
5. 编写 FavoriteServiceImplTest 覆盖核心场景
6. 运行 mvn test 输出到 run-folder/F14-收藏模块/test_output.log

## 验收标准
- 收藏时校验商品存在且未被收藏过
- 收藏成功后 product.favorite_count+1
- 收藏成功后通知卖家（预留）
- 取消收藏后 product.favorite_count-1
- 收藏列表关联商品信息并按收藏时间倒序分页
- 查询是否收藏返回 isFavorited 布尔值
- 同一用户对同一商品不能重复收藏
