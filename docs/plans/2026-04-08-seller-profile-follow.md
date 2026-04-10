# 卖家主页跳转 + 关注体系 + 个人信息扩展 Implementation Plan
 
> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.
 
**Goal:** 复用现有卖家主页页面，打通商品详情页卖家卡片跳转，并新增关注/粉丝、个人简介、IP属地、最后活跃等能力。
 
**Architecture:** 前端先在商品详情页做 seller 字段兼容映射，确保点击跳转稳定；后端新增 `user_follow` 表与用户扩展字段（bio、ip_region），并通过新接口提供关注状态与关注数据；卖家主页通过公开 profile 接口拿到卖家公开信息与商品列表，通过登录接口拿到关注状态并支持关注/取关。
 
**Tech Stack:** Java 17, Spring Boot 3.x, MyBatis-Plus, MySQL 8.0, uni-app(Vue 3)。
 
---
 
## 范围与约束
 
- 复用既有卖家主页：`miniapp/pages/seller/profile.vue`。
- 商品详情页卖家信息卡片必须能正确展示“发布账号”的卖家信息，并可点击进入对应卖家主页。
- 数据库变更需要同步更新 `sql/init.sql` 与新增增量脚本到 `sql/update/`。
- 用户隐私：IP属地仅展示省/市等粗粒度字符串，不展示原始IP；手机号不在卖家主页公开展示。
 
---
 
### Task 1: 商品详情页卖家卡片字段绑定与跳转稳定化
 
**Files:**
- Modify: `g:\Code\Graduation_project\miniapp\pages\product\detail\detail.vue`
 
**Step 1: 写一个最小复现实例（手工验证步骤）**
- 打开任意商品详情页。
- 观察“卖家信息”区是否展示卖家昵称/头像/认证/评分。
- 点击卖家信息区，确认能进入 `/pages/seller/profile?id=卖家ID`。
 
**Step 2: 修复 seller 字段结构不一致**
- 兼容两种数据结构：
  - `detail.seller`（对象，含 `id/nickName/avatarUrl/score/authStatus`）
  - `detail.sellerId/sellerNickName/sellerAvatarUrl/sellerScore/sellerAuthStatus`（扁平字段，后端 VO 现状）
- 推荐改法：将 `seller` computed 改为“优先取 detail.seller，否则从扁平字段组装一个 seller 对象”。
 
**Step 3: 修复 goSellerProfile 取 ID 的来源**
- `goSellerProfile()` 内使用 `seller.id`（由 Step 2 统一供给）。
- 如果 sellerId 缺失，直接 return。
- 如果 sellerId 等于当前用户 id，则跳转个人中心 tab。
 
**Step 4: 验证**
- 运行小程序真机/模拟器：进入详情页，点击卖家卡片能跳转。
- 兼容老数据：即使后端暂不改动 seller 结构，也能正常跳转。
- 若代码已实现 seller 兼容与跳转，完成验证后可直接标记本 Task 完成并跳过后续改动。
 
---
 
### Task 2: 扩展用户表字段（个人简介、IP属地）
 
**Files:**
- Modify: `g:\Code\Graduation_project\sql\init.sql`
- Create: `g:\Code\Graduation_project\sql\update\2026-04-08_fxx_user_follow_profile.sql`
- Modify: `g:\Code\Graduation_project\src\main\java\com\qingyuan\secondhand\entity\User.java`
 
**Step 1: 设计字段**
- `user.bio`：`varchar(200)`，默认 NULL，用于个人简介。
- `user.ip_region`：`varchar(64)`，默认 NULL，用于“IP属地”（省/市/国家等）。
 
**Step 2: 更新 init.sql**
- 在 `user` 表结构中补充字段定义，并保证顺序与注释清晰。
 
**Step 3: 新增增量 SQL**
- `ALTER TABLE user ADD COLUMN bio varchar(200) ...;`
- `ALTER TABLE user ADD COLUMN ip_region varchar(64) ...;`
- 注意 MySQL 兼容：必要时用 `ADD COLUMN ...` 两条语句。
 
**Step 4: 更新 User 实体**
- 在 `User` 实体新增 `bio`、`ipRegion` 字段（驼峰命名 + MP 自动映射）。
 
---
 
### Task 3: 新增关注/粉丝体系数据表与索引
 
**Files:**
- Modify: `g:\Code\Graduation_project\sql\init.sql`
- Create: `g:\Code\Graduation_project\sql\update\2026-04-08_fxx_user_follow_profile.sql`
- Create: `g:\Code\Graduation_project\src\main\java\com\qingyuan\secondhand\entity\UserFollow.java`
- Create: `g:\Code\Graduation_project\src\main\java\com\qingyuan\secondhand\mapper\UserFollowMapper.java`
 
**Step 1: 设计表结构**
- 表名：`user_follow`
- 字段：
  - `id` bigint 自增主键
  - `follower_id` bigint NOT NULL（关注者）
  - `followee_id` bigint NOT NULL（被关注者）
  - `create_time` datetime
  - `update_time` datetime
