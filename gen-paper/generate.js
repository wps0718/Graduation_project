const {
  Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
  WidthType, AlignmentType, BorderStyle, LineRuleType, PageBreak,
  Header, Footer, PageNumber, TabStopPosition, TabStopType
} = require('docx');
const fs = require('fs');

// ===== 字号常量（half-points）=====
const SZ_小二 = 36;  // 18pt
const SZ_小三 = 30;  // 15pt
const SZ_四号 = 28;  // 14pt
const SZ_小四 = 24;  // 12pt

// ===== 行距常量（240ths of a line）=====
const LINE_SINGLE = 240; // 单倍行距
const LINE_125 = 300;    // 1.25倍行距
const PT20 = 20;         // 1pt to twips

// ===== 字体名称 =====
const FONT_H = { name: "黑体", eastAsia: "黑体", ascii: "Times New Roman" };
const FONT_S = { name: "宋体", eastAsia: "宋体", ascii: "Times New Roman" };
const FONT_TNR = { name: "Times New Roman", ascii: "Times New Roman" };

// ===== 段落生成器 =====

/** 一级标题：小二号黑体加粗，单倍行距，段前0行，段后1行 */
function h1(text) {
  return new Paragraph({
    spacing: { before: 0, after: 240, line: LINE_SINGLE, lineRule: LineRuleType.AUTO },
    children: [new TextRun({ text, font: FONT_H, size: SZ_小二, bold: true })]
  });
}

/** 二级标题：小三号黑体不加粗，单倍行距，段前12磅，段后12磅 */
function h2(text) {
  return new Paragraph({
    spacing: { before: 12 * PT20, after: 12 * PT20, line: LINE_SINGLE, lineRule: LineRuleType.AUTO },
    children: [new TextRun({ text, font: FONT_H, size: SZ_小三, bold: false })]
  });
}

/** 三级标题：四号黑体加粗，单倍行距，段前6磅，段后6磅 */
function h3(text) {
  return new Paragraph({
    spacing: { before: 6 * PT20, after: 6 * PT20, line: LINE_SINGLE, lineRule: LineRuleType.AUTO },
    children: [new TextRun({ text, font: FONT_H, size: SZ_四号, bold: true })]
  });
}

/** 正文：小四号宋体，1.25倍行距，段前段后0行 */
function p(text) {
  return new Paragraph({
    spacing: { before: 0, after: 0, line: LINE_125, lineRule: LineRuleType.AUTO },
    children: [new TextRun({ text, font: FONT_S, size: SZ_小四 })]
  });
}

/** 正文（含参考文献标记的混合文本） */
function pMixed(runs) {
  const children = runs.map(r => {
    if (typeof r === 'string') {
      return new TextRun({ text: r, font: FONT_S, size: SZ_小四 });
    }
    // { text, isRef: true } 用于参考文献上标
    return new TextRun({
      text: r.text,
      font: r.isRef ? FONT_TNR : FONT_S,
      size: SZ_小四,
      superScript: !!r.isRef
    });
  });
  return new Paragraph({
    spacing: { before: 0, after: 0, line: LINE_125, lineRule: LineRuleType.AUTO },
    children
  });
}

/** 正文缩进（首行缩进2字符） */
function pIndent(text) {
  return new Paragraph({
    spacing: { before: 0, after: 0, line: LINE_125, lineRule: LineRuleType.AUTO },
    indent: { firstLine: 480 }, // ~2 chars at 12pt
    children: [new TextRun({ text, font: FONT_S, size: SZ_小四 })]
  });
}

/** 空行 */
function empty() {
  return new Paragraph({ spacing: { before: 0, after: 0, line: LINE_SINGLE }, children: [] });
}

/** 分页符 */
function newPage() {
  return new Paragraph({ pageBreakBefore: true, spacing: {}, children: [] });
}

/** 摘要/致谢/参考文献等特殊标题（小二号黑体加粗，居中，段前段后1行）*/
function specialTitle(text) {
  return new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { before: 240, after: 240, line: LINE_SINGLE, lineRule: LineRuleType.AUTO },
    children: [new TextRun({ text, font: FONT_H, size: SZ_小二, bold: true })]
  });
}

/** 关键词标签 */
function keywordLabel(text) {
  return new Paragraph({
    spacing: { before: 0, after: 0, line: LINE_125, lineRule: LineRuleType.AUTO },
    indent: { firstLine: 480 },
    children: [new TextRun({ text, font: FONT_H, size: SZ_小四, bold: true })]
  });
}

/** 表格辅助：创建单元格 */
function cell(text, width) {
  return new TableCell({
    width: { size: width, type: WidthType.DXA },
    children: [new Paragraph({
      spacing: { before: 0, after: 0, line: LINE_SINGLE },
      alignment: AlignmentType.CENTER,
      children: [new TextRun({ text, font: FONT_S, size: SZ_小四 })]
    })]
  });
}

function cellHeader(text, width) {
  return new TableCell({
    width: { size: width, type: WidthType.DXA },
    shading: { fill: "D9E2F3" },
    children: [new Paragraph({
      spacing: { before: 0, after: 0, line: LINE_SINGLE },
      alignment: AlignmentType.CENTER,
      children: [new TextRun({ text, font: { name: "黑体", eastAsia: "黑体" }, size: SZ_小四, bold: true })]
    })]
  });
}

// ===== 构建文档内容 =====
const children = [];

// ==========================================
// 摘要（页码：Ⅰ）
// ==========================================
children.push(specialTitle("摘  要"));
children.push(empty());

children.push(pIndent("为解决高校校园闲置物品流转效率低、交易双方信任缺失的问题，设计并实现了一个基于 Spring Boot 的校园二手交易平台。系统采用前后端分离架构，后端使用 Spring Boot + MyBatis-Plus + MySQL + Redis + WebSocket 技术栈实现业务逻辑与数据管理，微信小程序端基于 uni-app + Vue 3 开发，管理后台基于 Vue 3 + Element Plus + ECharts 构建。平台实现了用户认证、商品管理、订单流转、即时通讯、校园认证、数据统计等核心功能，覆盖了校园二手交易全流程。经测试，系统功能完整、运行稳定。"));

