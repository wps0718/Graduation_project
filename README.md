# 轻院二手交易平台 - 完整项目文档

## 📋 项目概述

**项目名称**：轻院二手（Qingyuan Secondhand Trading Platform）  
**项目类型**：校园二手交易平台（毕业设计项目）  
**目标用户**：广东轻工职业技术大学在校学生  
**开发模式**：前后端分离 + 双IDE协同开发  

### 核心定位

轻院二手是一款专为广东轻工职业技术大学校内学生打造的微信小程序，旨在解决校园内闲置二手商品出售难的问题。平台参考闲鱼模式，结合校园特色，提供商品发布、浏览搜索、即时通讯、面交撮合等核心功能，让闲置物品在校园内高效流转。

### 项目痛点

| 痛点 | 描述 |
|------|------|
| 信息分散 | 学生出售闲置物品依赖QQ群、微信群，信息零散、易刷屏、难检索 |
| 信任缺失 | 群内交易无信用体系，商品质量和卖家信誉无法保障 |
| 效率低下 | 买卖双方缺乏高效的沟通和交易管理工具 |
| 毕业浪费 | 每年毕业季大量物品被丢弃，缺乏集中的处理渠道 |

---

## 🏗️ 技术架构

### 技术栈

#### 后端技术栈
- **语言**：Java 17+
- **框架**：Spring Boot 3.3.7
- **ORM**：MyBatis-Plus 3.5.7（非原生 MyBatis）
- **数据库**：MySQL 5.7
- **缓存**：Redis
- **构建工具**：Maven
- **测试框架**：JUnit 5 + Mockito + MockMvc
- **密码加密**：BCryptPasswordEncoder
- **认证方式**：JWT（Interceptor 拦截验证）
- **即时通讯**：Spring Boot WebSocket（自建）

#### 前端技术栈
- **小程序端**：uni-app + Vue 3
- **管理端**：Vue 3 + Vite + Element Plus

#### 项目结构
```
Graduation_project/
├── admin/                          # 管理后台前端（Vue 3）
│   ├── src/
│   │   ├── api/                   # API 接口
│   │   ├── components/            # 公共组件
│   │   ├── layout/                # 布局组件
│   │   ├── router/                # 路由配置
│   │   ├── store/                 # 状态管理
│   │   ├── styles/                # 样式文件
│   │   ├── utils/                 # 工具函数
│   │   └── views/                 # 页面组件
│   └── package.json
├── miniapp/                        # 小程序前端（uni-app）
│   ├── components/                # 公共组件
│   ├── pages/                     # 页面
│   ├── static/                    # 静态资源
│   ├── App.vue
│   ├── main.js
│   ├── manifest.json
│   └── pages.json
├── src/                           # 后端源码
│   ├── main/
│   │   ├── java/com/qingyuan/secondhand/
│   │   │   ├── common/           # 公共模块
│   │   │   │   ├── constant/    # 常量定义
│   │   │   │   ├── context/     # 上下文（UserContext）
│   │   │   │   ├── enums/       # 枚举类
│   │   │   │   ├── exception/   # 异常处理
│   │   │   │   ├── interceptor/ # 拦截器
│   │   │   │   ├── result/      # 统一响应
│   │   │   │   └── util/        # 工具类
│   │   │   ├── config/          # 配置类
│   │   │   ├── controller/      # 控制器
│   │   │   │   ├── admin/       # 管理端接口
│   │   │   │   ├── common/      # 公共接口
│   │   │   │   └── mini/        # 小程序端接口
│   │   │   ├── dto/             # 请求参数对象
│   │   │   ├── entity/          # 实体类
│   │   │   ├── mapper/          # Mapper 接口
│   │   │   ├── service/         # Service 接口
│   │   │   │   └── impl/        # Service 实现
│   │   │   ├── task/            # 定时任务
│   │   │   ├── vo/              # 返回视图对象
│   │   │   ├── websocket/       # WebSocket 相关
│   │   │   └── SecondhandApplication.java
│   │   └── resources/
│   │       ├── mapper/          # MyBatis XML
│   │       └── application.yml  # 配置文件
│   └── test/                    # 测试代码
└── docs/                        # 项目文档
    ├── 需求+架构文档.md
    ├── CodeRedme.md
    └── plans/                   # 开发计划
```


### 核心架构规则

| 规则 | 说明 |
|------|------|
| MyBatis-Plus 规范 | Mapper 继承 BaseMapper，Service 继承 IService/ServiceImpl |
| 简单 CRUD 不写 SQL | 使用 MyBatis-Plus 内置方法和 LambdaQueryWrapper |
| 复杂查询用 XML | 多表 JOIN 等复杂 SQL 才用 Mapper XML |
| Controller 不含业务逻辑 | 只做参数接收、校验、调用 Service |
| Entity 不直接返回前端 | 必须转换为 VO |
| Entity 使用 MP 注解 | @TableName, @TableId, @TableField, @TableLogic |
| 自动填充 | createTime/updateTime 通过 MetaObjectHandler 自动填充 |
| 分页使用 MP 插件 | Page<T> + PaginationInnerInterceptor |
| 条件构造用 Lambda | LambdaQueryWrapper 而非字符串拼接 |
| 统一响应封装 | 所有接口返回 Result<T> |
| 异常全局处理 | 不在 Controller 中 try-catch 业务异常 |

---

## 📦 核心功能模块

### 功能总览

