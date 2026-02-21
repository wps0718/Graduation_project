---

---

# 📄 文档二：AI辅助开发指南（按模块拆分）

## 一、开发策略：前端先行

### 1.1 开发顺序

```
Phase 1: 项目初始化
  ├── 1.1 后端项目骨架搭建
  ├── 1.2 小程序项目骨架搭建
  └── 1.3 管理后台项目骨架搭建

Phase 2: 前端页面开发（Mock数据驱动）
  ├── 2.1 小程序基础框架（request封装、路由、主题、Mock数据）
  ├── 2.2 登录页
  ├── 2.3 首页
  ├── 2.4 搜索页
  ├── 2.5 商品详情页
  ├── 2.6 发布商品页
  ├── 2.7 个人中心页
  ├── 2.8 订单列表页
  ├── 2.9 消息中心页
  ├── 2.10 聊天列表/详情页
  ├── 2.11 其他页面（认证、收藏、评价、举报、设置、协议等）

Phase 3: 后端接口开发
  ├── 3.1 通用模块（Result、异常处理、JWT、文件上传）
  ├── 3.2 用户模块
  ├── 3.3 认证模块
  ├── 3.4 商品模块
  ├── 3.5 分类/校区/学院模块
  ├── 3.6 收藏模块
  ├── 3.7 订单模块
  ├── 3.8 评价模块
  ├── 3.9 举报模块
  ├── 3.10 消息通知模块
  ├── 3.11 搜索模块
  ├── 3.12 定时任务
  ├── 3.13 IM集成（OpenIM）

Phase 4: 前后端联调
  ├── 4.1 切换Mock为真实接口
  ├── 4.2 逐模块联调测试

Phase 5: 管理后台开发
  ├── 5.1 后台框架搭建
  ├── 5.2 各管理页面开发

Phase 6: 测试与上线
  ├── 6.1 功能测试
  ├── 6.2 性能测试
  ├── 6.3 小程序审核与发布
```

### 1.2 Mock数据策略

在前端先行开发阶段，在 `miniapp/utils/mock.js` 中维护Mock数据：

```javascript
// mock.js 示例
const USE_MOCK = true; // 联调时改为false

const mockData = {
  // 商品列表
  productList: {
    code: 1,
    data: {
      total: 3,
      records: [
        {
          id: 1,
          title: "iPad Air 4 64G 天蓝色",
          price: 2800.00,
          conditionText: "9成新",
          coverImage: "/static/mock/ipad.jpg",
          campusName: "南海北",
          createTime: "2小时前",
          seller: { nickName: "王同学", avatarUrl: "/static/mock/avatar.jpg", authStatus: 2, score: 4.9 }
        }
        // ...更多
      ]
    }
  }
  // ...其他接口的Mock
};
```

在 `request.js` 中统一封装：

```javascript
import { USE_MOCK, mockData } from './mock.js';

const request = (options) => {
  if (USE_MOCK) {
    return new Promise(resolve => {
      setTimeout(() => resolve(mockData[options.mockKey]), 300);
    });
  }
  // 真实请求逻辑...
};
```

---
## 二、各模块AI开发Prompt模板

# 📋 轻院二手 - 全模块AI开发Prompt（完整版）

## 目录

```
Phase 1: 项目初始化
  ├── Prompt 1.1: 后端项目骨架
  ├── Prompt 1.2: 小程序项目骨架
  └── Prompt 1.3: 管理后台项目骨架

Phase 2: 小程序前端页面开发（Mock驱动）
  ├── Prompt 2.1: 基础框架搭建（request/auth/mock/theme/store）
  ├── Prompt 2.2: 公共组件开发
  ├── Prompt 2.3: 登录页
  ├── Prompt 2.4: 校园认证页
  ├── Prompt 2.5: 首页
  ├── Prompt 2.6: 搜索页
  ├── Prompt 2.7: 商品详情页
  ├── Prompt 2.8: 发布商品页
  ├── Prompt 2.9: 我发布的商品页
  ├── Prompt 2.10: 卖家主页
  ├── Prompt 2.11: 个人中心页
  ├── Prompt 2.12: 设置页 + 编辑个人信息页
  ├── Prompt 2.13: 收藏列表页
  ├── Prompt 2.14: 订单列表页
  ├── Prompt 2.15: 评价页
  ├── Prompt 2.16: 举报页
  ├── Prompt 2.17: 消息中心页
  ├── Prompt 2.18: 聊天列表页 + 聊天详情页
  ├── Prompt 2.19: 用户协议 + 隐私政策 + 帮助页

Phase 3: 后端接口开发
  ├── Prompt 3.1: 通用模块（Result/异常/JWT/文件上传/枚举/常量）
  ├── Prompt 3.2: 数据库初始化SQL
  ├── Prompt 3.3: 用户模块
  ├── Prompt 3.4: 校园认证模块
  ├── Prompt 3.5: 分类模块
  ├── Prompt 3.6: 校区与面交地点模块
  ├── Prompt 3.7: 学院模块
  ├── Prompt 3.8: 商品模块
  ├── Prompt 3.9: 收藏模块
  ├── Prompt 3.10: 订单模块
  ├── Prompt 3.11: 评价模块
  ├── Prompt 3.12: 举报模块
  ├── Prompt 3.13: 消息通知模块
  ├── Prompt 3.14: Banner模块
  ├── Prompt 3.15: 搜索模块
  ├── Prompt 3.16: 公告模块
  ├── Prompt 3.17: 数据统计模块
  ├── Prompt 3.18: 管理员/员工模块
  ├── Prompt 3.19: 定时任务
  ├── Prompt 3.20: OpenIM集成

Phase 4: 前后端联调
  ├── Prompt 4.1: 小程序Mock切换真实接口

Phase 5: 管理后台页面开发
  ├── Prompt 5.1: 后台基础框架（路由/布局/登录）
  ├── Prompt 5.2: 数据概览Dashboard
  ├── Prompt 5.3: 商品审核页
  ├── Prompt 5.4: 商品列表页
  ├── Prompt 5.5: 认证审核页
  ├── Prompt 5.6: 用户管理页
  ├── Prompt 5.7: 订单管理页
  ├── Prompt 5.8: 举报管理页
  ├── Prompt 5.9: 分类管理页
  ├── Prompt 5.10: 校区管理页
  ├── Prompt 5.11: 学院管理页
  ├── Prompt 5.12: Banner管理页
  ├── Prompt 5.13: 公告管理页
  ├── Prompt 5.14: 员工管理页
```

---

# Phase 1: 项目初始化

---

## Prompt 1.1: 后端项目骨架

```markdown
【任务】创建「轻院二手」后端Spring Boot项目骨架

【项目信息】
- 项目名：qingyuan-secondhand
- GroupId：com.qingyuan
- ArtifactId：secondhand
- 包路径：com.qingyuan.secondhand
- JDK：17
- Spring Boot：3.x（最新稳定版）
- 构建工具：Maven

【需要引入的依赖】
1. spring-boot-starter-web
2. spring-boot-starter-validation（参数校验）
3. mybatis-plus-spring-boot3-starter（MyBatis-Plus，注意Spring Boot 3兼容版本）
4. mysql-connector-j（MySQL驱动）
5. spring-boot-starter-data-redis（Redis）
6. lombok
7. spring-boot-starter-aop（AOP）
8. jjwt-api + jjwt-impl + jjwt-jackson（JWT，0.12.x版本）
9. spring-boot-starter-test
10. hutool-all（工具库）
11. commons-io（文件操作）

【需要生成的文件】

1. pom.xml - 完整Maven配置

2. src/main/resources/application.yml
   ```yaml
   server:
     port: 8080
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/qingyuan_secondhand?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8mb4
       username: root
       password: root
       driver-class-name: com.mysql.cj.jdbc.Driver
     data:
       redis:
         host: localhost
         port: 6379
         database: 0
     servlet:
       multipart:
         max-file-size: 5MB
         max-request-size: 50MB
   mybatis-plus:
     configuration:
       map-underscore-to-camel-case: true
       log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
     global-config:
       db-config:
         id-type: auto
   # JWT配置
   jwt:
     secret: qingyuan-secondhand-jwt-secret-key-2025
     expiration: 86400000  # 24小时
     admin-expiration: 86400000
   # 文件上传配置
   upload:
     path: ./uploads/
     url-prefix: /uploads/
   ```

3. SecondhandApplication.java - 启动类

4. config/WebMvcConfig.java
   - 注册JWT拦截器
   - 配置静态资源映射（uploads目录）
   - 配置CORS（允许管理后台跨域）

5. config/MyBatisPlusConfig.java
   - 分页插件配置

6. config/RedisConfig.java
   - RedisTemplate序列化配置

7. common/result/Result.java
   ```java
   // 统一响应类
   // code=1成功，code=0失败
   // 静态方法：success(), success(data), error(msg)
   ```

8. common/exception/BusinessException.java
   - 自定义业务异常类

9. common/exception/GlobalExceptionHandler.java
   - @RestControllerAdvice
   - 处理BusinessException → code=0
   - 处理MethodArgumentNotValidException → code=0（参数校验失败）
   - 处理Exception → code=0 + 日志记录

10. common/util/JwtUtil.java
    - createToken(Long userId, Map<String, Object> claims)
    - parseToken(String token) → Claims
    - getUserId(String token) → Long
    - 区分小程序端token和管理端token

11. common/util/FileUtil.java
    - upload(MultipartFile file, String type) → String url
    - 校验文件类型（仅JPG/PNG/JPEG）
    - 校验文件大小（≤5MB）
    - 文件重命名为UUID
    - 按日期分目录存储：/uploads/{type}/{yyyy}/{MM}/{dd}/{uuid}.{ext}

12. common/interceptor/JwtInterceptor.java
    - 小程序端JWT拦截器
    - 从Header获取Authorization: Bearer {token}
    - 解析token，将userId存入ThreadLocal
    - token无效或过期返回code=401

13. common/interceptor/AdminJwtInterceptor.java
    - 管理后台JWT拦截器
    - 类似JwtInterceptor，但解析管理员信息

14. common/context/UserContext.java
    - ThreadLocal存储当前登录用户ID
    - getCurrentUserId()
    - setCurrentUserId()
    - removeCurrentUserId()

15. common/constant/RedisConstant.java
    - 所有Redis Key的常量定义

16. common/enums/ 目录下所有枚举类：
    - ProductStatus（0待审核/1在售/2已下架/3已售出/4审核驳回）
    - OrderStatus（1待面交/3已完成/4已评价/5已取消）
    - AuthStatus（0未认证/1审核中/2已认证/3已驳回）
    - ConditionLevel（1全新/2几乎全新/3九成新/4八成新/5七成新及以下）
    - ReportReason（1虚假商品/2违禁物品/3诈骗信息/4不当内容/5其他）
    - ReportStatus（0待处理/1已处理/2已忽略）
    - NotificationType（1-10各类型）
    - NotificationCategory（1交易/2系统）
    - UserStatus（0封禁/1正常/2注销中）
    - BannerLinkType（1商品详情/2活动页/3外部链接）

17. controller/common/CommonController.java
    - POST /common/upload → 文件上传接口

【编码规范】
- 使用构造函数注入
- 使用@Slf4j
- 注释清晰
- 所有枚举类包含code和description字段，提供getByCode方法

【项目结构】
请严格按照以下目录结构生成：
src/main/java/com/qingyuan/secondhand/
├── config/
├── controller/
│   ├── admin/
│   ├── mini/
│   └── common/
├── entity/
├── mapper/
├── service/
│   └── impl/
├── dto/
├── vo/
├── common/
│   ├── exception/
│   ├── result/
│   ├── util/
│   ├── interceptor/
│   ├── context/
│   ├── constant/
│   └── enums/
├── task/
└── SecondhandApplication.java
```

---

## Prompt 1.2: 小程序项目骨架

