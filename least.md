## 总结（已实现内容）喵~

### 后端喵~

- 新增用户关注体系：新增 `user_follow` 表结构（含唯一索引 follower_id+followee_id 与查询索引）并同步增量 SQL 与 init.sql 喵~
- 新增关注模块代码：包含 entity/mapper/service/controller，并按项目规范使用构造注入、BusinessException、LambdaQueryWrapper 喵~
- 关注相关缓存策略：对关注关系 check 与关注/粉丝统计 count 增加 Redis 缓存（TTL 10 分钟），在 follow/unfollow 后失效相关 key 喵~
- 卖家主页数据扩展：`UserProfileVO` 增加 `bio/ipRegion/lastActive.../followerCount/followingCount`，并将商品列表改为 `SellerProductVO` 分页返回，保证与 `ProductCard` 渲染字段对齐喵~
- 用户资料更新：`/mini/user/update` 支持更新 `bio`，并在 `/mini/user/info` 返回 `bio` 用于回显喵~
- 登录链路 ipRegion：在无外部 IP 解析依赖情况下，登录/新用户创建链路写入 `ipRegion` 的兜底值（如“未知/本地”），便于后续替换真实解析实现喵~
- 新增商品评论体系：新增 `product_comment` 表及配套增量 SQL，后端实现评论发布、分页查询与 VO 转换喵~
- 消息中心增强：实现收藏通知与回复通知的独立二级页面，支持按类型分页获取通知数据喵~
- 私信后端闭环：实现 `POST /mini/chat/message/send` 接口，支持通过 HTTP 协议发送文本与结构化商品卡片，解决了 IM 消息持久化与多端同步问题喵~
- 已读状态维护：实现 `POST /mini/chat/read` 接口，支持按会话标记已读，并同步更新 Redis 中的未读总数缓存喵~
- 用户状态透出：在 `UserProfileVO` 中补全 `status` 字段，支持前端根据卖家状态（封禁/注销）动态调整交互喵~

### 小程序前端（uni-app）喵~

- 私信功能完整实现：
    - 商品详情页“我想要”对接私信：自动创建会话并首发商品咨询卡片喵~
    - 卖家主页新增“私信”按钮：支持纯社交上下文发起对话，并根据卖家状态自动置灰喵~
    - 聊天详情页重构：支持商品卡片/文本消息混合渲染、已读状态实时显示（灰/蓝状态切换）喵~
    - 列表页体验优化：会话列表左侧图标根据是否有商品自动切换显示商品图或用户头像喵~
- 实时交互优化：实现聊天页 5s 短轮询机制，自动拉取新消息并同步已读状态，移除开发阶段的模拟回复逻辑喵~

### 验证与质量喵~

- 已进行 Java 端编译与单元测试回归：执行 `mvn clean test`，用例通过（0 failures / 0 errors）喵~
- 已补充私信模块单测：新增 `ChatMessageServiceImplTest` 覆盖商品卡片渲染逻辑、HTTP 消息发送接口及已读状态映射验证喵~
- 已适配用户状态单测：更新 `UserServiceImplTest` 确保 VO 扩展字段 `status` 的正确性喵~

## 实现进度对照（你列出的 6 条）喵~

1) 后端：实现关注模块（entity/mapper/service/controller）+ Redis缓存与失效策略喵~
- 状态：已实现✅喵~
- 证据：关注模块实现见 FollowServiceImpl / MiniFollowController / UserFollow* 相关文件喵~
- 缓存策略：`follow:check:{followerId}:{followeeId}` 与 `follow:stats:{userId}`，TTL 10 分钟，follow/unfollow 后删除相关 key 喵~

2) 后端：扩展UserProfileVO（bio/ipRegion/lastActive/关注统计/用户状态）+ SellerProductVO分页返回喵~
- 状态：已实现✅喵~
- 证据：`UserProfileVO` 字段已扩展，包含 `status` 字段用于私信权限判断喵~

3) 前端：卖家主页展示新增字段 + 关注/取关交互 + 商品列表分页对齐 + 私信入口喵~
- 状态：已实现✅喵~
- 说明：已新增“私信”按钮并添加多重状态拦截逻辑喵~

4) 验证：运行后端单测与编译；自检关键页面跳转与字段渲染；检查诊断喵~
- 状态：已完成✅喵~
- 已完成：后端编译+单测（mvn clean test）、私信全流程单测加固、IDE 诊断检查喵~

5) 后端：更新用户更新接口支持bio；登录链路尽力写入ipRegion；实现私信消息/已读接口喵~
- 状态：已实现✅喵~
- 说明：新增 `/mini/chat/message/send` 与 `/mini/chat/read` 接口喵~

6) 前端：编辑资料页增加个人简介bio编辑并提交；实现聊天详情页与消息已读显示喵~
- 状态：已实现✅喵~
- 说明：聊天页已实现“已读/未读”状态动态展示喵~

7) 后端：实现商品评论模块（entity/mapper/service/controller）喵~
- 状态：已实现✅喵~
- 证据：见 ProductComment* 相关文件及 MiniProductController 中的评论接口喵~

8) 前端：完成通知中心二级页面（收到收藏/回复）与聊天设置页喵~
- 状态：已实现✅喵~
- 说明：新增 received-favorites、received-replies 与 chat/settings 页面，并补齐相关图标资源喵~

