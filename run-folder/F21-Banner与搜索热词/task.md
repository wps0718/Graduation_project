# F21 Banner与搜索热词任务

## 目标
- 完成 Banner 列表展示与管理接口
- 完成搜索热词读取与缓存
- 接入 Redis 缓存并保证缓存失效
- 提供单元测试与测试证据

## 实施步骤
1. 新增 Banner、SearchKeyword 实体与 Mapper
2. 新增 BannerDTO、BannerVO、HotKeywordVO
3. 实现 BannerService / SearchKeywordService 及业务逻辑
4. 新增管理端与小程序端控制器接口
5. 同步 Redis 常量键并实现缓存逻辑
6. 完成单元测试并运行测试输出证据
7. 更新增量 SQL 脚本