```markdown
【任务】初始化uni-app小程序项目骨架

【项目信息】
- 项目名：qingyuan-secondhand-miniapp
- 框架：uni-app（Vue 3 + Composition API）
- 目标平台：微信小程序
- 目录位于项目根目录的 miniapp/ 下

【需要生成】

1. pages.json - 页面路由配置
   页面列表：
   - pages/login/login（登录页，非tabBar页）
   - pages/index/index（首页）★tabBar
   - pages/search/search（搜索页）
   - pages/product/detail/detail（商品详情）
   - pages/product/publish/publish（发布商品）★tabBar（中间按钮）
   - pages/product/my-list/my-list（我发布的）
   - pages/product/edit/edit（编辑商品）
   - pages/chat/list/list（消息/聊天列表）★tabBar
   - pages/chat/detail/detail（聊天详情）
   - pages/order/list/list（订单列表）
   - pages/order/detail/detail（订单详情）
   - pages/user/user（个人中心）★tabBar
   - pages/auth/auth（校园认证）
   - pages/favorite/favorite（收藏列表）
   - pages/notification/notification（消息中心）
   - pages/review/review（评价页）
   - pages/report/report（举报页）
   - pages/seller/profile（卖家主页）
   - pages/settings/settings（设置）
   - pages/settings/edit-profile（编辑个人信息）
   - pages/settings/about（关于我们）
   - pages/agreement/agreement（用户协议）
   - pages/privacy/privacy（隐私政策）
   - pages/help/help（帮助中心）

   tabBar配置：
   - 4个tab：首页(home)、发布(add，中间突出)、消息(chat)、我的(user)
   - 导航栏背景色使用品牌蓝色
   - tabBar选中色使用主色，未选中使用灰色

   全局window配置：
   - navigationBarBackgroundColor: 品牌蓝色
   - navigationBarTitleText: 轻院二手
   - navigationBarTextStyle: white
   - backgroundColor: #F5F5F5
   - enablePullDownRefresh: false（各页面按需开启）

2. styles/theme.css - 主题颜色变量
   ```css
   :root {
     /* 主色 */
     --primary-color: #4A90D9;
     --primary-light: #6BA3E0;
     --primary-dark: #3A7BC8;
     --primary-bg: #EBF3FB;
     
     /* 功能色 */
     --success-color: #52C41A;
     --warning-color: #FAAD14;
     --danger-color: #FF4D4F;
     --info-color: #909399;
     
     /* 价格色 */
     --price-color: #FF4D4F;
     
     /* 文字色 */
     --text-primary: #333333;
     --text-regular: #666666;
     --text-secondary: #999999;
     --text-placeholder: #CCCCCC;
     --text-white: #FFFFFF;
     
     /* 背景色 */
     --bg-page: #F5F5F5;
     --bg-white: #FFFFFF;
     --bg-grey: #F8F8F8;
     --bg-mask: rgba(0, 0, 0, 0.5);
     
     /* 边框色 */
     --border-color: #EEEEEE;
     --border-light: #F5F5F5;
     
     /* 字号 */
     --font-xl: 36rpx;
     --font-lg: 32rpx;
     --font-md: 28rpx;
     --font-sm: 24rpx;
     --font-xs: 20rpx;
     
     /* 间距 */
     --spacing-xs: 8rpx;
     --spacing-sm: 16rpx;
     --spacing-md: 24rpx;
     --spacing-lg: 32rpx;
     --spacing-xl: 48rpx;
     
     /* 圆角 */
     --radius-sm: 8rpx;
     --radius-md: 12rpx;
     --radius-lg: 16rpx;
     --radius-round: 999rpx;
   }
   ```

3. uni.scss - 全局样式变量（uni-app要求）
   - 引入theme.css
   - 定义全局通用样式类（flex布局、文本省略、安全区域等）

4. App.vue - 根组件
   - onLaunch中检查登录态
   - 引入全局样式

5. main.js - 入口文件
   - 引入store
   - 全局挂载

6. manifest.json - 小程序配置
   - appid占位
   - 微信小程序相关配置

【注意】
- 此Prompt只生成项目骨架和配置文件
- 页面文件先创建空白占位vue文件（包含基本template/script/style结构）
- 所有页面的style中通过 @import '../../styles/theme.css' 引入主题（或在App.vue全局引入）
```

---

## Prompt 1.3: 管理后台项目骨架

```markdown
【任务】初始化Vue 3管理后台项目骨架

【项目信息】
- 项目名：qingyuan-secondhand-admin
- 框架：Vue 3 + Vite + Element Plus + Vue Router + Pinia
- 目录位于项目根目录的 admin/ 下

【需要生成】

1. package.json - 依赖配置
   - vue@3.x
   - vue-router@4.x
   - pinia
   - element-plus
   - @element-plus/icons-vue
   - axios
   - echarts（数据图表）
   - dayjs（日期处理）
   - nprogress（路由加载进度条）

2. vite.config.js
   - 开发代理：/api → http://localhost:8080
   - Element Plus按需引入

3. src/main.js
   - 注册Element Plus
   - 注册全局图标
   - 注册Pinia
   - 注册Router

4. src/App.vue - 根组件

5. src/router/index.js
   - 路由配置（含路由守卫，未登录跳转登录页）
   - 路由列表：
     /login → LoginView（不需要布局）
     / → Layout布局组件
       /dashboard → DashboardView（数据概览）
       /product/review → ProductReview（商品审核）
       /product/list → ProductList（商品列表）
       /auth-review → AuthReview（认证审核）
       /user → UserList（用户管理）
       /order → OrderList（订单管理）
       /report → ReportList（举报管理）
       /category → CategoryList（分类管理）
       /campus → CampusList（校区管理）
       /college → CollegeList（学院管理）
       /banner → BannerList（Banner管理）
       /notice → NoticeList（公告管理）
       /employee → EmployeeList（员工管理）

6. src/layout/Layout.vue - 后台布局组件
   - 左侧菜单栏（可折叠）
   - 顶部导航栏（显示管理员姓名、退出登录）
   - 右侧内容区（router-view）
   - 菜单项对应路由列表

7. src/store/user.js - Pinia用户状态
   - token存储/获取
   - 管理员信息
   - 登录/登出方法

8. src/utils/request.js - Axios封装
   - baseURL: '/api'
   - 请求拦截器：添加Authorization header
   - 响应拦截器：统一处理code判断
   - 401跳转登录页
   - 错误提示使用ElMessage

9. src/utils/auth.js
   - getToken/setToken/removeToken（localStorage）

10. src/api/ 目录 - 按模块创建空文件
    - auth.js（登录相关）
    - product.js（商品相关）
    - user.js（用户相关）
    - order.js（订单相关）
    - category.js（分类相关）
    - campus.js（校区相关）
    - college.js（学院相关）
    - banner.js（Banner相关）
    - report.js（举报相关）
    - notice.js（公告相关）
    - employee.js（员工相关）
    - stats.js（数据统计相关）
    - campusAuth.js（认证审核相关）

11. src/views/ 目录 - 各页面创建空白占位Vue文件
    - 每个文件包含基本的template+script setup+style结构

12. src/styles/index.css - 全局样式
    - 重置样式
    - 通用样式类

【要求】
- Layout组件菜单栏需要有分组：
  - 工作台：数据概览
  - 审核管理：商品审核、认证审核
  - 业务管理：商品列表、订单管理、用户管理、举报管理
  - 内容管理：Banner管理、公告管理
  - 系统管理：分类管理、校区管理、学院管理、员工管理
- 菜单项需要图标（使用Element Plus Icons）
```

---

# Phase 2: 小程序前端页面开发

---

## Prompt 2.1: 基础框架搭建

```markdown
【任务】搭建小程序基础框架（工具类和Mock数据）

【项目上下文】
- 项目：轻院二手 校园二手交易微信小程序
- 框架：uni-app（Vue 3 Composition API）
- 已有文件：pages.json、styles/theme.css、App.vue、main.js、manifest.json
- 当前阶段：前端先行开发，使用Mock数据

【需要生成】

### 1. utils/request.js - 网络请求封装
```javascript
// 功能要求：
// 1. 支持Mock模式开关（const USE_MOCK = true）
// 2. Mock模式下根据url匹配mockData返回数据，模拟300ms延迟
// 3. 真实模式下使用uni.request
// 4. 统一添加Authorization: Bearer {token} header
// 5. 统一处理响应：
//    - code=1 → resolve(data)
//    - code=0 → reject + uni.showToast显示msg
//    - code=401 → 清除登录态 + 跳转登录页
// 6. 请求loading控制（可选参数showLoading）
// 7. 导出 get(url, params) 和 post(url, data) 方法
// 8. BASE_URL配置
```

### 2. utils/auth.js - 登录态管理
```javascript
// 功能：
// getToken() / setToken(token) / removeToken()
// getUserInfo() / setUserInfo(info) / removeUserInfo()
// isLogin() → boolean
// logout() → 清除所有登录信息
// 使用uni.setStorageSync / uni.getStorageSync
```

### 3. utils/constant.js - 常量定义
```javascript
// 包含：
// BASE_URL（后端地址）
// CONDITION_LEVELS: [{value:1,label:'全新'},{value:2,label:'几乎全新(99新)'},...] 
// ORDER_STATUS: {PENDING:1, COMPLETED:3, REVIEWED:4, CANCELLED:5}
// ORDER_STATUS_TEXT: {1:'待面交', 3:'已完成', 4:'已评价', 5:'已取消'}
// AUTH_STATUS: {NONE:0, PENDING:1, VERIFIED:2, REJECTED:3}
// AUTH_STATUS_TEXT: {0:'未认证', 1:'审核中', 2:'已认证', 3:'已驳回'}
// PRODUCT_STATUS: {PENDING:0, ON_SALE:1, OFF_SHELF:2, SOLD:3, REJECTED:4}
// PRODUCT_STATUS_TEXT: ...
// REPORT_REASONS: [{value:1,label:'虚假商品'},{value:2,label:'违禁物品'},...] 
// QUICK_REPLIES: ['还在吗？', '可以小刀吗？', '什么时候方便？']
```

### 4. utils/mock.js - Mock数据
```javascript
// 完整的Mock数据，覆盖所有页面需要的接口数据
// 按接口URL作为key组织

// 需要包含的Mock数据：

// 用户相关
// POST /mini/user/wx-login → 返回token和用户信息
// POST /mini/user/login → 同上
// POST /mini/user/sms-login → 同上
// POST /mini/user/sms/send → 成功
// GET /mini/user/info → 用户信息（王同学，已认证，4.9分）
// GET /mini/user/stats → {onSaleCount:12, soldCount:28, favoriteCount:56}
// GET /mini/user/profile/10001 → 卖家主页信息

// 认证相关
// POST /mini/auth/submit → 成功
// GET /mini/auth/status → {status:2, ...}
// GET /mini/college/list → [{id:1,name:'计算机学院'},{id:2,name:'机电学院'},...]

// 商品相关
// GET /mini/product/list → 分页商品列表（至少5条不同商品：iPad、教材、自行车、耳机、台灯）
// GET /mini/product/detail/1 → iPad详情（含卖家信息、收藏状态等）
// GET /mini/product/my-list → 我发布的商品（包含不同状态）
// POST /mini/product/publish → 成功
// POST /mini/product/update → 成功
// POST /mini/product/update-price → 成功
// POST /mini/product/off-shelf → 成功
// POST /mini/product/on-shelf → 成功

// 分类相关
// GET /mini/category/list → [{id:1,name:'书籍',icon:'📚'}, {id:2,name:'服饰',icon:'👕'}, {id:3,name:'生活',icon:'🏠'}, {id:4,name:'电子设备',icon:'📱'}, {id:5,name:'运动设备',icon:'⚽'}, {id:6,name:'潮玩娱乐',icon:'🎮'}]

// 校区相关
// GET /mini/campus/list → [{id:1,name:'南海北',code:'nanhai_north'}, {id:2,name:'南海南',code:'nanhai_south'}, {id:3,name:'新港',code:'xingang'}]
// GET /mini/campus/meeting-points/1 → [{id:1,name:'一饭门口'},{id:2,name:'图书馆门口'},{id:3,name:'南门快递站'}]

// Banner
// GET /mini/banner/list → [{id:1,title:'学长学姐闲置大甩卖',image:'/static/mock/banner1.jpg',linkType:2,linkUrl:'/pages/search/search'}]

// 收藏
// POST /mini/favorite/add → 成功
// POST /mini/favorite/cancel → 成功
// GET /mini/favorite/list → 收藏的商品列表
// GET /mini/favorite/check/1 → {isFavorited: false}

// 订单
// POST /mini/order/create → {orderId:1, orderNo:'TD20250205143000001234', expireTime:'2025-02-08 14:30:00'}
// GET /mini/order/list → 订单列表（包含不同状态的订单）
// GET /mini/order/detail/1 → 订单详情
// POST /mini/order/confirm → 成功
// POST /mini/order/cancel → 成功

// 评价
// POST /mini/review/submit → 成功
// GET /mini/review/detail/1 → 评价详情

// 举报
// POST /mini/report/submit → 成功

// 消息通知
// GET /mini/notification/list → 消息列表（包含各种类型）
// GET /mini/notification/unread-count → {total:5, trade:3, system:2}
// POST /mini/notification/read → 成功
// POST /mini/notification/read-all → 成功

// 搜索
// GET /mini/search/hot-keywords → [{keyword:'升本英语'},{keyword:'计算器'},{keyword:'台灯'},{keyword:'自行车'},{keyword:'教材'},{keyword:'耳机'},{keyword:'洗衣机'},{keyword:'升本政治'}]

