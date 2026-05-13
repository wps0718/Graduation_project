# qingyuan_secondhand - Design Spec

> This document is the human-readable design narrative — rationale, audience, style, color choices, content outline. It is read once by downstream roles for context.
>
> The machine-readable execution contract lives in `spec_lock.md` (short form of color / typography / icon / image decisions). Executor re-reads `spec_lock.md` before every SVG page to resist context-compression drift. Keep the two files in sync; if they diverge, `spec_lock.md` wins.

## I. Project Information

| Item | Value |
| ---- | ----- |
| **Project Name** | 轻院二手交易平台 |
| **Canvas Format** | PPT 16:9 (1280x720) |
| **Page Count** | 14 |
| **Design Style** | B) General Consulting — 数据清晰优先 |
| **Target Audience** | 毕业设计答辩委员会（教授/导师） |
| **Use Case** | 项目答辩汇报，展示技术深度、创新点与完成度 |
| **Created Date** | 2026-05-10 |

---

## II. Canvas Specification

| Property | Value |
| -------- | ----- |
| **Format** | PPT 16:9 |
| **Dimensions** | 1280x720 |
| **viewBox** | `0 0 1280 720` |
| **Margins** | left/right 60px, top/bottom 50px |
| **Content Area** | 1160x620 (centered) |

---

## III. Visual Theme

### Theme Style

- **Style**: B) General Consulting
- **Theme**: 混合主题 — 浅色页为主（P1-P6, P8, P10, P12-P14），深色页用于技术密集场景（P7 IM, P9 安全, P11 缓存）
- **Tone**: 专业、清晰、数据驱动，适合技术答辩场景

### Color Scheme

| Role | HEX | Purpose |
| ---- | --- | ------- |
| **Background** | `#FFFFFF` | 浅色页面背景 |
| **Secondary bg** | `#F9FAFB` | 卡片背景、次要区域 |
| **Primary** | `#3B82F6` | 标题装饰、图标、强调色 |
| **Accent** | `#1E40AF` | 深色背景页、渐变深端 |
| **Secondary accent** | `#F59E0B` | 橙色辅助、数据层标识 |
| **Body text** | `#1F2937` | 主要正文 |
| **Secondary text** | `#6B7280` | 注释、说明 |
| **Tertiary text** | `#9CA3AF` | 页脚、补充信息 |
| **Border/divider** | `#E5E7EB` | 卡片边框、分隔线 |
| **Success** | `#10B981` | 后端标识、成功状态、绿色系 |
| **Warning** | `#EF4444` | 痛点/错误标识 |
| **Purple** | `#8B5CF6` | 小程序端标识、社交相关 |
| **Dark bg** | `#1E293B` | 深色页面背景（P7/P9/P11） |
| **Dark card** | `#334155` | 深色页卡片背景 |
| **Dark text** | `#F1F5F9` | 深色页文字 |

### Gradient Scheme

```xml
<!-- 主渐变（封面/P14） -->
<linearGradient id="primaryGrad" x1="0%" y1="0%" x2="100%" y2="100%">
  <stop offset="0%" stop-color="#3B82F6"/>
  <stop offset="100%" stop-color="#1E40AF"/>
</linearGradient>

<!-- 背景装饰渐变 -->
<radialGradient id="bgDecor" cx="80%" cy="20%" r="50%">
  <stop offset="0%" stop-color="#3B82F6" stop-opacity="0.15"/>
  <stop offset="100%" stop-color="#3B82F6" stop-opacity="0"/>
</radialGradient>
```

---

## IV. Typography System

### Font Plan

**Typography direction**: Modern CJK sans（现代中文无衬线）

| Role | Chinese | English | Fallback tail |
| ---- | ------- | ------- | ------------- |
| **Title** | `"Microsoft YaHei", "PingFang SC"` | `Arial` | `sans-serif` |
| **Body** | `"Microsoft YaHei", "PingFang SC"` | `Arial` | `sans-serif` |
| **Code** | — | `Consolas, "Courier New"` | `monospace` |