- 索引：
  - UNIQUE(`follower_id`,`followee_id`) 防重复关注
  - KEY(`followee_id`) 用于粉丝数统计
  - KEY(`follower_id`) 用于关注数统计
 
**Step 2: 在 init.sql 增加建表语句**
 
**Step 3: 在增量 SQL 增加建表语句**
 
**Step 4: 创建实体与 Mapper**
- 实体字段：`followerId/followeeId/createTime/updateTime`。
- Mapper 继承 `BaseMapper<UserFollow>`。
 
---
 
### Task 4: 后端关注接口（关注/取关/检查/统计）
 
**Files:**
- Create: `g:\Code\Graduation_project\src\main\java\com\qingyuan\secondhand\controller\mini\MiniFollowController.java`
- Create: `g:\Code\Graduation_project\src\main\java\com\qingyuan\secondhand\service\FollowService.java`
- Create: `g:\Code\Graduation_project\src\main\java\com\qingyuan\secondhand\service\impl\FollowServiceImpl.java`
- Create: `g:\Code\Graduation_project\src\main\java\com\qingyuan\secondhand\dto\FollowUserDTO.java`
- Modify: `g:\Code\Graduation_project\src\main\java\com\qingyuan\secondhand\config\WebMvcConfig.java`（仅当需要调整白名单）
 
**Endpoints (mini):**
- `POST /mini/follow/follow` body: `{ "userId": 123 }`
- `POST /mini/follow/unfollow` body: `{ "userId": 123 }`
- `GET /mini/follow/check/{userId}` → `Boolean`（当前用户是否已关注该 userId）
- `GET /mini/follow/stats/{userId}` → `{ followerCount, followingCount }`（可选：若 profile 接口已返回则不需要）
 
**Step 1: DTO 与参数校验**
- `FollowUserDTO.userId` 必填，不能等于当前用户 id。
 
**Step 2: Service 实现**
- follow：
  - 校验登录（`UserContext.getCurrentUserId()`）
  - 校验目标用户存在且未封禁（必要时）
  - 插入 `user_follow`，若唯一索引冲突则视为已关注（幂等）
- unfollow：
  - 按 followerId + followeeId 删除（幂等）
- check：
  - count > 0 返回 true
- count：
  - `select count(*) where followee_id=?`（粉丝数）
  - `select count(*) where follower_id=?`（关注数）

**Step 2.1: Redis 缓存策略（避免频繁 count）**
- 缓存项：
  - `follow:stats:{userId}` → `{ followerCount, followingCount }`，TTL 10 分钟
  - `follow:check:{followerId}:{followeeId}` → `0/1`，TTL 10 分钟
- 失效策略：
  - follow/unfollow 成功后，删除：
    - 关注关系 check key（当前 followerId + followeeId）
    - 双方 stats key（followerId 与 followeeId）
  - 对于 check：未登录直接返回 false，不缓存
 
**Step 3: 拦截器白名单确认**
- `WebMvcConfig` 当前将 `/mini/user/profile/**` 放行是合理的。
- 关注接口必须需要登录，因此不应加入放行列表。
 
**Step 4: 后端单测（JUnit 5）**
- 覆盖：
  - 关注自己返回业务异常
  - 重复关注幂等
  - 取关幂等
  - check 返回符合预期
 
---
 
### Task 5: 扩展卖家主页 profile 接口返回（个人简介、IP属地、最后活跃、关注统计）
 
**Files:**
- Modify: `g:\Code\Graduation_project\src\main\java\com\qingyuan\secondhand\vo\UserProfileVO.java`
- Modify: `g:\Code\Graduation_project\src\main\java\com\qingyuan\secondhand\service\impl\UserServiceImpl.java`
- Modify: `g:\Code\Graduation_project\src\main\java\com\qingyuan\secondhand\mapper\UserMapper.java`
- Modify/Create: `g:\Code\Graduation_project\src\main\resources\mapper\UserMapper.xml`（若统计需要 XML）
 
**Step 1: 扩展 UserProfileVO 字段**
- `bio`：个人简介
- `ipRegion`：IP属地文案（如“安徽省”）
- `lastActiveDays`：距今多少天（int）
- `lastActiveText`：展示文案（如“147天前来过”）
- `followerCount`、`followingCount`
- `isFollowed`：可选（推荐不放进公开 profile，改由 `/mini/follow/check/{id}` 提供）
 
**Step 2: 计算 lastActiveDays/lastActiveText**
- 使用 `user.last_login_time`（实体字段 `User.lastLoginTime` 已存在）与 `LocalDateTime.now()` 计算天数差。
- 若为空则展示“很久以前来过”或“-”。
 
**Step 3: 返回关注统计**
- 在 `getUserProfile()` 内调用 follow 统计查询（或 userMapper 写 count SQL）。
 
