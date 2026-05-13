# 轻院二手交易平台 - Design Spec

> 毕业答辩 PPT 设计规格 — 5 分钟 / 10 页
> The machine-readable execution contract lives in `spec_lock.md`. If they diverge, `spec_lock.md` wins.

## I. Project Information

| Item | Value |
| ---- | ----- |
| **Project Name** | 轻院二手交易平台 |
| **Canvas Format** | PPT 16:9 (1280×720) |
| **Page Count** | 10 |
| **Design Style** | General Consulting (数据清晰、结构化) |
| **Target Audience** | 毕业答辩委员会（教授/评委） |
| **Use Case** | 5 分钟毕业设计答辩演示 |
| **Created Date** | 2026-05-10 |

---

## II. Canvas Specification

| Property | Value |
| -------- | ----- |
| **Format** | PPT 16:9 |
| **Dimensions** | 1280×720 |
| **viewBox** | `0 0 1280 720` |
| **Margins** | left/right 60px, top/bottom 50px |
| **Content Area** | 1160×620 |

---

## III. Visual Theme

### Theme Style

- **Style**: General Consulting
- **Theme**: Light theme（白底蓝调）
- **Tone**: 技术感、专业、清晰

### Color Scheme

| Role | HEX | Purpose |
| ---- | --- | ------- |
| **Background** | `#FFFFFF` | 页面背景 |
| **Secondary bg** | `#F5F7FA` | 卡片背景、区块底色 |
| **Primary** | `#1565C0` | 主标题、核心图标、关键区块 |
| **Accent** | `#FF6F00` | 数据高亮、重点强调 |
| **Secondary accent** | `#0D47A1` | 渐变补充、深蓝装饰 |
| **Body text** | `#1A1A2E` | 正文内容 |
| **Secondary text** | `#5A5A7A` | 辅助说明、标注 |
| **Tertiary text** | `#9E9E9E` | 页脚、页码 |
| **Border/divider** | `#E0E0E0` | 卡片边框、分割线 |
| **Success** | `#2E7D32` | 完成状态、正面指标 |
| **Warning** | `#C62828` | 问题标记、风险提示 |

### Gradient Scheme

```xml
<linearGradient id="titleGradient" x1="0%" y1="0%" x2="100%" y2="100%">
  <stop offset="0%" stop-color="#1565C0"/>
  <stop offset="100%" stop-color="#0D47A1"/>
</linearGradient>

<linearGradient id="accentGradient" x1="0%" y1="0%" x2="100%" y2="0%">
  <stop offset="0%" stop-color="#FF6F00"/>
  <stop offset="100%" stop-color="#FF8F00"/>
</linearGradient>

<radialGradient id="bgDecor" cx="90%" cy="10%" r="40%">
  <stop offset="0%" stop-color="#1565C0" stop-opacity="0.08"/>
  <stop offset="100%" stop-color="#1565C0" stop-opacity="0"/>
</radialGradient>
```

---

## IV. Typography System

### Font Plan

**Typography direction**: modern CJK sans — 以微软雅黑为主体，技术文档感强。

| Role | Chinese | English | Fallback tail |
| ---- | ------- | ------- | ------------- |
| **Title** | `"Microsoft YaHei"` | `Arial` | `sans-serif` |
| **Body** | `"Microsoft YaHei"` | `Arial` | `sans-serif` |
| **Emphasis** | — | — | same as Body |
| **Code** | — | `Consolas` | `monospace` |

**Per-role font stacks**:

- Title: `"Microsoft YaHei", Arial, sans-serif`
- Body: `"Microsoft YaHei", Arial, sans-serif`
- Emphasis: same as Body
- Code: `Consolas, "Courier New", monospace`

### Font Size Hierarchy

**Baseline**: Body font size = 20px（中等密度，技术展示）

| Purpose | Ratio to body | Size | Weight |
| ------- | ------------- | ---- | ------ |
| Cover title (hero headline) | 3x | 60px | Bold |
| Page title | 1.6x | 32px | Bold |
| Hero number (KPI) | 2x | 40px | Bold |
| Subtitle | 1.2x | 24px | SemiBold |
| **Body content** | **1x** | **20px** | Regular |
| Annotation / caption | 0.75x | 15px | Regular |
| Page number / footnote | 0.6x | 12px | Regular |

---

## V. Layout Principles

### Page Structure

- **Header area**: 顶部标题区，高度 80-100px，含页标题 + 装饰线
- **Content area**: 中部内容区，高度 500-540px
- **Footer area**: 底部 30px，含页码

### Layout Pattern Library