**Per-role font stacks**:

- Title: `"Microsoft YaHei", "PingFang SC", Arial, sans-serif`
- Body: `"Microsoft YaHei", "PingFang SC", Arial, sans-serif`
- Code: `Consolas, "Courier New", monospace`

### Font Size Hierarchy

**Baseline**: Body font size = 18px（内容密集型，每页6+要点）

| Purpose | Ratio to body | Current px | Weight |
| ------- | ------------- | ---------- | ------ |
| Cover title (hero headline) | 3.3x | 60px | Bold |
| Page title | 1.8x | 32px | Bold |
| Hero number (consulting KPIs) | 2.0x | 36px | Bold |
| Subtitle | 1.3x | 24px | SemiBold |
| **Body content** | **1x** | **18px** | Regular |
| Annotation / caption | 0.75x | 14px | Regular |
| Page number / footnote | 0.6x | 11px | Regular |

---

## V. Layout Principles

### Page Structure

- **Header area**: 顶部60px区域，含页面标题和装饰线
- **Content area**: 中间540px区域，主要内容
- **Footer area**: 底部40px区域，含页码和版本信息

### Layout Pattern Library

| Pattern | Suitable Scenarios | Used In |
| ------- | ----------------- | ------- |
| **Single column centered** | Cover, 总结页 | P1, P14 |
| **Asymmetric split (3:7 / 2:8)** | 左文字右图表 | P2, P5, P13 |
| **Three-column cards** | 功能卡片、并列要点 | P4, P12 |
| **Matrix grid (2×2)** | 四象限分类 | P7 |
| **Top-bottom split** | 流程图、时间轴 | P6, P10 |
| **Center-radiating** | 防护体系、核心概念 | P9 |
| **Symmetric split (5:5)** | 对称比较 | P8, P11 |
| **Vertical list** | 技术架构层级 | P3 |

### Spacing Specification

**Universal**:

| Element | Current Project |
| ------- | --------------- |
| Safe margin from canvas edge | 40px |
| Content block gap | 24px |
| Icon-text gap | 10px |

**Card-based layouts**:

| Element | Current Project |
| ------- | --------------- |
| Card gap | 16px |
| Card padding | 20px |
| Card border radius | 12px |
| Single-row card height | 280px |
| Double-row card height | 240px |
| Three-column card width | 340px |

---

## VI. Icon Usage Specification

### Source

- **Built-in icon library**: `tabler-filled`（圆润曲线风格，适合校园/技术展示）
- **Brand library**: `simple-icons`（Vue、Spring等技术栈品牌图标，按需使用）
- **Usage method**: SVG placeholder `<use data-icon="tabler-filled/icon-name" .../>`

### Recommended Icon List