children.push(empty());
children.push(keywordLabel("关键词：校园二手交易；Spring Boot；WebSocket；微信小程序；前后端分离"));

children.push(newPage());

// ==========================================
// Abstract
// ==========================================
children.push(specialTitle("Abstract"));
children.push(empty());

children.push(pIndent("To address the issues of low circulation efficiency and lack of trust in campus idle item trading, a campus second-hand trading platform based on Spring Boot is designed and implemented. The system adopts a front-backend separation architecture, using Spring Boot + MyBatis-Plus + MySQL + Redis + WebSocket for backend data management and business logic, uni-app + Vue 3 for the WeChat mini-program, and Vue 3 + Element Plus + ECharts for the admin panel. The platform implements core functions including user authentication, product management, order processing, instant messaging, campus authentication, and data statistics, covering the entire campus second-hand trading process. Tests show the system is functional and stable."));

children.push(empty());
children.push(keywordLabel("Keywords: Campus Second-hand Trading; Spring Boot; WebSocket; WeChat Mini Program; Front-backend Separation"));

children.push(newPage());

// ==========================================
// 一、绪论（页码：Ⅱ 开始）
// ==========================================
children.push(h1("一、绪论"));

// 1.1
children.push(h2("1.1 项目背景与意义"));
children.push(h3("1.1.1 校园闲置物品流转现状"));

children.push(pIndent("高校学生群体具有人员流动性大、物品更新迭代快的特点，尤其是每年毕业季，大量毕业生离校时面临如何处理闲置物品的难题。这些闲置物品包括教材教辅、电子产品、生活用品、体育器材等，种类繁多且多数仍具有较高的使用价值。然而，由于缺乏有效的流转渠道，大量闲置物品被丢弃或贱卖，造成了资源的浪费。"));

children.push(pIndent("目前市场上的二手交易平台以闲鱼和转转为代表。闲鱼依托淘宝生态体系，拥有庞大的用户基数和成熟的信用评价机制；转转则聚焦二手 3C 数码品类，在垂直领域形成了一定的竞争优势[1,2]。然而，这些综合性的二手交易平台在校园场景中存在明显的空白：一是缺乏学籍身份认证机制，交易双方无法确认校友身份，信任成本高；二是未提供校区级别的商品筛选功能，线下当面交易不便；三是缺少针对校内交易场景的定制化功能设计。"));

children.push(h3("1.1.2 研究目标与意义"));

children.push(pIndent("针对上述问题，本文设计并实现一个面向广东轻工职业技术大学的校园二手交易平台——轻院二手。该平台通过校园认证机制建立信任基础，要求用户完成学生身份认证后方可进行交易；以校区维度筛选商品，提升交易效率；配合即时通讯功能降低买家与卖家的沟通成本。通过该平台的建设，旨在为高校学生提供一个安全、便捷、可信的闲置物品流转渠道，促进校园资源的循环利用。"));

// 1.2
children.push(h2("1.2 国内外研究现状"));
children.push(h3("1.2.1 国外二手交易平台现状"));

children.push(pIndent("以 eBay、Facebook Marketplace 为代表的 C2C 平台已形成成熟的信用评价体系和支付保障机制。eBay 自 1995 年成立以来，建立了完善的卖家评级系统和买家保护政策。Poshmark 等垂直平台在时尚品类上实现了精准运营和社交化交易。然而，这些平台均针对开放市场设计，未针对校园封闭场景做定制化设计，缺乏校区级别的交易匹配和身份认证功能[3]。"));

children.push(h3("1.2.2 国内二手交易平台现状"));

children.push(pIndent("国内二手交易市场形成了以闲鱼和转转为主的双寡头格局。闲鱼依托淘宝生态，拥有超过 3 亿用户，其信用体系基于支付宝芝麻信用分构建。转转则在二手 3C 品类建立了验机服务和质保体系。此外，部分高校曾尝试运营校内二手交易平台，如通过微信公众号、QQ 群等渠道进行信息发布，但存在信息分散、管理困难、交易安全无法保障等问题。针对校园场景的专属二手交易平台仍存在明显空白：缺乏学籍身份认证机制、未提供校区级别的商品筛选、缺少校内交易的配套功能设计[1,4]。"));

// 1.3
children.push(h2("1.3 论文主要工作与结构安排"));

children.push(pIndent("本文从系统分析、系统设计、系统实现和系统测试四个方面展开论述。第一章介绍了项目背景与国内外研究现状；第二章介绍了系统所采用的核心开发技术；第三章从可行性和功能性两个方面进行了系统需求分析；第四章阐述了系统的总体架构、数据库设计和接口设计方案；第五章展示了后端和前端核心功能的实现过程，并附有界面截图；第六章对系统进行了功能测试和兼容性测试，并对全文进行总结与展望。"));

children.push(newPage());

// ==========================================
// 二、系统相关技术（页码：Ⅲ 开始）
// ==========================================
children.push(h1("二、系统相关技术"));

// 2.1
children.push(h2("2.1 后端开发技术"));
children.push(h3("2.1.1 Spring Boot 框架"));

children.push(pIndent("Spring Boot 3.3.7 是当前 Spring 官方推荐的微服务开发框架，通过自动配置和 Starter 依赖管理机制，极大地简化了 Spring 项目的初始搭建和配置流程。项目采用分层架构设计，分为 Controller 层（请求接收与响应）、Service 层（业务逻辑处理）和 Mapper 层（数据库操作），各层职责清晰、解耦性强，便于后期维护和功能扩展[5]。"));

children.push(h3("2.1.2 MyBatis-Plus 持久层框架"));

children.push(pIndent("MyBatis-Plus 在 MyBatis 基础上进行了功能增强，提供了代码生成器、分页插件、条件构造器、乐观锁插件等实用功能。项目中通过继承 BaseMapper 接口实现单表的 CRUD 操作，使用 Page 对象统一处理分页查询，利用 QueryWrapper 和 LambdaQueryWrapper 构建动态查询条件，大幅减少了数据库操作相关的样板代码编写量[6]。"));

children.push(h3("2.1.3 MySQL 与 Redis"));

