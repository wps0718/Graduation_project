---
alwaysApply: true
---
# 📄 文档三：AI开发规则

以下内容可直接作为AI的系统提示或项目规则：

```markdown
## 项目信息

你正在开发「轻院二手」校园二手交易微信小程序。
学校：广东轻工职业技术大学
校区：南海北、南海南、新港

## 技术栈

- 后端：Java 17 + Spring Boot 3.x + MyBatis-Plus + MySQL 8.0 + Redis 7.x
- 小程序：uni-app
- 管理后台：Vue 3 + Element Plus + Vite
- 即时通讯：OpenIM
- 构建工具：Maven
- 文件存储：V1.0本地存储，V2.0云OSS

## 项目包路径

- 后端：com.qingyuan.secondhand
- 小程序：miniapp/
- 管理后台：admin/

## 强制规则

### 一、一致性规则
1. 任何更改必须保证后端（src）、小程序（miniapp）、管理后台（admin）的一致性
2. 接口参数名、响应字段名前后端必须完全一致（驼峰命名）
3. 枚举值前后端保持一致（如订单状态 1-待面交 3-已完成 4-已评价 5-已取消）
4. 数据库字段使用下划线命名，Java实体使用驼峰命名，MyBatis-Plus自动转换

### 二、代码质量规则
1. 替换新方案后，必须检查旧代码是否还有使用，无用则删除
2. 每次修改后必须检查编译错误并立即解决
3. 禁止提交包含编译错误的代码
4. 数据库实体变更时，必须同步更新init.sql，并提供增量update SQL
5. 修改前先完整核对“被 import 的文件”上下文结构（尤其是全局样式/入口文件）
6. 修改后立刻进行一次编译链路自检（至少确保不会出现括号不闭合、重复块等低级错误）

### 三、接口规范
1. 统一响应格式：{"code":1,"msg":"success","data":obj}，code=1成功，code=0失败
2. 请求参数使用JSON格式
3. POST为主，GET用于查询类接口
4. 分页接口统一使用page和pageSize参数
5. 分页响应使用total和records字段
6. 需登录接口通过Header的Authorization字段传递Bearer Token
7. Token过期返回code=401

### 四、后端开发规范
1. 使用构造函数注入，禁止字段注入（@Autowired字段注入）
2. 使用MyBatis-Plus的LambdaQueryWrapper构建查询
3. 不允许使用数据库外键，关联关系在代码层面维护
4. 密码使用BCrypt加密
5. 使用@Validated + DTO进行参数校验
6. 使用@Slf4j记录日志
7. 业务异常抛出BusinessException，由GlobalExceptionHandler统一处理
8. Controller只做参数接收和响应返回，业务逻辑在Service中实现
9. 复杂查询使用MyBatis XML映射，简单CRUD使用MyBatis-Plus接口
10. 管理后台Controller类名以Admin开头（如AdminProductController）
11. 小程序Controller类名以Mini开头（如MiniProductController）
12. Redis Key定义在RedisConstant常量类中

### 五、前端开发规范（小程序）
1. 所有颜色值在styles/theme.css中统一定义CSS变量，禁止在页面中硬编码色值
2. 优先使用uni-app内置图标，其次第三方图标库，最后图片资源
3. 使用utils/request.js封装的请求方法，禁止直接使用uni.request
4. 登录态管理使用utils/auth.js
5. 页面数据加载统一在onLoad或onShow中处理
6. 列表页统一实现下拉刷新（onPullDownRefresh）和上拉加载（onReachBottom）
7. 空状态使用empty-state组件
8. 商品卡片使用product-card组件（统一复用）
9. 价格展示使用price组件（统一格式）
10. 表单提交按钮需做防重复点击处理（loading状态）
11. Mock模式通过request.js中的开关控制，联调时只需改一个变量

### 六、前端开发规范（管理后台）
1. 使用Element Plus组件
2. 表格页面统一结构：筛选条件区 + 操作按钮区 + 表格 + 分页
3. 接口封装在api/目录下，按模块划分文件
4. 路由配置在router/中，支持权限控制

### 七、安全规则
1. 密码不返回给前端（VO中不包含密码字段）
2. 手机号返回前端时脱敏（138****8000）
3. 文件上传校验类型和大小（仅JPG/PNG/JPEG，≤5MB）
4. 上传文件重命名为UUID，禁止使用原始文件名
5. SQL查询使用参数化，防止SQL注入
6. 输入内容做XSS过滤

### 八、数据库规则
1. 表名使用小写下划线
2. 必须有id主键（bigint自增）
3. 必须有create_time和update_time
4. 状态字段使用tinyint
5. 金额字段使用decimal(10,2)
6. 字符集utf8mb4
7. 合理建立索引（查询条件、外键关联字段）
8. 逻辑删除使用is_deleted字段（非物理删除）

### 九、Git规则
1. 提交前确保编译通过、无报错
2. commit message格式：feat(模块): 描述 / fix(模块): 描述 / docs: 描述
3. 每个功能模块一个feature分支

### 十、注意事项
1. 生成代码前先理解当前模块在整个系统中的位置和关联
2. 涉及到已有代码时，先确认现有实现再修改，避免覆盖
3. 新增接口时检查是否和现有接口重复或冲突
4. 修改表结构时检查是否影响其他模块的查询
5. 前端页面开发时，考虑无数据、加载中、错误三种状态
6. 列表页必须考虑空列表的展示
7. 表单页必须做完整的前端校验
8. 敏感操作（删除、取消、注销）必须有二次确认弹窗

### 十一、任务文件规则
1. 任务文件采用 tasksNN.md 命名（NN 为两位递增数字），读取与写入都以最新编号文件为准
2. 当前最新任务文件为 tasks02.md，后续任务请写入 tasks02.md
3. 当最新任务文件超过 2000 行或 200KB 时，立即创建下一编号文件（如 tasks03.md）并转移到新文件继续记录
4. 旧的 tasks 文件仅作为历史记录，禁止继续追加

```