// 文件上传
// POST /common/upload → {url:'/static/mock/uploaded.jpg'}

// 每条Mock数据要符合PRD中定义的响应格式：{code:1, msg:'success', data:{...}}
// 商品数据要丰富真实，包含不同分类、不同校区、不同价格的商品
```

### 5. store/index.js + store/user.js + store/app.js - 状态管理
```javascript
// store/user.js：
// - state: token, userInfo, isLogin
// - actions: login(data), logout(), setUserInfo(info), updateStats()

// store/app.js：
// - state: currentCampusId, currentCampusName, campusList, categoryList
// - actions: setCampus(id, name), loadCampusList(), loadCategoryList()
```

【要求】
- Mock数据要覆盖全面，每个页面开发时都能直接使用
- Mock数据要真实，如同学姓名用"王同学""张同学""李同学"
- 商品数据要多样化（至少5-8条不同商品）
- request.js在Mock模式下模拟网络延迟（300ms）
- 所有文件使用ES6+语法
- 代码注释清晰
```

---

## Prompt 2.2: 公共组件开发

```markdown
【任务】开发小程序公共复用组件

【项目上下文】
- 项目：轻院二手 uni-app小程序
- 已有：pages.json、theme.css、request.js、auth.js、constant.js、mock.js、store
- 风格参考：原型图中的蓝色系简洁风格

【需要生成的组件】

### 1. components/product-card/product-card.vue - 商品卡片组件
- Props：product对象（id, title, price, originalPrice, conditionLevel, conditionText, coverImage, campusName, createTime, seller{nickName, avatarUrl, authStatus, score}）
- 展示：左侧商品图片（固定宽高比）+ 右侧信息（标题最多两行省略、磨损程度标签、校区、底部价格+时间）
- 点击整个卡片跳转商品详情页
- 样式使用theme.css变量
- 价格使用红色（--price-color），带¥符号

### 2. components/price/price.vue - 价格展示组件
- Props：price(Number), originalPrice(Number, 可选), size('lg'/'md'/'sm')
- 展示：¥符号 + 价格数值，如果有原价则显示删除线原价
- 价格颜色使用--price-color

### 3. components/empty-state/empty-state.vue - 空状态组件
- Props：type('no-data'/'no-result'/'no-order'/'no-message'/'no-favorite'/'network-error'), text(String, 可选自定义文案)
- 展示：图标/图片 + 文案
- 居中展示
- 默认文案：
  - no-data: 暂无数据
  - no-result: 未找到相关商品
  - no-order: 暂无订单记录
  - no-message: 暂无消息
  - no-favorite: 暂无收藏
  - network-error: 网络异常，请重试

### 4. components/user-avatar/user-avatar.vue - 用户头像组件
- Props：avatarUrl(String), nickName(String), authStatus(Number), size('lg'/'md'/'sm'), showAuth(Boolean, 默认true)
- 展示：圆形头像 + 右下角认证标识（已认证显示蓝色勾）
- 无头像时显示昵称首字符
- size对应的头像大小：lg=120rpx, md=80rpx, sm=60rpx

### 5. components/status-tag/status-tag.vue - 状态标签组件
- Props：type('auth'/'product'/'order'/'condition'), value(Number)
- 根据type和value显示对应的文本和颜色：
  - auth: 未认证(灰)/审核中(橙)/已认证(绿)/已驳回(红)
  - product: 待审核(橙)/在售(绿)/已下架(灰)/已售出(蓝)/已驳回(红)
  - order: 待面交(橙)/已完成(绿)/已评价(蓝)/已取消(灰)
  - condition: 全新(绿)/几乎全新(绿)/9成新(蓝)/8成新(橙)/7成新及以下(灰)
- 小圆角标签样式

### 6. components/order-card/order-card.vue - 订单卡片组件
- Props：order对象, role('buyer'/'seller')
- 展示：商品图片+标题+价格 + 对方用户信息 + 状态标签 + 时间/校区
- 操作按钮根据status和role动态显示：
  - 待面交+买家：联系卖家、取消交易
  - 待面交+卖家：联系买家、取消交易
  - 已完成：去评价
  - 已评价+买家：查看评价、再次购买
  - 已评价+卖家：查看评价
  - 已取消：删除订单
- 点击卡片跳转订单详情
- 操作按钮点击使用emit通知父组件

### 7. components/navbar/navbar.vue - 自定义导航栏（可选）
- 适配不同机型的状态栏高度
- Props：title, showBack(Boolean), customLeft(slot)

【要求】
- 所有组件使用Vue 3 Composition API（<script setup>）
- 所有颜色使用theme.css变量
- 组件要有良好的Props类型定义和默认值
- 样式使用scoped
- 代码注释说明Props和用法
```

---

## Prompt 2.3: 登录页

```markdown
【任务】开发小程序登录页

【页面路径】pages/login/login

【项目上下文】
- 框架：uni-app Vue 3 Composition API
- 已有工具：request.js（Mock模式）、auth.js、store/user.js、theme.css
- 所有颜色使用theme.css CSS变量

【原型描述】
- 顶部区域（占屏幕约35%）：
  - 品牌Logo图标（居中）
  - 品牌名"轻院二手"（大字）
  - Slogan "让闲置物品流转起来"（小字）
  - "校园二手交易"（小字）
- 中间表单区域：
  - Tab切换：「账号登录」|「短信验证登录」
  - 账号登录Tab：
    - 手机号输入框（带手机图标前缀，placeholder="请输入手机号"）
    - 密码输入框（带锁图标前缀，placeholder="请输入密码"，password模式）
    - 登录按钮（品牌蓝色，圆角，全宽）
  - 短信验证Tab：
    - 手机号输入框
    - 验证码输入框 + 右侧「获取验证码」按钮（倒计时60秒）
    - 登录按钮
- 底部区域：
  - 分割线 + "其他登录方式"
  - 微信登录图标按钮（圆形微信logo）
  - 最底部：勾选框 + "登录即代表同意《用户协议》和《隐私政策》"
    - 《用户协议》和《隐私政策》为蓝色可点击文字

【调用接口（Mock模式）】
- POST /mini/user/login → mockKey: 'userLogin'
- POST /mini/user/sms/send → mockKey: 'smsSend'
- POST /mini/user/sms-login → mockKey: 'smsLogin'
- POST /mini/user/wx-login → mockKey: 'wxLogin'

【交互逻辑】
1. 默认显示「账号登录」Tab
2. 切换Tab时清空对应表单数据
3. 手机号格式校验（11位数字），输入非数字字符自动过滤
4. 密码不能为空
5. 获取验证码：
   - 校验手机号格式
   - 点击后按钮变为"60s后重试"倒计时，倒计时期间不可点击
   - 倒计时结束恢复为"获取验证码"
6. 验证码为6位数字
7. 登录前必须勾选用户协议，未勾选时点击登录按钮提示"请先同意用户协议和隐私政策"
8. 登录成功后：
   - 存储token到本地（auth.setToken）
   - 存储用户信息到store
   - 跳转首页（uni.switchTab）
9. 微信登录：
   - 调用uni.login获取code
   - 将code发送给后端换取token
   - 后续流程同上
10. 《用户协议》点击跳转 /pages/agreement/agreement
11. 《隐私政策》点击跳转 /pages/privacy/privacy
12. 页面设置：不显示导航栏（自定义导航或隐藏）

【请生成】
- pages/login/login.vue（完整页面代码）
- 使用<script setup>语法
- 样式中所有颜色使用CSS变量
```

---

## Prompt 2.4: 校园认证页

```markdown
【任务】开发小程序校园认证页

【页面路径】pages/auth/auth

【项目上下文】
- 已有：request.js、auth.js、constant.js、theme.css、store
- 所有颜色使用theme.css CSS变量
- 使用Vue 3 Composition API

【原型描述】
- 顶部：Logo + "轻院二手" + Slogan
- 表单区域标题："登录并认证" 或 "校园认证"
- 表单字段：
  - 学院（下拉选择，数据来自接口）
  - 学号（输入框）
  - 班级（输入框）
  - 认证材料上传区域："请上传一卡通或者3.0截图"
    - 点击上传区域选择图片
    - 上传后显示预览图，可点击重新选择
    - 支持JPG/PNG/JPEG，最大5MB
  - 「确定」提交按钮

【调用接口（Mock）】
- GET /mini/college/list → 学院列表
- POST /common/upload → 上传图片
- POST /mini/auth/submit → 提交认证
- GET /mini/auth/status → 查询认证状态

【交互逻辑】
1. 进入页面先查询当前认证状态：
   - 未认证 / 已驳回 → 显示认证表单
   - 审核中 → 显示"认证审核中，请耐心等待"状态页
   - 已认证 → 显示"已认证"状态页 + 认证信息展示
   - 已驳回 → 显示驳回原因 + 可重新填写表单
2. 学院列表在onLoad时加载
3. 图片上传：
   - 调用uni.chooseImage选择图片
   - 选择后调用上传接口获取图片URL
   - 显示上传进度或loading
4. 提交前校验：学院必选、学号必填（纯数字）、班级必填、认证材料必传
5. 提交成功后显示"提交成功，请等待审核"提示

【请生成】
- pages/auth/auth.vue
```

---

## Prompt 2.5: 首页

```markdown
【任务】开发小程序首页

【页面路径】pages/index/index

【项目上下文】
- 已有：request.js、auth.js、constant.js、mock.js、theme.css、store
- 已有组件：product-card、empty-state
- 使用Vue 3 Composition API

【原型描述】
- 顶部导航区域：
  - 左侧：校区名称（如"广轻大 南海校区"），后面有下拉箭头图标，点击弹出校区选择器
  - 右侧：搜索输入框占位（不可输入，点击跳转搜索页）
- Banner轮播区域：
  - 圆角轮播图
  - 自动轮播间隔5秒
  - 支持手动左右滑动
  - 点击跳转对应链接
- 分类入口区域：
  - 横向排列分类图标（图标+文字）
  - 一行展示，如果超出可左右滑动或换行
  - 分类：书籍📚、服饰👕、生活🏠、电子设备📱、运动设备⚽、潮玩娱乐🎮、更多➕
  - 点击分类跳转搜索页（带categoryId参数）
- "最新发布"区域标题
- 商品列表区域：
  - 使用product-card组件
  - 列表形式（非瀑布流），一行一个
  - 无数据时显示empty-state组件

【调用接口（Mock）】
- GET /mini/campus/list → 校区列表
- GET /mini/banner/list?campusId={id} → Banner列表
- GET /mini/category/list → 分类列表
- GET /mini/product/list?page=1&pageSize=20&campusId={id}&sortBy=latest → 商品列表

【交互逻辑】
1. onLoad：
   - 从本地存储读取上次选择的校区（默认第一个校区）
   - 加载校区列表、Banner、分类、商品列表
2. 校区切换：
   - 点击校区名称弹出uni.showActionSheet或picker选择校区
   - 切换后存储到本地 + 更新store
   - 重新加载Banner和商品列表
3. 搜索框点击：uni.navigateTo跳转/pages/search/search
4. Banner点击：根据linkType跳转（商品详情/页面/外链）
5. 分类点击：uni.navigateTo跳转/pages/search/search?categoryId={id}
6. 商品卡片点击：跳转商品详情
7. 下拉刷新：enablePullDownRefresh=true，重新加载所有数据
8. 上拉加载：page++，追加商品数据，无更多数据时提示"没有更多了"
9. 页面每次onShow时刷新商品列表（因为发布/交易后数据可能变化）

【请生成】
- pages/index/index.vue
- 如product-card组件已有则直接使用，如未有则同时生成
```

---

## Prompt 2.6: 搜索页

```markdown
【任务】开发小程序搜索页

【页面路径】pages/search/search

【项目上下文】
- 已有：request.js、mock.js、theme.css、store、product-card组件、empty-state组件
- 使用Vue 3 Composition API

【原型描述】
- 顶部搜索栏：
  - 返回箭头 + 搜索输入框（自动聚焦）+ 搜索按钮
  - 输入框右侧有清除X按钮
- 搜索前状态（未输入或输入框为空时）：
  - 搜索历史区域：
    - 标题"搜索历史" + 右侧清空图标
    - 标签云形式展示历史关键词（圆角标签）
    - 点击标签直接搜索
    - 最多展示20条
  - 热门搜索区域：
    - 标题"热门搜索"
    - 标签云形式展示热门关键词
    - 点击标签直接搜索
- 搜索后状态（有搜索结果时）：
  - 筛选条件栏：
    - 全部分类（下拉）| 校区选择（下拉）| 价格不限（下拉：不限/自定义区间）| 排序（最新发布/价格↑/价格↓）
  - 搜索结果数量提示："搜索结果 128件"
  - 商品列表（使用product-card组件）
  - 无结果时显示empty-state(type='no-result')

【接收页面参数】
- categoryId（可选，从首页分类入口跳转时携带）
- keyword（可选）

【调用接口（Mock）】
- GET /mini/search/hot-keywords → 热门搜索
- GET /mini/category/list → 分类列表（筛选用）
- GET /mini/campus/list → 校区列表（筛选用）
- GET /mini/product/list?page=1&pageSize=20&keyword=xxx&campusId=&categoryId=&sortBy=latest&minPrice=&maxPrice=

【交互逻辑】
1. 如果携带categoryId参数，直接设置分类筛选并执行搜索
2. 搜索历史：本地存储（uni.setStorageSync('searchHistory', [...])），最多20条，最新在前
3. 清空搜索历史：弹窗确认后清空
4. 执行搜索时：
   - 将关键词存入搜索历史
   - 调用商品列表接口
   - 显示搜索结果
5. 筛选条件变化时自动重新搜索（page重置为1）
6. 支持上拉加载更多
7. 价格自定义区间：点击后弹出输入框填写最低价和最高价

【请生成】
- pages/search/search.vue
```