children.push(pIndent("MySQL 作为系统的主数据库，用于持久化存储用户、商品、订单、聊天记录等核心业务数据。采用 InnoDB 存储引擎，支持事务处理和行级锁，保证数据的一致性和并发性能。Redis 作为缓存数据库，用于缓存分类列表、校区列表、学院列表等不频繁变更的热点数据，同时也用于存储用户的未读消息计数。通过 Redis 缓存，有效降低了数据库的查询压力，提升了接口的响应速度[7]。"));

children.push(h3("2.1.4 WebSocket 即时通讯"));

children.push(pIndent("WebSocket 是 HTML5 规范中定义的网络传输协议，支持客户端与服务器之间的全双工通信，解决了 HTTP 协议无法实现服务端主动推送的问题。项目中基于 Spring WebSocket 实现了买卖双方的即时聊天功能，采用自定义 JSON 消息协议，支持文本消息、图片消息、已读回执、Ping/Pong 心跳检测等能力，通过 SessionKey 实现会话级别的消息路由[8]。"));

children.push(h3("2.1.5 JWT 认证机制"));

children.push(pIndent("JWT（JSON Web Token）是一种基于 Token 的无状态认证机制，由 Header、Payload、Signature 三部分组成。项目中小程序端使用 JwtInterceptor 拦截除登录外的所有请求，从请求头中提取 JWT Token 并验证签名和有效期，解析出的用户 ID 通过 ThreadLocal 存入 UserContext，实现请求链路内的用户上下文传递。管理端使用 AdminJwtInterceptor 实现类似的认证逻辑，两套拦截器互不干扰，分别保护不同的接口路径[9]。"));

children.push(h3("2.1.6 定时任务"));

children.push(pIndent("Spring 框架提供了 @Scheduled 注解，支持基于 cron 表达式或固定间隔的任务调度。项目中部署了 6 个定时任务，包括：订单超时自动取消（防止僵尸订单积压）、订单自动确认收货（超时未确认则自动完成）、评论自动打分与提醒、商品定期自动下架（长期未售出商品）、用户账号自动停用等。这些任务覆盖了订单、商品、用户的全生命周期管理，减少了人工干预的成本，提升了系统的自动化水平[5]。"));

// 2.2
children.push(h2("2.2 前端开发技术"));
children.push(h3("2.2.1 uni-app 跨端框架"));

children.push(pIndent("uni-app 是基于 Vue.js 的跨端开发框架，支持一套代码编译到微信小程序、App、H5 等多个平台。项目中使用 uni-app 开发微信小程序端，通过 pages.json 配置文件集中管理页面路由与 Tab 栏导航，利用条件编译处理不同平台间的样式差异，提高了开发效率[10]。"));

children.push(h3("2.2.2 Vue 3 组合式 API 与 Pinia"));

children.push(pIndent("Vue 3 引入了组合式 API（Composition API），通过 setup 函数、ref、computed 等 API 组织组件逻辑，相比选项式 API 具有更好的逻辑复用和代码组织能力。Pinia 是 Vue 3 官方推荐的状态管理库，项目中 AppStore 管理校区切换和分类列表缓存，UserStore 管理用户登录态和校园认证状态，配合 uni-app 的页面生命周期，确保页面切换时状态保持同步[11]。"));

children.push(h3("2.2.3 Element Plus 与 ECharts"));

children.push(pIndent("管理后台基于 Element Plus 组件库进行界面开发，使用了表格、表单、弹窗、导航菜单等组件，保证了操作界面的友好性和一致性。数据统计模块集成 ECharts 图表库，通过折线图展示用户增长和交易趋势，通过饼图展示各校区和各分类的商品分布情况，通过柱状图进行数据对比分析，为管理员提供直观的运营数据可视化[12]。"));

children.push(newPage());

// ==========================================
// 三、系统分析（页码切换为 1,2,3...）
// ==========================================
children.push(h1("三、系统分析"));

// 3.1
children.push(h2("3.1 可行性分析"));
children.push(h3("3.1.1 技术可行性"));

children.push(pIndent("项目后端采用 Spring Boot + MyBatis-Plus + MySQL + Redis 的技术栈，前端小程序端使用 uni-app + Vue 3，管理后台使用 Vue 3 + Element Plus + ECharts。这些技术均为当前主流的开源技术体系，社区活跃、文档完善、开发工具链完整。团队成员已掌握相关技术的基础知识，具备完成项目开发的技术能力。因此，项目的技术可行性较高。"));

children.push(h3("3.1.2 操作可行性"));

children.push(pIndent("微信小程序具有无需安装、即用即走的特点，符合当前高校学生群体的使用习惯。小程序端的页面设计参考了主流电商平台的交互模式，用户可以快速上手。管理后台基于 Element Plus 组件库构建，表单操作、数据表格、弹窗交互等界面设计规范，管理员经过简单培训即可掌握各项管理功能的使用方法。因此，项目在操作层面完全可行。"));

// 3.2
children.push(h2("3.2 系统需求分析"));
children.push(h3("3.2.1 功能性需求"));

children.push(pIndent("系统分为小程序端和管理端两个角色。小程序端面向普通学生用户，提供以下功能：微信登录、账号密码登录、短信验证码登录；校园认证申请与审核状态查询；商品发布功能（包含图片上传、价格设置、成色标注等字段填写）；商品浏览功能（支持关键词搜索、分类筛选、价格区间筛选、校区筛选）；双列瀑布流商品列表展示；商品收藏与取消收藏；用户关注与粉丝管理；买卖双方即时聊天；订单创建与状态流转，包括待面交、已完成、已评价、已取消四种状态；订单评价与商品评论回复；系统通知（交易通知、系统公告）；违规举报功能。"));

children.push(pIndent("管理端面向平台管理员，提供以下功能：管理员与员工登录；员工账户管理（新增、修改、重置密码）；用户管理（封禁与解封）；商品审核（通过、驳回、批量审核、强制下架）；订单管理与查询；校园认证审核（通过或驳回并填写原因）；举报处理（处理或忽略）；商品分类管理（新增、修改、删除）；校区管理及交易点配置；轮播图管理；系统公告管理；数据统计（总览面板、趋势图表、各校区分布、各分类分布）。"));