| Purpose | Icon Path | Page |
| ------- | --------- | ---- |
| 项目类型 | `tabler-filled/school` | P1 |
| 技术栈 | `tabler-filled/code` | P1 |
| 进度 | `tabler-filled/chart-pie` | P1 |
| 痛点-信息 | `tabler-filled/message-circle` | P2 |
| 痛点-信任 | `tabler-filled/shield-x` | P2 |
| 痛点-效率 | `tabler-filled/clock` | P2 |
| 痛点-浪费 | `tabler-filled/trash` | P2 |
| 小程序端 | `tabler-filled/device-mobile` | P3 |
| 后端服务 | `tabler-filled/server` | P3 |
| 数据层 | `tabler-filled/database` | P3 |
| JWT认证 | `tabler-filled/shield-lock` | P3 |
| WebSocket | `tabler-filled/message` | P3 |
| 缓存 | `tabler-filled/bolt` | P3 |
| 定时任务 | `tabler-filled/clock` | P3 |
| 分布式锁 | `tabler-filled/lock` | P3 |
| 密码加密 | `tabler-filled/key` | P3 |
| 微信登录 | `tabler-filled/login` | P4 |
| 校园认证 | `tabler-filled/certificate` | P4 |
| 商品发布 | `tabler-filled/upload` | P4 |
| 搜索 | `tabler-filled/search` | P4 |
| 聊天 | `tabler-filled/messages` | P4 |
| 订单 | `tabler-filled/clipboard-check` | P4 |
| 评价 | `tabler-filled/star` | P4 |
| 收藏 | `tabler-filled/heart` | P4 |
| 通知 | `tabler-filled/bell` | P4 |
| 审核 | `tabler-filled/clipboard-check` | P5 |
| 用户管理 | `tabler-filled/users` | P5 |
| 订单管理 | `tabler-filled/clipboard-list` | P5 |
| 配置 | `tabler-filled/settings` | P5 |
| 安全盾牌 | `tabler-filled/shield-check` | P9 |
| 定时任务 | `tabler-filled/calendar` | P10 |
| 缓存 | `tabler-filled/database` | P11 |
| 性能 | `tabler-filled/gauge` | P13 |
| 测试 | `tabler-filled/test-pipe` | P13 |
| 总结 | `tabler-filled/sparkles` | P14 |
| 展望 | `tabler-filled/rocket` | P14 |
| 品牌-Vue | `simple-icons/vuedotjs` | P3 |
| 品牌-Spring | `simple-icons/springboot` | P3 |
| 品牌-MySQL | `simple-icons/mysql` | P3 |
| 品牌-Redis | `simple-icons/redis` | P3 |

---

## VII. Visualization Reference List

**Catalog read**: 70 templates / 10 categories

**Runners-up considered**:
- `vertical_list` (rejected: P3架构更适合`layered_architecture`的层级视觉)
- `process_flow` (rejected: P6交易流程用`timeline`更符合时间轴叙事)
- `mind_map` (rejected: P9安全用`concentric_circles`更符合防护层概念)

| Visualization Type | Reference Template | Used In |
| ------------------ | ------------------ | ------- |
| bar_chart | `templates/charts/bar_chart.svg` | P2 (痛点数据柱状图) |
| layered_architecture | `templates/charts/layered_architecture.svg` | P3 (三层技术架构) |
| icon_grid | `templates/charts/icon_grid.svg` | P4 (9功能卡片网格) |
| kpi_cards | `templates/charts/kpi_cards.svg` | P5 (4指标卡片), P12 (三端完成度) |
| line_chart | `templates/charts/line_chart.svg` | P5 (7日趋势折线图) |
| donut_chart | `templates/charts/donut_chart.svg` | P5 (分类分布饼图) |
| timeline | `templates/charts/timeline.svg` | P6 (交易7步流程), P10 (定时任务时间轴) |
| client_server_flow | `templates/charts/client_server_flow.svg` | P7 (IM架构流程图) |
| org_chart | `templates/charts/org_chart.svg` | P8 (ER关系图) |
| concentric_circles | `templates/charts/concentric_circles.svg` | P9 (七层安全防护) |
| vertical_list | `templates/charts/vertical_list.svg` | P11 (缓存Key树) |
| progress_bar_chart | `templates/charts/progress_bar_chart.svg` | P12 (三端环形进度) |
| pyramid_chart | `templates/charts/pyramid_chart.svg` | P13 (测试金字塔) |
| roadmap_vertical | `templates/charts/roadmap_vertical.svg` | P14 (未来路线图) |

---

## VIII. Image Resource List

> 以下为需要用户后续手动插入的图片占位符。每张图片在SVG中以虚线边框矩形标注，用户可在PowerPoint中直接替换为实际图片。