| Pattern | Used In |
| ------- | ------- |
| **Single column centered** | P01 封面、P10 致谢 |
| **Asymmetric split (3:7)** | P02 痛点、P03 架构、P07 技术亮点 |
| **Three-column cards** | P04 用户体系、P05 交易闭环、P09 项目总结 |
| **Top-bottom split** | P06 IM 架构 |
| **Full-bleed + floating** | P08 系统演示 |

### Spacing Specification

**Universal**:

| Element | Value |
| ------- | ----- |
| Safe margin from canvas edge | 50px |
| Content block gap | 30px |
| Icon-text gap | 12px |

**Card-based layouts**:

| Element | Value |
| ------- | ----- |
| Card gap | 24px |
| Card padding | 24px |
| Card border radius | 12px |

---

## VI. Icon Usage Specification

### Source

- **Built-in icon library**: `tabler-filled`
- **Usage method**: SVG placeholder `<use data-icon="tabler-filled/icon-name" .../>`

### Icon Inventory

| Purpose | Icon Path | Page |
| ------- | --------- | ---- |
| 首页/项目 | `tabler-filled/home` | P01 |
| 痛点标记 | `tabler-filled/alert-triangle` | P02 |
| 代码/技术 | `tabler-filled/code` | P03 |
| 数据库 | `tabler-filled/database` | P03 |
| 手机 | `tabler-filled/device-mobile` | P03 |
| 用户 | `tabler-filled/users` | P04 |
| 盾牌/安全 | `tabler-filled/shield-lock` | P04 |
| 购物车/交易 | `tabler-filled/shopping-cart` | P05 |
| 消息 | `tabler-filled/messages` | P06 |
| 时钟/定时 | `tabler-filled/clock-hour-1` | P07 |
| 设置/配置 | `tabler-filled/settings` | P07 |
| 图表 | `tabler-filled/chart-bar` | P09 |
| 检查/完成 | `tabler-filled/check` | P09 |
| 星星/评价 | `tabler-filled/star` | P05 |

---

## VII. Visualization Reference List

**Read-audit**:

```
Catalog read: 70 templates / 10 categories
Runners-up considered:
- layered_architecture (used for P03, best match for three-tier system diagram)
- process_flow (considered for P05 but transaction flow is better as numbered_steps)
- kpi_cards (used for P09, best for completion metrics display)
- icon_grid (used for P04, best for feature overview with icons)
```

| Visualization Type | Reference Template | Used In |
| ------------------ | ------------------ | ------- |
| layered_architecture | `templates/charts/layered_architecture.svg` | P03 技术架构 |
| icon_grid | `templates/charts/icon_grid.svg` | P04 核心功能 |
| numbered_steps | `templates/charts/numbered_steps.svg` | P05 交易闭环 |
| kpi_cards | `templates/charts/kpi_cards.svg` | P09 项目总结 |
| client_server_flow | `templates/charts/client_server_flow.svg` | P06 IM 通讯 |

---

## VIII. Image Resource List

本 PPT 不使用 AI 生成图片，所有视觉元素通过 SVG 矢量图和图标实现。

---

## IX. Content Outline

### Slide 01 - 封面

- **Layout**: Single column centered（anchor）
- **Title**: 轻院二手交易平台
- **Subtitle**: 校园二手交易微信小程序 — 毕业设计答辩
- **Info**: 广东轻工职业技术大学 | 姓名 | 2026 年 5 月
- **Rhythm**: anchor

### Slide 02 - 项目背景与痛点

- **Layout**: Asymmetric split (2:8) — 左侧痛点列表，右侧核心定位
- **Title**: 项目背景与痛点
- **Content**:
  - 痛点 1: 信息分散 — 依赖 QQ/微信群，信息零散难检索
  - 痛点 2: 信任缺失 — 无信用体系，质量无法保障
  - 痛点 3: 效率低下 — 缺乏沟通和交易管理工具
  - 痛点 4: 毕业浪费 — 大量物品被丢弃，缺乏处理渠道
  - 底部定位条: "专为广东轻工职业技术大学打造的校园二手交易微信小程序"
- **Rhythm**: dense

### Slide 03 - 技术架构总览

- **Layout**: Asymmetric split (3:7) — 左侧架构图（三端分层），右侧技术栈列表
- **Title**: 技术架构
- **Visualization**: layered_architecture
- **Content**:
  - 后端: Spring Boot 3.3.7 + MyBatis-Plus + MySQL + Redis
  - 小程序: uni-app + Vue 3 + Pinia
  - 管理后台: Vue 3 + Vite + Element Plus + ECharts
  - 通讯: WebSocket 自建 IM
- **Rhythm**: dense