| 模块 | 子功能 | 优先级 | 版本 |
|------|--------|--------|------|
| **登录注册** | 微信登录、手机号+密码登录、短信验证登录、自动注册、用户协议 | P0 | V1.0 |
| **校园认证** | 填写学院/学号/班级、上传认证材料、人工审核 | P0 | V1.0 |
| **首页** | 校区切换、Banner运营位、分类入口、最新发布商品流 | P0 | V1.0 |
| **搜索筛选** | 关键词搜索、热门搜索、搜索历史、多维度筛选 | P0 | V1.0 |
| **商品详情** | 图片轮播/放大、价格/新旧度/描述、卖家信息、收藏、举报、我想要、分享 | P0 | V1.0 |
| **商品发布** | 图片上传、商品信息填写、面交地点选择、编辑/下架/重新上架 | P0 | V1.0 |
| **卖家主页** | 卖家信息展示、该卖家在售商品列表 | P0 | V1.0 |
| **IM即时通讯** | 文字消息、商品卡片、快捷回复、聊天列表、确认购买入口 | P0 | V1.0 |
| **订单管理** | 创建订单、订单状态流转、确认收货、取消交易 | P0 | V1.0 |
| **评价系统** | 交易后互评、三维度评分、用户综合评分计算 | P1 | V1.0 |
| **收藏** | 收藏/取消收藏、我的收藏列表 | P0 | V1.0 |
| **足迹** | 浏览历史记录、我的足迹列表 | P2 | 规划中 |
| **举报** | 商品举报、举报分类、后台处理 | P1 | V1.0 |
| **个人中心** | 用户信息、统计数据、功能入口、设置、账号注销 | P0 | V1.0 |
| **消息中心** | 交易通知、系统通知、审核通知、收藏提醒、全部已读 | P0 | V1.0 |
| **后台管理** | 商品审核、认证审核、举报处理、数据统计、Banner/分类/校区/学院/公告管理 | P0 | V1.0 |


### 核心业务流程

#### 1. 用户注册/登录流程
```
用户打开小程序
    │
    ├─→ [微信一键登录] → 获取openId → 自动创建账号 → 进入首页
    │
    ├─→ [手机号+密码登录] → 校验账号密码 → 进入首页
    │
    └─→ [短信验证登录] → 输入手机号 → 获取验证码 → 校验通过
                              │
                              └─→ 未注册手机号 → 自动创建账号 → 进入首页

    ※ 首次登录需勾选同意《用户协议》和《隐私政策》
    ※ 登录成功后返回JWT Token，后续请求携带Token
```

#### 2. 校园认证流程
```
用户进入认证页面
    │
    ├─→ 填写：学院（下拉选择）、学号、班级
    ├─→ 上传：一卡通照片 或 3.0系统截图
    │
    └─→ [提交认证] → 后台人工审核
                        │
                        ├─→ [通过] → 用户标记"已认证" → 站内通知
                        └─→ [驳回] → 站内通知（附驳回原因）→ 用户可重新提交
```

#### 3. 商品发布流程
```
用户点击底部Tab"发布"
    │
    ├─→ 上传图片（最多9张，支持拖拽排序，首张为封面）
    ├─→ 填写：商品名称、二手价格、原价、产品类型（分类）、磨损程度、描述
    ├─→ 选择：交易校区、面交地点（预设+自定义）
    │
    └─→ [发布] → 进入审核队列
                    │
                    ├─→ [审核通过] → 商品上架，首页可见 → 站内通知
                    └─→ [审核驳回] → 站内通知（附原因）→ 用户可编辑后重新提交
```

#### 4. 交易完整流程
```
买家点击"我想要"
    │
    └─→ 跳转至与卖家的IM聊天界面（聊天中关联商品卡片）
            │
            └─→ 双方协商价格、面交时间地点
                    │
                    ├─→ [卖家修改价格]（可选，议价场景）
                    │       → 买家看到新价格后决定是否购买
                    │
                    └─→ [买家在聊天中点击"确认购买"] → 生成订单
                            │
                            │   订单状态：待面交
                            │   开始计时：72小时
                            │   同一商品同时只允许一个待面交订单（先到先得）
                            │
                            ├─→ [72小时内完成面交]
                            │       │
                            │       └─→ [买家点击"确认收货"]
                            │               │
                            │               └─→ 订单状态：已完成
                            │                       │
                            │                       └─→ [双方互评]（7天窗口期）
                            │                               │
                            │                               ├─→ 双方都评价 → 订单状态：已评价
                            │                               └─→ 7天未评价 → 系统自动默认好评
                            │
                            ├─→ [72小时超时] → 订单自动取消 → 通知双方 → 商品恢复在售
                            │
                            ├─→ [买家取消交易] → 选择取消原因 → 订单取消 → 通知卖家
                            │
                            └─→ [卖家取消交易] → 选择取消原因 → 订单取消 → 通知买家
```


---

## 🗄️ 数据库设计

### 核心数据表

#### 1. 用户表（user）
```sql
-- 用户ID自增起始值：10000
-- 关键字段：open_id(微信唯一标识), phone(手机号), auth_status(认证状态), score(综合评分)
-- 状态枚举：status(0-封禁/1-正常/2-注销中), auth_status(0-未认证/1-审核中/2-已认证/3-已驳回)
```

#### 2. 商品表（product）
```sql
-- 关键字段：user_id(发布者), title(标题), price(价格), category_id(分类), status(状态)
-- 状态枚举：0-待审核/1-在售/2-已下架/3-已售出/4-审核驳回
-- 成色枚举：condition_level(1-全新/2-几乎全新/3-9成新/4-8成新/5-7成新及以下)
-- 逻辑删除：is_deleted(@TableLogic)
-- 自动下架：auto_off_time(发布后90天)
```

#### 3. 订单表（trade_order）
```sql
-- 订单号格式：TD + yyyyMMddHHmmss + 4位随机数
-- 关键字段：product_id, buyer_id, seller_id, price, status
-- 状态枚举：1-待面交/2-预留/3-已完成/4-已评价/5-已取消
-- 超时机制：expire_time(创建后72h), confirm_deadline(创建后7天)
-- 软删除：is_deleted_buyer, is_deleted_seller
```