## 详细检查清单（按执行顺序，可直接照着验收）喵~

### A. 数据库与脚本喵~

- [ ] 在测试库执行增量脚本：`sql/update/2026-04-08_f07_user_follow_profile.sql` 喵~
- [ ] 在测试库执行增量脚本：`sql/update/2026-04-10_f08_product_comment.sql` 喵~
- [ ] 检查 `user` 表新增字段是否存在：`bio`、`ip_region` 喵~
- [ ] 检查 `user_follow` 表是否创建成功喵~
- [ ] 检查 `product_comment` 表是否创建成功喵~
- [ ] 检查 `chat_message` 表是否支持 `msg_type=2` (商品卡片) 喵~
- [ ] 如已在生产/准生产执行过，确认脚本重复执行不会报错喵~

### B. 后端接口（功能验收）喵~

#### 私信模块喵~

- [ ] 调用 `POST /mini/chat/message/send`：body 传入 `sessionKey`, `type=1`, `content="test"`，应返回消息 ID 喵~
- [ ] 调用 `POST /mini/chat/message/send`：body 传入 `type=2` 且 content 为商品 JSON，后端 `calculateLastMsg` 应正确处理为 `[商品卡片]` 喵~
- [ ] 调用 `POST /mini/chat/read`：应成功清除该会话在 Redis 中的未读数喵~
- [ ] 调用 `GET /mini/user/profile/{id}`：应返回 `status` 字段（1-正常, 0-封禁, 2-注销）喵~

#### 关注模块喵~

- [ ] 登录后调用 `POST /mini/follow/follow`，body：`{ "userId": 卖家ID }`，应返回 code=1 喵~
- [ ] 重复调用 follow（同一对 follower/followee），应幂等成功喵~
- [ ] 调用 `POST /mini/follow/unfollow`，应成功删除关系，并保持幂等喵~
- [ ] 调用 `GET /mini/follow/check/{userId}`：关注后应为 true，取关后应为 false 喵~
- [ ] 调用 `GET /mini/follow/stats/{userId}`：粉丝数/关注数应与数据库一致喵~
- [ ] 关注自己：follow body 传入自身 userId，应返回业务错误“不能关注自己”喵~

#### 卖家主页数据喵~

- [ ] 调用 `GET /mini/user/profile/{sellerId}?page=1&pageSize=8`，响应中应包含：`bio/ipRegion/lastActiveText/followerCount/followingCount` 与 `products.total/products.records` 喵~
- [ ] `products.records[*]` 字段应包含：`id/title/price/originalPrice/coverImage/campusName/conditionLevel/createTime` 喵~

#### 商品评论与通知喵~

- [ ] 调用 `POST /mini/product/comment` 发布评论，应成功并产生对应通知喵~
- [ ] 调用 `GET /mini/notification/received-favorites` 应返回分页的收藏通知列表喵~
- [ ] 调用 `GET /mini/notification/received-replies` 应返回分页的回复通知列表喵~

### C. Redis 缓存（策略验收）喵~

- [ ] 调用 checkFollow 后应写入缓存 key：`follow:check:{followerId}:{followeeId}`（值为 "1"/"0"，TTL≈10min）喵~
- [ ] 调用 getFollowStats 后应写入缓存 key：`follow:stats:{userId}`（JSON，TTL≈10min）喵~
- [ ] follow/unfollow 后应删除上述 check key 与双方 stats key，确保统计数据及时刷新喵~

### D. 小程序页面（手工验收）喵~

#### 商品详情 -> 卖家主页跳转喵~

- [ ] 打开任意商品详情页，点击底部卖家区域，应跳转到 `/pages/seller/profile?id={sellerId}` 喵~
- [ ] 卖家区域字段缺失时仍能渲染基本信息（已做 seller 兼容映射），且跳转不报错喵~

#### 卖家主页展示与分页喵~

- [ ] 卖家主页顶部展示：昵称、认证状态、评分、IP属地、最后活跃文本喵~
- [ ] 统计区展示：在售/已成交/粉丝/关注四项数字喵~
- [ ] 简介存在时展示“简介”模块，长度较长可展开/收起喵~
- [ ] 商品列表首屏可见商品卡片，`coverImage/campusName/conditionText/createTime/price/originalPrice` 均正常渲染喵~
- [ ] 上拉触底触发分页加载，直到显示“没有更多了”喵~

#### 关注/取关交互喵~

- [ ] 未登录点击“关注”，应提示登录并跳转登录页喵~
- [ ] 已登录点击“关注”，按钮状态变为“已关注”，并刷新粉丝数（优先从 stats 接口刷新，失败则本地兜底 +1/-1）喵~
- [ ] 点击“已关注”执行取关，按钮恢复“关注”，粉丝数同步减少喵~

#### 编辑资料页（bio）喵~

- [ ] 进入“编辑资料”页，bio 能从 `/mini/user/info` 回显喵~
- [ ] 修改 bio 后点击保存，调用 `/mini/user/update` 成功，并在重新进入页面/个人中心回显最新 bio 喵~

### E. 自动化验证（回归）喵~

- [ ] 执行后端：`mvn clean test`，确保无新增失败用例喵~
- [ ] 若后续引入真实 IP 解析库，需补充针对 ipRegion 的单测与接口回归喵~