---

## Prompt 2.7: 商品详情页

```markdown
【任务】开发小程序商品详情页

【页面路径】pages/product/detail/detail

【项目上下文】
- 已有：request.js、mock.js、theme.css、store、auth.js
- 已有组件：price、user-avatar、status-tag、empty-state
- 使用Vue 3 Composition API

【原型描述】
- 顶部图片轮播区域：
  - swiper组件，展示所有商品图片
  - 右下角显示当前第几张/总张数（如 1/5）
  - 点击图片调用uni.previewImage全屏预览
- 价格区域：
  - 二手价格（红色大字 ¥2,800）
  - 原价（灰色删除线 ¥4,799）
  - 磨损程度标签（如"9成新"）
- 商品描述区域：
  - 描述文字（多行展示）
- 标签区域：
  - 分类标签（如"电子产品""平板""Apple"）
- 数据区域：
  - 浏览 234 · 收藏 12 · 2小时前发布
- 卖家信息区域：
  - 头像 + 昵称 + 认证标识 + 评分
  - 整行可点击，跳转卖家主页
  - 右侧箭头图标
- 面交信息区域：
  - 图标 + "南海北 · 一饭门口"
- 底部固定操作栏（安全区域适配）：
  - 左侧：☆收藏按钮（显示收藏数，已收藏变实心红色）
  - 中间：⚠举报按钮
  - 右侧：「我想要」大按钮（品牌蓝色，圆角）
- 右上角"⋮"更多菜单：
  - 自己的商品：编辑、下架/上架、删除
  - 别人的商品：分享、举报

【接收参数】
- id：商品ID

【调用接口（Mock）】
- GET /mini/product/detail/{id}
- POST /mini/favorite/add
- POST /mini/favorite/cancel
- GET /mini/favorite/check/{id}

【交互逻辑】
1. onLoad通过参数id加载商品详情
2. 如果已登录，查询是否已收藏
3. 图片点击预览（uni.previewImage，传入所有图片urls和当前index）
4. 收藏/取消收藏：
   - 未登录提示去登录
   - 切换收藏状态，更新收藏数（本地+1/-1）
5. 「我想要」：
   - 未登录提示去登录
   - 自己的商品提示"不能购买自己的商品"
   - 跳转聊天详情页：/pages/chat/detail/detail?userId={sellerId}&productId={id}
6. 举报：跳转/pages/report/report?targetType=1&targetId={id}
7. 右上角菜单：uni.showActionSheet
   - 编辑→跳转/pages/product/edit/edit?id={id}
   - 下架→确认弹窗→调用下架接口→成功后返回上一页
   - 删除→确认弹窗→调用删除接口→成功后返回上一页
   - 分享→触发onShareAppMessage
8. 微信分享：onShareAppMessage返回 {title: 商品标题, path: '/pages/product/detail/detail?id=xxx', imageUrl: 首图url}
9. 浏览计数：详情加载成功后自动计入（后端处理去重）

【请生成】
- pages/product/detail/detail.vue
```

---

## Prompt 2.8: 发布商品页

```markdown
【任务】开发小程序发布商品页

【页面路径】pages/product/publish/publish

【项目上下文】
- 已有：request.js、mock.js、theme.css、store、constant.js
- 使用Vue 3 Composition API

【原型描述】
- 顶部导航：左侧关闭X按钮 + 标题"发布闲置" + 右侧"发布"按钮
- 图片上传区域：
  - 九宫格形式，最多9张
  - 第一个位置显示"+"上传按钮（带文字提示"最多9张"）
  - 已上传的图片支持：长按拖拽排序、点击右上角X删除
  - 第一张标注"封面"
- 表单字段：
  - 商品名称：输入框，placeholder="设备名称"
  - 二手价格：数字输入框，placeholder="二手价格"，前缀¥
  - 原价：数字输入框，placeholder="原价"，前缀¥
  - 产品类型：选择器（下拉选择分类）
  - 磨损程度：选择器（全新/几乎全新/9成新/8成新/7成新及以下）
  - 商品描述：多行文本框，placeholder="介绍一下你的闲置物品..."，右下角显示字数xxx/500
  - 交易校区：选择器（南海北/南海南/新港）
  - 面交地点：选择器（选校区后加载预设地点）+ 自定义输入切换
- 底部固定区域：
  - 底部tab栏保持显示（首页/发布/消息/我的）

【调用接口（Mock）】
- GET /mini/category/list → 分类列表
- GET /mini/campus/list → 校区列表
- GET /mini/campus/meeting-points/{campusId} → 面交地点
- POST /common/upload → 上传图片
- POST /mini/product/publish → 发布商品

【交互逻辑】
1. 图片上传：
   - 点击"+"调用uni.chooseImage（最多选择 9-已上传数量 张）
   - 选择后逐张调用上传接口
   - 上传中显示loading遮罩
   - 上传失败的图片提示并移除
2. 图片排序：长按图片进入拖拽模式（可用movable-view或简化为点击交换首图）
3. 表单校验：
   - 图片：至少1张
   - 商品名称：1-50字，必填
   - 二手价格：>0，必填
   - 原价：>0或不填
   - 产品类型：必选
   - 磨损程度：必选
   - 商品描述：1-500字，必填
   - 交易校区：必选
4. 面交地点：
   - 选择校区后自动加载该校区的预设面交地点
   - 可选择预设地点或切换为"自定义输入"模式手动输入
5. 发布按钮：
   - 校验通过后提交
   - 防重复点击（loading状态）
   - 成功后提示"发布成功，等待审核" + 跳转首页或我发布的
6. 校区选择器默认选中用户当前校区（从store获取）

【请生成】
- pages/product/publish/publish.vue
```

---

## Prompt 2.9: 我发布的商品页

```markdown
【任务】开发小程序「我发布的商品」页面

【页面路径】pages/product/my-list/my-list

【项目上下文】
- 已有：request.js、mock.js、theme.css、store、constant.js、status-tag组件、empty-state组件
- 使用Vue 3 Composition API

【原型描述】
- 顶部导航：返回箭头 + 标题"我发布的"
- Tab筛选栏：全部 | 在售 | 待审核 | 已下架 | 已驳回 | 已售出
- 商品列表：
  - 每条显示：商品图片 + 标题 + 价格 + 状态标签
  - 已驳回的商品显示驳回原因
  - 右侧操作按钮根据状态不同：
    - 在售：编辑、下架
    - 待审核：编辑（重新提交）
    - 已下架：重新上架、删除
    - 已驳回：编辑（重新提交）、删除
    - 已售出：删除
- 无数据时显示empty-state

【调用接口（Mock）】
- GET /mini/product/my-list?page=1&pageSize=20&status=（不传=全部）
- POST /mini/product/off-shelf
- POST /mini/product/on-shelf
- POST /mini/product/delete

【交互逻辑】
1. Tab切换时重新加载列表（page=1）
2. 下拉刷新、上拉加载更多
3. 编辑：跳转/pages/product/edit/edit?id={id}
4. 下架：确认弹窗→调用接口→刷新列表
5. 重新上架：确认弹窗→调用接口→状态变为待审核→刷新列表
6. 删除：确认弹窗→调用接口→刷新列表
7. 已驳回的商品显示红色驳回原因文字

【请生成】
- pages/product/my-list/my-list.vue
```

---

## Prompt 2.10: 卖家主页

```markdown
【任务】开发小程序卖家个人主页

【页面路径】pages/seller/profile

【项目上下文】
- 已有：request.js、mock.js、theme.css、store、user-avatar组件、product-card组件、empty-state组件
- 使用Vue 3 Composition API

【原型描述】
- 顶部卖家信息区域（蓝色背景卡片）：
  - 头像（大号）+ 昵称
  - 认证状态标签
  - 综合评分
  - 统计：在售商品数 | 已成交数
- 下方：该卖家在售商品列表
  - 使用product-card组件
  - 支持上拉加载更多
  - 无商品时显示empty-state

【接收参数】
- id：卖家用户ID

【调用接口（Mock）】
- GET /mini/user/profile/{id} → 卖家信息+商品列表（分页）

【交互逻辑】
1. onLoad加载卖家信息和商品列表
2. 商品卡片点击跳转商品详情
3. 上拉加载更多商品
4. 如果是自己的主页，不显示（因为有"我发布的"页面）

【请生成】
- pages/seller/profile.vue
```

---

## Prompt 2.11: 个人中心页

```markdown
【任务】开发小程序个人中心页

【页面路径】pages/user/user

【项目上下文】
- 已有：request.js、mock.js、theme.css、store、auth.js、user-avatar组件、status-tag组件
- 使用Vue 3 Composition API

【原型描述】
- 顶部用户信息卡片（蓝色渐变背景）：
  - 左侧大头像
  - 右侧：昵称 + "实名认证"标签（已认证时显示）
  - 下方三个统计数字（横向排列，居中）：
    - 综合评分（如4.9）
    - 在售商品数（如12）
    - 已成交数（如28）
    - 收藏数（如56）
- 功能列表区域（白色卡片，圆角）：
  - 我发布的 → 右箭头
  - ❤ 我收藏的 → 右箭头
  - 📋 交易记录 → 右箭头
  - 🔔 消息通知 → 右箭头 + 未读数角标（红色圆点+数字）
  - ⚙ 设置 → 右箭头
  - 💬 客服与帮助 → 右箭头
- 未登录状态：
  - 头像区域显示默认头像 + "点击登录"
  - 点击头像区域跳转登录页

【调用接口（Mock）】
- GET /mini/user/info → 用户信息
- GET /mini/user/stats → 统计数据
- GET /mini/notification/unread-count → 未读消息数

【交互逻辑】
1. 每次onShow时刷新用户信息和统计数据
2. 未登录时只显示"点击登录"，点击跳转登录页
3. 功能入口点击跳转对应页面：
   - 我发布的 → /pages/product/my-list/my-list
   - 我收藏的 → /pages/favorite/favorite
   - 交易记录 → /pages/order/list/list
   - 消息通知 → /pages/notification/notification
   - 设置 → /pages/settings/settings
   - 客服与帮助 → /pages/help/help
4. 未认证用户：认证标签显示"未认证"（灰色），点击可跳转认证页
5. 消息通知入口右侧显示未读消息数角标（>99显示99+）

【请生成】
- pages/user/user.vue
```

---

## Prompt 2.12: 设置页 + 编辑个人信息页

```markdown
【任务】开发小程序设置页和编辑个人信息页

【项目上下文】
- 已有：request.js、mock.js、theme.css、store、auth.js
- 使用Vue 3 Composition API

### 页面1：设置页
【页面路径】pages/settings/settings

【原型描述】
- 列表菜单项（白色卡片分组）：
  - 第一组：
    - 个人信息 → 右箭头（跳转编辑信息页）
    - 校园认证 → 右箭头 + 认证状态标签（跳转认证页）
  - 第二组：
    - 用户协议 → 右箭头
    - 隐私政策 → 右箭头
    - 关于我们 → 右箭头
  - 第三组：
    - 账号注销 → 红色文字
  - 底部：退出登录按钮（红色边框按钮）

【交互逻辑】
1. 各菜单项点击跳转对应页面
2. 退出登录：确认弹窗→清除登录态→跳转登录页
3. 账号注销：
   - 确认弹窗"注销后30天内可恢复，超过30天数据将永久删除"
   - 检查是否有进行中的交易（Mock模式下模拟无进行中交易）
   - 确认后调用注销接口→清除登录态→跳转登录页

### 页面2：编辑个人信息页
【页面路径】pages/settings/edit-profile

【原型描述】
- 头像（点击更换，调用uni.chooseImage + 上传）
- 昵称输入框
- 性别选择（男/女/保密）
- 手机号（脱敏显示，不可编辑）
- 保存按钮

【调用接口（Mock）】
- GET /mini/user/info
- POST /mini/user/update
- POST /common/upload（头像上传）

【交互逻辑】
1. 进入页面加载当前用户信息填充表单
2. 头像点击：选择图片→上传→更新头像URL
3. 保存：校验昵称非空→调用更新接口→成功提示→更新store

【请生成】
- pages/settings/settings.vue
- pages/settings/edit-profile.vue
- pages/settings/about.vue（简单的关于页面：Logo+版本号+学校名称）
```