#### 4. 评价表（review）
```sql
-- 三维度评分：score_desc(描述相符), score_attitude(沟通态度), score_experience(交易体验)
-- 评分范围：1-5分
-- 自动评价：is_auto(0-否/1-是)，7天未评价自动好评
-- 综合评分计算：(三维度之和/3)的所有评价平均值
```

#### 5. 聊天会话表（chat_session）
```sql
-- 会话标识：sessionKey = min(userA,userB)_max(userA,userB)_productId
-- 双向记录：每对用户+商品产生两条记录（各自管理未读数、置顶、删除）
-- 关键字段：user_id, peer_id, product_id, unread, last_msg, last_time
```

#### 6. 聊天消息表（chat_message）
```sql
-- 消息类型：1-文本/2-商品卡片/3-订单卡片/4-系统提示/5-快捷回复
-- 关键字段：session_key, sender_id, receiver_id, msg_type, content, is_read
```

### 数据库关系图
```
user(用户) ──1:N──→ product(商品)
user(用户) ──1:N──→ trade_order(订单) ←──N:1── product(商品)
user(用户) ──1:N──→ favorite(收藏) ←──N:1── product(商品)
user(用户) ──1:1──→ campus_auth(校园认证) ──N:1──→ college(学院)
trade_order(订单) ──1:2──→ review(评价)
campus(校区) ──1:N──→ meeting_point(面交地点)
user(用户) ──1:N──→ chat_session(会话)
chat_session(会话) ──1:N──→ chat_message(消息)
```

### 枚举类说明

#### 1. NotificationType（通知类型）
| code | description | template |
|------|-------------|----------|
| 1 | 交易成功 | 你购买的「{productName}」交易已完成，给卖家一个评价吧！ |
| 2 | 新消息 | {nickName}回复了你的消息："{content}" |
| 3 | 商品审核通过 | 您的商品《{productName}》已通过审核，现已上架！ |
| 4 | 商品审核驳回 | 您的商品《{productName}》未通过审核，驳回原因：{reason} |
| 5 | 系统公告 | {content} |
| 6 | 商品被收藏 | 你的商品《{productName}》被{count}位用户收藏了 |
| 7 | 订单已取消 | 你与{nickName}的交易「{productName}」已取消 |
| 8 | 校园认证通过 | 恭喜您，您的校园认证已通过审核！ |
| 9 | 校园认证被驳回 | 您的校园认证未通过审核，驳回原因：{reason} |
| 10 | 评价提醒 | 你购买的「{productName}」交易已完成3天，还未评价哦 |

#### 2. NotificationCategory（通知分类）
| code | description |
|------|-------------|
| 1 | 交易 |
| 2 | 系统 |

#### 3. BannerLinkType（Banner链接类型）
| code | description |
|------|-------------|
| 1 | 商品详情 |
| 2 | 活动页 |
| 3 | 外部链接 |

#### 4. ReportStatus（举报状态）
| code | description |
|------|-------------|
| 0 | 待处理 |
| 1 | 已处理 |
| 2 | 已驳回 |

#### 5. ReportReason（举报原因）
| code | description |
|------|-------------|
| 1 | 虚假商品 |
| 2 | 价格欺诈 |
| 3 | 违禁物品 |
| 4 | 恶意骚扰 |
| 5 | 侵权商品 |
| 6 | 其他 |


---

## 🔌 核心接口说明

### 统一响应格式
```json
// 成功响应
{
  "code": 1,
  "msg": "success",
  "data": { ... }
}

// 失败响应
{
  "code": 0,
  "msg": "错误信息描述",
  "data": null
}
```

### 小程序端核心接口

#### 用户模块（/mini/user）
- `POST /mini/user/wx-login` - 微信登录（公开）
- `POST /mini/user/login` - 手机号密码登录（公开）
- `POST /mini/user/sms/send` - 发送短信验证码（公开）
- `POST /mini/user/sms-login` - 短信验证登录（公开）
- `GET /mini/user/info` - 获取当前用户信息
- `POST /mini/user/update` - 更新用户信息
- `POST /mini/user/accept-agreement` - 确认同意协议（需登录）
- `GET /mini/user/stats` - 获取用户统计数据
- `GET /mini/user/profile/{id}` - 查看卖家主页（公开）
- `POST /mini/user/deactivate` - 申请注销账号（需登录）
- `POST /mini/user/restore` - 恢复注销中账号（需登录）

#### 商品模块（/mini/product）
- `POST /mini/product/publish` - 发布商品
- `POST /mini/product/update` - 编辑商品
- `GET /mini/product/detail/{id}` - 商品详情（公开）
- `GET /mini/product/list` - 商品列表（公开，支持筛选排序）
- `GET /mini/product/my-list` - 我发布的商品
- `POST /mini/product/update-price` - 修改价格（议价场景）
- `POST /mini/product/off-shelf` - 下架商品（请求参数：productId）
- `POST /mini/product/on-shelf` - 上架商品（请求参数：productId）
- `POST /mini/product/delete` - 删除商品（请求参数：productId）

#### 订单模块（/mini/order）
- `POST /mini/order/create` - 创建订单（含分布式锁）
- `GET /mini/order/list` - 订单列表（支持买家/卖家视角）
- `GET /mini/order/detail/{id}` - 订单详情
- `POST /mini/order/confirm` - 确认收货（请求参数：orderId）
- `POST /mini/order/cancel` - 取消订单
- `POST /mini/order/delete` - 删除订单（请求参数：orderId）