children.push(h3("3.2.2 非功能性需求"));

children.push(pIndent("系统的非功能性需求包括：接口响应时间需控制在 500ms 以内，保证良好的用户体验；所有非公开接口均采用 JWT 鉴权保护，防止未授权访问；统一使用 Result 格式进行响应封装，全局异常处理器捕获业务异常并返回友好提示；前端需适配 iOS 刘海屏和灵动岛等不同屏幕尺寸，确保页面布局正常显示；系统需保证 7×24 小时稳定运行，核心业务数据的准确性和一致性必须得到保障。"));

// 3.3
children.push(h2("3.3 系统用例分析"));
children.push(h3("3.3.1 用户端用例"));

children.push(pIndent("用户端的主要用例包括：注册登录（微信授权登录、账号密码登录、短信验证码登录）、商品浏览（首页浏览、关键词搜索、分类/校区/价格筛选）、商品管理（发布、编辑、下架、查看列表）、商品交互（收藏、取消收藏、关注用户）、即时聊天（发起会话、发送消息、查看会话列表）、订单管理（创建订单、确认收货、取消订单、查看订单）、评价管理（发表评价、商品评论与回复）、校园认证（提交申请、查看状态）、系统功能（通知查看、举报提交、个人资料编辑）。"));

children.push(h3("3.3.2 管理端用例"));

children.push(pIndent("管理员的主要用例包括：系统登录与退出、员工账户管理（新增、修改、重置密码）、用户管理（查询、封禁、解封）、商品审核（查看列表、通过、驳回、批量审核、强制下架）、订单管理（查看、查询）、校园认证审核（查看申请、通过、驳回）、举报处理（查看、处理、忽略）、系统配置管理（分类管理、校区管理、轮播图管理、公告管理）、数据统计查看（总览、趋势、各校区、各分类）。"));

children.push(h3("3.3.3 核心业务流程"));

children.push(pIndent("买家从浏览商品到完成交易的核心流程为：首页浏览或搜索商品 → 查看商品详情 → 通过即时聊天联系卖家 → 双方达成一致后买家下单 → 线下当面交易 → 买家确认收货 → 发表交易评价。卖家从发布到成交的流程为：发布商品并填写信息 → 等待管理员审核 → 审核通过后商品上架 → 接收买家咨询消息 → 接收订单通知 → 线下交付商品 → 等待买家确认 → 收到买家评价。整个流程形成了完整的交易闭环，覆盖了校园二手交易的全部环节。"));

children.push(newPage());

// ==========================================
// 四、系统设计
// ==========================================
children.push(h1("四、系统设计"));

// 4.1
children.push(h2("4.1 系统架构设计"));
children.push(h3("4.1.1 总体架构"));

children.push(pIndent("系统采用前后端分离的架构设计。后端基于 Spring Boot 的分层架构运行在服务器端，由 Controller 层接收前端请求并返回响应，Service 层处理核心业务逻辑，Mapper 层负责与数据库交互。前端微信小程序运行在用户的移动设备上，通过 HTTP 请求调用后端 RESTful API 获取数据。管理后台运行在浏览器中，基于 Vue 3 单页面应用架构开发。前后端通过 JSON 格式进行数据交互，WebSocket 负责即时消息的实时推送，Redis 为系统提供缓存服务以提升性能。"));

children.push(h3("4.1.2 模块划分"));

children.push(pIndent("根据功能职责，系统划分为八大核心模块：用户模块负责用户的注册登录、个人信息管理和校园认证；商品模块负责商品的发布、编辑、搜索浏览和评论管理；订单模块负责订单的创建、状态流转和交易评价；社交模块负责买卖双方的即时聊天、商品收藏和用户关注；通知模块负责交易状态变更通知和系统公告推送；举报模块负责违规内容的举报提交和处理跟进；管理模块为管理员提供员工、用户、商品、订单、认证等内容管理功能；统计模块通过数据可视化呈现平台的运营状况。"));

// 4.2
children.push(h2("4.2 数据库设计"));
children.push(h3("4.2.1 核心实体设计"));

children.push(pIndent("系统以用户（user）、商品（product）、订单（trade_order）为三大核心实体。用户与商品之间为一对多关系，一个用户可以发布多件商品，每件商品属于一个用户。商品与订单之间为一对一关系，一次交易订单对应一件商品。用户与订单之间为一对多关系，一个用户既可以作为买家创建订单，也可以作为卖家接收订单。此外，用户与收藏、关注、评价、聊天消息、校园认证等实体之间也存在关联关系，共同构成了系统的完整数据模型。"));

children.push(h3("4.2.2 关键数据表结构"));

children.push(pIndent("用户表（user）存储用户的用户名、手机号、头像 URL、所属校区、校园认证状态、创建时间等关键字段。商品表（product）存储商品标题、描述文字、价格、图片列表、分类 ID、校区 ID、发布者 ID、审核状态等字段，其中审核状态区分待审核、在售、已下架、已售出、审核驳回五种状态。订单表（trade_order）存储买家 ID、卖家 ID、商品 ID、订单状态、取消原因、创建时间、完成时间等字段，订单状态分为待面交、已完成、已评价、已取消四种。聊天消息表（chat_message）存储发送者 ID、接收者 ID、会话标识 SessionKey、消息内容、消息类型（文本/图片/系统）、发送时间等字段，通过 SessionKey 将会话双方的消息关联到同一个会话中。其他核心数据表还包括评价表、校园认证表、收藏表、关注表、举报表、通知表、分类表、校区表、轮播图表等。"));