**Step 4: 商品列表字段对齐前端 ProductCard**
当前 `UserProfileVO.products` 的 `ProductSimpleVO` 字段不足以支撑 `ProductCard`（需要 `coverImage/campusName/conditionText/createTime` 等），建议二选一：
- 方案 A（推荐，按此实施）：新增 `SellerProductVO`，字段与 `ProductCard` 需要的一致，然后将 `products` 类型改为 `Page<SellerProductVO>`。
- 方案 B（备选）：扩展 `ProductSimpleVO` 增加 `coverImage/campusName/originalPrice/conditionLevel` 等字段，并调整 mapper 查询。
 
---
 
### Task 6: 登录时写入 IP属地（ip_region）
 
**Files:**
- Modify: `g:\Code\Graduation_project\src\main\java\com\qingyuan\secondhand\controller\mini\MiniUserController.java`
- Modify: `g:\Code\Graduation_project\src\main\java\com\qingyuan\secondhand\service\impl\UserServiceImpl.java`
- Modify: `g:\Code\Graduation_project\pom.xml`（如需引入离线 IP 解析库）
- Create: `g:\Code\Graduation_project\src\main\java\com\qingyuan\secondhand\common\util\IpRegionUtil.java`
- Create: `g:\Code\Graduation_project\src\main\resources\ip2region.xdb`（如采用离线库）
 
**Step 1: 取客户端 IP**
- 在 Controller 中通过 `HttpServletRequest` 获取：
  - `X-Forwarded-For`、`X-Real-IP`、`remoteAddr` 兜底。
 
**Step 2: IP -> region**
推荐离线方案，避免外部依赖：
- 引入 ip2region（Java binding）并在 resources 放入 `ip2region.xdb`。
- `IpRegionUtil.resolve(ip)` 返回省/市字符串（若解析失败返回 null）。
 
**Step 3: 登录成功后写入**
- 在 `wxLogin/accountLogin/smsLogin` 成功更新用户时，将 `ipRegion` 更新到 `user.ip_region`。
- 注意：只存 region，不存原始 IP。
 
---
 
### Task 7: 小程序卖家主页展示与关注交互
 
**Files:**
- Modify: `g:\Code\Graduation_project\miniapp\pages\seller\profile.vue`
- Modify/Create: `g:\Code\Graduation_project\miniapp\api/follow.js`（若项目已有 api 目录则放入；否则直接在页面内调用 request）
 
**Step 1: UI 增加信息**
- 在顶部卡片新增：
  - 关注按钮（未登录点击提示登录）
  - 粉丝数、关注数
  - IP属地
  - 最后活跃（“xx天前来过”）
  - 个人简介（支持展开/收起，未填写显示默认文案）
 
**Step 2: 数据加载策略**
- `GET /mini/user/profile/{id}`：拿公开信息（bio/ipRegion/lastActiveText/统计/商品列表）
- 若已登录：
  - `GET /mini/follow/check/{id}`：拿 isFollowed
 
**Step 3: 关注/取关**
- follow：`POST /mini/follow/follow` body `{ userId: sellerId }`
- unfollow：`POST /mini/follow/unfollow` body `{ userId: sellerId }`
- 成功后本地更新：
  - isFollowed 状态
  - followerCount +1/-1
 
**Step 4: 回归验证**
- 从商品详情页点击卖家卡片进入卖家主页，关注按钮可用。
- 未登录访问卖家主页依然能看到公开信息，但无法关注。
 
---
 
### Task 8: 小程序个人中心编辑“个人简介”
 
**Files:**
- Modify: `g:\Code\Graduation_project\miniapp\pages\settings\edit-profile.vue`（如存在）
- Modify: `g:\Code\Graduation_project\src\main\java\com\qingyuan\secondhand\dto\UserUpdateDTO.java`
- Modify: `g:\Code\Graduation_project\src\main\java\com\qingyuan\secondhand\service\impl\UserServiceImpl.java`
- Modify: `g:\Code\Graduation_project\src\main\java\com\qingyuan\secondhand\vo\UserInfoVO.java`（如需要在 /mini/user/info 返回 bio）
 
**Step 1: DTO 增加 bio 字段并校验**
- `bio` 长度 0-200，可选。
 
**Step 2: updateUserInfo 支持更新 bio**
- 更新后清理用户缓存（若存在相关 Redis 缓存 key）。
 
**Step 3: 前端增加输入框**
- 提示文案与字数统计，提交走现有 `/mini/user/update`。
 
---
 
## 验证命令
 
- 后端编译：`mvn -DskipTests compile`
- 后端测试：`mvn test`
 
## 交付检查清单
 
- 商品详情页卖家卡片：展示正确卖家信息 + 点击能进入卖家主页。
- 卖家主页：展示 bio、IP属地、最后活跃、粉丝/关注统计。
- 关注/取关：接口幂等、需要登录、前端状态与计数同步。
- SQL：`init.sql` 与增量脚本都包含新字段/新表。