#### 聊天模块（/mini/chat）
- `POST /mini/chat/session/create` - 创建会话
- `GET /mini/chat/sessions` - 会话列表
- `GET /mini/chat/list` - 会话列表（小程序分页格式：total/records）
- `POST /mini/chat/session/delete` - 删除会话
- `POST /mini/chat/delete` - 删除会话（兼容小程序）
- `POST /mini/chat/session/top` - 会话置顶/取消置顶
- `GET /mini/chat/messages` - 消息历史（分页）
- `POST /mini/chat/read` - 标记已读
- `GET /mini/chat/unread-total` - 未读总数

#### WebSocket 端点
- `ws://host:port/ws/chat?token={jwt_token}` - WebSocket 连接
- 消息类型：PING/PONG（心跳）、CHAT（聊天）、READ/READ_ACK（已读）、SYSTEM（系统提示）、FORCE_OFFLINE（踢下线）

#### 校园认证模块（/mini/auth）
- `POST /mini/auth/submit` - 提交校园认证
- `GET /mini/auth/status` - 获取我的认证状态

#### 校区与面交地点模块（/mini/campus）
- `GET /mini/campus/list` - 校区列表
- `GET /mini/campus/meeting-points/{campusId}` - 校区面交地点列表

#### 分类模块（/mini/category）
- `GET /mini/category/list` - 分类列表

#### Banner模块（/mini/banner）
- `GET /mini/banner/list` - Banner列表（请求参数：campusId）

#### 收藏模块（/mini/favorite）
- `POST /mini/favorite/add` - 收藏商品
- `POST /mini/favorite/cancel` - 取消收藏
- `GET /mini/favorite/list` - 我的收藏列表（分页）
- `GET /mini/favorite/check/{productId}` - 是否已收藏

#### 举报模块（/mini/report）
- `POST /mini/report/submit` - 提交举报
- `GET /mini/report/detail/{id}` - 举报详情（用于确认提交结果/展示）

#### 搜索模块（/mini/search）
- `GET /mini/search/hot-keywords` - 热门搜索词

#### 消息中心模块（/mini/notification）
- `GET /mini/notification/list` - 通知列表（分页，支持 category 筛选）
- `POST /mini/notification/read` - 标记单条已读（请求参数：id）
- `POST /mini/notification/read-all` - 全部标记已读
- `GET /mini/notification/unread-count` - 未读数（总数/交易/系统）

### 管理端核心接口

#### 商品审核（/admin/product）
- `GET /admin/product/page` - 商品分页查询
- `POST /admin/product/approve` - 审核通过
- `POST /admin/product/reject` - 审核驳回
- `POST /admin/product/batch-approve` - 批量通过
- `POST /admin/product/force-off` - 强制下架

#### 认证审核（/admin/auth）
- `GET /admin/auth/page` - 认证分页查询
- `POST /admin/auth/approve` - 认证通过
- `POST /admin/auth/reject` - 认证驳回

#### 用户管理（/admin/user）
- `GET /admin/user/page` - 用户分页查询
- `POST /admin/user/ban` - 封禁用户
- `POST /admin/user/unban` - 解封用户

#### 数据统计（/admin/stats）
- `GET /admin/stats/overview` - 数据概览
- `GET /admin/stats/trend` - 趋势数据
- `GET /admin/stats/campus` - 校区维度统计
- `GET /admin/stats/category` - 分类维度统计

#### 员工管理（/admin/employee）
- `POST /admin/employee/login` - 管理员登录
- `GET /admin/employee/info` - 获取当前管理员信息
- `GET /admin/employee/page` - 分页查询管理员列表
- `POST /admin/employee/add` - 添加管理员
- `POST /admin/employee/update` - 更新管理员信息（启用/禁用通过status字段控制）
- `POST /admin/employee/reset-password` - 重置管理员密码

#### 订单管理（/admin/order）
- `GET /admin/order/page` - 分页查询订单列表
- `GET /admin/order/detail/{id}` - 订单详情

#### 举报处理（/admin/report）
- `GET /admin/report/page` - 分页查询举报列表
- `GET /admin/report/detail/{id}` - 举报详情
- `POST /admin/report/handle` - 处理举报

#### 分类管理（/admin/category）
- `GET /admin/category/page` - 分页查询分类列表
- `GET /admin/category/list` - 分类列表（下拉框用）
- `POST /admin/category/add` - 添加分类
- `POST /admin/category/update` - 更新分类
- `POST /admin/category/delete` - 删除分类

#### 校区管理（/admin/campus）
- `GET /admin/campus/list` - 校区列表
- `POST /admin/campus/add` - 添加校区
- `POST /admin/campus/update` - 更新校区
- `GET /admin/campus/meeting-point/list/{campusId}` - 面交地点列表
- `POST /admin/campus/meeting-point/add` - 添加面交地点
- `POST /admin/campus/meeting-point/update` - 更新面交地点
- `POST /admin/campus/meeting-point/delete` - 删除面交地点

#### Banner管理（/admin/banner）
- `GET /admin/banner/page` - 分页查询Banner列表
- `POST /admin/banner/add` - 添加Banner
- `POST /admin/banner/update` - 更新Banner
- `POST /admin/banner/delete` - 删除Banner

#### 公告管理（/admin/notice）
- `GET /admin/notice/page` - 分页查询公告列表
- `POST /admin/notice/add` - 添加公告
- `POST /admin/notice/update` - 更新公告
- `POST /admin/notice/delete` - 删除公告

#### 学院管理（/admin/college）
- `GET /admin/college/list` - 学院列表
- `POST /admin/college/add` - 添加学院
- `POST /admin/college/update` - 更新学院
- `POST /admin/college/delete` - 删除学院


---

## ⚙️ 环境配置与部署

### 开发环境要求