### 十二、项目结构

```
qingyuan-secondhand/
├── src/                                    # Java 后端
│   ├── main/
│   │   ├── java/com/qingyuan/secondhand/
│   │   │   ├── config/                     # 配置类
│   │   │   ├── controller/
│   │   │   │   ├── admin/                  # 管理后台控制器（Admin前缀）
│   │   │   │   │   ├── AdminEmployeeController.java
│   │   │   │   │   ├── AdminProductController.java
│   │   │   │   │   ├── AdminAuthController.java
│   │   │   │   │   ├── AdminUserController.java
│   │   │   │   │   ├── AdminCategoryController.java
│   │   │   │   │   ├── AdminCampusController.java
│   │   │   │   │   ├── AdminCollegeController.java
│   │   │   │   │   ├── AdminBannerController.java
│   │   │   │   │   ├── AdminReportController.java
│   │   │   │   │   ├── AdminOrderController.java
│   │   │   │   │   ├── AdminNoticeController.java
│   │   │   │   │   └── AdminStatsController.java
│   │   │   │   ├── mini/                   # 小程序控制器
│   │   │   │   │   ├── MiniUserController.java
│   │   │   │   │   ├── MiniAuthController.java
│   │   │   │   │   ├── MiniProductController.java
│   │   │   │   │   ├── MiniCategoryController.java
│   │   │   │   │   ├── MiniCampusController.java
│   │   │   │   │   ├── MiniCollegeController.java
│   │   │   │   │   ├── MiniOrderController.java
│   │   │   │   │   ├── MiniFavoriteController.java
│   │   │   │   │   ├── MiniReviewController.java
│   │   │   │   │   ├── MiniReportController.java
│   │   │   │   │   ├── MiniNotificationController.java
│   │   │   │   │   ├── MiniSearchController.java
│   │   │   │   │   └── MiniBannerController.java
│   │   │   │   └── common/
│   │   │   │       └── CommonController.java  # 文件上传等通用接口
│   │   │   ├── entity/                     # 实体类
│   │   │   ├── mapper/                     # MyBatis-Plus Mapper
│   │   │   ├── service/
│   │   │   │   └── impl/
│   │   │   ├── dto/                        # 请求参数对象
│   │   │   ├── vo/                         # 响应视图对象
│   │   │   ├── common/
│   │   │   │   ├── exception/
│   │   │   │   │   ├── BusinessException.java
│   │   │   │   │   └── GlobalExceptionHandler.java
│   │   │   │   ├── result/
│   │   │   │   │   └── Result.java
│   │   │   │   ├── interceptor/
│   │   │   │   │   ├── JwtInterceptor.java
│   │   │   │   │   └── AdminJwtInterceptor.java
│   │   │   │   ├── util/
│   │   │   │   │   ├── JwtUtil.java
│   │   │   │   │   ├── SmsUtil.java
│   │   │   │   │   ├── FileUtil.java
│   │   │   │   │   └── OrderNoUtil.java
│   │   │   │   ├── constant/
│   │   │   │   │   └── RedisConstant.java
│   │   │   │   └── enums/
│   │   │   │       ├── OrderStatus.java
│   │   │   │       ├── ProductStatus.java
│   │   │   │       ├── AuthStatus.java
│   │   │   │       ├── ReportReason.java
│   │   │   │       └── NotificationType.java
│   │   │   ├── task/                       # 定时任务
│   │   │   │   ├── OrderExpireTask.java
│   │   │   │   ├── OrderAutoConfirmTask.java
│   │   │   │   ├── ProductAutoOffTask.java
│   │   │   │   ├── ReviewAutoTask.java
│   │   │   │   └── UserDeactivateTask.java
│   │   │   └── SecondhandApplication.java
│   │   ├── resources/
│   │   │   ├── application.yml
│   │   │   └── static/uploads/             # V1.0本地存储
├── miniapp/                                # uni-app 小程序
│   ├── pages/
│   │   ├── login/login.vue                 # 登录页
│   │   ├── auth/auth.vue                   # 校园认证页
│   │   ├── index/index.vue                 # 首页
│   │   ├── search/search.vue               # 搜索页
│   │   ├── product/
│   │   │   ├── detail/detail.vue           # 商品详情
│   │   │   ├── publish/publish.vue         # 发布商品
│   │   │   └── my-list/my-list.vue         # 我发布的
│   │   ├── seller/profile.vue              # 卖家主页
│   │   ├── chat/
│   │   │   ├── list/list.vue               # 聊天列表
│   │   │   └── detail/detail.vue           # 聊天详情
│   │   ├── order/
│   │   │   ├── list/list.vue               # 订单列表
│   │   │   └── detail/detail.vue           # 订单详情
│   │   ├── review/review.vue               # 评价页
│   │   ├── favorite/favorite.vue           # 收藏列表
│   │   ├── notification/notification.vue   # 消息中心
│   │   ├── user/user.vue                   # 个人中心
│   │   ├── settings/
│   │   │   ├── settings.vue                # 设置页
│   │   │   ├── edit-profile.vue            # 编辑个人信息
│   │   │   └── about.vue                   # 关于我们
│   │   ├── report/report.vue               # 举报页
│   │   ├── agreement/agreement.vue         # 用户协议
│   │   ├── privacy/privacy.vue             # 隐私政策
│   │   └── help/help.vue                   # 帮助页
│   ├── components/
│   │   ├── product-card/product-card.vue
│   │   ├── order-card/order-card.vue
│   │   ├── user-avatar/user-avatar.vue
│   │   ├── empty-state/empty-state.vue
│   │   ├── status-tag/status-tag.vue
│   │   └── price/price.vue
│   ├── styles/
│   │   └── theme.css
│   ├── utils/
│   │   ├── request.js
│   │   ├── auth.js
│   │   ├── constant.js
│   │   └── mock.js                         # Mock数据（开发阶段）
│   ├── store/
│   │   ├── index.js
│   │   ├── user.js
│   │   └── app.js
│   ├── static/
│   ├── App.vue
│   ├── main.js
│   ├── manifest.json
│   ├── pages.json
│   └── uni.scss
├── admin/                                  # Vue 管理后台
│   ├── src/
│   │   ├── views/
│   │   │   ├── login/LoginView.vue
│   │   │   ├── dashboard/DashboardView.vue
│   │   │   ├── product/
│   │   │   │   ├── ProductReview.vue       # 商品审核
│   │   │   │   └── ProductList.vue         # 商品列表
│   │   │   ├── auth-review/AuthReview.vue
│   │   │   ├── user/UserList.vue
│   │   │   ├── order/OrderList.vue
│   │   │   ├── report/ReportList.vue
│   │   │   ├── category/CategoryList.vue
│   │   │   ├── campus/CampusList.vue
│   │   │   ├── college/CollegeList.vue
│   │   │   ├── banner/BannerList.vue
│   │   │   ├── notice/NoticeList.vue
│   │   │   └── employee/EmployeeList.vue
│   │   ├── components/
│   │   ├── api/
│   │   ├── router/
│   │   ├── store/
│   │   ├── utils/
│   │   ├── styles/
│   │   ├── App.vue
│   │   └── main.js
│   ├── public/
│   ├── package.json
│   └── vite.config.js
├── sql/
│   ├── init.sql                            # 全量初始化SQL
│   └── update/                             # 增量SQL
├── docs/
│   ├── PRD.md
│   └── AI-DEV-GUIDE.md
├── .gitignore
├── pom.xml
└── README.md
```
```
