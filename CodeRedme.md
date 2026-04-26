# 轻院二手交易平台 - 项目文档

**版本：V1.7 | 最后更新：2026-04-26 | 项目状态：开发中**

---

## 目录

- [📋 项目概述](#-项目概述)
- [🚀 项目进度状态](#-项目进度状态)
- [🏗️ 技术架构](#️-技术架构)
- [📦 核心功能模块](#-核心功能模块)
- [🗄️ 数据库设计](#️-数据库设计)
- [🔌 核心接口说明](#-核心接口说明)
- [⚙️ 环境配置与部署](#️-环境配置与部署)
- [🔐 安全机制](#-安全机制)
- [⏰ 定时任务](#-定时任务)
- [📊 缓存策略](#-缓存策略)
- [💬 即时通讯（IM）实现](#-即时通讯im实现)
- [🧪 测试规范](#-测试规范)
- [📝 开发规范](#-开发规范)
- [🚀 性能优化](#-性能优化)
- [🔧 常见问题（FAQ）](#-常见问题faq)
- [🔄 更新日志](#-更新日志)

---

## 📋 项目概述

### 基本信息

| 属性 | 说明 |
|------|------|
| 项目名称 | 轻院二手（Qingyuan Secondhand Trading Platform） |
| 项目类型 | 校园二手交易平台（毕业设计项目） |
| 目标用户 | 广东轻工职业技术大学在校学生 |
| 开发模式 | 前后端分离 + 双 IDE 协同开发 |

### 核心定位

轻院二手是一款专为广东轻工职业技术大学校内学生打造的微信小程序，旨在解决校园内闲置二手商品出售难的问题。平台参考闲鱼模式，结合校园特色，提供商品发布、浏览搜索、即时通讯、面交撮合等核心功能，让闲置物品在校园内高效流转。

### 项目痛点

| 痛点 | 描述 |
|------|------|
| 信息分散 | 学生出售闲置物品依赖 QQ 群、微信群，信息零散、易刷屏、难检索 |
| 信任缺失 | 群内交易无信用体系，商品质量和卖家信誉无法保障 |
| 效率低下 | 买卖双方缺乏高效的沟通和交易管理工具 |
| 毕业浪费 | 每年毕业季大量物品被丢弃，缺乏集中的处理渠道 |

---

## 🚀 项目进度状态

> 更新于 2026-04-26，反映当前实际开发完成情况。

### 整体进度

| 端 | 完成度 | 说明 |
|------|--------|------|
| 后端服务 | ✅ 95% | 核心业务模块、API 接口、定时任务、WebSocket IM 均已实现 |
| 管理后台 | ✅ 95% | 核心审核与管理页面已完成，配置类模块全部完成 |
| 小程序端 | ✅ 90% | 核心业务流程全部完成，部分辅助页面基础实现 |

### 管理后台页面状态

| 模块 | 状态 | 说明 |
|------|:----:|------|
| 登录页 | ✅ 已完成 | `login/LoginView.vue` |
| 数据概览 | ✅ 已完成 | `dashboard/DashboardView.vue`（ECharts 图表、统计卡片） |
| 认证审核 | ✅ 已完成 | `AuthReview.vue`（含历史时间线、多版本对比） |
| 商品审核 | ✅ 已完成 | `product/ProductList.vue` + `ProductReview.vue` |
| 用户管理 | ✅ 已完成 | `user/UserList.vue` + `UserManage.vue` |
| 订单管理 | ✅ 已完成 | `order/OrderList.vue` + `OrderManage.vue` |
| 举报处理 | ✅ 已完成 | `report/ReportList.vue` + `ReportManage.vue` |
| 分类管理 | ✅ 已完成 | `category/CategoryList.vue`，含完整增删改、状态切换、排序功能 |
| 校区管理 | ✅ 已完成 | `campus/CampusList.vue`，含左右分栏布局、校区+面交地点双层管理 |
| 学院管理 | ✅ 已完成 | `college/CollegeList.vue`，含完整增删改、状态切换、搜索功能 |
| Banner 管理 | ✅ 已完成 | `content/BannerAdmin.vue`，含图片上传、时间范围、链接类型联动 |
| 公告管理 | ✅ 已完成 | `content/NoticeAdmin.vue`，含完整增删改、富文本内容编辑 |
| 员工管理 | ✅ 已完成 | `employee/EmployeeList.vue`，含角色权限、密码重置、手机号脱敏 |

### 小程序端页面状态

| 模块 | 状态 | 说明 |
|------|:----:|------|
| 首页 | ✅ 已完成 | `index/index.vue` |
| 登录相关 | ✅ 已完成 | `login-sub/` 下所有页面（微信登录、手机号登录、短信登录） |
| 校园认证 | ✅ 已完成 | `auth-sub/` 下所有页面（提交、历史列表、详情、内容对比） |
| 商品相关 | ✅ 已完成 | `product/` 下所有页面（详情、发布、我的列表、编辑） |
| 聊天功能 | ✅ 已完成 | `chat/` 下所有页面（会话列表、聊天详情、设置） |
| 订单管理 | ✅ 已完成 | `order/` 下所有页面（列表、详情） |
| 评价功能 | ✅ 已完成 | `review/` 页面 |
| 收藏功能 | ✅ 已完成 | `favorite/` 页面 |
| 消息通知 | ✅ 已完成 | `notification-sub/` 下所有页面（通知、收到回复、收到收藏、新粉丝） |
| 个人中心 | ✅ 已完成 | `user/` 和 `user-sub/` 下页面 |
| 卖家主页 | ✅ 已完成 | `seller/profile.vue`（含关注按钮） |
| 举报功能 | ✅ 已完成 | `report/report.vue` |
| 搜索功能 | ✅ 已完成 | `search/` 页面（关键词搜索、热门搜索、历史记录） |
| 设置与资料 | ✅ 已完成 | `settings/` 下页面（设置、编辑资料、关于） |
| 用户协议 | ⚠️ 基础完成 | `agreement/`，内容已填充，交互待优化 |
| 隐私政策 | ⚠️ 基础完成 | `privacy/`，内容已填充，交互待优化 |
| 帮助页面 | ⚠️ 基础完成 | `help/help.vue`，基础内容已完成 |
| 足迹功能 | ⚠️ 基础完成 | `user-sub/footprint/footprint.vue`，基础功能已完成 |

---

## 🏗️ 技术架构

### 技术栈

#### 后端技术栈

| 类别 | 技术选型 |
|------|---------|
| 语言 | Java 17+ |
| 框架 | Spring Boot 3.3.7 |
| ORM | MyBatis-Plus 3.5.7（非原生 MyBatis） |
| 数据库 | MySQL 5.7 |
| 缓存 | Redis 6.0+ |
| 构建工具 | Maven |
| 测试框架 | JUnit 5 + Mockito + MockMvc |
| 密码加密 | BCryptPasswordEncoder |
| 认证方式 | JWT（Interceptor 拦截验证） |
| 即时通讯 | Spring Boot WebSocket（自建） |

#### 前端技术栈

| 类别 | 技术选型 |
|------|---------|
| 小程序端 | uni-app + Vue 3 |
| 管理端 | Vue 3 + Vite + Element Plus + ECharts |

### 项目结构

```text
Graduation_project/
├── admin/                              # 管理后台前端（Vue 3 + Element Plus）
│   ├── src/
│   │   ├── api/                       # API 接口封装
│   │   ├── components/                # 公共组件
│   │   ├── layout/                    # 布局组件（Layout.vue）
│   │   ├── router/                    # 路由配置
│   │   ├── store/                     # 状态管理
│   │   ├── styles/                    # 样式文件
│   │   ├── utils/                     # 工具函数
│   │   └── views/                     # 页面组件
│   │       ├── login/                 # ✅ 登录页
│   │       ├── dashboard/             # ✅ 数据概览（ECharts 图表）
│   │       ├── AuthReview.vue         # ✅ 认证审核（含历史时间线）
│   │       ├── product/               # ✅ 商品审核
│   │       ├── user/                  # ✅ 用户管理
│   │       ├── order/                 # ✅ 订单管理
│   │       ├── report/                # ✅ 举报处理
│   │       ├── category/              # ✅ 分类管理（含增删改）
│   │       ├── campus/                # ✅ 校区管理（含增删改）
│   │       ├── college/               # ✅ 学院管理（含增删改）
│   │       ├── banner/                # ✅ Banner 管理（含增删改）
│   │       ├── notice/                # ✅ 公告管理（含增删改）
│   │       └── employee/              # ✅ 员工管理（含增删改）
│   └── package.json
│
├── miniapp/                            # 小程序前端（uni-app + Vue 3）
│   ├── components/                    # 公共组件
│   │   ├── product-card/              # 商品卡片
│   │   ├── order-card/                # 订单卡片
│   │   ├── user-avatar/               # 用户头像
│   │   ├── empty-state/               # 空状态
│   │   ├── status-tag/                # 状态标签
│   │   └── price/                     # 价格展示
│   ├── pages/                         # 页面
│   │   ├── index/                     # ✅ 首页
│   │   ├── login/                     # ✅ 登录（login + sms-login）
│   │   ├── auth/                      # ✅ 校园认证（auth + history/list,detail,compare）
│   │   ├── search/                    # ✅ 搜索
│   │   ├── product/                   # ✅ 商品（detail + publish + my-list + edit）
│   │   ├── seller/                    # ✅ 卖家主页（profile，含关注按钮）
│   │   ├── chat/                      # ✅ 聊天（list + detail + settings）
│   │   ├── order/                     # ✅ 订单（list + detail）
│   │   ├── review/                    # ✅ 评价
│   │   ├── favorite/                  # ✅ 收藏
│   │   ├── footprint/                 # ⚠️ 足迹（开发中，页面框架存在）
│   │   ├── notification/              # ✅ 消息（notification + received-replies
│   │   │                              #           + received-favorites + follower）
│   │   ├── user/                      # ✅ 个人中心
│   │   ├── settings/                  # ✅ 设置（settings + edit-profile + about）
│   │   ├── report/                    # ✅ 举报
│   │   ├── agreement/                 # ⚠️ 用户协议（基础完成）
│   │   ├── privacy/                   # ⚠️ 隐私政策（基础完成）
│   │   └── help/                      # ⚠️ 帮助（基础完成）
│   ├── styles/                        # 样式（theme.css）
│   ├── utils/                         # 工具（request.js + auth.js + constant.js + mock.js）
│   ├── store/                         # 状态管理（index.js + user.js + app.js）
│   ├── static/                        # 静态资源
│   ├── App.vue
│   ├── main.js
│   ├── manifest.json
│   └── pages.json
│
├── src/                                # 后端源码
│   ├── main/
│   │   ├── java/com/qingyuan/secondhand/
│   │   │   ├── common/               # 公共模块
│   │   │   │   ├── constant/         # 常量定义
│   │   │   │   ├── context/          # 上下文（UserContext）
│   │   │   │   ├── enums/            # 枚举类
│   │   │   │   ├── exception/        # 异常处理
│   │   │   │   ├── interceptor/      # 拦截器（JwtInterceptor + AdminJwtInterceptor）
│   │   │   │   ├── result/           # 统一响应封装
│   │   │   │   └── util/             # 工具类
│   │   │   ├── config/               # 配置类（WebMvcConfig、RedisConfig 等）
│   │   │   ├── controller/           # 控制器（共 22 个）
│   │   │   │   ├── admin/            # 管理端接口
│   │   │   │   ├── common/           # 公共接口（文件上传等）
│   │   │   │   └── mini/             # 小程序端接口
│   │   │   ├── dto/                  # 请求参数对象
│   │   │   ├── entity/               # 实体类
│   │   │   ├── mapper/               # Mapper 接口
│   │   │   ├── service/              # Service 接口
│   │   │   │   └── impl/             # Service 实现
│   │   │   ├── task/                 # 定时任务（7 个，全部已实现）
│   │   │   ├── vo/                   # 返回视图对象
│   │   │   ├── websocket/            # WebSocket 相关
│   │   │   │   ├── WebSocketServer.java          # 核心 WebSocket 服务
│   │   │   │   ├── WebSocketSessionManager.java  # 会话管理
│   │   │   │   ├── ChatHandshakeInterceptor.java # 握手鉴权
│   │   │   │   └── handler/          # 消息处理器
│   │   │   │       ├── ChatMessageHandler.java   # 聊天消息
│   │   │   │       ├── ReadMessageHandler.java   # 已读回执
│   │   │   │       ├── PingMessageHandler.java   # 心跳保活
│   │   │   │       └── SystemMessageHandler.java # 系统消息
│   │   │   └── SecondhandApplication.java
│   │   └── resources/
│   │       ├── mapper/               # MyBatis XML（复杂查询）
│   │       └── application.yml       # 配置文件
│   └── test/                         # 测试代码
│
├── sql/                                # 数据库脚本
│   ├── init.sql                       # 初始建表脚本
│   └── update/                        # 增量更新脚本
│
└── docs/                              # 项目文档
    ├── README.md                      # 本文档
    ├── 需求+架构文档.md
    ├── CodeReadme.md
    └── plans/                         # 开发计划
```

### 核心架构规则

| 规则 | 说明 |
|------|------|
| MyBatis-Plus 规范 | Mapper 继承 `BaseMapper`，Service 继承 `IService` / `ServiceImpl` |
| 简单 CRUD 不写 SQL | 使用 MyBatis-Plus 内置方法和 `LambdaQueryWrapper` |
| 复杂查询用 XML | 多表 JOIN 等复杂 SQL 才使用 Mapper XML |
| Controller 不含业务逻辑 | 只做参数接收、校验、调用 Service |
| Entity 不直接返回前端 | 必须转换为 VO |
| Entity 使用 MP 注解 | `@TableName`、`@TableId`、`@TableField`、`@TableLogic` |
| 自动填充 | `createTime` / `updateTime` 通过 `MetaObjectHandler` 自动填充 |
| 分页使用 MP 插件 | `Page<T>` + `PaginationInnerInterceptor` |
| 条件构造用 Lambda | `LambdaQueryWrapper` 而非字符串拼接 |
| 统一响应封装 | 所有接口返回 `Result<T>` |
| 异常全局处理 | 不在 Controller 中 try-catch 业务异常 |

---

## 📦 核心功能模块

### 功能总览

| 模块 | 子功能 | 优先级 | 状态 |
|------|--------|--------|------|
| 登录注册 | 微信登录、手机号+密码登录、短信验证登录、自动注册、用户协议 | P0 | ✅ 已完成 |
| 校园认证 | 填写学院/学号/班级、上传认证材料、人工审核、认证历史、认证内容对比、修改资料重新提交 | P0 | ✅ 已完成 |
| 首页 | 校区切换、Banner 运营位、分类入口、最新发布商品流 | P0 | ✅ 已完成 |
| 搜索筛选 | 关键词搜索、热门搜索、搜索历史、多维度筛选 | P0 | ✅ 已完成 |
| 商品详情 | 图片轮播/放大、价格/新旧度/描述、卖家信息、收藏、举报、我想要、分享、商品评论/留言 | P0 | ✅ 已完成 |
| 商品发布 | 图片上传、商品信息填写、面交地点选择、编辑/下架/重新上架 | P0 | ✅ 已完成 |
| 卖家主页 | 卖家信息展示、在售商品列表、关注/取关卖家 | P0 | ✅ 已完成 |
| IM 即时通讯 | 文字消息、商品卡片、快捷回复、聊天列表、确认购买入口 | P0 | ✅ 已完成 |
| 订单管理 | 创建订单、订单状态流转、确认收货、取消交易 | P0 | ✅ 已完成 |
| 评价系统 | 交易后互评、三维度评分、综合评分计算 | P1 | ✅ 已完成 |
| 收藏 | 收藏/取消收藏、我的收藏列表 | P0 | ✅ 已完成 |
| 商品评论（留言） | 商品详情页留言、回复留言、删除留言、收到回复通知、未读回复数 | P1 | ✅ 已完成 |
| 用户关注 | 关注/取关卖家、粉丝列表、关注/粉丝统计 | P1 | ✅ 已完成 |
| 个人信息扩展 | 个人简介、IP 属地展示 | P2 | ✅ 已完成 |
| 举报 | 商品举报、举报分类、后台处理 | P1 | ✅ 已完成 |
| 个人中心 | 用户信息、统计数据、功能入口、设置、账号注销/恢复 | P0 | ✅ 已完成 |
| 消息中心 | 交易通知、系统通知、审核通知、收藏提醒、新增粉丝、收到回复、全部已读 | P0 | ✅ 已完成 |
| 后台管理 | 商品审核、认证审核（含历史时间线）、举报处理、数据统计 | P0 | ✅ 已完成 |
| 后台配置 | Banner/分类/校区/学院/公告/员工管理 | P1 | ✅ 已完成 |
| 足迹 | 浏览历史记录、我的足迹列表 | P2 | ⚠️ 基础完成 |

### 核心业务流程

#### 1. 用户注册 / 登录流程

```text
用户打开小程序
    │
    ├─→ [微信一键登录]
    │       └─→ 获取 openId → 自动创建账号 → 进入首页
    │
    ├─→ [手机号 + 密码登录]
    │       └─→ 校验账号密码 → 进入首页
    │
    └─→ [短信验证登录]
            └─→ 输入手机号 → 获取验证码 → 校验通过
                    │
                    └─→ 未注册手机号 → 自动创建账号 → 进入首页

※ 首次登录需勾选同意《用户协议》和《隐私政策》
※ 登录成功后返回 JWT Token，后续请求携带 Token
※ 登录失败 5 次后锁定 15 分钟（Redis 计数）
```

#### 2. 校园认证流程

```text
用户进入认证页面
    │
    ├─→ 填写：学院（下拉选择）、姓名、学号、班级
    ├─→ 上传：一卡通照片 或 教务系统截图
    │
    └─→ [提交认证] → 后台人工审核
                        │
                        ├─→ [通过] → 用户标记"已认证" → 发送站内通知
                        └─→ [驳回] → 发送站内通知（含驳回原因）→ 用户可重新提交
```

##### 2.1 认证历史

```text
认证页展示「认证历史」按钮
    │
    ├─→ 未认证（auth_status = 0）→ 按钮不可点击
    │
    └─→ 审核中 / 已认证 / 已驳回 → 进入认证历史列表页
            │
            ├─→ 点击任意历史记录进入详情页
            │       ├─→ 展示当次提交的姓名 / 学院 / 学号 / 班级 / 认证材料
            │       ├─→ 若驳回，底部展示管理员驳回原因
            │       └─→ 提供「修改资料」按钮
            │
            └─→ 点击「修改资料」→ 回填到认证页并重新提交
                    └─→ 重新提交后状态统一变为审核中（auth_status = 1）
```

**认证历史相关接口**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/mini/auth/history` | 查询当前用户认证历史列表（按提交时间倒序） |
| GET | `/mini/auth/history/{id}` | 查询认证历史详情（按用户隔离，防越权） |
| GET | `/admin/auth/history/{authId}` | 管理端按认证 ID 查询历史时间线 |

**状态说明**

| 状态来源 | 字段 | 枚举值 |
|---------|------|--------|
| 认证主表 | `campus_auth.status` | 0 待审核 / 1 通过 / 2 驳回 |
| 小程序映射 | `/mini/auth/status` 响应 | 1 审核中 / 2 已认证 / 3 已驳回 |
| 历史记录 | `campus_auth_history.status` | 0 待审核 / 1 通过 / 2 驳回（同样映射） |
| 个人中心 | `user.auth_status` | 0 未认证 / 1 审核中 / 2 已认证 / 3 已驳回 |

> **状态一致性保障**：后端在提交/通过/驳回时会主动清理 `user:info:{userId}` 与 `user:stats:{userId}` 缓存；小程序个人中心页在 `onShow` 时额外调用 `/mini/auth/status` 刷新认证状态。

**认证历史展示规则**
- 列表按最新记录置顶展示（按 `campus_auth_history.id` 倒序）
- 除第一条（最新记录）外，其余历史记录在状态前增加「已失效」标签

##### 2.2 认证资料修改

```text
审核中 / 已通过状态下，认证状态卡片可点击
    │
    ├─→ 点击状态卡片 → 进入「认证内容对比」页
    │       ├─→ 展示最新待审核内容详情
    │       └─→ 展示最近一次已通过内容详情
    │
    └─→ 点击「修改资料」→ 进入编辑态，允许重新提交
            └─→ 重新提交后状态重置为审核中（进入新一轮审核）
```

**管理后台认证审核详情页增强**
- 新增历史时间线面板，展示每次提交记录、审核结果和时间
- 点击时间线节点可切换右侧详情，查看对应版本的完整认证信息
- 审核人员可在同一页面完成「全轨迹回溯 + 当前审核操作」

#### 3. 商品发布流程

```text
用户点击底部 Tab「发布」
    │
    ├─→ 上传图片（最多 9 张，首张为封面，单张最大 5MB）
    ├─→ 填写：商品名称、二手价格、原价、分类、磨损程度、描述
    ├─→ 选择：交易校区、面交地点（预设 + 自定义）
    │
    └─→ [发布] → 进入审核队列
                    │
                    ├─→ [审核通过] → 商品上架，首页可见 → 发送站内通知
                    └─→ [审核驳回] → 发送站内通知（含原因）→ 用户可编辑后重新提交
```

#### 4. 交易完整流程

```text
买家点击「我想要」
    │
    └─→ 跳转至与卖家的 IM 聊天界面（自动发送商品卡片）
            │
            └─→ 双方协商价格、面交时间地点
                    │
                    ├─→ [可选] 卖家修改价格 → 买家确认新价格
                    │
                    └─→ [买家点击"确认购买"] → 生成订单（Redis 分布式锁防并发）
                            │
                            │   订单状态：待面交（72 小时计时）
                            │   同一商品同时只允许一个待面交订单
                            │
                            ├─→ [72 小时内完成面交]
                            │       └─→ [买家点击"确认收货"]
                            │               └─→ 订单状态：已完成
                            │                       └─→ [双方互评]（7 天窗口期）
                            │                               ├─→ 双方均评价 → 订单状态：已评价
                            │                               └─→ 7 天未评价 → 系统自动默认好评
                            │
                            ├─→ [72 小时超时]
                            │       └─→ 订单自动取消 → 通知双方 → 商品恢复在售
                            │
                            ├─→ [买家取消交易]
                            │       └─→ 选择取消原因 → 订单取消 → 通知卖家
                            │
                            └─→ [卖家取消交易]
                                    └─→ 选择取消原因 → 订单取消 → 通知买家
```

---

## 🗄️ 数据库设计

### 核心数据表

#### 1. 用户表（user）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键自增，起始值 10000 |
| open_id | varchar | 微信唯一标识 |
| phone | varchar | 手机号 |
| nick_name | varchar | 昵称 |
| avatar_url | varchar | 头像 |
| status | tinyint | 账号状态：0 封禁 / 1 正常 / 2 注销中 |
| auth_status | tinyint | 认证状态：0 未认证 / 1 审核中 / 2 已认证 / 3 已驳回 |
| score | decimal | 综合评分，默认 5.0 |
| bio | varchar(200) | 个人简介 |
| ip_region | varchar(64) | IP 属地 |
| create_time | datetime | 创建时间（自动填充） |
| update_time | datetime | 更新时间（自动填充） |

#### 2. 商品表（product）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键自增 |
| user_id | bigint | 发布者 ID |
| title | varchar | 商品标题 |
| price | decimal | 二手价格 |
| original_price | decimal | 原价 |
| category_id | int | 分类 ID |
| campus_id | int | 交易校区 ID |
| condition_level | tinyint | 成色：1 全新 / 2 几乎全新 / 3 九成新 / 4 八成新 / 5 七成新及以下 |
| status | tinyint | 状态：0 待审核 / 1 在售 / 2 已下架 / 3 已售出 / 4 审核驳回 |
| auto_off_time | datetime | 自动下架时间（发布后 90 天） |
| is_deleted | tinyint | 逻辑删除（@TableLogic） |
| create_time | datetime | 发布时间（自动填充） |
| update_time | datetime | 更新时间（自动填充） |

#### 3. 订单表（trade_order）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键自增 |
| order_no | varchar | 订单号，格式：TD + yyyyMMddHHmmss + 4 位随机数 |
| product_id | bigint | 商品 ID |
| buyer_id | bigint | 买家 ID |
| seller_id | bigint | 卖家 ID |
| price | decimal | 成交价格 |
| status | tinyint | 状态：1 待面交 / 2 预留 / 3 已完成 / 4 已评价 / 5 已取消 |
| cancel_by | tinyint | 取消方：0 系统 / 1 买家 / 2 卖家 |
| expire_time | datetime | 超时时间（创建后 72 小时，超时自动取消） |
| confirm_deadline | datetime | 自动确认收货时间（创建后 7 天） |
| complete_time | datetime | 完成时间 |
| is_deleted_buyer | tinyint | 买家软删除标记 |
| is_deleted_seller | tinyint | 卖家软删除标记 |

> **`expire_time` vs `confirm_deadline` 区别**：`expire_time` 为 72 小时超时自动取消，由 `OrderExpireTask` 每 5 分钟检查；`confirm_deadline` 为 7 天自动确认收货，由 `OrderAutoConfirmTask` 每天凌晨 2:00 执行。

#### 4. 评价表（review）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键自增 |
| order_id | bigint | 关联订单 ID |
| reviewer_id | bigint | 评价者 ID |
| reviewee_id | bigint | 被评价者 ID |
| score_desc | tinyint | 描述相符评分（1-5 分） |
| score_attitude | tinyint | 沟通态度评分（1-5 分） |
| score_experience | tinyint | 交易体验评分（1-5 分） |
| content | varchar | 评价内容 |
| is_auto | tinyint | 是否自动好评：0 否 / 1 是 |

> **综合评分计算**：单次评价得分 = (三维度之和 / 3)，用户综合评分 = 所有收到评价的单次得分平均值，保留一位小数，新用户默认 5.0 分。

#### 5. 聊天会话表（chat_session）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键自增 |
| session_key | varchar | 会话唯一标识，格式见下方说明 |
| user_id | bigint | 当前用户 ID |
| peer_id | bigint | 对方用户 ID |
| product_id | bigint | 关联商品 ID |
| unread | int | 当前用户未读消息数 |
| last_msg | varchar | 最后一条消息摘要 |
| last_time | datetime | 最后消息时间 |
| is_top | tinyint | 是否置顶：0 否 / 1 是 |
| is_deleted | tinyint | 是否删除（仅对自己生效） |

> **SessionKey 生成规则**：`min(userA, userB)_max(userA, userB)_productId`，每对用户+商品产生**两条**记录，各自独立管理未读数、置顶、删除状态。

#### 6. 聊天消息表（chat_message）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键自增 |
| session_key | varchar | 所属会话 Key |
| sender_id | bigint | 发送者 ID |
| receiver_id | bigint | 接收者 ID |
| msg_type | tinyint | 消息类型：1 文本 / 2 商品卡片 / 3 订单卡片 / 4 系统提示 / 5 快捷回复 |
| content | text | 消息内容 |
| is_read | tinyint | 是否已读：0 未读 / 1 已读 |
| create_time | datetime | 发送时间 |

#### 7. 校园认证表（campus_auth）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键自增 |
| user_id | bigint | 关联用户 ID |
| college_id | int | 学院 ID |
| real_name | varchar(32) | 真实姓名 |
| student_no | varchar | 学号 |
| class_name | varchar | 班级 |
| cert_image | varchar | 认证材料图片 URL |
| status | tinyint | 状态：0 待审核 / 1 通过 / 2 驳回 |
| reject_reason | varchar | 驳回原因 |
| review_time | datetime | 审核时间 |
| reviewer_id | bigint | 审核人 ID |

#### 8. 校园认证历史表（campus_auth_history）

> **作用**：保存每次认证提交的完整快照，不覆盖历史数据，支持全轨迹回溯。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键自增 |
| auth_id | bigint | 关联 campus_auth.id |
| user_id | bigint | 用户 ID |
| college_id | int | 学院 ID |
| real_name | varchar(32) | 真实姓名 |
| student_no | varchar | 学号 |
| class_name | varchar | 班级 |
| cert_image | varchar | 认证材料图片 URL |
| status | tinyint | 状态：0 待审核 / 1 通过 / 2 驳回 |
| reject_reason | varchar | 驳回原因 |
| review_time | datetime | 审核时间 |
| reviewer_id | bigint | 审核人 ID |
| create_time | datetime | 提交时间（自动填充） |

#### 9. 用户关注表（user_follow）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键自增 |
| follower_id | bigint | 关注者 ID |
| followee_id | bigint | 被关注者 ID |
| create_time | datetime | 关注时间（自动填充） |

> **约束**：唯一索引 `(follower_id, followee_id)` 防止重复关注；不能关注自己（Service 层校验）。

#### 10. 商品留言表（product_comment）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键自增 |
| product_id | bigint | 关联商品 ID |
| user_id | bigint | 留言者 ID |
| parent_id | bigint | 父评论 ID，为空表示第一层留言 |
| root_id | bigint | 根评论 ID，用于组装树形结构 |
| reply_to_user_id | bigint | 被回复用户 ID，用于通知和展示 |
| content | varchar | 留言内容 |
| is_read | tinyint | 被回复人是否已读：0 未读 / 1 已读 |
| is_deleted | tinyint | 逻辑删除（@TableLogic） |
| create_time | datetime | 留言时间（自动填充） |

### 数据库变更说明

| 增量脚本 | 变更内容 |
|---------|---------|
| `2026-02-21_f19_notification.sql` | 新增 `notification` 通知表 |
| `2026-02-21_f21_banner_search.sql` | 新增 `banner` 表、搜索热词相关结构 |
| `2026-02-22_f_im_02_chat_session.sql` | 新增 `chat_session` 表 |
| `2026-02-23_f_im_03_chat_message.sql` | 新增 `chat_message` 表 |
| `2026-04-06_f06_auth_real_name.sql` | `campus_auth` 表新增 `real_name` 字段；新增 `campus_auth_history` 表 |
| `2026-04-08_f07_user_follow_profile.sql` | 新增 `user_follow` 表；`user` 表新增 `bio`、`ip_region` 字段 |
| `2026-04-10_f08_product_comment.sql` | 新增 `product_comment` 表 |

### 数据库关系图

```text
user(用户) ──1:N──→ product(商品)
user(用户) ──1:N──→ trade_order(订单) ←──N:1── product(商品)
user(用户) ──1:N──→ favorite(收藏)    ←──N:1── product(商品)
user(用户) ──1:1──→ campus_auth(校园认证) ──N:1──→ college(学院)
campus_auth(校园认证) ──1:N──→ campus_auth_history(认证历史)
trade_order(订单) ──1:2──→ review(评价)
campus(校区) ──1:N──→ meeting_point(面交地点)
user(用户) ──1:N──→ chat_session(会话) ──1:N──→ chat_message(消息)
user(用户) ──1:N──→ user_follow(关注)  ──N:1──→ user(被关注用户)
product(商品) ──1:N──→ product_comment(留言) ──N:1──→ user(留言者)
```

### 枚举类说明

#### 1. NotificationType（通知类型）

| code | 描述 | 模板 |
|------|------|------|
| 1 | 交易成功 | 你购买的「{productName}」交易已完成，给卖家一个评价吧！ |
| 2 | 新消息 | {nickName} 回复了你的消息："{content}" |
| 3 | 商品审核通过 | 您的商品《{productName}》已通过审核，现已上架！ |
| 4 | 商品审核驳回 | 您的商品《{productName}》未通过审核，驳回原因：{reason} |
| 5 | 系统公告 | {content} |
| 6 | 商品被收藏 | 你的商品《{productName}》被 {count} 位用户收藏了 |
| 7 | 订单已取消 | 你与 {nickName} 的交易「{productName}」已取消 |
| 8 | 校园认证通过 | 恭喜您，您的校园认证已通过审核！ |
| 9 | 校园认证被驳回 | 您的校园认证未通过审核，驳回原因：{reason} |
| 10 | 评价提醒 | 你购买的「{productName}」交易已完成 3 天，还未评价哦 |
| 11 | 新增粉丝 | {nickName} 关注了你，快去看看吧！ |

#### 2. NotificationCategory（通知分类）

| code | 描述 |
|------|------|
| 1 | 交易 |
| 2 | 系统 |

#### 3. 其他枚举

| 枚举名 | code | 描述 |
|--------|------|------|
| BannerLinkType | 1 | 商品详情 |
| BannerLinkType | 2 | 活动页 |
| BannerLinkType | 3 | 外部链接 |
| ReportStatus | 0 | 待处理 |
| ReportStatus | 1 | 已处理 |
| ReportStatus | 2 | 已驳回 |
| ReportReason | 1 | 虚假商品 |
| ReportReason | 2 | 价格欺诈 |
| ReportReason | 3 | 违禁物品 |
| ReportReason | 4 | 恶意骚扰 |
| ReportReason | 5 | 侵权商品 |
| ReportReason | 6 | 其他 |

---

## 🔌 核心接口说明

### 统一响应格式

```json
// 成功响应
{
  "code": 1,
  "msg": "success",
  "data": { }
}

// 失败响应
{
  "code": 0,
  "msg": "错误信息描述",
  "data": null
}
```

### 小程序端接口

#### 用户模块（`/mini/user`）

| 方法 | 路径 | 说明 | 是否需要登录 |
|------|------|------|:----------:|
| POST | `/mini/user/wx-login` | 微信登录 | ❌ |
| POST | `/mini/user/login` | 手机号密码登录 | ❌ |
| POST | `/mini/user/sms/send` | 发送短信验证码 | ❌ |
| POST | `/mini/user/sms-login` | 短信验证登录 | ❌ |
| GET | `/mini/user/info` | 获取当前用户信息 | ✅ |
| POST | `/mini/user/update` | 更新用户信息（含 bio、ip_region） | ✅ |
| POST | `/mini/user/accept-agreement` | 确认同意协议 | ✅ |
| GET | `/mini/user/stats` | 获取用户统计数据 | ✅ |
| GET | `/mini/user/profile/{id}` | 查看卖家主页 | ❌ |
| POST | `/mini/user/deactivate` | 申请注销账号 | ✅ |
| POST | `/mini/user/restore` | 恢复注销中账号 | ✅ |

#### 商品模块（`/mini/product`）

| 方法 | 路径 | 说明 | 是否需要登录 |
|------|------|------|:----------:|
| POST | `/mini/product/publish` | 发布商品 | ✅ |
| POST | `/mini/product/update` | 编辑商品 | ✅ |
| GET | `/mini/product/detail/{id}` | 商品详情 | ❌ |
| GET | `/mini/product/list` | 商品列表（支持筛选排序） | ❌ |
| GET | `/mini/product/my-list` | 我发布的商品 | ✅ |
| POST | `/mini/product/update-price` | 修改价格（议价场景） | ✅ |
| POST | `/mini/product/off-shelf` | 下架商品 | ✅ |
| POST | `/mini/product/on-shelf` | 上架商品 | ✅ |
| POST | `/mini/product/delete` | 删除商品 | ✅ |

#### 订单模块（`/mini/order`）

| 方法 | 路径 | 说明 | 是否需要登录 |
|------|------|------|:----------:|
| POST | `/mini/order/create` | 创建订单（含 Redis 分布式锁防并发） | ✅ |
| GET | `/mini/order/list` | 订单列表（支持买家/卖家视角） | ✅ |
| GET | `/mini/order/detail/{id}` | 订单详情 | ✅ |
| POST | `/mini/order/confirm` | 确认收货 | ✅ |
| POST | `/mini/order/cancel` | 取消订单 | ✅ |
| POST | `/mini/order/delete` | 删除订单记录 | ✅ |

#### 聊天模块（`/mini/chat`）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/mini/chat/session/create` | 创建会话 |
| GET | `/mini/chat/sessions` | 会话列表 |
| GET | `/mini/chat/list` | 会话列表（分页格式：total/records） |
| POST | `/mini/chat/session/delete` | 删除会话 |
| POST | `/mini/chat/delete` | 删除会话（兼容小程序） |
| POST | `/mini/chat/session/top` | 会话置顶 / 取消置顶 |
| GET | `/mini/chat/messages` | 消息历史（分页） |
| POST | `/mini/chat/read` | 标记已读 |
| GET | `/mini/chat/unread-total` | 未读消息总数 |
| POST | `/mini/chat/message/send` | HTTP 发送消息（返回 msgId；主要用于调试/补偿） |

**WebSocket 端点**

```
ws://host:port/ws/chat?token={jwt_token}
```

> WebSocket 连接在握手阶段由 `ChatHandshakeInterceptor` 验证 JWT，通过后由 `WebSocketSessionManager` 管理在线会话。

**WebSocket 消息协议**

```json
// 客户端 → 服务端（发送消息）
{
  "type": "CHAT",
  "data": {
    "receiverId": 10002,
    "productId": 10086,
    "msgType": 1,
    "content": "你好，这个还在吗？"
  }
}

// 服务端 → 客户端（推送消息）
{
  "type": "CHAT",
  "data": {
    "msgId": 12345,
    "sessionKey": "10001_10002_10086",
    "senderId": 10001,
    "senderName": "张三",
    "senderAvatar": "https://...",
    "receiverId": 10002,
    "msgType": 1,
    "content": "你好，这个还在吗？",
    "createTime": "2026-04-26 10:30:00"
  }
}
```

**WebSocket 消息类型**

| 消息类型 | 处理器 | 说明 |
|---------|--------|------|
| PING / PONG | `PingMessageHandler` | 心跳保活 |
| CHAT | `ChatMessageHandler` | 聊天消息（发送/接收） |
| READ / READ_ACK | `ReadMessageHandler` | 已读回执 |
| SYSTEM | `SystemMessageHandler` | 系统提示（价格变更等） |
| FORCE_OFFLINE | — | 踢下线（服务端推送） |

#### 校园认证模块（`/mini/auth`）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/mini/auth/submit` | 提交校园认证 |
| GET | `/mini/auth/status` | 获取我的认证状态 |
| GET | `/mini/auth/history` | 认证历史列表（按提交时间倒序） |
| GET | `/mini/auth/history/{id}` | 认证历史详情（按用户隔离，防越权） |

#### 用户关注模块（`/mini/follow`）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/mini/follow/follow` | 关注用户 |
| POST | `/mini/follow/unfollow` | 取消关注 |
| GET | `/mini/follow/check/{userId}` | 检查是否已关注 |
| GET | `/mini/follow/stats/{userId}` | 获取关注/粉丝统计 |

> **业务规则**：不能关注自己（Service 层校验）；关注成功后发送站内通知（类型 11 - 新增粉丝）；关注状态和统计数据均有 Redis 缓存。

#### 商品评论模块（`/mini/product/comment`）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/mini/product/comment/add` | 添加评论（支持多级回复） |
| POST | `/mini/product/comment/delete/{commentId}` | 删除评论（仅留言者本人） |
| GET | `/mini/product/comment/list/{productId}` | 商品评论列表（树形结构） |
| GET | `/mini/product/comment/received-replies` | 收到的回复（分页） |
| GET | `/mini/product/comment/unread-reply-count` | 未读回复数 |
| POST | `/mini/product/comment/mark-read` | 标记已读 |

**留言规则**
- 第一层留言：`parent_id` 为空，直接回复商品
- 第二层回复：`parent_id` 不为空，回复他人留言
- 若当前用户不是商品发布者，自动将回复目标设为发布者
- 返回数据组装为两层树形结构（根留言 + 回复列表）
- 被回复人收到站内通知（类型 2 - 新消息）

#### 文件上传模块（`/common/upload`）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/common/upload/image` | 上传图片（返回访问 URL） |

> **限制**：单文件最大 5MB，总请求最大 50MB，支持 JPG / PNG / GIF 格式。上传后以静态资源方式提供 HTTP 访问，路径前缀由 `upload.url-prefix` 配置。

#### 其他小程序端模块

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 校区 | GET | `/mini/campus/list` | 校区列表 |
| 校区 | GET | `/mini/campus/meeting-points/{campusId}` | 校区面交地点列表 |
| 分类 | GET | `/mini/category/list` | 分类列表 |
| Banner | GET | `/mini/banner/list` | Banner 列表（参数：campusId） |
| 收藏 | POST | `/mini/favorite/add` | 收藏商品 |
| 收藏 | POST | `/mini/favorite/cancel` | 取消收藏 |
| 收藏 | GET | `/mini/favorite/list` | 我的收藏列表（分页） |
| 收藏 | GET | `/mini/favorite/check/{productId}` | 是否已收藏 |
| 举报 | POST | `/mini/report/submit` | 提交举报 |
| 举报 | GET | `/mini/report/detail/{id}` | 举报详情 |
| 搜索 | GET | `/mini/search/hot-keywords` | 热门搜索词 |
| 消息 | GET | `/mini/notification/list` | 通知列表（支持 category 筛选） |
| 消息 | GET | `/mini/notification/favorite-list` | 收到的收藏提醒列表 |
| 消息 | GET | `/mini/notification/follower-list` | 新增粉丝提醒列表 |
| 消息 | POST | `/mini/notification/read` | 标记单条已读 |
| 消息 | POST | `/mini/notification/read-batch` | 批量标记已读 |
| 消息 | POST | `/mini/notification/read-type` | 按通知类型标记已读（参数：type） |
| 消息 | POST | `/mini/notification/read-all` | 全部标记已读 |
| 消息 | GET | `/mini/notification/unread-count` | 未读数统计 |

### 管理端接口

#### 商品审核（`/admin/product`）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/product/page` | 商品分页查询 |
| GET | `/admin/product/detail/{productId}` | 商品详情（管理端视角） |
| GET | `/admin/product/export` | 导出商品列表（CSV，responseType=blob） |
| POST | `/admin/product/approve` | 审核通过 |
| POST | `/admin/product/reject` | 审核驳回 |
| POST | `/admin/product/batch-approve` | 批量通过 |
| POST | `/admin/product/force-off` | 强制下架（参数：productId） |

#### 认证审核（`/admin/auth`）

| 方法 | 路径 | 说明 | 参数 |
|------|------|------|------|
| GET | `/admin/auth/page` | 认证申请分页查询 | page、size、status（可选）、collegeId（可选） |
| GET | `/admin/auth/detail/{id}` | 认证申请详情 | id（path） |
| GET | `/admin/auth/history/{authId}` | 认证历史时间线 | authId（path） |
| POST | `/admin/auth/approve` | 审核通过 | `{id}` |
| POST | `/admin/auth/reject` | 审核驳回 | `{id, rejectReason}` |

#### 用户管理（`/admin/user`）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/user/page` | 用户分页查询 |
| GET | `/admin/user/detail/{id}` | 用户详情 |
| POST | `/admin/user/ban` | 封禁用户 |
| POST | `/admin/user/unban` | 解封用户 |

#### 数据统计（`/admin/stats`）

| 方法 | 路径 | 说明 | 参数 | 响应数据 |
|------|------|------|------|---------|
| GET | `/admin/stats/overview` | 数据总览 | 无 | totalUsers、totalProducts、totalOrders、totalReviews、pendingAuthCount、pendingProductCount、todayNewUsers、todayNewProducts、todayNewOrders、totalAmount |
| GET | `/admin/stats/trend` | 趋势折线图 | days（默认 7，可选 7/30） | [{date, newUsers, newProducts, newOrders}] |
| GET | `/admin/stats/campus` | 校区维度统计 | 无 | [{campusName, productCount, orderCount, userCount}] |
| GET | `/admin/stats/category` | 分类维度统计 | 无 | [{categoryName, productCount, percentage}] |

#### 其他管理端模块

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 员工管理 | POST | `/admin/employee/login` | 管理员登录 |
| 员工管理 | GET | `/admin/employee/info` | 获取当前管理员信息 |
| 员工管理 | GET | `/admin/employee/page` | 分页查询管理员列表 |
| 员工管理 | POST | `/admin/employee/add` | 添加管理员 |
| 员工管理 | POST | `/admin/employee/update` | 更新管理员信息（启用/禁用通过 status 字段控制） |
| 员工管理 | POST | `/admin/employee/reset-password` | 重置管理员密码 |
| 订单管理 | GET | `/admin/order/page` | 分页查询订单列表 |
| 订单管理 | GET | `/admin/order/detail/{id}` | 订单详情 |
| 举报处理 | GET | `/admin/report/page` | 分页查询举报列表 |
| 举报处理 | GET | `/admin/report/detail/{id}` | 举报详情 |
| 举报处理 | POST | `/admin/report/handle` | 处理举报 |
| 分类管理 | GET | `/admin/category/page` | 分页查询分类列表 |
| 分类管理 | GET | `/admin/category/list` | 分类列表（下拉框用） |
| 分类管理 | POST | `/admin/category/add` | 添加分类 |
| 分类管理 | POST | `/admin/category/update` | 更新分类 |
| 分类管理 | POST | `/admin/category/delete` | 删除分类 |
| 校区管理 | GET | `/admin/campus/list` | 校区列表 |
| 校区管理 | POST | `/admin/campus/add` | 添加校区 |
| 校区管理 | POST | `/admin/campus/update` | 更新校区 |
| 校区管理 | GET | `/admin/campus/meeting-point/list/{campusId}` | 面交地点列表 |
| 校区管理 | POST | `/admin/campus/meeting-point/add` | 添加面交地点 |
| 校区管理 | POST | `/admin/campus/meeting-point/update` | 更新面交地点 |
| 校区管理 | POST | `/admin/campus/meeting-point/delete` | 删除面交地点 |
| Banner 管理 | GET | `/admin/banner/page` | 分页查询 Banner 列表 |
| Banner 管理 | POST | `/admin/banner/add` | 添加 Banner |
| Banner 管理 | POST | `/admin/banner/update` | 更新 Banner |
| Banner 管理 | POST | `/admin/banner/delete` | 删除 Banner |
| 公告管理 | GET | `/admin/notice/page` | 分页查询公告列表 |
| 公告管理 | POST | `/admin/notice/add` | 添加公告 |
| 公告管理 | POST | `/admin/notice/update` | 更新公告 |
| 公告管理 | POST | `/admin/notice/delete` | 删除公告 |
| 学院管理 | GET | `/admin/college/list` | 学院列表 |
| 学院管理 | POST | `/admin/college/add` | 添加学院 |
| 学院管理 | POST | `/admin/college/update` | 更新学院 |
| 学院管理 | POST | `/admin/college/delete` | 删除学院 |

> **注**：以上管理端配置类接口（分类/校区/Banner/公告/学院/员工）后端 API 均已实现，管理前端目前仅完成了列表展示页，增删改操作页面待开发。

---

## ⚙️ 环境配置与部署

### 开发环境要求

#### 后端环境

| 工具 | 要求 |
|------|------|
| JDK | 17+ |
| Maven | 3.6+ |
| MySQL | 5.7 |
| Redis | 6.0+ |
| IDE | IntelliJ IDEA |

#### 前端环境

| 工具 | 要求 |
|------|------|
| Node.js | 16+ |
| 包管理器 | npm 或 yarn |
| 小程序工具 | 微信开发者工具 |
| IDE | VS Code |

### 配置文件说明

```yaml
# application.yml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/secondhand?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password           # ⚠️ 生产环境使用环境变量
    driver-class-name: com.mysql.cj.jdbc.Driver

  data:
    redis:
      host: localhost
      port: 6379
      database: 0
      # password: your_redis_password  # ⚠️ 生产环境使用环境变量

  servlet:
    multipart:
      max-file-size: 5MB              # 单文件最大 5MB
      max-request-size: 50MB          # 总请求最大 50MB（多图上传场景）

# MyBatis-Plus 配置
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # 开发环境开启 SQL 日志
  global-config:
    db-config:
      id-type: auto

# JWT 配置
jwt:
  secret: "your_jwt_secret"            # ⚠️ 生产环境使用环境变量，至少 32 位
  expiration: 86400000                  # 小程序端 Token 有效期：24 小时
  admin-expiration: 86400000            # 管理端 Token 有效期：24 小时

# 微信小程序配置
wx:
  appId: "your_wx_appid"
  appSecret: "your_wx_appsecret"       # ⚠️ 生产环境使用环境变量

# 文件上传配置
upload:
  path: ./uploads/                     # 本地存储路径，生产建议改用 OSS
  url-prefix: /uploads/                # 访问 URL 前缀

# 定时任务开关（false 可单独关闭对应任务）
task:
  enabled:
    order-expire: true          # 订单超时取消（每 5 分钟）
    order-auto-confirm: true    # 订单自动确认（每天凌晨 2:00）
    review-auto: true           # 自动好评（每天凌晨 3:00）
    review-remind: true         # 评价提醒（每天上午 10:00）
    product-auto-off: true      # 商品自动下架（每天凌晨 4:00）
    user-deactivate: true       # 注销账号清理（每天凌晨 5:00）
```

> ⚠️ **安全警告**：以下配置**严禁**明文提交到代码仓库，生产环境务必使用环境变量或外部配置中心管理：
>
> | 配置项 | 风险级别 | 建议处理方式 |
> |--------|---------|------------|
> | `spring.datasource.password` | 🔴 高 | 环境变量 `SPRING_DATASOURCE_PASSWORD` |
> | `jwt.secret` | 🔴 高 | 环境变量 `JWT_SECRET`（至少 32 位随机字符串） |
> | `wx.appSecret` | 🔴 高 | 环境变量 `WX_APPSECRET` |
> | `spring.data.redis.password` | 🔴 高 | 环境变量 `REDIS_PASSWORD` |
> | `upload.url-prefix` | 🟡 中 | 建议使用 OSS/CDN，本地存储需配置访问控制 |

### 数据库初始化

**第一步：创建数据库**

```sql
CREATE DATABASE secondhand CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**第二步：执行建表脚本**

```bash
# 进入项目根目录
cd G:\Code\Graduation_project

# 执行初始建表脚本
mysql -u root -p secondhand < sql/init.sql

# 按序执行增量更新脚本
mysql -u root -p secondhand < sql/update/2026-02-21_f19_notification.sql
mysql -u root -p secondhand < sql/update/2026-02-21_f21_banner_search.sql
mysql -u root -p secondhand < sql/update/2026-02-22_f_im_02_chat_session.sql
mysql -u root -p secondhand < sql/update/2026-02-23_f_im_03_chat_message.sql
mysql -u root -p secondhand < sql/update/2026-04-06_f06_auth_real_name.sql
mysql -u root -p secondhand < sql/update/2026-04-08_f07_user_follow_profile.sql
mysql -u root -p secondhand < sql/update/2026-04-10_f08_product_comment.sql
```

**第三步：初始化基础数据**

```sql
-- 插入默认管理员（默认密码：123456）
INSERT INTO employee (username, password, name, role, status)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '超级管理员', 1, 1);

-- 插入校区数据
INSERT INTO campus (name, code, sort, status) VALUES
('南海北', 'nanhai_north', 1, 1),
('南海南', 'nanhai_south', 2, 1),
('新港',   'xingang',      3, 1);

-- 插入分类数据
INSERT INTO category (name, sort, status) VALUES
('书籍',     1, 1),
('服饰',     2, 1),
('生活',     3, 1),
('电子设备', 4, 1),
('运动设备', 5, 1),
('潮玩娱乐', 6, 1);
```

### 启动步骤

**后端启动**

```bash
# 1. 克隆项目
git clone <repository_url>
cd Graduation_project

# 2. 配置 application.yml 中的数据库、Redis、微信小程序等信息

# 3. 安装依赖并启动
mvn clean install
mvn spring-boot:run
# 或使用 IntelliJ IDEA 直接运行 SecondhandApplication.java
```

**小程序端启动**

```bash
cd miniapp
npm install
# 使用微信开发者工具打开 miniapp 目录
# 在 manifest.json 中配置 AppID 和后端接口地址
```

**管理端启动**

```bash
cd admin
npm install
npm run dev
# 浏览器访问 http://localhost:5173
```

**运行测试**

```bash
# 运行所有测试
mvn test

# 运行指定测试类
mvn test -Dtest=UserServiceTest

# 查看测试覆盖率报告
mvn clean test jacoco:report
```

---

## 🔐 安全机制

### 认证与鉴权

#### JWT Token 机制

| 属性 | 说明 |
|------|------|
| 适用范围 | 小程序端和管理端使用独立 Token 体系 |
| 有效期 | 24 小时 |
| 传递方式 | `Authorization: Bearer {token}` |
| 小程序拦截器 | `JwtInterceptor` |
| 管理端拦截器 | `AdminJwtInterceptor` |
| 特殊处理 | OPTIONS 预检请求直接放行，避免跨域问题 |

#### CORS 跨域配置

```java
// WebMvcConfig.java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
            .allowedOrigins(
                "http://localhost:3000", "http://127.0.0.1:3000",
                "http://localhost:5173", "http://127.0.0.1:5173"
            )
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
}
```

> **说明**：使用明确的域名列表而非通配符 `*`，解决 `allowCredentials(true)` 与通配符冲突的问题。

**跨域请求处理流程**

```text
浏览器发送 OPTIONS 预检请求
    ↓
JWT 拦截器检测到 OPTIONS → 直接放行（preHandle 首行判断）
    ↓
CORS 配置处理 → 返回 200 状态码
    ↓
浏览器发送真实请求（携带 Token）
    ↓
JWT 拦截器校验 Token → 正常处理业务
```

### 其他安全措施

| 安全项 | 实现方式 |
|--------|---------|
| 密码存储 | BCryptPasswordEncoder 加密 |
| 登录失败限制 | 失败 5 次锁定 15 分钟（Redis 计数，Key：`login:fail:{phone}`） |
| 用户上下文 | ThreadLocal（UserContext），请求结束自动清理 |
| 手机号脱敏 | 展示格式：`138****8888` |
| Entity 安全 | password 字段不出现在任何 VO 中 |
| SQL 注入防护 | 全部使用 `#{}` 参数绑定，禁止 `${}` 字符串拼接 |
| XSS 防护 | 前端输入校验 + 后端参数校验注解 |
| 并发安全 | 订单创建使用 Redis 分布式锁（Key：`product:lock:{productId}`，TTL：30 秒） |
| 重复操作防护 | 收藏唯一索引、关注唯一索引、举报同目标只能一次 |
| 越权防护 | 认证历史详情按用户 ID 隔离，订单详情校验买卖家身份 |

---

## ⏰ 定时任务

### 任务列表

| 任务名称 | 执行频率 | 功能说明 | 实现文件 |
|---------|---------|---------|---------|
| OrderExpireTask | 每 5 分钟 | 订单超时自动取消（72 小时未面交） | `task/OrderExpireTask.java` |
| OrderAutoConfirmTask | 每天凌晨 2:00 | 订单自动确认收货（创建后 7 天） | `task/OrderAutoConfirmTask.java` |
| ReviewAutoTask | 每天凌晨 3:00 | 自动好评（交易完成后 7 天未评价） | `task/ReviewAutoTask.java` |
| ReviewRemindTask | 每天上午 10:00 | 评价提醒（交易完成后第 3 天仍未评价） | `task/ReviewRemindTask.java` |
| ProductAutoOffTask | 每天凌晨 4:00 | 商品自动下架（发布后 90 天） | `task/ProductAutoOffTask.java` |
| UserDeactivateTask | 每天凌晨 5:00 | 注销账号数据清理（申请后 30 天） | `task/UserDeactivateTask.java` |
| WebSocketHeartbeatTask | 每 30 秒 | WebSocket 心跳检测（心跳 TTL = 60 秒，超时断线） | `task/WebSocketHeartbeatTask.java` |

> 所有任务均已实现，可通过 `application.yml` 中 `task.enabled.*` 开关单独控制，无需修改代码。

### 关键业务逻辑

**订单超时取消**
```text
查询条件：expire_time < NOW() AND status = 1（待面交）
执行操作：status → 5（已取消），cancel_by → 0（系统取消）
后续处理：通知买卖双方 + 商品恢复在售状态
```

**订单自动确认收货**
```text
查询条件：confirm_deadline < NOW() AND status = 1（待面交）
执行操作：status → 3（已完成），记录 complete_time
后续处理：通知买卖双方 + 商品状态 → 3（已售出）
```

**自动好评**
```text
查询条件：status = 3 AND complete_time + 7天 < NOW() 且存在未评价方
执行操作：为未评价方生成默认好评（三维度均 5 分，is_auto = 1）
后续处理：重新计算被评价人综合评分 + 订单 status → 4（已评价）
```

**账号注销清理**
```text
查询条件：status = 2（注销中）AND 申请时间 + 30天 < NOW()
执行操作：清空敏感字段，昵称改为"已注销用户"，status → 0（封禁）
说明：不物理删除用户行数据，30天内用户可随时恢复账号
```

---

## 📊 缓存策略

### Redis 缓存 Key 一览

| 缓存 Key | 数据类型 | TTL | 用途 |
|---------|---------|-----|------|
| `user:info:{userId}` | String | 10 分钟 | 用户信息缓存 |
| `user:stats:{userId}` | String | 10 分钟 | 用户统计数据 |
| `product:view:{productId}:{userId}` | String | 24 小时 | 浏览去重 |
| `category:list` | String | 1 小时 | 分类列表 |
| `campus:list` | String | 1 小时 | 校区列表 |
| `banner:list:{campusId}` | String | 30 分钟 | Banner 列表 |
| `search:hot:keywords` | String | 1 小时 | 热搜词 |
| `sms:code:{phone}` | String | 5 分钟 | 短信验证码 |
| `sms:limit:{phone}` | String | 60 秒 | 短信发送频率限制（60 秒内不重复发送） |
| `sms:daily:{phone}` | String | 24 小时 | 短信每日发送上限 |
| `login:fail:{phone}` | String | 15 分钟 | 登录失败次数（≥5 次锁定） |
| `product:lock:{productId}` | String | 30 秒 | 订单创建分布式锁 |
| `im:unread:{userId}` | String | 持久 | IM 未读消息总数 |
| `im:online:{userId}` | String | 持久 | 在线标记 |
| `im:heartbeat:{userId}` | String | 60 秒 | 心跳 TTL（超时即视为断线） |
| `follow:stats:{userId}` | String | 30 分钟 | 关注/粉丝统计缓存 |

### 缓存更新策略

- **Cache-Aside 模式**：查询优先走缓存，未命中则查数据库并写入缓存
- **主动失效**：数据更新时主动删除相关缓存（不使用延迟双删）
- **基础数据**：分类、校区等变更时同步清除对应缓存
- **认证状态**：提交/通过/驳回时主动清理 `user:info:{userId}` 与 `user:stats:{userId}`

---

## 💬 即时通讯（IM）实现

### 技术方案

基于 **Spring Boot WebSocket** 自建，不依赖第三方 IM 服务，降低外部依赖和运维成本。

### 架构设计

```text
客户端 ──ws://host:port/ws/chat?token={jwt_token}──→
    ChatHandshakeInterceptor（握手阶段验证 JWT）
        ↓
    WebSocketServer（TextWebSocketHandler）
        ↓
    WebSocketSessionManager（管理在线用户连接）
        ↓
    MessageHandler 分发处理
        ├── ChatMessageHandler   聊天消息处理
        ├── ReadMessageHandler   已读回执处理
        ├── PingMessageHandler   心跳保活
        └── SystemMessageHandler 系统消息处理
```

### 核心功能说明

| 功能 | 实现方式 |
|------|---------|
| 握手鉴权 | `ChatHandshakeInterceptor` 在连接建立前验证 JWT Token |
| 心跳保活 | 客户端每 30 秒发 PING，服务端回 PONG；心跳 TTL 60 秒，超时由 `WebSocketHeartbeatTask` 断线处理 |
| 消息持久化 | 所有消息存储到 `chat_message` 表，支持历史消息分页查询 |
| 未读管理 | 每个会话独立维护未读数，Redis 维护全局总未读数（`im:unread:{userId}`） |
| 已读回执 | 接收方读取消息后推送 READ 帧，发送方收到 READ_ACK 确认 |
| 离线消息 | 接收方离线时写入站内通知，上线后可在消息中心查看 |
| 业务卡片 | 商品卡片（点击"我想要"自动发送）、订单卡片（创建订单后推送）、系统提示（价格修改等） |
| 踢下线 | 服务端主动推送 `FORCE_OFFLINE` 帧（如封号场景） |

---

## 🧪 测试规范

### 测试策略

| 层次 | 框架 | 说明 |
|------|------|------|
| Service 单元测试 | JUnit 5 + Mockito | Mock Mapper 层，覆盖所有业务逻辑分支 |
| Controller 集成测试 | MockMvc | 测试完整请求-响应流程，验证参数校验和异常处理 |

### 测试示例

```java
@SpringBootTest
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void testWxLogin_NewUser_ShouldCreateUser() {
        // Given
        String code = "wx_code_123";
        String openId = "openid_123";
        when(wxService.getOpenId(code)).thenReturn(openId);
        when(userMapper.selectOne(any())).thenReturn(null);
        when(userMapper.insert(any())).thenReturn(1);

        // When
        LoginVO result = userService.wxLogin(code);

        // Then
        assertNotNull(result);
        assertTrue(result.getIsNew());
        assertNotNull(result.getToken());
        verify(userMapper, times(1)).insert(any());
    }

    @Test
    void testAccountLogin_WrongPassword_ShouldIncrementFailCount() {
        // Given
        String phone = "13800138000";
        User user = new User();
        user.setPassword("$2a$10$...");
        when(userMapper.selectOne(any())).thenReturn(user);

        // When & Then
        assertThrows(BusinessException.class,
            () -> userService.accountLogin(phone, "wrong_password"));
        verify(redisTemplate.opsForValue()).increment("login:fail:" + phone);
    }
}
```

### 测试要求

| 规范 | 说明 |
|------|------|
| 覆盖率要求 | Service 层核心业务逻辑覆盖率 ≥ 80% |
| 验收标准 | 所有 acceptance_criteria 必须有对应测试用例 |
| 禁止无效断言 | 禁止使用 `assertTrue(true)` 等无意义断言 |
| 禁止跳过测试 | 禁止使用 `@Disabled` 跳过关键测试 |

---

## 📝 开发规范

### 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 类名 | 大驼峰（PascalCase） | `UserServiceImpl` |
| 方法名 / 变量名 | 小驼峰（camelCase） | `getUserById` |
| 常量 | 全大写下划线分隔 | `MAX_RETRY_COUNT` |
| 包名 | 全小写，单词间不分隔 | `com.qingyuan.secondhand` |

### MyBatis-Plus 使用规范

**Entity 注解**

```java
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("nick_name")
    private String nickName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic  // 仅用于有逻辑删除需求的表（如 product、product_comment）
    private Integer isDeleted;
}
```

**Service 继承**

```java
public interface UserService extends IService<User> {
    LoginVO wxLogin(String code);
}

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {
    // 实现业务逻辑
}
```

**条件查询**

```java
// ✅ 推荐：使用 LambdaQueryWrapper
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
        .eq(User::getPhone, phone)
        .eq(User::getStatus, 1);
User user = userMapper.selectOne(wrapper);

// ❌ 禁止：字符串字段名拼接（存在字段名写错风险）
QueryWrapper<User> wrapper = new QueryWrapper<>();
wrapper.eq("phone", phone);
```

### 异常处理规范

```java
// ✅ 推荐：抛出业务异常，由全局处理器统一处理
@PostMapping("/login")
public Result<LoginVO> login(@RequestBody @Valid AccountLoginDTO dto) {
    LoginVO vo = userService.accountLogin(dto.getPhone(), dto.getPassword());
    return Result.success(vo);
}

// 全局异常处理器
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.error(e.getMessage());
    }
}

// ❌ 禁止：在 Controller 中 try-catch 业务异常
```

### 事务管理

```java
// 多表操作必须添加事务注解
@Transactional(rollbackFor = Exception.class)
public void createOrder(OrderCreateDTO dto) {
    tradeOrderMapper.insert(order);      // 创建订单
    productMapper.updateById(product);   // 更新商品状态
    notificationService.send(...);       // 发送通知
}
```

---

## 🚀 性能优化

### 数据库索引设计

```sql
-- 用户表
KEY `idx_open_id` (`open_id`),
KEY `idx_phone`   (`phone`),
KEY `idx_status`  (`status`)

-- 商品表
KEY `idx_user_id`                        (`user_id`),
KEY `idx_status_campus_category_create` (`status`, `campus_id`, `category_id`, `create_time`),
KEY `idx_status_price`                   (`status`, `price`)

-- 订单表
KEY `idx_buyer_id`          (`buyer_id`),
KEY `idx_seller_id`         (`seller_id`),
KEY `idx_expire_time`       (`expire_time`),
KEY `idx_confirm_deadline`  (`confirm_deadline`)

-- 用户关注表
UNIQUE KEY `uk_follower_followee` (`follower_id`, `followee_id`)
```

### 避免 N+1 查询

```java
// ❌ 禁止：循环内查询（N+1 问题）
List<Product> products = productMapper.selectList(wrapper);
for (Product product : products) {
    User user = userMapper.selectById(product.getUserId()); // 每次循环都发查询
}

// ✅ 推荐：使用 MyBatis XML 关联查询
// ProductMapper.xml
<select id="selectProductWithUser" resultMap="ProductDetailMap">
    SELECT p.*, u.nick_name, u.avatar_url
    FROM product p
    LEFT JOIN user u ON p.user_id = u.id
    WHERE p.status = 1
</select>
```

### 缓存防护策略

**防缓存穿透（缓存空值）**

```java
String cacheKey = "user:info:" + userId;
String cached = redisTemplate.opsForValue().get(cacheKey);
if (cached != null) {
    return "null".equals(cached) ? null : JSON.parseObject(cached, User.class);
}

User user = userMapper.selectById(userId);
if (user == null) {
    redisTemplate.opsForValue().set(cacheKey, "null", 5, TimeUnit.MINUTES);
    return null;
}
redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(user), 30, TimeUnit.MINUTES);
return user;
```

**防缓存雪崩（随机过期时间）**

```java
int randomSeconds = ThreadLocalRandom.current().nextInt(60, 300);
redisTemplate.opsForValue().set(key, value, 30 * 60 + randomSeconds, TimeUnit.SECONDS);
```

### 异步处理

```java
// 浏览量更新（异步，不阻塞主流程）
@Async
public void incrementViewCount(Long productId) {
    productMapper.incrementViewCount(productId);
}

// 通知发送（异步）
@Async
public void send(Long userId, Integer type, String title, String content) {
    Notification notification = new Notification();
    // 设置字段 ...
    notificationMapper.insert(notification);
}
```

---

## 🔧 常见问题（FAQ）

### 开发相关

**Q：为什么使用 MyBatis-Plus 而不是原生 MyBatis？**

A：MyBatis-Plus 提供了丰富的内置方法（CRUD、分页、Lambda 条件构造器），大幅减少样板代码。简单的 CRUD 操作无需编写 XML，复杂多表查询仍可使用 XML，两者可以共存。

---

**Q：`expire_time` 和 `confirm_deadline` 有什么区别？**

| 字段 | 含义 | 触发任务 |
|------|------|---------|
| `expire_time` | 创建后 72 小时，超时自动取消订单 | `OrderExpireTask`（每 5 分钟） |
| `confirm_deadline` | 创建后 7 天，自动确认收货 | `OrderAutoConfirmTask`（每天凌晨 2:00） |

---

**Q：为什么商品搜索不使用 FULLTEXT 全文索引？**

A：项目规模较小，`LIKE` 模糊匹配已满足需求。FULLTEXT 索引对中文支持需要额外配置分词器（如 IK Analyzer），引入了不必要的运维成本，后续如有需求可平滑迁移至 Elasticsearch。

---

**Q：如何处理同一商品的并发下单？**

A：使用 Redis 分布式锁（Key：`product:lock:{productId}`，TTL：30 秒）。创建订单前先获取锁，并检查是否已存在待面交订单，确保同一商品同时只有一个有效订单。锁在订单创建完成后释放。

---

**Q：账号注销后多久数据会被删除？**

A：申请注销后 30 天内用户可随时恢复账号；超过 30 天后由 `UserDeactivateTask` 执行数据匿名化处理（清空敏感字段、昵称改为"已注销用户"、账号置为封禁状态），**不会物理删除用户行数据**，以保证关联历史数据（订单、评价等）的完整性。

---

**Q：用户综合评分如何计算？**

A：
1. 单次评价得分 = (描述相符 + 沟通态度 + 交易体验) ÷ 3
2. 用户综合评分 = 所有收到评价的单次得分平均值
3. 结果保留一位小数，新用户默认 5.0 分
4. 自动好评（`is_auto = 1`）三维度均为 5 分，同样参与综合分计算

---

**Q：管理后台配置类页面（分类/校区等）开发状态如何？**

A：已全部完成。分类管理、校区管理（含面交地点）、学院管理、公告管理、员工管理、Banner 管理均已实现完整 CRUD 功能。

---

### 部署相关

**Q：如何关闭某个定时任务？**

A：编辑 `application.yml` 中对应的 `task.enabled.xxx` 设置为 `false` 即可，无需修改代码。

---

**Q：文件上传路径如何配置？**

A：编辑 `application.yml` 中的 `upload.path`（本地存储路径）和 `upload.url-prefix`（访问 URL 前缀）。生产环境建议使用对象存储（OSS/COS）替代本地存储，并配置 CDN 加速。

---

**Q：如何处理 WebSocket 跨域问题？**

A：WebSocket 连接在握手阶段由 `ChatHandshakeInterceptor` 处理，JWT Token 通过 URL 参数 `?token={jwt_token}` 传递。CORS 配置在 `WebMvcConfig` 中统一处理 HTTP 请求跨域，WebSocket 握手走独立通道不受影响。

---

## 🔄 更新日志

### V1.7（2026-04-26）

**后台管理前端完成配置类模块开发**
1. **分类管理**：完整实现 CRUD、状态切换、排序调整功能
2. **校区管理**：实现左右分栏布局，支持校区+面交地点双层数据管理
3. **学院管理**：完整实现 CRUD、状态切换、关键词搜索功能
4. **公告管理**：完整实现 CRUD、富文本内容编辑、置顶/有效期控制
5. **员工管理**：完整实现 CRUD、角色权限控制、密码重置、手机号脱敏展示
6. **Banner 管理**：完整实现 CRUD、图片上传预览、时间范围选择、链接类型联动

**后端修复**
1. `BannerVO.java` 补充 `@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")` 注解，修复时间字段序列化格式问题

---

### V1.6（2026-04-26）

**文档更新**
1. 新增「🚀 项目进度状态」章节，全面反映各端实际开发完成情况
2. 修正管理后台页面状态描述：配置类页面（分类/校区/学院/Banner/公告/员工）从「待开发」更新为「仅列表完成，增删改待完善」
3. 修正小程序页面状态描述：足迹页面标注为「开发中」，协议/帮助页面标注为「基础完成」
4. 补充 WebSocket 实现细节：新增消息处理器列表（`ChatMessageHandler`、`ReadMessageHandler`、`PingMessageHandler`、`SystemMessageHandler`）和 `WebSocketSessionManager` 说明
5. 定时任务表新增「实现文件」列，明确所有 7 个任务均已实现
6. 补充文件上传接口（`/common/upload/image`）及配置说明
7. 新增「管理后台配置类接口后端已实现」补充说明
8. 完善 FAQ：新增 WebSocket 跨域问题、管理后台配置页面进度说明
9. 项目结构注释更新，反映实际文件状态（✅ / ⚠️ / 🚧）
10. 功能总览表新增「状态」列，替代原「版本」列，真实反映当前进度

---

### V1.5（2026-04-22）

**后端更新**
1. CORS 跨域配置优化（`WebMvcConfig.java`）：将 `allowedOriginPatterns("*")` 改为明确指定前端地址
2. JWT 拦截器增强（`AdminJwtInterceptor.java`、`JwtInterceptor.java`）：OPTIONS 预检请求直接放行
3. 管理端统计接口（`/admin/stats/*`）：新增总览、趋势、校区、分类四个接口

**前端更新**
1. Dashboard 数据概览页完整重构（ECharts 图表、统计卡片、响应式布局）
2. 认证审核页补充历史时间线面板

---

### V1.1（2026-04-10）

**后端更新**
1. 用户关注体系：新增 `user_follow` 表、`/mini/follow/*` 接口组、Redis 缓存、新粉丝通知
2. 用户个人信息扩展：`user` 表新增 `bio`、`ip_region` 字段
3. 校园认证增强：`campus_auth` 表新增 `real_name`，新增 `campus_auth_history` 表和历史时间线接口
4. 商品评论功能：新增 `product_comment` 表和 `/mini/product/comment/*` 接口组

**前端更新**
1. 新增卖家主页关注按钮
2. 新增个人资料编辑页面（支持 bio 编辑）
3. 新增粉丝列表展示页
4. 新增收到的回复展示页
5. 完善认证历史功能（列表 + 详情 + 内容对比）

---

*最后更新时间：2026-04-26 ｜ 文档版本：V1.7 ｜ 项目路径：`G:\Code\Graduation_project`*