#### 后端环境
- JDK 17+
- Maven 3.6+
- MySQL 5.7
- Redis 6.0+
- IDE：IntelliJ IDEA

#### 前端环境
- Node.js 16+
- npm 或 yarn
- 微信开发者工具（小程序开发）
- VS Code

### 配置文件说明

#### application.yml
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/secondhand?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  data:
    redis:
      host: localhost
      port: 6379
      database: 0

# JWT配置
jwt:
  secret: "your_jwt_secret"
  expiration: 86400000  # 24小时

# 微信小程序配置
wx:
  appId: "your_wx_appid"
  appSecret: "your_wx_appsecret"

# 文件上传配置
upload:
  path: ./uploads/
  url-prefix: /uploads/

# 定时任务开关
task:
  enabled:
    order-expire: true          # 订单超时取消
    order-auto-confirm: true    # 订单自动确认
    review-auto: true           # 自动好评
    review-remind: true         # 评价提醒
    product-auto-off: true      # 商品自动下架
    user-deactivate: true       # 注销账号清理
```

### 数据库初始化

> **SQL文件位置**：`sql/` 目录
> - `sql/init.sql` - 初始建表脚本
> - `sql/update/` - 增量更新脚本（按日期命名）

1. 创建数据库
```sql
CREATE DATABASE secondhand CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行建表脚本
```bash
# 进入项目根目录
cd G:\Code\Graduation_project

# 执行初始建表脚本
mysql -u root -p secondhand < sql/init.sql

# 按需执行增量更新脚本（如有）
mysql -u root -p secondhand < sql/update/2026-02-21_f19_notification.sql
mysql -u root -p secondhand < sql/update/2026-02-21_f21_banner_search.sql
mysql -u root -p secondhand < sql/update/2026-02-22_f_im_02_chat_session.sql
mysql -u root -p secondhand < sql/update/2026-02-23_f_im_03_chat_message.sql
```

3. 初始化基础数据
```sql
-- 插入默认管理员（密码：123456）
INSERT INTO employee (username, password, name, role, status) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '超级管理员', 1, 1);

-- 插入校区数据
INSERT INTO campus (name, code, sort, status) VALUES
('南海北', 'nanhai_north', 1, 1),
('南海南', 'nanhai_south', 2, 1),
('新港', 'xingang', 3, 1);

-- 插入分类数据
INSERT INTO category (name, sort, status) VALUES
('书籍', 1, 1),
('服饰', 2, 1),
('生活', 3, 1),
('电子设备', 4, 1),
('运动设备', 5, 1),
('潮玩娱乐', 6, 1);
```


### 启动步骤

#### 后端启动
```bash
# 1. 克隆项目
git clone <repository_url>
cd Graduation_project

# 2. 配置数据库和Redis连接信息
# 编辑 src/main/resources/application.yml

# 3. 安装依赖并启动
mvn clean install
mvn spring-boot:run

# 或使用IDE直接运行 SecondhandApplication.java
```

#### 小程序端启动
```bash
# 1. 进入小程序目录
cd miniapp

# 2. 安装依赖
npm install

# 3. 使用微信开发者工具打开 miniapp 目录
# 配置 AppID 和后端接口地址
```

#### 管理端启动
```bash
# 1. 进入管理端目录
cd admin

# 2. 安装依赖
npm install

# 3. 启动开发服务器
npm run dev

# 4. 浏览器访问 http://localhost:5173
```

### 测试运行

```bash
# 运行所有测试
mvn test

# 运行指定测试类
mvn test -Dtest=UserServiceTest

# 查看测试覆盖率
mvn clean test jacoco:report
```

---

## 🔐 安全机制

### 认证与鉴权

#### JWT Token 机制
- 小程序端和管理端使用独立的 Token 体系
- Token 有效期：24小时
- Token 存储位置：Header `Authorization: Bearer {token}`
- 拦截器：`JwtInterceptor`（小程序）、`AdminJwtInterceptor`（管理端）

#### 用户上下文
- 使用 `UserContext`（ThreadLocal）存储当前登录用户信息
- 请求结束后自动清理，避免内存泄漏

### 密码安全
- 使用 `BCryptPasswordEncoder` 加密存储
- 登录失败5次锁定15分钟（Redis计数）
- 密码重置需验证身份

### 数据安全
- 手机号脱敏显示（138****8888）
- Entity 的 password 字段不出现在 VO 中
- SQL 注入防护：全部使用 `#{}` 参数绑定
- XSS 防护：前端输入校验 + 后端参数校验

### 业务安全
- 订单创建使用 Redis 分布式锁防止并发
- 同一商品同时只允许一个待面交订单
- 举报防滥用：同一用户对同一目标只能举报一次
- 收藏防重复：唯一索引约束

### ⚠️ 敏感配置安全警告
> **请勿将敏感配置提交到代码仓库！**

生产环境以下配置必须使用环境变量或外部配置中心管理：

| 配置项 | 风险级别 | 建议处理方式 |
|--------|---------|-------------|
| `spring.datasource.password` | 🔴 高 | 环境变量：`SPRING_DATASOURCE_PASSWORD` |
| `jwt.secret` | 🔴 高 | 环境变量：`JWT_SECRET`（至少32位随机字符串） |
| `wx.appSecret` | 🔴 高 | 环境变量：`WX_APPSECRET` |
| `spring.data.redis.password` | 🔴 高 | 环境变量：`REDIS_PASSWORD` |
| `upload.url-prefix` | 🟡 中 | 建议使用OSS/CDN，本地存储需配置访问控制 |


---

## ⏰ 定时任务

### 任务列表