---

## Prompt 2.13: 收藏列表页

```markdown
【任务】开发小程序收藏列表页

【页面路径】pages/favorite/favorite

【项目上下文】
- 已有：request.js、mock.js、theme.css、product-card组件、empty-state组件
- 使用Vue 3 Composition API

【原型描述】
- 顶部导航：返回箭头 + 标题"我的收藏"
- 商品列表（使用product-card组件）
- 已下架/已售出的商品灰色展示，标注"已下架"或"已售出"
- 支持左滑取消收藏（或长按弹出菜单取消收藏）
- 无收藏时显示empty-state(type='no-favorite')

【调用接口（Mock）】
- GET /mini/favorite/list?page=1&pageSize=20

【交互逻辑】
1. onLoad加载收藏列表
2. 下拉刷新、上拉加载更多
3. 点击商品卡片跳转商品详情
4. 取消收藏：操作后从列表中移除该项
5. 已下架/已售出商品点击后提示"该商品已下架"或"该商品已售出"

【请生成】
- pages/favorite/favorite.vue
```

---

## Prompt 2.14: 订单列表页

```markdown
【任务】开发小程序订单列表页

【页面路径】pages/order/list/list

【项目上下文】
- 已有：request.js、mock.js、theme.css、constant.js、order-card组件、empty-state组件
- 使用Vue 3 Composition API

【原型描述】
- 顶部导航：返回箭头 + 标题"交易记录"
- Tab切换栏：「我买到的」|「我卖出的」
- 订单列表（使用order-card组件）
- 无订单时显示empty-state(type='no-order')

【调用接口（Mock）】
- GET /mini/order/list?page=1&pageSize=20&role=buyer（或seller）&status=
- POST /mini/order/confirm（确认收货）
- POST /mini/order/cancel（取消交易）
- POST /mini/order/delete（删除订单）

【交互逻辑】
1. 默认显示"我买到的"Tab
2. Tab切换重新加载（page=1）
3. 下拉刷新、上拉加载更多
4. order-card组件的操作按钮点击事件处理：
   - 联系卖家/买家：跳转聊天详情页
   - 取消交易：弹窗选择取消原因（使用uni.showActionSheet：双方协商取消/对方无响应/不想买了/其他）→调用接口→刷新列表
   - 确认收货：确认弹窗→调用接口→刷新列表
   - 去评价：跳转评价页 /pages/review/review?orderId={id}
   - 查看评价：跳转评价详情（可复用评价页，带readonly参数）
   - 再次购买：跳转卖家主页 /pages/seller/profile?id={sellerId}
   - 删除订单：确认弹窗→调用接口→从列表中移除

【请生成】
- pages/order/list/list.vue
```

---

## Prompt 2.15: 评价页

```markdown
【任务】开发小程序评价页

【页面路径】pages/review/review

【项目上下文】
- 已有：request.js、mock.js、theme.css、constant.js
- 使用Vue 3 Composition API

【原型描述】
- 两种模式：评价模式（提交评价）/ 查看模式（只读查看）

### 评价模式：
- 顶部：商品信息简略（图片+标题+价格）
- 三个评分维度（每个维度一行）：
  - 商品描述相符 ⭐⭐⭐⭐⭐（5颗星，可点击选择1-5星）
  - 沟通态度 ⭐⭐⭐⭐⭐
  - 交易体验 ⭐⭐⭐⭐⭐
- 文字评价：多行文本框，placeholder="分享你的交易体验..."，右下角字数xxx/200
- 提交按钮

### 查看模式：
- 显示双方评价内容（我的评价 + 对方的评价）
- 星级展示（不可修改）
- 文字评价内容
- 自动评价标注"系统默认好评"

【接收参数】
- orderId：订单ID
- readonly：是否只读（查看模式）

【调用接口（Mock）】
- GET /mini/order/detail/{orderId} → 获取订单信息（含商品简略信息）
- POST /mini/review/submit → 提交评价
- GET /mini/review/detail/{orderId} → 获取评价详情

【交互逻辑】
1. 根据readonly参数判断模式
2. 评价模式：
   - 默认5星
   - 点击星星切换评分
   - 提交前校验：三个维度必须有分值
   - 提交成功后提示"评价成功"→返回上一页
   - 防重复提交
3. 查看模式：
   - 加载评价详情
   - 展示双方评价

【请生成】
- pages/review/review.vue
```

---

## Prompt 2.16: 举报页

```markdown
【任务】开发小程序举报页

【页面路径】pages/report/report

【项目上下文】
- 已有：request.js、mock.js、theme.css、constant.js
- 使用Vue 3 Composition API

【原型描述】
- 顶部导航：返回箭头 + 标题"举报"
- 举报原因选择（单选，使用radio-group）：
  - 虚假商品
  - 违禁物品
  - 诈骗信息
  - 不当内容
  - 其他
- 补充说明：多行文本框，placeholder="请描述具体情况..."，最长255字
- 提交按钮

【接收参数】
- targetType：1=商品 2=用户
- targetId：目标ID

【调用接口（Mock）】
- POST /mini/report/submit

【交互逻辑】
1. 举报原因必选
2. 补充说明可选
3. 提交成功提示"举报已提交，我们会尽快处理"→返回上一页
4. 防重复提交

【请生成】
- pages/report/report.vue
```

---

## Prompt 2.17: 消息中心页

```markdown
【任务】开发小程序消息中心页

【页面路径】pages/notification/notification

【项目上下文】
- 已有：request.js、mock.js、theme.css、constant.js、empty-state组件
- 使用Vue 3 Composition API

【原型描述】
- 顶部导航：关闭X按钮 + 标题"消息中心" + 右侧"清空"文字按钮
- Tab切换栏：全部 | 交易 | 系统
- 消息列表：
  - 每条消息：
    - 左侧图标（根据消息类型不同图标/颜色）：
      - ✓ 交易成功（绿色）
      - 💬 新消息（蓝色）
      - ⚠ 审核提醒（橙色）
      - 📢 系统公告（蓝色）
      - ❤ 被收藏（红色）
      - ✕ 订单取消（灰色）
    - 右侧内容：标题（粗体）+ 内容详情 + 时间
    - 未读消息左侧有蓝色小圆点
  - 点击消息跳转关联页面
- 无消息时显示empty-state(type='no-message')

【调用接口（Mock）】
- GET /mini/notification/list?page=1&pageSize=20&category=（不传=全部，1=交易，2=系统）
- POST /mini/notification/read（标记单条已读）
- POST /mini/notification/read-all（全部标记已读）

【交互逻辑】
1. Tab切换重新加载（page=1）
2. 下拉刷新、上拉加载更多
3. 点击消息：
   - 标记该消息已读
   - 根据related_type和related_id跳转：
     - 商品相关 → 商品详情页
     - 订单相关 → 订单详情页
     - 认证相关 → 认证页
     - 系统公告 → 无跳转（就地展示）
4. "清空"按钮：确认弹窗"确定将所有消息标为已读吗？"→调用read-all接口→刷新列表

【请生成】
- pages/notification/notification.vue
```

---

## Prompt 2.18: 聊天列表页 + 聊天详情页

```markdown
【任务】开发小程序聊天列表页和聊天详情页

【项目上下文】
- 已有：request.js、mock.js、theme.css、store、auth.js、user-avatar组件
- IM方案：OpenIM（V1.0先用Mock数据模拟聊天界面，后续集成OpenIM SDK）
- 使用Vue 3 Composition API

### 页面1：聊天列表页
【页面路径】pages/chat/list/list

【原型描述】
- 此页面为tabBar的"消息"tab
- 会话列表：
  - 每条会话：对方头像 + 对方昵称 + 最新消息预览（一行省略）+ 时间 + 未读数角标
  - 按最新消息时间倒序
  - 支持左滑删除会话
- 无会话时显示empty-state

【Mock数据】
- 模拟3-5个会话，每个会话包含对方信息和最新消息

【交互逻辑】
1. onShow时加载会话列表
2. 点击会话跳转聊天详情页
3. 左滑显示"删除"按钮，点击确认删除

### 页面2：聊天详情页
【页面路径】pages/chat/detail/detail

【原型描述】
- 顶部导航：返回箭头 + 对方昵称 + 右侧"⋮"更多按钮
- 顶部悬浮：关联商品卡片（小型：图片+标题+价格）→ 点击跳转商品详情
- 聊天区域（scroll-view）：
  - 消息气泡：自己的在右侧（蓝色背景白字），对方的在左侧（灰色背景黑字）
  - 每条消息显示头像和气泡
  - 时间戳分隔（每隔5分钟以上的消息显示时间）
  - 支持系统消息卡片（如订单创建成功的卡片消息）
- 底部输入区域（安全区域适配）：
  - 输入框 + 发送按钮
  - 输入框上方：快捷回复按钮（横向滚动）
    - "还在吗？"
    - "可以小刀吗？"
    - "什么时候方便？"
  - 「确认购买」按钮（小型，在输入框旁或快捷回复栏中）

【接收参数】
- userId：对方用户ID
- productId：关联商品ID（可选，从商品详情"我想要"跳转时携带）

【Mock数据】
- 模拟一段完整的对话（参考原型中的iPad对话内容）

【调用接口（Mock）】
- 聊天消息：V1.0使用Mock数据模拟，后续接入OpenIM SDK
- GET /mini/product/detail/{productId} → 获取关联商品简略信息
- POST /mini/order/create → 确认购买创建订单

【交互逻辑】
1. 进入页面加载对方用户信息和关联商品信息
2. 加载历史消息（Mock）
3. 消息列表自动滚动到底部
4. 输入消息点击发送：
   - 消息添加到列表右侧
   - 输入框清空
   - 自动滚动到底部
   - Mock模式下模拟对方1-2秒后自动回复
5. 快捷回复点击：直接发送对应文字
6. 「确认购买」点击：
   - 弹窗显示商品信息和当前价格
   - 确认后调用创建订单接口
   - 成功后在聊天中插入一条系统消息"订单已创建，请尽快面交"
   - 如果商品已有待面交订单，提示"该商品已有待交易订单"
7. 关联商品卡片点击跳转商品详情
8. 新消息自动滚动到底部
9. 「⋮」更多菜单：举报用户

【请生成】
- pages/chat/list/list.vue
- pages/chat/detail/detail.vue

【特别说明】
- V1.0聊天功能使用Mock数据模拟，不实际接入OpenIM
- 代码结构需要预留OpenIM SDK接入的位置
- 建议将聊天相关逻辑封装在utils/chat.js中，后续替换为OpenIM SDK
```

---

## Prompt 2.19: 用户协议 + 隐私政策 + 帮助页

```markdown
【任务】开发用户协议、隐私政策和帮助中心页面

【项目上下文】
- 已有：theme.css
- 使用Vue 3 Composition API

### 页面1：用户协议
【页面路径】pages/agreement/agreement
- 标题：轻院二手用户协议
- 内容：静态富文本页面，包含以下章节：
  1. 总则
  2. 用户注册与账号管理
  3. 用户行为规范
  4. 商品发布规范
  5. 交易规则
  6. 违规处理
  7. 免责声明
  8. 其他条款
- 内容为占位文本即可，结构要完整

### 页面2：隐私政策
【页面路径】pages/privacy/privacy
- 标题：轻院二手隐私政策
- 内容：静态富文本页面，包含：
  1. 我们收集的信息
  2. 信息的使用方式
  3. 信息的共享
  4. 信息的存储与安全
  5. 您的权利
  6. 未成年人保护
  7. 隐私政策的更新
  8. 联系我们

### 页面3：帮助中心
【页面路径】pages/help/help
- 标题：客服与帮助
- FAQ列表（可折叠展开）：
  1. 如何发布商品？
  2. 如何购买商品？
  3. 如何进行校园认证？
  4. 商品审核需要多久？
  5. 如何取消交易？
  6. 发现违规商品怎么举报？
  7. 如何修改个人信息？
  8. 如何注销账号？
- 底部：联系客服入口（预留，显示客服微信号或邮箱）

【请生成】
- pages/agreement/agreement.vue
- pages/privacy/privacy.vue
- pages/help/help.vue
```

---

# Phase 3: 后端接口开发

---