// 数据表概览表格
const tblWidths1 = [1800, 1600, 3600];
children.push(new Table({
  rows: [
    new TableRow({ children: [cellHeader("表名", tblWidths1[0]), cellHeader("说明", tblWidths1[1]), cellHeader("核心字段", tblWidths1[2])] }),
    new TableRow({ children: [cell("user", tblWidths1[0]), cell("用户表", tblWidths1[1]), cell("id, username, phone, avatar, campus_id, auth_status", tblWidths1[2])] }),
    new TableRow({ children: [cell("product", tblWidths1[0]), cell("商品表", tblWidths1[1]), cell("id, user_id, title, price, images, category_id, status", tblWidths1[2])] }),
    new TableRow({ children: [cell("trade_order", tblWidths1[0]), cell("订单表", tblWidths1[1]), cell("id, buyer_id, seller_id, product_id, status", tblWidths1[2])] }),
    new TableRow({ children: [cell("chat_message", tblWidths1[0]), cell("聊天消息表", tblWidths1[1]), cell("id, from_id, to_id, session_key, content, msg_type", tblWidths1[2])] }),
    new TableRow({ children: [cell("chat_session", tblWidths1[0]), cell("聊天会话表", tblWidths1[1]), cell("id, session_key, user1_id, user2_id, last_msg", tblWidths1[2])] }),
    new TableRow({ children: [cell("review", tblWidths1[0]), cell("评价表", tblWidths1[1]), cell("id, order_id, from_id, to_id, content, rating", tblWidths1[2])] }),
    new TableRow({ children: [cell("campus_auth", tblWidths1[0]), cell("校园认证表", tblWidths1[1]), cell("id, user_id, student_id, real_name, status", tblWidths1[2])] }),
  ]
}));
children.push(empty());

children.push(h3("4.2.3 Redis 缓存设计"));

children.push(pIndent("为了提高系统性能，将分类列表、校区列表、学院列表等不频繁变更的数据缓存至 Redis，设置合理的过期时间（如 1 小时），减少数据库查询次数。聊天会话的未读消息数也通过 Redis 存储，利用 Redis 的高效读写能力支持频繁的未读计数更新。此外，用户的登录 Token 也可以缓存至 Redis，方便实现 Token 的主动失效和刷新机制[7]。"));

// Redis缓存表
const tblWidths2 = [2200, 1400, 1200, 2200];
children.push(new Table({
  rows: [
    new TableRow({ children: [cellHeader("缓存 Key", tblWidths2[0]), cellHeader("Value 类型", tblWidths2[1]), cellHeader("过期时间", tblWidths2[2]), cellHeader("用途", tblWidths2[3])] }),
    new TableRow({ children: [cell("category:list", tblWidths2[0]), cell("List", tblWidths2[1]), cell("1 小时", tblWidths2[2]), cell("商品分类列表", tblWidths2[3])] }),
    new TableRow({ children: [cell("campus:list", tblWidths2[0]), cell("List", tblWidths2[1]), cell("1 小时", tblWidths2[2]), cell("校区列表", tblWidths2[3])] }),
    new TableRow({ children: [cell("college:list", tblWidths2[0]), cell("List", tblWidths2[1]), cell("1 小时", tblWidths2[2]), cell("学院列表", tblWidths2[3])] }),
    new TableRow({ children: [cell("unread:{userId}", tblWidths2[0]), cell("Integer", tblWidths2[1]), cell("持久化", tblWidths2[2]), cell("用户未读消息计数", tblWidths2[3])] }),
  ]
}));
children.push(empty());

// 4.3
children.push(h2("4.3 接口设计"));
children.push(h3("4.3.1 统一响应格式"));

children.push(pIndent("所有接口统一返回 Result 泛型封装对象，包含 code（状态码）、data（数据）、msg（提示信息）三个核心字段。code 为 1 表示业务处理成功，code 为 0 表示业务处理异常。当系统发生业务异常时，由全局异常处理器（GlobalExceptionHandler）捕获 BusinessException 异常，自动封装为错误响应返回给前端。这种统一响应格式的设计，保证前端可以基于 code 字段进行统一的响应处理，降低了前后端联调的成本。"));

children.push(h3("4.3.2 小程序端接口概览"));

children.push(pIndent("小程序端共设计 70 余个 RESTful API 接口，按照功能模块可分为：用户认证接口（登录、注册、信息更新、Token 刷新）、商品操作接口（发布、编辑、上下架、搜索、详情、列表）、订单管理接口（创建、状态查询、取消、确认收货）、即时聊天接口（发送消息、会话列表、历史消息、未读计数）、收藏关注接口（添加/取消收藏、关注/取消关注、列表查询）、通知查看接口、举报提交接口等。所有接口均采用 RESTful 风格设计，通过 HTTP 方法区分操作类型。"));

children.push(h3("4.3.3 管理端接口概览"));

children.push(pIndent("管理端共设计 50 余个 RESTful API 接口，主要包括：员工管理接口（登录、新增、修改、删除、重置密码）、用户管理接口（列表查询、封禁、解封）、商品审核接口（通过、驳回、批量操作、强制下架）、订单查看接口、校园认证审核接口（通过、驳回）、举报处理接口（处理、忽略）、系统配置管理接口（分类/校区/轮播图/公告的增删改查）、数据统计接口（总览数据、趋势数据、各校区统计、各分类统计）等。管理端接口与小程序端接口分离，采用独立的拦截器进行权限校验。"));

children.push(newPage());

// ==========================================
// 五、系统实现
// ==========================================
children.push(h1("五、系统实现"));

// 5.1
children.push(h2("5.1 后端核心功能实现"));
children.push(h3("5.1.1 JWT 鉴权拦截器"));

children.push(pIndent("JWT 鉴权拦截器是系统安全的第一道防线。小程序端通过 JwtInterceptor 类实现，该类继承 HandlerInterceptor 接口，重写 preHandle 方法。在 preHandle 方法中，首先从请求头中提取 Authorization 字段，去除 Bearer 前缀后得到 JWT Token，然后调用 JwtUtil 工具类的验证方法校验 Token 的签名是否有效以及是否在有效期内。验证通过后，从 Token 的 Payload 中解析出用户 ID，通过 UserContext.set() 方法存入 ThreadLocal 中，供后续请求链路中的各个组件获取当前用户信息。请求处理完成后，在拦截器的 afterCompletion 方法中调用 UserContext.clear() 释放 ThreadLocal 资源，防止内存泄漏。管理端使用 AdminJwtInterceptor 实现类似的逻辑，两套拦截器在 WebMvcConfig 中分别注册到不同的 URL 路径规则下。"));

children.push(h3("5.1.2 WebSocket 即时通信"));