| 任务名称 | 执行频率 | 功能说明 |
|---------|---------|---------|
| OrderExpireTask | 每5分钟 | 订单超时自动取消（72小时未面交） |
| OrderAutoConfirmTask | 每天凌晨2点 | 订单自动确认收货（创建后7天） |
| ReviewAutoTask | 每天凌晨3点 | 自动好评（交易完成后7天未评价） |
| ReviewRemindTask | 每天上午10点 | 评价提醒（交易完成后第3天仍未评价） |
| ProductAutoOffTask | 每天凌晨4点 | 商品自动下架（发布后90天） |
| UserDeactivateTask | 每天凌晨5点 | 注销账号数据清理（申请后30天） |
| WebSocketHeartbeatTask | 每30秒 | WebSocket 心跳检测（心跳TTL=60秒，超时断线） |

### 任务开关配置
```yaml
task:
  enabled:
    order-expire: true
    order-auto-confirm: true
    review-auto: true
    review-remind: true
    product-auto-off: true
    user-deactivate: true
```

### 关键业务逻辑

#### 订单超时取消
```java
// 查询条件：expire_time < NOW() AND status = 1
// 操作：status → 5（已取消），cancel_by → 0（系统）
// 通知：买卖双方站内通知
// 商品：恢复在售状态
```

#### 订单自动确认
```java
// 查询条件：confirm_deadline < NOW() AND status = 1
// 操作：status → 3（已完成），设置 complete_time
// 通知：买卖双方站内通知
// 商品：status → 3（已售出）
```

#### 自动好评
```java
// 查询条件：status = 3 AND complete_time + 7天 < NOW()
// 操作：为未评价方生成默认好评（5/5/5分，is_auto=1）
// 评分：重新计算被评价人综合评分
// 订单：status → 4（已评价）
```

---

## 📊 缓存策略

### Redis 缓存使用场景

| 缓存Key | 数据类型 | TTL | 用途 |
|---------|---------|-----|------|
| `user:info:{userId}` | String | 10分钟 | 用户信息缓存 |
| `user:stats:{userId}` | String | 10分钟 | 用户统计数据 |
| `product:view:{productId}:{userId}` | String | 24小时 | 浏览去重 |
| `category:list` | String | 1小时 | 分类列表 |
| `campus:list` | String | 1小时 | 校区列表 |
| `banner:list:{campusId}` | String | 30分钟 | Banner列表 |
| `search:hot:keywords` | String | 1小时 | 热搜词 |
| `sms:code:{phone}` | String | 5分钟 | 短信验证码 |
| `sms:limit:{phone}` | String | 60秒 | 短信发送频率限制 |
| `sms:daily:{phone}` | String | 24小时 | 短信每日上限 |
| `login:fail:{phone}` | String | 15分钟 | 登录失败次数 |
| `product:lock:{productId}` | String | 30秒 | 订单创建分布式锁 |
| `im:unread:{userId}` | String | - | IM未读消息总数 |
| `im:session:unread:{userId}` | String | - | 会话未读总数/扩展键（实现中保留） |
| `im:online:{userId}` | String | - | 在线标记 |
| `im:heartbeat:{userId}` | String | 60秒 | 心跳TTL（用于断线检测） |

### 缓存更新策略
- 查询优先走缓存，缓存未命中查数据库并写入缓存
- 数据更新时主动删除相关缓存（Cache-Aside Pattern）
- 分类、校区等基础数据变更时清除对应缓存


---

## 💬 即时通讯（IM）实现

### 技术方案
- 基于 Spring Boot WebSocket 自建
- 替代原 OpenIM 方案，降低外部依赖

### 架构设计

#### WebSocket 连接
```
客户端 → ws://host:port/ws/chat?token={jwt_token}
       ↓
握手拦截器（ChatHandshakeInterceptor）
       ↓ 验证JWT
WebSocketServer（TextWebSocketHandler）
       ↓
SessionManager（管理在线用户）
```

#### 消息协议
```json
// 客户端 → 服务端
{
  "type": "CHAT",  // PING/CHAT/READ
  "data": {
    "receiverId": 10002,
    "productId": 10086,
    "msgType": 1,  // 1-文本/2-商品卡片/3-订单卡片/4-系统提示/5-快捷回复
    "content": "你好，这个还在吗？"
  }
}

// 服务端 → 客户端
{
  "type": "CHAT",
  "data": {
    "msgId": 12345,
    "sessionKey": "10001_10002_10086",
    "senderId": 10001,
    "senderName": "张三",
    "senderAvatar": "...",
    "receiverId": 10002,
    "productId": 10086,
    "msgType": 1,
    "content": "你好，这个还在吗？",
    "createTime": "2025-02-24 10:30:00"
  }
}
```

### 核心功能

#### 1. 心跳保活
- 客户端每30秒发送 `{type: "PING"}`
- 服务端回复 `{type: "PONG"}`
- 心跳TTL为60秒，后台每30秒扫描一次超时连接并清理

#### 2. 消息持久化
- 所有聊天消息存储到 `chat_message` 表
- 会话信息存储到 `chat_session` 表
- 支持历史消息分页查询

#### 3. 未读管理
- 每个会话独立维护未读数（`chat_session.unread`）
- 接收消息时 `unread + 1`
- 标记已读时 `unread = 0`
- Redis 维护总未读数（`im:unread:{userId}`）

#### 4. 离线消息
- 接收方离线时写入站内通知
- 上线后可在消息中心查看
- 历史消息通过 REST API 查询

#### 5. 业务卡片
- 商品卡片：点击"我想要"时自动发送
- 订单卡片：创建订单时自动发送
- 系统提示：价格修改、订单取消等场景

### 会话管理

#### SessionKey 生成规则
```java
// 格式：min(userA, userB)_max(userA, userB)_productId
// 示例：10001_10002_10086
String sessionKey = Math.min(userId1, userId2) + "_" + 
                    Math.max(userId1, userId2) + "_" + 
                    productId;
```