| Filename | Dimensions | Ratio | Purpose | Type | Status | Placement Description |
| -------- | --------- | ----- | ------- | ---- | ------ | --------------------- |
| hero_cover.png | 1280x720 | 1.78 | 封面背景 | Background | Placeholder | P1封面：全屏背景图，展示校园/二手交易场景，建议拍摄校园实景或产品展示墙，图片会被半透明蓝色渐变覆盖 |
| campus_students.png | 400x300 | 1.33 | 目标用户 | Photography | Placeholder | P2左侧：校园学生群体照片，3-5名学生手持手机/浏览商品的场景，展示目标用户画像 |
| admin_dashboard.png | 700x400 | 1.75 | 管理后台截图 | Diagram | Placeholder | P5右侧：管理后台数据概览页面截图，展示ECharts图表和核心数据指标的完整界面 |
| chat_interface.png | 400x500 | 0.80 | IM聊天界面 | Diagram | Placeholder | P7下方：小程序聊天界面截图，展示商品卡片消息、文字消息、未读提示等IM交互场景 |
| miniapp_screenshots.png | 700x350 | 2.00 | 小程序多页截图拼图 | Diagram | Placeholder | P12下方：3-4张小程序核心页面截图拼图（首页、商品详情、订单页、个人中心），展示最终产品形态 |
| future_vision.png | 1280x400 | 3.20 | 未来展望背景 | Illustration | Placeholder | P14下方：科技感/未来感插画，展示校园生态、多校区互联、数字化交易的愿景概念图 |

### 图片需求说明

**必填图片（建议优先准备）：**
1. **hero_cover.png** — 封面背景：校园场景或产品展示，16:9横版，分辨率≥1920x1080
2. **admin_dashboard.png** — 管理后台截图：截取实际管理后台的数据概览页面
3. **miniapp_screenshots.png** — 小程序截图：从微信开发者工具截取3-4个核心页面

**建议图片（增强展示效果）：**
4. **campus_students.png** — 校园学生照片：真实校园场景或学生使用手机的照片
5. **chat_interface.png** — 聊天界面截图：从实际小程序截取对话页面
6. **future_vision.png** — 未来展望插画：可使用免费插画素材或AI生成