## Prompt 3.1: 通用模块

```
（已在Prompt 1.1中包含，如有补充则在此说明）
此步骤确认Prompt 1.1中生成的通用模块代码已就绪，包括：
- Result.java
- BusinessException.java + GlobalExceptionHandler.java
- JwtUtil.java
- FileUtil.java
- JwtInterceptor.java + AdminJwtInterceptor.java
- UserContext.java（ThreadLocal）
- RedisConstant.java
- 所有枚举类
- CommonController.java（文件上传）
- WebMvcConfig.java
- MyBatisPlusConfig.java
- RedisConfig.java

如果有未生成的，请补充生成。
```

---

## Prompt 3.2: 数据库初始化SQL

```markdown
【任务】生成完整的数据库初始化SQL脚本

【数据库信息】
- 数据库名：qingyuan_secondhand
- 字符集：utf8mb4
- 排序规则：utf8mb4_unicode_ci

【需要包含的表】
按照PRD中的数据结构设计，生成所有表的CREATE TABLE语句和初始化数据INSERT语句：

1. user - 用户表
2. employee - 管理员表（含初始管理员数据）
3. campus_auth - 校园认证表
4. college - 学院表（含广东轻工职业技术大学的真实学院数据，请列举主要学院）
5. campus - 校区表（含南海北/南海南/新港数据）
6. meeting_point - 面交地点表（每个校区预设3-5个常用地点）
7. category - 商品分类表（含6个分类初始数据）
8. product - 商品表
9. favorite - 收藏表
10. trade_order - 交易订单表
11. review - 评价表
12. report - 举报表
13. notification - 消息通知表
14. banner - Banner表
15. search_keyword - 搜索热词表（含初始热门关键词数据）
16. notice - 系统公告表

【要求】
- 每个表的字段、索引严格按照PRD定义
- 包含建库语句：CREATE DATABASE IF NOT EXISTS
- 包含USE语句
- 表之间注释清晰分隔
- 初始数据要合理真实
- 学院数据请参考广东轻工职业技术大学官网的学院设置
- 面交地点使用通用校园地点名称（如：一饭门口、图书馆门口、南门快递站、体育馆门口、宿舍区超市门口等）
- 文件保存路径：sql/init.sql

【请生成】
- sql/init.sql（完整SQL脚本，可直接执行）
```

---

## Prompt 3.3: 用户模块

```markdown
【当前模块】用户模块 MODULE-USER

【项目上下文】
- 框架：Spring Boot 3.x + MyBatis-Plus + MySQL + Redis
- JDK：17
- 包路径：com.qingyuan.secondhand
- 已有公共类：Result, BusinessException, GlobalExceptionHandler, JwtUtil, UserContext, RedisConstant, 所有枚举类

【相关表结构】
（粘贴user表的完整CREATE TABLE SQL）

【需要实现的接口】

### 1. POST /mini/user/wx-login - 微信登录
请求参数：WxLoginDTO { code: String }
业务逻辑：
  a. 用code调用微信API（https://api.weixin.qq.com/sns/jscode2session）获取openId和session_key
  b. 根据openId查询用户表
  c. 存在→更新session_key和last_login_time→生成JWT Token返回
  d. 不存在→创建新用户（openId、session_key、默认昵称"微信用户"、默认头像）→生成Token返回
  e. 账号被封禁→抛异常
  f. 账号注销中→返回特殊标识让前端提示恢复
响应：LoginVO { token, userId, isNew, authStatus, nickName, avatarUrl }

### 2. POST /mini/user/login - 手机号+密码登录
请求参数：AccountLoginDTO { phone: String, password: String }
业务逻辑：
  a. 根据phone查询用户
  b. 不存在→抛异常"账号不存在"
  c. BCrypt验证密码
  d. 密码错误→Redis记录失败次数（key: login:fail:{phone}），5次锁定15分钟
  e. 密码正确→清除失败计数→更新last_login_time→生成Token
  f. 检查封禁/注销状态
响应：LoginVO

### 3. POST /mini/user/sms/send - 发送短信验证码
请求参数：SmsSendDTO { phone: String }
业务逻辑：
  a. 校验手机号格式
  b. 检查Redis频率限制：sms:limit:{phone}存在→抛异常"请60秒后重试"
  c. 检查每日限制：sms:daily:{phone} >= 10 → 抛异常"今日发送次数已达上限"
  d. 生成6位随机数字验证码
  e. 存Redis：sms:code:{phone} = 验证码，TTL=5分钟
  f. 设置频率限制：sms:limit:{phone}，TTL=60秒
  g. 增加每日计数：sms:daily:{phone}++，TTL=24小时
  h. 调用短信服务发送（V1.0可以只打印日志，不实际发送）
  i. 返回成功

### 4. POST /mini/user/sms-login - 短信验证登录
请求参数：SmsLoginDTO { phone: String, smsCode: String }
业务逻辑：
  a. 从Redis获取验证码：sms:code:{phone}
  b. 不存在→抛异常"验证码已过期"
  c. 不匹配→抛异常"验证码错误"
  d. 验证通过→删除Redis中的验证码
  e. 根据phone查询用户
  f. 不存在→自动创建新用户（phone、默认昵称）
  g. 生成Token返回
响应：LoginVO

### 5. GET /mini/user/info - 获取当前用户信息
业务逻辑：
  a. 从UserContext获取当前userId
  b. 查询用户信息
  c. 关联查询校区名称
  d. 手机号脱敏处理
响应：UserInfoVO { id, nickName, avatarUrl, phone(脱敏), gender, campusId, campusName, authStatus, score, status }

### 6. POST /mini/user/update - 更新用户信息
请求参数：UserUpdateDTO { nickName, avatarUrl, gender }
业务逻辑：
  a. 从UserContext获取userId
  b. 校验参数
  c. 更新用户信息
  d. 清除用户信息缓存

### 7. GET /mini/user/stats - 获取用户统计数据
业务逻辑：
  a. 先查Redis缓存：user:stats:{userId}
  b. 缓存不存在→查询数据库：
     - onSaleCount: product表 status=1 and user_id=userId and is_deleted=0 的数量
     - soldCount: trade_order表 status in (3,4) and (buyer_id=userId or seller_id=userId) 的数量
     - favoriteCount: favorite表 user_id=userId 的数量
  c. 存入Redis，TTL=10分钟
响应：UserStatsVO { onSaleCount, soldCount, favoriteCount }

### 8. GET /mini/user/profile/{id} - 卖家主页
业务逻辑：
  a. 查询目标用户信息
  b. 查询该用户在售商品列表（分页，status=1, is_deleted=0）
  c. 统计在售数和成交数
响应：UserProfileVO { id, nickName, avatarUrl, authStatus, score, onSaleCount, soldCount, products{total, records} }

### 9. POST /mini/user/deactivate - 申请注销
业务逻辑：
  a. 检查是否有进行中的订单（status=1）→有则抛异常
  b. 更新status=2，设置deactivate_time=now()
  c. 下架所有在售商品

### 10. POST /mini/user/restore - 恢复账号
业务逻辑：
  a. 检查status是否=2
  b. 更新status=1，清空deactivate_time

【请生成以下文件】
1. dto/WxLoginDTO.java
2. dto/AccountLoginDTO.java
3. dto/SmsSendDTO.java
4. dto/SmsLoginDTO.java
5. dto/UserUpdateDTO.java
6. vo/LoginVO.java
7. vo/UserInfoVO.java
8. vo/UserStatsVO.java
9. vo/UserProfileVO.java
10. entity/User.java
11. mapper/UserMapper.java
12. service/UserService.java
13. service/impl/UserServiceImpl.java
14. controller/mini/MiniUserController.java
15. config/WxConfig.java（微信小程序配置：appId, appSecret）

【注意】
- 微信API调用使用RestTemplate
- BCrypt使用Spring Security的BCryptPasswordEncoder
- 验证码发送V1.0只打印日志：log.info("发送验证码到{}：{}", phone, code)
- 手机号脱敏方法写在工具类中
- application.yml中添加微信小程序配置项
```

---

## Prompt 3.4: 校园认证模块

```markdown
【当前模块】校园认证模块 MODULE-AUTH

【项目上下文】
- 已有：用户模块全部代码、公共模块
- 包路径：com.qingyuan.secondhand

【相关表结构】
（粘贴campus_auth表和college表的SQL）

【需要实现的接口】

### 小程序端
1. POST /mini/auth/submit - 提交校园认证
   请求：AuthSubmitDTO { collegeId, studentNo, className, certImage }
   逻辑：
   a. 校验当前用户认证状态（审核中则不允许重复提交）
   b. 校验学号唯一性（同一学号不能被两个账号认证）
   c. 保存或更新认证记录（已驳回可重新提交=更新）
   d. 更新用户表auth_status=1（审核中）
   e. 返回成功

2. GET /mini/auth/status - 查询认证状态
   逻辑：查询当前用户的认证记录
   响应：AuthStatusVO { status, collegeName, studentNo, className, certImage, rejectReason, reviewTime }

3. GET /mini/college/list - 获取学院列表
   逻辑：查询status=1的学院，按sort排序，优先走Redis缓存
   响应：List<CollegeVO> { id, name }

### 管理端
4. GET /admin/auth/page - 认证审核分页
   参数：page, pageSize, status, collegeId
   逻辑：分页查询认证记录，关联用户信息和学院名称

5. GET /admin/auth/detail/{id} - 认证详情
   逻辑：查询认证记录详情

6. POST /admin/auth/approve - 认证通过
   请求：{ authId }
   逻辑：更新认证status=1(通过)，更新用户auth_status=2，发送站内通知

7. POST /admin/auth/reject - 认证驳回
   请求：{ authId, rejectReason }
   逻辑：更新认证status=2(驳回)，更新用户auth_status=3，发送站内通知（含驳回原因）

【请生成】
- dto/AuthSubmitDTO.java
- vo/AuthStatusVO.java, CollegeVO.java, AuthPageVO.java
- entity/CampusAuth.java, College.java
- mapper/CampusAuthMapper.java, CollegeMapper.java
- service/CampusAuthService.java + impl
- service/CollegeService.java + impl
- controller/mini/MiniAuthController.java, MiniCollegeController.java
- controller/admin/AdminAuthController.java
```

---

## Prompt 3.5 - 3.8: 分类/校区/学院/商品模块