#### 双向会话记录
- 每对用户+商品产生两条 `chat_session` 记录
- 各自独立管理：未读数、置顶、删除状态
- 删除会话不影响对方


---

## 🧪 测试规范

### 测试策略

#### 单元测试（Service 层）
- 使用 JUnit 5 + Mockito
- Mock Mapper 层依赖
- 覆盖所有业务逻辑分支
- 测试 acceptance_criteria 中的每一项

#### 集成测试（Controller 层）
- 使用 MockMvc
- 测试完整的请求-响应流程
- 验证参数校验和异常处理

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
        String password = "wrong_password";
        User user = new User();
        user.setPassword("$2a$10$...");
        
        when(userMapper.selectOne(any())).thenReturn(user);
        
        // When & Then
        assertThrows(BusinessException.class, 
            () -> userService.accountLogin(phone, password));
        verify(redisTemplate).opsForValue()
            .increment("login:fail:" + phone);
    }
}
```

### 测试覆盖要求
- Service 层核心业务逻辑覆盖率 ≥ 80%
- 所有 acceptance_criteria 必须有对应测试
- 禁止使用 `assertTrue(true)` 等无意义断言
- 禁止使用 `@Disabled` 跳过关键测试

---

## 📝 开发规范

### 代码规范

#### 命名规范
- 类名：大驼峰（PascalCase）
- 方法名/变量名：小驼峰（camelCase）
- 常量：全大写下划线分隔（UPPER_SNAKE_CASE）
- 包名：全小写，单词间不分隔

#### 注释规范
```java
/**
 * 用户登录
 * 
 * @param phone 手机号
 * @param password 密码
 * @return 登录结果（包含Token和用户信息）
 * @throws BusinessException 账号不存在或密码错误
 */
public LoginVO accountLogin(String phone, String password) {
    // 实现逻辑
}
```

### MyBatis-Plus 使用规范

#### Entity 注解
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
    
    @TableLogic  // 仅用于 product.isDeleted
    private Integer isDeleted;
}
```

#### Service 继承
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

#### 条件查询
```java
// ✅ 推荐：使用 LambdaQueryWrapper
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(User::getPhone, phone)
       .eq(User::getStatus, 1);
User user = userMapper.selectOne(wrapper);

// ❌ 禁止：字符串拼接
QueryWrapper<User> wrapper = new QueryWrapper<>();
wrapper.eq("phone", phone);  // 不推荐
```


### 异常处理规范

#### 业务异常
```java
// 抛出业务异常
if (user == null) {
    throw new BusinessException("账号不存在");
}

// 全局异常处理器自动捕获
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.error(e.getMessage());
    }
}
```

#### Controller 层
```java
// ✅ 推荐：不捕获业务异常
@PostMapping("/login")
public Result<LoginVO> login(@RequestBody @Valid AccountLoginDTO dto) {
    LoginVO vo = userService.accountLogin(dto.getPhone(), dto.getPassword());
    return Result.success(vo);
}

// ❌ 禁止：在 Controller 中 try-catch 业务异常
@PostMapping("/login")
public Result<LoginVO> login(@RequestBody AccountLoginDTO dto) {
    try {
        LoginVO vo = userService.accountLogin(dto.getPhone(), dto.getPassword());
        return Result.success(vo);
    } catch (BusinessException e) {  // 不推荐
        return Result.error(e.getMessage());
    }
}
```

### 事务管理
```java
// 多表操作必须添加事务注解
@Transactional(rollbackFor = Exception.class)
public void createOrder(OrderCreateDTO dto) {
    // 创建订单
    tradeOrderMapper.insert(order);
    
    // 更新商品状态
    productMapper.updateById(product);
    
    // 发送通知
    notificationService.send(...);
}
```

---

## 🚀 性能优化

### 数据库优化

#### 索引设计
```sql
-- 用户表
KEY `idx_open_id` (`open_id`),
KEY `idx_phone` (`phone`),
KEY `idx_status` (`status`)

-- 商品表
KEY `idx_user_id` (`user_id`),
KEY `idx_status_campus_category_create` (`status`, `campus_id`, `category_id`, `create_time`),
KEY `idx_status_price` (`status`, `price`)

-- 订单表
KEY `idx_buyer_id` (`buyer_id`),
KEY `idx_seller_id` (`seller_id`),
KEY `idx_expire_time` (`expire_time`),
KEY `idx_confirm_deadline` (`confirm_deadline`)
```

#### 分页查询优化
```java
// 使用 MyBatis-Plus 分页插件
Page<Product> page = new Page<>(pageNum, pageSize);
LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
wrapper.eq(Product::getStatus, 1)
       .orderByDesc(Product::getCreateTime);
Page<Product> result = productMapper.selectPage(page, wrapper);
```

#### 避免 N+1 查询
```java
// ❌ 禁止：循环查询
List<Product> products = productMapper.selectList(wrapper);
for (Product product : products) {
    User user = userMapper.selectById(product.getUserId());  // N+1问题
    // ...
}

// ✅ 推荐：使用 MyBatis XML 关联查询
<select id="selectProductWithUser" resultMap="ProductDetailMap">
    SELECT p.*, u.nick_name, u.avatar_url
    FROM product p
    LEFT JOIN user u ON p.user_id = u.id
    WHERE p.status = 1
</select>
```

### 缓存优化

#### 缓存穿透防护
```java
// 查询不存在的数据时缓存空值
String cacheKey = "user:info:" + userId;
String cached = redisTemplate.opsForValue().get(cacheKey);
if (cached != null) {
    return cached.equals("null") ? null : JSON.parseObject(cached, User.class);
}

User user = userMapper.selectById(userId);
if (user == null) {
    redisTemplate.opsForValue().set(cacheKey, "null", 5, TimeUnit.MINUTES);
    return null;
}

redisTemplate.opsForValue().set(cacheKey, JSON.toJSONString(user), 30, TimeUnit.MINUTES);
return user;
```