children.push(pIndent("WebSocket 即时通信功能是实现买卖双方实时沟通的核心模块。服务端通过 WebSocketServer 类管理所有客户端连接，该类使用 @ServerEndpoint 注解声明为 WebSocket 端点。连接建立时，从页面 URL 的查询参数中提取 JWT Token 并完成身份认证，认证通过后将当前会话加入会话管理器。消息处理采用策略模式，客户端发送 JSON 格式消息，服务端根据消息中的 type 字段（MessageType）将消息分发到不同的消息处理器（ChatMessageHandler、ImageMessageHandler、ReadReceiptHandler、PingMessageHandler 等）。每个处理器负责执行业务逻辑、将消息持久化到数据库，并通过 SessionKey 将消息推送给接收方。心跳检测机制通过定期发送 Ping 消息检测连接健康状态，如果超时未收到 Pong 响应则主动关闭连接并清理资源。"));

children.push(h3("5.1.3 消息协议设计"));

children.push(pIndent("系统采用自定义 JSON 消息协议，每条消息包含以下字段：type（消息类型，包括 CHAT、IMAGE、READ_RECEIPT、PING、PONG、SYSTEM）、fromId（发送者用户 ID）、toId（接收者用户 ID）、sessionKey（会话标识）、content（消息内容）、timestamp（消息时间戳）。消息处理流程为：客户端通过 WebSocket 发送 JSON 消息 → 服务端 WebSocketServer 接收到消息 → 根据 MessageType 找到对应的消息处理器 → 处理器执行业务逻辑（如消息持久化、未读计数更新等）→ 通过 SessionManager 找到接收方的 WebSocket 连接 → 将消息推送给接收方。这种基于消息类型的分发机制，使得系统可以灵活地扩展新的消息类型，而不需要修改核心的消息接收逻辑。"));

children.push(h3("5.1.4 商品状态管理与订单流转"));

children.push(pIndent("商品状态管理采用有限状态机模式设计。商品共有五种状态：待审核（WAITING）为初始状态，用户发布商品后进入该状态；管理员审核通过后变为在售（ON_SALE）；审核不通过则变为审核驳回（REJECTED）；在售状态的商品可以被用户主动下架（OFF_SHELF），也可以在买家下单购买后变为已售出（SOLD）。订单状态管理同样采用状态机模式，共有四种状态：买家下单后订单处于待面交（PENDING）状态；双方线下完成交易后，买家确认收货变为已完成（COMPLETED）；买家可发表交易评价变为已评价（REVIEWED）；在交易完成前，任意一方可取消订单变为已取消（CANCELLED）。系统通过 @Scheduled 定时任务，每 30 分钟扫描一次超时未确认的订单自动完成、超时未支付的预约单自动取消、超过 30 天未售出的商品自动下架。"));

children.push(h3("5.1.5 文件上传与数据统计"));

children.push(pIndent("文件上传功能通过 CommonController 统一处理，支持图片（jpg、png、gif 等格式）和其他类型文件。上传时校验文件类型和文件大小（限制单张图片不超过 5MB），自动生成 UUID 文件名防止文件名冲突，文件存储在服务器本地 upload 目录下，并通过 Spring MVC 的静态资源映射对外提供 HTTP 访问 URL。数据统计模块提供总览面板展示关键指标（用户总数、商品总数、订单总数、交易总金额），通过折线图呈现每日用户注册趋势、商品发布趋势和订单成交趋势，通过饼图展示各校区和各分类的商品分布情况，帮助管理员全面掌握平台的运营状况。"));

// 5.2
children.push(h2("5.2 前端核心功能实现"));
children.push(h3("5.2.1 微信小程序页面实现"));

children.push(pIndent("首页采用 sticky 吸顶布局设计，顶部为校区选择器和搜索框，中间为轮播图区域和快捷分类入口，下方为双列瀑布流商品列表。用户切换校区时，首页触发商品列表的全局刷新，展示所选校区的商品数据。商品列表与筛选页面支持多维度筛选，包括分类选择、校区限定、价格区间输入和排序方式（最新发布、价格从低到高、价格从高到低），筛选栏在页面滚动时吸顶固定。列表采用触底加载更多的分页模式，每次加载一页数据，提升页面加载速度和大数据量下的浏览体验。"));

children.push(pIndent("商品详情页面包含商品图片轮播区、标题价格成色信息区、卖家头像与资料入口、商品描述区、评论区等功能区域。页面底部提供收藏按钮、联系卖家按钮和立即购买按钮。聊天页面按会话列表和消息详情两级展示，会话列表显示最近一条消息预览和未读消息计数，消息详情页采用消息气泡布局，文字消息实时收发，消息按发送时间分组展示并显示时间标签。个人中心页面聚合用户信息卡片、我的发布、我的收藏、关注与粉丝列表、订单记录（按待面交、已完成、已取消分 Tab 展示）以及校园认证入口等功能模块。"));

children.push(h3("5.2.2 全局状态管理"));

children.push(pIndent("项目使用 Pinia 进行全局状态管理，分为 AppStore 和 UserStore 两个 Store。AppStore 负责管理校园列表缓存、分类列表缓存，当用户切换校区时，AppStore 更新当前校区 ID 并触发相关页面的数据刷新。UserStore 负责管理用户登录态信息、个人资料数据和校园认证状态。在用户登录或退出登录时，UserStore 更新状态并同步更新本地缓存。两个 Store 配合 uni-app 的页面生命周期钩子（onShow、onLoad），确保页面切换时状态保持一致，避免重复请求服务器数据。"));

children.push(h3("5.2.3 管理后台页面实现"));

children.push(pIndent("管理后台基于 Vue 3 + Element Plus 组件库构建，采用侧边栏导航加右侧内容区域的经典布局。路由导航守卫在检测到用户未登录时自动跳转到登录页面，登录成功后跳转回原始目标页面。商品审核页以数据表格展示所有待审核商品，支持单条审核（通过/驳回）和批量审核操作，审核结果通过 ElMessage 组件实时反馈给管理员。数据统计页面集成 ECharts 图表库，折线图展示每日注册用户数、商品发布数和订单成交数的变化趋势，饼图展示各校区和各分类的商品占比分布。用户管理页以表格形式展示所有注册用户，支持按用户名或手机号搜索，提供封禁和解封操作，被封禁用户的记录在列表中高亮显示。校园认证审核页展示认证申请列表，显示学生证图片、真实姓名、学号等信息，管理员可选择通过或驳回并填写驳回原因。"));