> **占位符规格**：SVG中用浅灰色(#E5E7EB)虚线矩形标注，内含"待插入图片"文字和建议尺寸。用户在PowerPoint中可直接删除矩形并插入实际图片。

---

## IX. Content Outline

### Part 1: 项目介绍（P1-P2）

#### Slide 01 - 封面

- **Layout**: 全屏渐变背景 + 居中标题
- **Rhythm**: anchor
- **Title**: 轻院二手交易平台
- **Subtitle**: 校园闲置物品流转解决方案
- **Content**:
  - 三个信息卡片：项目类型（毕业设计小程序）、技术栈（Spring+Vue3）、开发状态（核心完成98%）
  - 底部：版本V1.9 | 更新:2026-05
- **Visualization**: kpi_cards（三个信息卡片）
- **Image**: hero_cover.png（封面背景，可选）

#### Slide 02 - 项目背景与痛点

- **Layout**: 左30%文字 + 右70%卡片矩阵
- **Rhythm**: dense
- **Title**: 为什么做这个项目？
- **Content**:
  - 左侧：目标用户 5000+ 潜在用户
  - 右侧2×2痛点矩阵：信息分散(78%)、信任缺失(65%)、效率低下(52%)、毕业浪费(43%)
  - 底部柱状图
- **Visualization**: bar_chart（痛点数据对比）
- **Image**: campus_students.png（左下角用户画像）

### Part 2: 技术方案（P3-P5）

#### Slide 03 - 技术架构

- **Layout**: 垂直流式架构图 + 底部标签云
- **Rhythm**: anchor
- **Title**: 技术选型与系统架构
- **Content**:
  - 三层架构：小程序端(蓝) → 后端服务(绿) → 数据层(橙)
  - 6个技术标签：JWT认证、WebSocket通讯、缓存优化、定时任务、分布式锁、密码加密
- **Visualization**: layered_architecture

#### Slide 04 - 核心功能（一）用户端

- **Layout**: 3×3网格矩阵
- **Rhythm**: dense
- **Title**: 用户端核心功能
- **Content**:
  - 9个功能卡片：微信登录、校园认证、商品发布、智能搜索、即时通讯、订单管理、互评系统、收藏关注、消息中心
  - 底部：✓ 9大核心模块 | 42个API接口 | 96%完成
- **Visualization**: icon_grid

#### Slide 05 - 核心功能（二）管理端

- **Layout**: 左40%功能列表 + 右60%数据面板
- **Rhythm**: dense
- **Title**: 管理端功能与数据概览
- **Content**:
  - 左侧三模块：审核、管理、配置
  - 右侧四指标：用户5,247↑12.5% | 商品1,839↑8.3% | 订单892↑15.7% | 评价654↑6.2%
  - 图表：7日趋势、分类饼图、校区柱状图
- **Visualization**: kpi_cards + line_chart + donut_chart
- **Image**: admin_dashboard.png（右侧数据面板区域）

### Part 3: 核心设计（P6-P8）

#### Slide 06 - 交易流程设计

- **Layout**: 垂直时间轴
- **Rhythm**: breathing
- **Title**: 完整交易闭环流程
- **Content**:
  - 7个节点：我想要→协商→创建订单→卖家接单→双向确认→互评→完成
  - 底部：平均周期3.5天 | 完成率87% | 好评率92% | 重复率34.6%
- **Visualization**: timeline

#### Slide 07 - 即时通讯(IM)实现

- **Layout**: 深色背景，上方架构图 + 下方2×2特性网格
- **Rhythm**: dense
- **Title**: 自建WebSocket即时通讯方案
- **Content**:
  - 架构：客户端→WebSocket Server→分发器→Handler
  - 4特性：握手鉴权、消息持久化、心跳保活、离线消息
  - 底部：并发500+ | 延迟<50ms | 推送成功率99.2%
- **Visualization**: client_server_flow
- **Image**: chat_interface.png（架构图下方展示实际聊天界面）

#### Slide 08 - 数据库设计

- **Layout**: 左50% ER图 + 右50%表详情
- **Rhythm**: dense
- **Title**: 数据库设计与核心表结构
- **Content**:
  - ER关系：user→product(1:N)、user→trade_order(1:N)、trade_order→review(1:2)
  - 核心表：user(15字段)、trade_order(17字段双向确认)、campus_auth+history
  - 概览：22张表、286字段、45索引、125MB
- **Visualization**: org_chart

### Part 4: 技术亮点（P9-P11）

#### Slide 09 - 安全机制

- **Layout**: 深色背景，盾牌中心 + 七层防护环
- **Rhythm**: anchor
- **Title**: 七层安全防护体系
- **Content**:
  - 七层：JWT认证→BCrypt加密→防暴力破解→SQL注入防护→XSS防护→分布式锁→越权防护
  - 底部：拦截SQL注入0次 | XSS 0次 | 异常登录23次 | 安全评分A+ 98.5分
- **Visualization**: concentric_circles

#### Slide 10 - 定时任务调度

- **Layout**: 水平时间轴 + 任务卡片
- **Rhythm**: dense
- **Title**: 7大自动化定时任务
- **Content**:
  - 7个任务：订单超时(5分钟)、自动确认(2:00)、自动好评(3:00)、评价提醒(10:00)、商品下架(4:00)、账号清理(5:00)、心跳(30秒)
  - 底部：全部运行中 | 今日328次 | 成功率99.7%
- **Visualization**: timeline

#### Slide 11 - 缓存优化策略

- **Layout**: 深色背景，左40%缓存Key树 + 右60%策略详情
- **Rhythm**: dense
- **Title**: Redis多级缓存优化策略
- **Content**:
  - 左侧四类缓存：业务/会话/安全/配置
  - 右侧策略：Cache-Aside、防穿透、防雪崩、主动失效
  - 底部：命中率87.3% | Redis 128MB/512MB | Key 5,247
- **Visualization**: vertical_list

### Part 5: 项目成果（P12-P13）

#### Slide 12 - 项目进度与成果

- **Layout**: 三环形进度条 + 里程碑 + 成果矩阵
- **Rhythm**: dense
- **Title**: 开发完成度与核心成果
- **Content**:
  - 三端完成度：后端98% | 管理端99% | 小程序端96%
  - 里程碑：2026-02基础→V1.1 IM→V1.6认证→V1.9双向确认
  - 6个成果卡片
  - 底部：22表 | 286字段 | 45索引 | 42API | 43,613行 | 80%测试
- **Visualization**: progress_bar_chart + roadmap_vertical
- **Image**: miniapp_screenshots.png（展示实际产品截图）

#### Slide 13 - 性能与测试质量保障

- **Layout**: 左50%性能优化 + 右50%测试规范
- **Rhythm**: dense
- **Title**: 性能优化与测试质量保障
- **Content**:
  - 左侧：数据库优化、缓存优化(87%命中)、异步处理、综合指标(8ms/1200+QPS/500+并发)
  - 右侧：测试金字塔(单元75%/集成20%/E2E 5%)、覆盖率78%、123用例100%通过
- **Visualization**: pyramid_chart + kpi_cards

### Part 6: 总结展望（P14）

#### Slide 14 - 总结与展望

- **Layout**: 渐变深蓝到浅蓝背景，三卡片 + 路线图 + 致谢
- **Rhythm**: anchor
- **Title**: 项目总结与未来展望
- **Content**:
  - 三亮点卡片：技术(自建IM/七层安全/缓存)、业务(校园定位/交易闭环/信任体系)、创新(双向确认/认证历史/多级评论)
  - 路线图：短期(OSS/微信支付)→中期(ES搜索/RabbitMQ/推荐算法)→长期(大数据/AI客服/多校区)
  - 致谢：导师指导、团队协作、开源社区
  - 结束语：Thank You! 谢谢观看 | Q&A环节
- **Visualization**: roadmap_vertical
- **Image**: future_vision.png（路线图区域背景，可选）

---

## X. Speaker Notes Requirements

- **File naming**: Match SVG names, e.g., `01_cover.md`, `02_background.md`
- **Content includes**: 演讲要点、时间控制提示、过渡语句
- **Total duration**: 约15-20分钟
- **Notes style**: formal（正式答辩风格）
- **Presentation purpose**: report（汇报展示）

---

## XI. Technical Constraints Reminder

### SVG Generation Must Follow:

1. viewBox: `0 0 1280 720`
2. Background uses `<rect>` elements
3. Text wrapping uses `<tspan>` (`<foreignObject>` FORBIDDEN)
4. Transparency uses `fill-opacity` / `stroke-opacity`; `rgba()` FORBIDDEN
5. FORBIDDEN: `mask`, `<style>`, `class`, `foreignObject`
6. FORBIDDEN: `textPath`, `animate*`, `script`
7. Text characters: write typography & symbols as raw Unicode (em dash `—`, en dash `–`, `©`, `®`, `→`, NBSP, etc.); HTML named entities (`&nbsp;`, `&mdash;`, `&copy;`, `&reg;` …) are FORBIDDEN
8. `marker-start` / `marker-end` conditionally allowed: `<marker>` must be in `<defs>`, `orient="auto"`, shape must be triangle / diamond / circle
9. `clipPath` conditionally allowed **only on `<image>` elements**

### PPT Compatibility Rules:

- `<g opacity="...">` FORBIDDEN (group opacity); set on each child element individually
- Image transparency uses overlay mask layer
- Inline styles only; external CSS and `@font-face` FORBIDDEN