#### 缓存雪崩防护
```java
// 设置随机过期时间
int randomSeconds = ThreadLocalRandom.current().nextInt(60, 300);
redisTemplate.opsForValue().set(key, value, 
    30 * 60 + randomSeconds, TimeUnit.SECONDS);
```

### 异步处理

#### 浏览量更新
```java
@Async
public void incrementViewCount(Long productId) {
    productMapper.incrementViewCount(productId);
}
```

#### 通知发送
```java
@Async
public void send(Long userId, Integer type, String title, String content) {
    Notification notification = new Notification();
    // 设置字段
    notificationMapper.insert(notification);
}
```


---

## 🔧 常见问题（FAQ）

### 开发相关

**Q: 为什么使用 MyBatis-Plus 而不是原生 MyBatis？**  
A: MyBatis-Plus 提供了丰富的内置方法（CRUD、分页、条件构造器），大幅减少样板代码。简单的 CRUD 操作无需编写 XML，复杂查询仍可使用 XML。

**Q: 订单的 expire_time 和 confirm_deadline 有什么区别？**  
A: 
- `expire_time`：订单创建后72小时，用于超时自动取消（OrderExpireTask）
- `confirm_deadline`：订单创建后7天，用于自动确认收货（OrderAutoConfirmTask）

**Q: 为什么商品搜索不使用 FULLTEXT 索引？**  
A: 项目规模较小，LIKE 模糊匹配已满足需求。FULLTEXT 索引需要额外配置和维护成本，且对中文支持需要分词器。

**Q: 如何处理同一商品的并发下单？**  
A: 使用 Redis 分布式锁（`product:lock:{productId}`），锁定30秒。创建订单前检查是否已有待面交订单，确保同一商品同时只有一个待面交订单。

### 部署相关

**Q: 如何修改数据库连接信息？**  
A: 编辑 `src/main/resources/application.yml` 中的 `spring.datasource` 配置。

**Q: 如何配置微信小程序 AppID 和 AppSecret？**  
A: 编辑 `application.yml` 中的 `wx.appId` 和 `wx.appSecret`。

**Q: 文件上传路径如何配置？**  
A: 编辑 `application.yml` 中的 `upload.path`（本地存储路径）和 `upload.url-prefix`（访问URL前缀）。

**Q: 如何关闭某个定时任务？**  
A: 编辑 `application.yml` 中的 `task.enabled.xxx` 设置为 `false`。

### 业务相关

**Q: 用户综合评分如何计算？**  
A: 
1. 单次评价得分 = (商品描述相符 + 沟通态度 + 交易体验) / 3
2. 用户综合评分 = 所有收到的评价的单次评价得分的平均值
3. 保留一位小数，新用户默认5.0分

**Q: 自动好评的规则是什么？**  
A: 交易完成后7天内未评价，系统自动生成默认好评（三维度均为5分，`is_auto=1`）。

**Q: 商品发布后多久会自动下架？**  
A: 商品发布后90天自动下架（`auto_off_time`），由 `ProductAutoOffTask` 定时任务执行。

**Q: 账号注销后多久数据会被删除？**  
A: 当前代码实现为：申请注销后30天内可恢复；超过30天后由 `UserDeactivateTask` 执行“数据匿名化/清理”（清空敏感字段、昵称改为“已注销用户”、并将账号置为封禁状态），不会物理删除用户行数据。

---

## 📚 相关文档

### 项目文档
- [需求+架构文档](./需求+架构文档.md) - 完整的PRD和技术架构说明
- [开发计划](./plans/) - 功能开发计划和进度跟踪
- [功能列表](../feature_list.json) - 所有功能模块的详细定义

### 技术文档
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [MyBatis-Plus 官方文档](https://baomidou.com/)
- [uni-app 官方文档](https://uniapp.dcloud.net.cn/)
- [微信小程序开发文档](https://developers.weixin.qq.com/miniprogram/dev/framework/)

### 数据库设计
- 详细的表结构设计见 [需求+架构文档 - 第五章](./需求+架构文档.md#五数据结构设计)
- 包含所有表的字段定义、索引设计、枚举值说明

### 接口文档
- 详细的接口定义见 [需求+架构文档 - 第六章](./需求+架构文档.md#六接口设计)
- 包含请求参数、响应格式、业务规则说明

---

## 👥 开发团队与协作

### 双IDE协同开发模式

本项目采用独特的双IDE协同开发模式：

- **Trae IDE（执行者）**：负责编写业务代码和测试代码
- **Kiro IDE（监督者）**：负责代码审查、质量把控、验收决策

### 协作流程
1. 执行者从 `feature_list.json` 读取待开发功能
2. 执行者编写代码并运行测试
3. 执行者创建 `.ready-for-review` 信号文件
4. 监督者检测信号并进行多维度代码审查
5. 监督者独立复跑测试验证
6. 通过：修改 `passes: true`，创建 `.review-passed`
7. 驳回：给出具体修改意见，创建 `.review-rejected`

### 质量保障机制
- MyBatis-Plus 规范审查
- 功能正确性审查
- 安全性审查
- 代码质量审查
- 测试覆盖审查
- 数据库一致性审查
- 独立复跑验证

---

## 📄 许可证

本项目为毕业设计项目，仅供学习和参考使用。

---

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- 项目路径：`G:\Code\Graduation_project`
- 文档位置：`docs/`
- 任务跟踪：`tasks02.md`

---

**最后更新时间**：2026-03-15  
**文档版本**：V1.1  
**项目状态**：开发中

---