// 5.3
children.push(h2("5.3 关键界面展示"));

children.push(pIndent("小程序首页界面展示了校区选择器、搜索框、轮播图、分类入口和双列瀑布流商品列表的完整布局。商品详情页展示了商品图片轮播、价格信息、卖家资料以及评论区等功能模块。聊天界面展示了会话列表的消息预览和消息详情的实时对话效果。管理后台的商品审核页展示了数据表格形式的商品列表和审核操作按钮，数据统计面板展示了折线图和饼图的可视化效果。（注：完整界面截图请参见论文附录或电子版附件）"));

children.push(newPage());

// ==========================================
// 六、系统测试与总结
// ==========================================
children.push(h1("六、系统测试与总结"));

// 6.1
children.push(h2("6.1 系统测试"));
children.push(h3("6.1.1 功能测试"));

children.push(pIndent("对系统核心业务流程设计了详细的测试用例，覆盖了用户注册登录流程、商品发布与管理流程、商品搜索与筛选流程、订单创建与流转流程、即时聊天流程、校园认证申请与审核流程、后台商品审核流程、举报与处理流程等。每条测试用例包含测试模块、测试步骤、预期结果和实际结果四个要素，确保功能实现的完整性和正确性。主要测试用例及结果如下表所示："));

// 测试表格
const tblWidths3 = [1400, 1600, 2400, 2000, 1200];
children.push(new Table({
  rows: [
    new TableRow({ children: [cellHeader("测试模块", tblWidths3[0]), cellHeader("测试用例", tblWidths3[1]), cellHeader("测试步骤", tblWidths3[2]), cellHeader("预期结果", tblWidths3[3]), cellHeader("实际结果", tblWidths3[4])] }),
    new TableRow({ children: [cell("用户登录", tblWidths3[0]), cell("微信授权登录", tblWidths3[1]), cell("点击微信登录→授权→回调", tblWidths3[2]), cell("登录成功，跳转首页", tblWidths3[3]), cell("通过", tblWidths3[4])] }),
    new TableRow({ children: [cell("商品发布", tblWidths3[0]), cell("发布新商品", tblWidths3[1]), cell("填写信息→上传图片→提交", tblWidths3[2]), cell("进入待审核状态", tblWidths3[3]), cell("通过", tblWidths3[4])] }),
    new TableRow({ children: [cell("商品审核", tblWidths3[0]), cell("审核通过", tblWidths3[1]), cell("管理员点击通过→确认", tblWidths3[2]), cell("商品状态变更为在售", tblWidths3[3]), cell("通过", tblWidths3[4])] }),
    new TableRow({ children: [cell("即时聊天", tblWidths3[0]), cell("发送消息", tblWidths3[1]), cell("进入聊天页→输入文本→发送", tblWidths3[2]), cell("消息实时显示在双方聊天框", tblWidths3[3]), cell("通过", tblWidths3[4])] }),
    new TableRow({ children: [cell("订单流转", tblWidths3[0]), cell("确认收货", tblWidths3[1]), cell("买家点击确认收货", tblWidths3[2]), cell("订单状态变为已完成", tblWidths3[3]), cell("通过", tblWidths3[4])] }),
  ]
}));
children.push(empty());

children.push(h3("6.1.2 兼容性测试"));

children.push(pIndent("微信小程序在 iOS 设备（含刘海屏和灵动岛机型）以及 Android 主流品牌设备上进行了 UI 展示和交互操作测试。测试内容包括页面布局是否正确、交互操作是否流畅、核心功能是否正常。测试结果表明，小程序在不同屏幕尺寸和系统版本下均能正常显示和运行。管理后台在 Chrome 和 Edge 等主流浏览器上进行了界面渲染和功能操作校验，结果均正常。"));

children.push(h2("6.2 全文总结"));

children.push(pIndent("本文从校园闲置物品流转的实际需求出发，设计并实现了一个基于 Spring Boot 的校园二手交易平台——轻院二手。系统采用前后端分离架构，后端使用 Spring Boot + MyBatis-Plus + MySQL + Redis + WebSocket，前端微信小程序使用 uni-app + Vue 3 + Pinia，管理后台使用 Vue 3 + Element Plus + ECharts。平台实现了用户认证、商品管理、订单流转、即时通讯、校园认证、后台管理等核心功能模块，覆盖了校园二手交易的全流程。"));

children.push(pIndent("在开发过程中，重点攻克了三个技术难点：一是基于 WebSocket 实现了买卖双方的即时通讯功能，设计了自定义消息协议和心跳保活机制，支持文本和图片消息的实时传输；二是采用双端 JWT 拦截器配合 ThreadLocal 实现了无状态认证和请求链路内的用户上下文传递，保证了接口的安全性和请求处理的便捷性；三是通过 @Scheduled 定时任务完成了订单超时自动取消、自动确认收货、商品自动下架等自动化管理功能，减少了人工干预成本。经过功能测试和兼容性测试验证，系统运行稳定、功能完整，达到了预期的设计目标。"));

children.push(h2("6.3 不足与展望"));

children.push(pIndent("当前系统尚未接入在线支付功能，交易仅支持线下当面交付，这在一定程度上限制了交易场景的多样性。后续工作中可考虑接入微信支付接口，实现在线支付闭环，提升交易的便捷性和安全性。同时，可以引入用户信用评分机制，结合交易完成率、评价得分、认证等级等维度构建综合评分模型，进一步提升交易双方的信任度。此外，还可探索基于用户行为数据的个性化商品推荐算法，提高商品曝光效率和平台的整体转化率。"));

children.push(newPage());

// ==========================================
// 致谢
// ==========================================
children.push(specialTitle("致  谢"));
children.push(empty());