```markdown
【任务】开发分类、校区、学院、商品四个模块的后端代码

【我将按以下顺序分别提供每个模块的Prompt，请逐个实现】

--- 3.5 分类模块 MODULE-PRODUCT ---

【相关表】category表SQL

【接口】
小程序端：
- GET /mini/category/list（公开，Redis缓存1小时）

管理端：
- GET /admin/category/page（分页查询）
- GET /admin/category/list（全部列表，不分页）
- POST /admin/category/add（添加分类，清除缓存）
- POST /admin/category/update（更新分类，清除缓存）
- POST /admin/category/delete（删除分类，检查是否有商品使用该分类）

请生成：entity, mapper, service+impl, dto, vo, controller(mini+admin)

--- 3.6 校区与面交地点模块 ---

【相关表】campus表、meeting_point表SQL

【接口】
小程序端：
- GET /mini/campus/list（公开，Redis缓存）
- GET /mini/campus/meeting-points/{campusId}（公开）

管理端：
- GET /admin/campus/list
- POST /admin/campus/add
- POST /admin/campus/update
- GET /admin/campus/meeting-point/list/{campusId}
- POST /admin/campus/meeting-point/add
- POST /admin/campus/meeting-point/update
- POST /admin/campus/meeting-point/delete

请生成：entity, mapper, service+impl, dto, vo, controller(mini+admin)

--- 3.7 学院模块 ---

【相关表】college表SQL

【接口】
管理端：
- GET /admin/college/list
- POST /admin/college/add
- POST /admin/college/update
- POST /admin/college/delete（检查是否有认证使用该学院）

请生成：controller/admin/AdminCollegeController.java
（entity和service在认证模块中已有，如需补充请一并生成）

--- 3.8 商品模块 ---

【相关表】product表SQL

【接口】
小程序端：
1. POST /mini/product/publish - 发布商品
   请求：ProductPublishDTO { title, description, price, originalPrice, categoryId, conditionLevel, campusId, meetingPointId, meetingPointText, images(List<String>) }
   逻辑：
   a. 参数校验（标题1-50字，价格>0，图片1-9张，描述1-500字）
   b. 保存商品，status=0(待审核)，is_deleted=0
   c. auto_off_time = now + 90天
   d. images字段存JSON数组字符串

2. POST /mini/product/update - 编辑商品
   请求：ProductUpdateDTO（同publish + productId）
   逻辑：
   a. 校验是否是自己的商品
   b. 更新商品信息
   c. 状态重置为待审核(0)，清空reject_reason
   d. 重新设置auto_off_time

3. GET /mini/product/detail/{id} - 商品详情
   逻辑：
   a. 查询商品（is_deleted=0）
   b. 关联查询卖家信息、分类名、校区名、面交地点名
   c. 如果已登录：查是否收藏(isFavorited)、是否自己的(isOwner)、是否有活跃订单(hasActiveOrder)
   d. 浏览量+1（Redis去重：product:view:{productId}:{userId}，24h TTL）
   e. 异步更新数据库浏览量

4. GET /mini/product/list - 商品列表
   参数：page, pageSize, campusId, categoryId, keyword, sortBy(latest/price_asc/price_desc), minPrice, maxPrice
   逻辑：
   a. 只查status=1(在售) and is_deleted=0
   b. 关键词模糊搜索title和description
   c. 多条件组合筛选
   d. 排序
   e. 每条关联卖家基本信息
   f. 如果有keyword，异步记录搜索关键词（search_keyword表search_count+1）

5. GET /mini/product/my-list - 我发布的
   参数：page, pageSize, status（可选）
   逻辑：查询user_id=当前用户 and is_deleted=0的商品，按status筛选

6. POST /mini/product/off-shelf - 下架
   请求：{ productId }
   逻辑：校验是自己的商品，检查无进行中订单，status→2

7. POST /mini/product/on-shelf - 重新上架
   请求：{ productId }
   逻辑：校验是自己的商品，status→0(重新审核)

8. POST /mini/product/delete - 删除
   请求：{ productId }
   逻辑：校验是自己的商品，检查无进行中订单，is_deleted→1

9. POST /mini/product/update-price - 修改价格
   请求：{ productId, price }
   逻辑：校验是自己的商品，直接更新价格（不需要重新审核）

管理端：
10. GET /admin/product/page - 商品分页（支持status筛选）
11. GET /admin/product/detail/{id} - 商品详情
12. POST /admin/product/approve - 审核通过（status→1，发通知）
13. POST /admin/product/reject - 审核驳回（status→4，记录原因，发通知）
14. POST /admin/product/batch-approve - 批量通过
15. POST /admin/product/force-off - 强制下架（status→2，发通知）

请生成：entity, mapper(含XML如需复杂查询), service+impl, 所有dto, 所有vo, controller(mini+admin)

【注意】
- 商品列表查询关联卖家信息，建议使用MyBatis XML写关联查询
- 浏览量去重使用Redis，异步更新数据库使用@Async
- images字段存储为JSON字符串，取出时转List<String>
```

---

## Prompt 3.9: 收藏模块

```markdown
【当前模块】收藏模块 MODULE-FAVORITE

【已有】用户模块、商品模块

【相关表】favorite表SQL

【接口】
1. POST /mini/favorite/add { productId }
   逻辑：校验商品存在，校验未收藏过，插入记录，product.favorite_count+1，通知卖家

2. POST /mini/favorite/cancel { productId }
   逻辑：删除收藏记录，product.favorite_count-1

3. GET /mini/favorite/list?page=1&pageSize=20
   逻辑：查询用户收藏列表，关联商品信息（含状态），按收藏时间倒序

4. GET /mini/favorite/check/{productId}
   逻辑：查询当前用户是否收藏该商品
   响应：{ isFavorited: true/false }

请生成：entity, mapper, service+impl, dto, vo, controller
```

---

## Prompt 3.10: 订单模块

```markdown
【当前模块】订单模块 MODULE-ORDER

【已有】用户模块、商品模块

【相关表】trade_order表SQL

【接口】

### 小程序端
1. POST /mini/order/create - 创建订单
   请求：OrderCreateDTO { productId, price, campusId, meetingPoint }
   逻辑：
   a. 校验商品存在且在售
   b. 校验不能购买自己的商品
   c. 使用Redis分布式锁（product:lock:{productId}，30s）防并发
   d. 校验该商品无进行中的订单（status=1）→有则抛异常
   e. 生成订单号：TD + yyyyMMddHHmmss + 4位随机数
   f. 创建订单，status=1
   g. expire_time = now + 72h
   h. 释放锁
   i. 发送通知给卖家
   响应：OrderCreateVO { orderId, orderNo, expireTime }

2. GET /mini/order/list - 订单列表
   参数：page, pageSize, role(buyer/seller), status
   逻辑：
   a. role=buyer → buyer_id=当前用户 and is_deleted_buyer=0
   b. role=seller → seller_id=当前用户 and is_deleted_seller=0
   c. 关联商品信息和对方用户信息
   d. 按create_time倒序

3. GET /mini/order/detail/{id} - 订单详情
   逻辑：校验是买家或卖家，返回完整订单+商品+双方用户信息

4. POST /mini/order/confirm - 确认收货
   请求：{ orderId }
   逻辑：
   a. 校验当前用户是买家
   b. 校验status=1
   c. status→3(已完成)
   d. complete_time=now()
   e. confirm_deadline = now + 7天（用于自动好评倒计时）
   f. 商品status→3(已售出)
   g. 通知卖家

5. POST /mini/order/cancel - 取消订单
   请求：OrderCancelDTO { orderId, cancelReason }
   逻辑：
   a. 校验当前用户是买家或卖家
   b. 校验status=1
   c. status→5(已取消)
   d. cancel_by=当前userId
   e. 商品status恢复→1(在售)
   f. 通知对方

6. POST /mini/order/delete - 删除订单
   请求：{ orderId }
   逻辑：
   a. 校验status=4或5（已评价或已取消才能删除）
   b. 根据当前用户角色设置is_deleted_buyer=1或is_deleted_seller=1

### 管理端
7. GET /admin/order/page - 订单分页查询
8. GET /admin/order/detail/{id} - 订单详情

请生成：entity, mapper(含XML), service+impl, 所有dto, 所有vo, controller(mini+admin)
OrderNoUtil工具类（生成订单号）
```

---

## Prompt 3.11: 评价模块

```markdown
【当前模块】评价模块 MODULE-REVIEW

【已有】用户模块、订单模块

【相关表】review表SQL

【接口】
1. POST /mini/review/submit - 提交评价
   请求：ReviewSubmitDTO { orderId, scoreDesc(1-5), scoreAttitude(1-5), scoreExperience(1-5), content(可选,max200) }
   逻辑：
   a. 校验当前用户是该订单的买家或卖家
   b. 校验订单status=3(已完成)或status=4(已评价，但自己未评)
   c. 校验该用户未评价过该订单
   d. 校验评价窗口期（complete_time + 7天内）
   e. 保存评价记录
   f. 检查双方是否都已评价→都评了则订单status→4(已评价)
   g. 重新计算被评价人的综合评分并更新user.score
   h. 通知对方

2. GET /mini/review/detail/{orderId} - 查看评价
   逻辑：
   a. 查询该订单的所有评价记录
   b. 如果订单status=3（有一方未评），只返回自己的评价
   c. 如果status=4（都评了），返回双方评价

【评分计算】
- 单次评价得分 = (scoreDesc + scoreAttitude + scoreExperience) / 3.0
- 用户综合评分 = 所有收到的评价的单次评价得分的平均值，保留一位小数
- 如果没有收到过评价，默认5.0

请生成：entity, mapper, service+impl, dto, vo, controller
```

---

## Prompt 3.12: 举报模块

```markdown
【当前模块】举报模块 MODULE-REPORT

【已有】用户模块、商品模块

【相关表】report表SQL

【接口】
### 小程序端
1. POST /mini/report/submit - 提交举报
   请求：ReportSubmitDTO { targetType(1商品/2用户), targetId, reasonType(1-5), description(可选) }
   逻辑：
   a. 校验不能举报自己
   b. 校验同一用户对同一目标只能举报一次（唯一索引处理）
   c. 保存举报记录，status=0(待处理)

### 管理端
2. GET /admin/report/page - 举报分页
3. GET /admin/report/detail/{id} - 举报详情（关联被举报商品/用户信息）
4. POST /admin/report/handle - 处理举报
   请求：ReportHandleDTO { reportId, action(off_shelf/warn/ban/ignore), handleResult }
   逻辑：
   a. 更新举报状态和处理结果
   b. action=off_shelf → 商品强制下架 + 通知卖家
   c. action=warn → 通知被举报用户
   d. action=ban → 用户封禁 + 在售商品下架 + 进行中订单取消 + 通知用户
   e. action=ignore → 仅更新状态

请生成：entity, mapper, service+impl, dto, vo, controller(mini+admin)
```

---

## Prompt 3.13: 消息通知模块

```markdown
【当前模块】消息通知模块 MODULE-NOTIFICATION

【已有】所有业务模块

【相关表】notification表SQL

【接口】
1. GET /mini/notification/list?page=1&pageSize=20&category=
2. POST /mini/notification/read { notificationId }
3. POST /mini/notification/read-all
4. GET /mini/notification/unread-count

【额外】
- 提供NotificationService.send(userId, type, title, content, relatedId, relatedType, category)方法
- 被其他模块调用（商品审核通过/驳回、订单创建/取消/完成、认证通过/驳回、被收藏等）
- send方法为异步（@Async），避免阻塞主业务

请生成：entity, mapper, service+impl, vo, controller
确保send方法被其他模块正确引用（检查已有service是否需要补充调用NotificationService.send）
```

---

## Prompt 3.14 - 3.18: Banner/搜索/公告/统计/员工模块

```markdown
【任务】开发Banner、搜索、公告、数据统计、员工管理五个模块的后端代码

--- 3.14 Banner模块 ---
【接口】
- GET /mini/banner/list?campusId=（公开，Redis缓存30分钟）
- GET /admin/banner/page
- POST /admin/banner/add（清除缓存）
- POST /admin/banner/update（清除缓存）
- POST /admin/banner/delete

--- 3.15 搜索模块 ---
【接口】
- GET /mini/search/hot-keywords（Redis缓存1小时）
  逻辑：is_hot=1 或 search_count最高的前10个

（搜索关键词记录逻辑已在商品列表接口中实现，此处只提供热词查询接口）

--- 3.16 公告模块 ---
【接口】
- GET /admin/notice/page
- POST /admin/notice/add（发布后推送站内通知给所有用户）
- POST /admin/notice/update
- POST /admin/notice/delete

公告推送逻辑：
- 发布公告时，异步批量插入notification记录给所有正常状态用户
- 考虑用户量大时的分批插入（每批1000条）

--- 3.17 数据统计模块 ---
【接口】
- GET /admin/stats/overview → 今日数据+累计数据+待处理事项
- GET /admin/stats/trend?days=7 → 近N天的趋势数据（新增用户/新增商品/成交量/GMV）
- GET /admin/stats/campus → 按校区统计（商品数/交易数/用户数）
- GET /admin/stats/category → 按分类统计（商品数/交易数）

所有统计使用SQL聚合查询，不使用缓存（管理后台访问量小）

--- 3.18 员工模块 ---
【接口】
- POST /admin/employee/login
  请求：{ username, password }
  逻辑：查询employee表，BCrypt验证密码，生成管理端JWT Token
- GET /admin/employee/info
- GET /admin/employee/page
- POST /admin/employee/add（仅超级管理员，默认密码123456的BCrypt值）
- POST /admin/employee/update
- POST /admin/employee/reset-password（重置为123456）

请逐个模块生成：entity, mapper, service+impl, dto, vo, controller
```

---

## Prompt 3.19: 定时任务

```markdown
【当前模块】定时任务

【项目上下文】
- 已有：所有业务模块的Service
- 使用Spring @Scheduled

【需要实现的定时任务】

1. task/OrderExpireTask.java - 订单超时取消
   Cron: 0 */5 * * * ?（每5分钟）
   逻辑：
   a. 查询 expire_time < NOW() AND status = 1 的订单
   b. 批量更新 status → 5(已取消), cancel_by → 0(系统)
   c. 对应商品 status → 1(恢复在售)
   d. 通知买卖双方（异步）
   e. 记录日志

2. task/OrderAutoConfirmTask.java - 自动确认收货
   Cron: 0 0 2 * * ?（每天凌晨2点）
   逻辑：
   a. 查询 confirm_deadline < NOW() AND status = 1 的订单
   b. （注意：confirm_deadline在确认收货时才设置，此任务应查的是created后7天+72小时都没操作的？
      实际逻辑修正：这里应该是面交后7天自动确认。但V1.0没有"待确认"中间状态，
      所以这个任务应该是：status=1 且 create_time + 7天 < NOW 的订单自动确认）
   c. 更新 status → 3(已完成), complete_time = NOW()
   d. 商品 status → 3(已售出)
   e. 通知双方

3. task/ReviewAutoTask.java - 自动好评
   Cron: 0 0 3 * * ?（每天凌晨3点）
   逻辑：
   a. 查询 status = 3(已完成) AND complete_time + 7天 < NOW() 的订单
   b. 检查哪一方未评价
   c. 为未评价方生成默认好评（5/5/5分，is_auto=1）
   d. 订单 status → 4(已评价)
   e. 重新计算被评价人综合评分

4. task/ProductAutoOffTask.java - 商品90天自动下架
   Cron: 0 0 4 * * ?（每天凌晨4点）
   逻辑：
   a. 查询 auto_off_time < NOW() AND status = 1 的商品
   b. 批量更新 status → 2(已下架)
   c. 通知卖家

5. task/UserDeactivateTask.java - 账号注销清理
   Cron: 0 0 5 * * ?（每天凌晨5点）
   逻辑：
   a. 查询 status = 2(注销中) AND deactivate_time + 30天 < NOW() 的用户
   b. 物理删除或标记永久删除（根据需求，建议物理删除个人信息，保留交易记录）
   c. 记录日志

【配置】
- 在SecondhandApplication启动类上添加@EnableScheduling
- 在application.yml中添加定时任务开关配置（方便关闭）

请生成所有定时任务类，每个任务包含完善的日志记录和异常处理（单条失败不影响其他）
```