### Slide 04 - 核心功能：用户体系

- **Layout**: Three-column cards — 每列一个功能模块
- **Title**: 用户体系
- **Content**:
  - Card 1: 多方式登录 — 微信登录 / 手机号+密码 / 短信验证，JWT Token 认证
  - Card 2: 校园认证 — 上传一卡通/教务截图，后台人工审核，认证历史全轨迹回溯
  - Card 3: 安全机制 — BCrypt 密码加密、Redis 登录锁定、手机号脱敏、防越权
- **Rhythm**: dense

### Slide 05 - 核心功能：交易闭环

- **Layout**: numbered_steps（横向流程图 5 步）
- **Title**: 交易闭环
- **Visualization**: numbered_steps
- **Content**:
  - Step 1: 商品发布 — 图片上传、信息填写、审核上架
  - Step 2: IM 沟通 — 「我想要」发起聊天、实时消息
  - Step 3: 下单交易 — Redis 分布式锁防并发、72h 超时机制
  - Step 4: 双向确认 — 卖家确认交付 + 买家确认收货，双方均确认才完成
  - Step 5: 互评体系 — 三维度评分、自动好评、综合评分
- **Rhythm**: dense

### Slide 06 - IM 即时通讯架构

- **Layout**: Top-bottom split — 上方 WebSocket 架构图，下方核心特性
- **Title**: IM 即时通讯
- **Visualization**: client_server_flow
- **Content**:
  - 架构: 握手鉴权 → 消息分发 → 处理器链（Chat / Read / Ping）
  - 核心特性: 心跳保活（30s）、未读管理（Redis）、已读回执、离线通知、业务卡片、踢下线
- **Rhythm**: breathing

### Slide 07 - 关键技术亮点

- **Layout**: Three-column cards — 每列一个技术点
- **Title**: 关键技术亮点
- **Content**:
  - Card 1: Redis 分布式锁 — 订单创建防并发（product:lock:{id}, TTL 30s）
  - Card 2: 定时任务体系 — 6 个 @Scheduled 任务，可配置开关，覆盖订单/评价/商品/用户全生命周期
  - Card 3: 多级缓存策略 — Cache-Aside 模式、防穿透（缓存空值）、防雪崩（随机 TTL）
- **Rhythm**: dense

### Slide 08 - 系统演示

- **Layout**: Full-bleed — 展示小程序和管理后台界面截图区域
- **Title**: 系统演示
- **Content**:
  - 小程序端界面展示区域（占位矩形）
  - 管理后台界面展示区域（占位矩形）
  - 底部说明: 小程序 + 管理后台双端完整实现
- **Rhythm**: breathing

### Slide 09 - 项目总结与成果

- **Layout**: kpi_cards（上方 4 个 KPI 卡片）+ 底部功能清单
- **Title**: 项目总结
- **Visualization**: kpi_cards
- **Content**:
  - KPI 1: 后端完成度 98%
  - KPI 2: 管理后台 99%
  - KPI 3: 小程序端 96%
  - KPI 4: 核心功能 20+ 项
  - 底部: 登录注册 / 校园认证 / 商品管理 / IM 通讯 / 订单交易 / 评价系统 / 举报处理 / 后台管理
- **Rhythm**: dense

### Slide 10 - 致谢

- **Layout**: Single column centered（anchor）
- **Title**: 感谢各位老师的指导与聆听
- **Content**: 项目名称 + 姓名 + 导师 + 日期
- **Rhythm**: anchor

---

## X. Speaker Notes Requirements

- **File naming**: Match SVG names (`01_cover.svg` → `notes/01_cover.md`)
- **Duration**: 5 分钟总计（每页约 30 秒）
- **Style**: formal / 答辩汇报风格
- **Purpose**: inform + report

---

## XI. Technical Constraints Reminder

### SVG Generation Must Follow:

1. viewBox: `0 0 1280 720`
2. Background uses `<rect>` elements
3. Text wrapping uses `<tspan>` (`<foreignObject>` FORBIDDEN)
4. Transparency uses `fill-opacity` / `stroke-opacity`; `rgba()` FORBIDDEN
5. FORBIDDEN: `mask`, `<style>`, `class`, `foreignObject`
6. FORBIDDEN: `textPath`, `animate*`, `script`
7. Text characters: write as raw Unicode; HTML named entities FORBIDDEN
8. `clipPath` conditionally allowed only on `<image>` elements

### PPT Compatibility Rules:

- `<g opacity="...">` FORBIDDEN — set on each child element individually
- Image transparency uses overlay mask layer
- Inline styles only; external CSS and `@font-face` FORBIDDEN