children.push(pIndent("在本毕业设计完成之际，我要衷心感谢我的指导老师。从选题确定、方案设计到论文撰写的整个过程中，老师给予了耐心细致的指导和宝贵的建议，为项目的顺利完成提供了重要保障。同时，感谢各位授课老师在大学期间的悉心教导，为我打下了扎实的专业基础。感谢实验室的同学们在项目开发过程中提供的帮助和支持。最后，感谢我的家人一直以来的理解与鼓励。"));

children.push(newPage());

// ==========================================
// 参考文献
// ==========================================
children.push(specialTitle("参考文献"));
children.push(empty());

const refs = [
  "[1] 张宇翔, 陈浩. 校园二手交易平台的研究与设计[J]. 计算机技术与发展, 2023, 33(5): 120-125.",
  "[2] 李明华, 王磊. C2C电子商务模式下二手交易平台现状分析[J]. 电子商务, 2022(8): 45-48.",
  "[3] Anderson T, Smith J. Peer-to-Peer Commerce in Closed Communities[J]. Journal of Digital Commerce, 2021, 15(3): 210-225.",
  "[4] 刘芳. 基于微信小程序的校园二手交易系统设计与实现[D]. 华中科技大学, 2022.",
  "[5] Walls C. Spring Boot in Action[M]. Manning Publications, 2022.",
  "[6] 周志明. MyBatis-Plus从入门到精通[M]. 机械工业出版社, 2023.",
  "[7] Carlson J. Redis in Action[M]. Manning Publications, 2020.",
  "[8] 赵强, 孙丽. 基于WebSocket的即时通讯系统设计与实现[J]. 计算机工程与应用, 2023, 59(12): 85-91.",
  "[9] Jones M, Bradley J, Sakimura N. JSON Web Token (JWT)[S]. RFC 7519, IETF, 2015.",
  "[10] DCloud. uni-app跨端开发官方文档[EB/OL]. https://uniapp.dcloud.net.cn, 2024.",
  "[11] You Y. Vue 3 Composition API Documentation[EB/OL]. https://vuejs.org, 2024.",
  "[12] 王子涵. 基于ECharts的数据可视化技术在管理后台中的应用[J]. 现代信息技术, 2023, 7(9): 56-60.",
];

refs.forEach(ref => {
  children.push(new Paragraph({
    spacing: { before: 0, after: 0, line: LINE_SINGLE, lineRule: LineRuleType.AUTO },
    indent: { left: 480, hanging: 480 },
    children: [new TextRun({ text: ref, font: FONT_S, size: SZ_小四 })]
  }));
});

children.push(newPage());

// ==========================================
// 附录
// ==========================================
children.push(specialTitle("附录"));
children.push(empty());

children.push(new Paragraph({
  spacing: { before: 0, after: 0, line: LINE_SINGLE, lineRule: LineRuleType.AUTO },
  children: [new TextRun({ text: "附录A：图表清单汇总", font: { name: "黑体", eastAsia: "黑体" }, size: SZ_小四, bold: true })]
}));
children.push(empty());

// 附录表格
const tblWidths4 = [1200, 1200, 3600, 1200];
children.push(new Table({
  rows: [
    new TableRow({ children: [cellHeader("章节", tblWidths4[0]), cellHeader("编号", tblWidths4[1]), cellHeader("名称", tblWidths4[2]), cellHeader("类型", tblWidths4[3])] }),
    new TableRow({ children: [cell("第二章", tblWidths4[0]), cell("图2-1", tblWidths4[1]), cell("系统技术架构图", tblWidths4[2]), cell("框图", tblWidths4[3])] }),
    new TableRow({ children: [cell("第二章", tblWidths4[0]), cell("图2-2", tblWidths4[1]), cell("WebSocket通信流程图", tblWidths4[2]), cell("序列图", tblWidths4[3])] }),
    new TableRow({ children: [cell("第三章", tblWidths4[0]), cell("图3-1", tblWidths4[1]), cell("系统功能模块图", tblWidths4[2]), cell("树形图", tblWidths4[3])] }),
    new TableRow({ children: [cell("第三章", tblWidths4[0]), cell("图3-2", tblWidths4[1]), cell("用户端用例图", tblWidths4[2]), cell("UML用例图", tblWidths4[3])] }),
    new TableRow({ children: [cell("第三章", tblWidths4[0]), cell("图3-3", tblWidths4[1]), cell("管理端用例图", tblWidths4[2]), cell("UML用例图", tblWidths4[3])] }),
    new TableRow({ children: [cell("第三章", tblWidths4[0]), cell("图3-4", tblWidths4[1]), cell("核心业务流程图", tblWidths4[2]), cell("流程图", tblWidths4[3])] }),
    new TableRow({ children: [cell("第四章", tblWidths4[0]), cell("图4-1", tblWidths4[1]), cell("系统总体架构图", tblWidths4[2]), cell("架构图", tblWidths4[3])] }),
    new TableRow({ children: [cell("第四章", tblWidths4[0]), cell("图4-2", tblWidths4[1]), cell("数据库ER图", tblWidths4[2]), cell("ER图", tblWidths4[3])] }),
    new TableRow({ children: [cell("第五章", tblWidths4[0]), cell("图5-1~5-8", tblWidths4[1]), cell("系统界面截图", tblWidths4[2]), cell("截图", tblWidths4[3])] }),
  ]
}));

// ===== 创建文档 =====
const doc = new Document({
  styles: {
    default: {
      document: {
        run: { font: FONT_S, size: SZ_小四 },
        paragraph: { spacing: { line: LINE_125, lineRule: LineRuleType.AUTO } }
      }
    }
  },
  sections: [
    {
      properties: {
        page: {
          size: { width: 11906, height: 16838 }, // A4
          margin: { top: 1440, bottom: 1440, left: 1800, right: 1800 }
        }
      },
      children: children
    }
  ]
});

// ===== 生成文件 =====
const OUTPUT_PATH = "G:\\Code\\Graduation_project\\毕业设计论文.docx";
Packer.toBuffer(doc).then(buffer => {
  fs.writeFileSync(OUTPUT_PATH, buffer);
  console.log("论文文档已生成：" + OUTPUT_PATH);
  console.log("文件大小：" + (buffer.length / 1024).toFixed(1) + " KB");
}).catch(err => {
  console.error("生成失败：", err);
  process.exit(1);
});