---

## Prompt 3.20: OpenIM集成

```markdown
【当前模块】OpenIM集成 MODULE-IM

【说明】
OpenIM是独立部署的IM服务，本模块主要做以下对接工作：

1. 用户注册OpenIM账号
   - 在用户注册/首次登录时，调用OpenIM API注册IM用户
   - OpenIM userId使用系统userId
   - 在UserService中补充调用

2. 获取OpenIM UserToken
   - 新增接口：GET /mini/im/token
   - 后端调用OpenIM admin API获取用户Token
   - 前端使用该Token初始化OpenIM SDK

3. 小程序端OpenIM SDK集成
   - 在miniapp中引入OpenIM uni-app SDK
   - 封装utils/im.js：初始化、登录、发送消息、接收消息、获取会话列表
   - 替换之前的Mock聊天数据

【V1.0简化方案】
如果OpenIM部署复杂，V1.0可以先用以下简化方案：
- 使用WebSocket实现简单的实时聊天
- 后端实现简单的消息存储表
- 后续再迁移到OpenIM

请提供：
1. OpenIM对接方案文档（而非完整代码）
2. im相关的service和controller
3. 小程序端utils/im.js的接口封装（预留SDK接入）

【如果选择简化WebSocket方案，请额外生成】
- message表设计（conversation_id, sender_id, receiver_id, content, type, create_time）
- conversation表设计（id, user1_id, user2_id, last_message, last_time, unread_count_1, unread_count_2）
- WebSocket配置和Handler
- 消息Service
```

---

# Phase 4: 前后端联调

---

## Prompt 4.1: Mock切换真实接口

```markdown
【任务】将小程序从Mock模式切换到真实后端接口

【当前状态】
- 小程序所有页面已开发完成，使用Mock数据运行
- 后端所有接口已开发完成
- request.js中有USE_MOCK开关

【需要做的事】
1. 将request.js中的 USE_MOCK = false
2. 设置 BASE_URL = 'http://localhost:8080'（开发环境）
3. 逐页面检查接口调用，确认：
   - 请求URL正确
   - 请求参数字段名与后端DTO一致（驼峰命名）
   - 响应数据字段名与后端VO一致
4. 处理真实环境的特殊逻辑：
   - 微信登录的code获取（uni.login）
   - 文件上传改为真实上传（uni.uploadFile）
   - 图片URL拼接BASE_URL前缀

【检查清单】
请逐一检查以下页面的接口调用是否与后端一致：
1. 登录页 - wx-login / login / sms-login / sms-send
2. 认证页 - auth/submit / auth/status / college/list / upload
3. 首页 - campus/list / banner/list / category/list / product/list
4. 搜索页 - search/hot-keywords / product/list
5. 商品详情 - product/detail / favorite/add / favorite/cancel
6. 发布页 - product/publish / upload
7. 个人中心 - user/info / user/stats / notification/unread-count
8. 订单 - order/list / order/create / order/confirm / order/cancel
9. 评价 - review/submit / review/detail
10. 举报 - report/submit
11. 消息中心 - notification/list / notification/read
12. 收藏 - favorite/list
13. 设置 - user/update / user/deactivate

请列出所有不一致的地方并修改。
```

---

# Phase 5: 管理后台页面开发

---

## Prompt 5.1: 后台基础框架

```markdown
【任务】开发管理后台登录页和基础布局（已在Prompt 1.3中创建了骨架，现在补充完整功能）

【已有】Vue 3 + Element Plus + Vite项目骨架、路由配置、Layout组件、Axios封装

【需要完善】

### 1. views/login/LoginView.vue - 登录页
- 居中登录卡片（白色卡片，阴影）
- 顶部：Logo + "轻院二手 管理后台"
- 表单：用户名输入框 + 密码输入框 + 登录按钮
- 记住密码勾选（localStorage）
- 回车键触发登录
- 登录成功跳转Dashboard

### 2. 完善Layout.vue
- 左侧菜单栏默认展开，支持折叠
- 面包屑导航
- 顶部右侧：管理员姓名 + 下拉菜单（个人信息/退出登录）
- 退出登录确认弹窗

【调用接口】
- POST /admin/employee/login → api/auth.js
- GET /admin/employee/info → api/auth.js

请生成完整的LoginView.vue和完善后的Layout.vue。
同时完善api/auth.js中的接口调用。
```

---

## Prompt 5.2: 数据概览Dashboard

```markdown
【任务】开发管理后台数据概览Dashboard页面

【页面路径】views/dashboard/DashboardView.vue

【页面布局】
- 第一行：4个数据卡片（今日新增用户/今日新增商品/今日成交量/今日GMV）
  - 每个卡片：图标+数值+标题+较昨日增长百分比
- 第二行：3个待处理事项卡片（待审核商品/待审核认证/待处理举报）
  - 点击卡片跳转对应管理页面
- 第三行：趋势图表（使用ECharts）
  - Tab切换：近7天/近30天
  - 折线图：新增用户+新增商品+成交量 三条线
- 第四行：两个饼图并排
  - 左：按校区统计商品分布
  - 右：按分类统计商品分布

【调用接口】
- GET /admin/stats/overview
- GET /admin/stats/trend?days=7
- GET /admin/stats/campus
- GET /admin/stats/category

请生成DashboardView.vue和api/stats.js
```

---

## Prompt 5.3 - 5.14: 管理后台各功能页面

```markdown
【任务】开发管理后台全部功能页面

以下每个页面遵循统一结构：
- 顶部筛选条件区（Element Plus Form + Select/Input/DatePicker）
- 操作按钮区（新增/批量操作等）
- 数据表格（Element Plus Table，带分页）
- 详情/编辑弹窗（Element Plus Dialog）

--- 5.3 商品审核页 views/product/ProductReview.vue ---
筛选：审核状态(待审核/已通过/已驳回) + 分类 + 校区 + 时间范围
表格列：ID/首图/标题/分类/价格/发布者/校区/提交时间/状态/操作
操作：查看详情(Dialog显示全部图片和信息) / 通过 / 驳回(弹窗输入原因)
支持：复选框批量通过
接口：/admin/product/page, /admin/product/approve, /admin/product/reject, /admin/product/batch-approve

--- 5.4 商品列表页 views/product/ProductList.vue ---
筛选：状态(全部/在售/已下架/已售出) + 分类 + 校区 + 关键词搜索
表格列：ID/首图/标题/分类/价格/卖家/校区/浏览量/收藏量/状态/发布时间/操作
操作：查看详情 / 强制下架
接口：/admin/product/page, /admin/product/force-off

--- 5.5 认证审核页 views/auth-review/AuthReview.vue ---
筛选：审核状态 + 学院 + 时间范围
表格列：ID/用户昵称/手机号/学院/学号/班级/认证材料(点击查看大图)/提交时间/状态/操作
操作：查看材料(Dialog显示大图) / 通过 / 驳回(输入原因)
接口：/admin/auth/page, /admin/auth/approve, /admin/auth/reject

--- 5.6 用户管理页 views/user/UserList.vue ---
筛选：搜索(昵称/手机号/学号) + 认证状态 + 账号状态
表格列：ID/头像/昵称/手机号/校区/认证状态/评分/账号状态/注册时间/操作
操作：查看详情(Dialog) / 封禁(输入原因) / 解封
接口：/admin/user/page, /admin/user/detail, /admin/user/ban, /admin/user/unban

--- 5.7 订单管理页 views/order/OrderList.vue ---
筛选：订单状态 + 校区 + 时间范围 + 搜索(订单号/用户名)
表格列：订单号/商品标题/买家/卖家/金额/校区/面交地点/状态/创建时间
操作：查看详情(Dialog)
接口：/admin/order/page, /admin/order/detail

--- 5.8 举报管理页 views/report/ReportList.vue ---
筛选：处理状态(待处理/已处理/已忽略) + 举报类型 + 时间范围
表格列：ID/举报人/目标类型/被举报内容/举报原因/补充说明/状态/举报时间/操作
操作：查看详情 / 处理(Dialog: 选择操作类型+填写处理结果)
接口：/admin/report/page, /admin/report/handle

--- 5.9 分类管理页 views/category/CategoryList.vue ---
表格列：ID/分类名称/图标/排序/状态/操作
操作：编辑(Dialog) / 删除(确认弹窗)
新增按钮
接口：/admin/category/page, /admin/category/add, /admin/category/update, /admin/category/delete

--- 5.10 校区管理页 views/campus/CampusList.vue ---
上半部分：校区列表
表格列：ID/校区名称/校区编码/排序/状态/操作
操作：编辑 / 管理面交地点

下半部分（或弹窗）：面交地点管理
选择校区后显示该校区的面交地点列表
表格列：ID/地点名称/排序/状态/操作
操作：编辑/删除，新增
接口：/admin/campus/*, /admin/campus/meeting-point/*

--- 5.11 学院管理页 views/college/CollegeList.vue ---
表格列：ID/学院名称/排序/状态/操作
操作：编辑/删除，新增
接口：/admin/college/*

--- 5.12 Banner管理页 views/banner/BannerList.vue ---
表格列：ID/图片预览/标题/跳转类型/展示校区/排序/状态/有效期/操作
操作：编辑(Dialog，含图片上传)/删除，新增
接口：/admin/banner/*

--- 5.13 公告管理页 views/notice/NoticeList.vue ---
表格列：ID/标题/类型/状态/发布人/发布时间/操作
操作：编辑(Dialog)/删除，新增
新增时确认"发布后将通知所有用户"
接口：/admin/notice/*

--- 5.14 员工管理页 views/employee/EmployeeList.vue ---
表格列：ID/用户名/姓名/手机号/角色/状态/创建时间/操作
操作：编辑/重置密码(确认弹窗)，新增
接口：/admin/employee/*

【统一要求】
- 每个页面对应的api/xxx.js文件需同步生成
- 表格使用Element Plus的el-table + el-pagination
- 筛选条件使用el-form inline模式
- 弹窗使用el-dialog
- 删除操作使用ElMessageBox.confirm确认
- 操作成功使用ElMessage.success提示
- 表格loading状态
- 分页参数：page/pageSize，默认pageSize=20
- 时间格式化使用dayjs
```

---

# 完成总结

---

## 📋 Prompt使用检查清单

| 使用Prompt前 | 检查事项 |
|-------------|----------|
| ✅ | 确认已安装好开发环境（JDK17、Maven、Node.js、MySQL、Redis） |
| ✅ | 确认AI的系统规则已配置（文档三的AI规则） |
| ✅ | 按Phase顺序执行，不要跳过 |
| ✅ | 每个Prompt执行后验证代码可运行再进入下一个 |
| ✅ | 后续Prompt中提到"已有"的文件，确认确实已生成 |

| 使用Prompt时 | 注意事项 |
|-------------|----------|
| ✅ | 如果AI生成的代码有编译错误，让AI立即修复 |
| ✅ | 如果生成的代码与已有代码冲突，告诉AI已有代码的内容 |
| ✅ | 复杂模块（如商品、订单）建议分2-3次对话完成 |
| ✅ | 每个模块完成后做一次Git commit |

| Prompt执行后 | 验证事项 |
|-------------|----------|
| ✅ | 后端：编译通过、启动不报错、接口可用Postman测试 |
| ✅ | 小程序：编译通过、页面可正常展示、Mock数据正确渲染 |
| ✅ | 管理后台：编译通过、页面可正常访问、表格数据展示正确 |

---

**以上就是全部AI开发Prompt。按照Phase 1→2→3→4→5的顺序逐个执行即可完成整个项目的开发。**

如需对某个Prompt进行细化或调整，请随时告诉我 👇