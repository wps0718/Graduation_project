# 二手交易平台 接口文档

> 项目：二手交易平台（毕业设计）  
> 根包：`com.qingyuan.secondhand`  
> 统一响应：`Result<T>`  
> 认证方式：JWT（Header 携带 token）

---

## 一、公共接口 `/common`

### CommonController

| 方法 | 路径 | 说明 | 参数 | 返回 |
|------|------|------|------|------|
| POST | `/common/upload` | 文件上传 | `file`(MultipartFile), `type`(默认"common") | `Result<Map<String,String>>` url |

---

## 二、管理端接口 `/admin`

### 2.1 员工管理 `/admin/employee`

| 方法 | 路径 | 说明 | 参数 | 返回 |
|------|------|------|------|------|
| POST | `/admin/employee/login` | 管理员登录 | `EmployeeLoginDTO` | `Result<EmployeeLoginVO>` |
| GET | `/admin/employee/info` | 获取当前员工信息 | — | `Result<EmployeeVO>` |
| GET | `/admin/employee/page` | 员工分页列表 | `page`(默认1), `pageSize`(默认10), `keyword`(可选) | `Result<Page<EmployeeVO>>` |
| POST | `/admin/employee/add` | 新增员工 | `EmployeeDTO` | `Result<Void>` |
| POST | `/admin/employee/update` | 修改员工 | `EmployeeDTO` | `Result<Void>` |
| POST | `/admin/employee/reset-password` | 重置密码 | `id`(query) | `Result<Void>` |


### 2.2 用户管理 `/admin/user`

| 方法 | 路径 | 说明 | 参数 | 返回 |
|------|------|------|------|------|
| GET | `/admin/user/page` | 用户分页列表 | `page`(默认1), `pageSize`(默认10), `keyword`(可选), `status`(可选), `authStatus`(可选), `campusId`(可选) | `Result<Page<AdminUserPageVO>>` |
| GET | `/admin/user/detail/{id}` | 用户详情 | `id`(path) | `Result<AdminUserDetailVO>` |
| POST | `/admin/user/ban` | 封禁用户 | `AdminUserBanDTO`(userId, banReason) | `Result<Void>` |
| POST | `/admin/user/unban` | 解封用户 | `AdminUserUnbanDTO`(userId) | `Result<Void>` |

### 2.3 商品管理 `/admin/product`

| 方法 | 路径 | 说明 | 参数 | 返回 |
|------|------|------|------|------|
| GET | `/admin/product/page` | 商品分页列表 | `page`, `pageSize`, `status`(可选) | `Result<IPage<AdminProductPageVO>>` |
| GET | `/admin/product/detail/{productId}` | 商品详情 | `productId`(path) | `Result<ProductDetailVO>` |
| POST | `/admin/product/approve` | 审核通过 | `productId`(query) | `Result<Void>` |
| POST | `/admin/product/reject` | 审核驳回 | `ProductRejectDTO`(productId, rejectReason) | `Result<Void>` |
| POST | `/admin/product/batch-approve` | 批量审核通过 | `List<Long>`(body) | `Result<Void>` |
| POST | `/admin/product/force-off` | 强制下架 | `productId`(query) | `Result<Void>` |

### 2.4 订单管理 `/admin/order`

| 方法 | 路径 | 说明 | 参数 | 返回 |
|------|------|------|------|------|
| GET | `/admin/order/page` | 订单分页列表 | `page`, `pageSize`, `status`(可选) | `Result<IPage<AdminOrderPageVO>>` |
| GET | `/admin/order/detail/{id}` | 订单详情 | `id`(path) | `Result<OrderDetailVO>` |

### 2.5 认证审核 `/admin/auth`

| 方法 | 路径 | 说明 | 参数 | 返回 |
|------|------|------|------|------|
| GET | `/admin/auth/page` | 认证申请分页 | `page`(默认1), `size`(默认10), `status`(可选), `collegeId`(可选) | `Result<Page<AuthPageVO>>` |
| GET | `/admin/auth/detail/{id}` | 认证申请详情 | `id`(path) | `Result<AuthPageVO>` |
| GET | `/admin/auth/history/{authId}` | 认证历史记录 | `authId`(path) | `Result<List<AuthHistoryVO>>` |
| POST | `/admin/auth/approve` | 审核通过 | `{id}`(body) | `Result<Void>` |
| POST | `/admin/auth/reject` | 审核驳回 | `{id, rejectReason}`(body) | `Result<Void>` |


### 2.6 举报管理 `/admin/report`

| 方法 | 路径 | 说明 | 参数 | 返回 |
|------|------|------|------|------|
| GET | `/admin/report/page` | 举报分页列表 | `page`, `pageSize`, `status`(可选), `targetType`(可选) | `Result<IPage<ReportPageVO>>` |
| GET | `/admin/report/detail/{id}` | 举报详情 | `id`(path) | `Result<ReportDetailVO>` |
| POST | `/admin/report/handle` | 处理举报 | `ReportHandleDTO` | `Result<Void>` |

### 2.7 分类管理 `/admin/category`

| 方法 | 路径 | 说明 | 参数 | 返回 |
|------|------|------|------|------|
| GET | `/admin/category/page` | 分类分页列表 | `page`(默认1), `pageSize`(默认10), `name`(可选) | `Result<Page<CategoryVO>>` |
| GET | `/admin/category/list` | 分类全量列表 | — | `Result<List<CategoryVO>>` |
| POST | `/admin/category/add` | 新增分类 | `CategoryDTO` | `Result<Void>` |
| POST | `/admin/category/update` | 修改分类 | `CategoryDTO` | `Result<Void>` |
| POST | `/admin/category/delete` | 删除分类 | `id`(query) | `Result<Void>` |

### 2.8 校区管理 `/admin/campus`

| 方法 | 路径 | 说明 | 参数 | 返回 |
|------|------|------|------|------|
| GET | `/admin/campus/list` | 校区列表 | — | `Result<List<CampusVO>>` |
| POST | `/admin/campus/add` | 新增校区 | `CampusDTO` | `Result<Void>` |
| POST | `/admin/campus/update` | 修改校区 | `CampusDTO` | `Result<Void>` |
| GET | `/admin/campus/meeting-point/list/{campusId}` | 交易点列表 | `campusId`(path) | `Result<List<MeetingPointVO>>` |
| POST | `/admin/campus/meeting-point/add` | 新增交易点 | `MeetingPointDTO` | `Result<Void>` |
| POST | `/admin/campus/meeting-point/update` | 修改交易点 | `MeetingPointDTO` | `Result<Void>` |
| POST | `/admin/campus/meeting-point/delete` | 删除交易点 | `id`(query) | `Result<Void>` |

### 2.9 学院管理 `/admin/college`

| 方法 | 路径 | 说明 | 参数 | 返回 |
|------|------|------|------|------|
| GET | `/admin/college/list` | 学院列表 | — | `Result<List<College>>` |
| POST | `/admin/college/add` | 新增学院 | `CollegeDTO` | `Result<Void>` |
| POST | `/admin/college/update` | 修改学院 | `CollegeDTO` | `Result<Void>` |
| POST | `/admin/college/delete` | 删除学院 | `id`(query) | `Result<Void>` |

### 2.10 轮播图管理 `/admin/banner`

| 方法 | 路径 | 说明 | 参数 | 返回 |
|------|------|------|------|------|
| GET | `/admin/banner/page` | 轮播图分页 | `page`(默认1), `pageSize`(默认10), `status`(可选), `campusId`(可选) | `Result<IPage<BannerVO>>` |
| POST | `/admin/banner/add` | 新增轮播图 | `BannerDTO` | `Result<Void>` |
| POST | `/admin/banner/update` | 修改轮播图 | `id`(query), `BannerDTO` | `Result<Void>` |
| POST | `/admin/banner/delete` | 删除轮播图 | `id`(query) | `Result<Void>` |

### 2.11 公告管理 `/admin/notice`

| 方法 | 路径 | 说明 | 参数 | 返回 |
|------|------|------|------|------|
| GET | `/admin/notice/page` | 公告分页列表 | `page`(默认1), `pageSize`(默认10), `type`(可选), `status`(可选) | `Result<Page<NoticeVO>>` |
| POST | `/admin/notice/add` | 新增公告 | `NoticeDTO` | `Result<Void>` |
| POST | `/admin/notice/update` | 修改公告 | `NoticeDTO` | `Result<Void>` |
| POST | `/admin/notice/delete` | 删除公告 | `id`(query) | `Result<Void>` |

### 2.12 数据统计 `/admin/stats`

| 方法 | 路径 | 说明 | 参数 | 返回 |
|------|------|------|------|------|
| GET | `/admin/stats/overview` | 总览数据 | — | `Result<StatsOverviewVO>` |
| GET | `/admin/stats/trend` | 趋势数据 | `days`(可选) | `Result<List<StatsTrendVO>>` |
| GET | `/admin/stats/campus` | 各校区统计 | — | `Result<List<StatsCampusVO>>` |
| GET | `/admin/stats/category` | 各分类统计 | — | `Result<List<StatsCategoryVO>>` |


---

## 三、小程序端接口 `/mini`

### 3.1 用户 `/mini/user`

| 方法 | 路径 | 说明 | 参数 | 返回 |
|------|------|------|------|------|
| POST | `/mini/user/wx-login` | 微信登录 | `WxLoginDTO`(code) | `Result<LoginVO>` |
| POST | `/mini/user/login` | 账号密码登录 | `AccountLoginDTO` | `Result<LoginVO>` |
| POST | `/mini/user/sms/send` | 发送短信验证码 | `SmsSendDTO`(phone) | `Result<Void>` |
| POST | `/mini/user/sms-login` | 短信验证码登录 | `SmsLoginDTO`(phone, code) | `Result<LoginVO>` |
| GET | `/mini/user/info` | 获取当前用户信息 | — | `Result<UserInfoVO>` |
| POST | `/mini/user/update` | 更新用户信息 | `UserUpdateDTO` | `Result<Void>` |
| POST | `/mini/user/accept-agreement` | 接受用户协议 | — | `Result<Void>` |
| GET | `/mini/user/stats` | 用户统计数据 | — | `Result<UserStatsVO>` |
| GET | `/mini/user/profile/{id}` | 查看他人主页 | `id`(path), `page`(默认1), `pageSize`(默认10) | `Result<UserProfileVO>` |
| POST | `/mini/user/deactivate` | 申请注销账号 | — | `Result<Void>` |
| POST | `/mini/user/restore` | 撤销注销申请 | — | `Result<Void>` |

### 3.2 校园认证 `/mini/auth`

| 方法 | 路径 | 说明 | 参数 | 返回 |
|------|------|------|------|------|
| POST | `/mini/auth/submit` | 提交认证申请 | `AuthSubmitDTO` | `Result<Void>` |
| GET | `/mini/auth/status` | 获取认证状态 | — | `Result<AuthStatusVO>` |
| GET | `/mini/auth/history` | 认证历史列表 | — | `Result<List<AuthHistoryVO>>` |
| GET | `/mini/auth/history/{id}` | 认证历史详情 | `id`(path) | `Result<AuthHistoryVO>` |

### 3.3 商品 `/mini/product`

| 方法 | 路径 | 说明 | 参数 | 返回 |
|------|------|------|------|------|
| POST | `/mini/product/publish` | 发布商品 | `ProductPublishDTO` | `Result<Void>` |
| POST | `/mini/product/update` | 修改商品 | `ProductUpdateDTO` | `Result<Void>` |
| POST | `/mini/product/update-price` | 修改价格 | `ProductUpdatePriceDTO`(productId, price) | `Result<Void>` |
| POST | `/mini/product/off-shelf` | 下架商品 | `ProductIdDTO`(productId) | `Result<Void>` |
| POST | `/mini/product/on-shelf` | 重新上架 | `ProductIdDTO`(productId) | `Result<Void>` |
| POST | `/mini/product/delete` | 删除商品 | `ProductIdDTO`(productId) | `Result<Void>` |
| GET | `/mini/product/detail/{productId}` | 商品详情 | `productId`(path) | `Result<ProductDetailVO>` |
| GET | `/mini/product/list` | 商品列表（含搜索/筛选） | `page`, `pageSize`, `campusId`(可选), `categoryId`(可选), `keyword`(可选), `minPrice`(可选), `maxPrice`(可选), `sortBy`(默认"latest") | `Result<IPage<ProductListVO>>` |
| GET | `/mini/product/my-list` | 我的商品列表 | `page`, `pageSize`, `status`(可选) | `Result<IPage<ProductListVO>>` |

### 3.4 商品评论 `/mini/product/comment`

| 方法 | 路径 | 说明 | 参数 | 返回 |
|------|------|------|------|------|
| POST | `/mini/product/comment/add` | 发表评论/回复 | `ProductCommentAddDTO` | `Result<Void>` |
| POST | `/mini/product/comment/delete/{commentId}` | 删除评论 | `commentId`(path) | `Result<Void>` |
| GET | `/mini/product/comment/list/{productId}` | 商品评论列表 | `productId`(path) | `Result<List<ProductCommentVO>>` |
| GET | `/mini/product/comment/received-replies` | 收到的回复列表 | `page`(默认1), `pageSize`(默认10) | `Result<IPage<ReceivedReplyVO>>` |
| GET | `/mini/product/comment/unread-reply-count` | 未读回复数量 | — | `Result<Long>` |
| POST | `/mini/product/comment/mark-read` | 标记回复已读 | — | `Result<Void>` |


### 3.5 订单 `/mini/order`

| 方法 | 路径 | 说明 | 参数 | 返回 |
|------|------|------|------|------|
| POST | `/mini/order/create` | 创建订单 | `OrderCreateDTO` | `Result<OrderCreateVO>` |
| GET | `/mini/order/list` | 订单列表 | `role`(buyer/seller), `status`(可选), `page`(默认1), `pageSize`(默认10) | `Result<IPage<OrderListVO>>` |
| GET | `/mini/order/detail/{id}` | 订单详情 | `id`(path) | `Result<OrderDetailVO>` |
| POST | `/mini/order/confirm` | 确认收货 | `OrderIdDTO`(orderId) | `Result<Void>` |
| POST | `/mini/order/cancel` | 取消订单 | `OrderCancelDTO`(orderId, cancelReason) | `Result<Void>` |
| POST | `/mini/order/delete` | 删除订单记录 | `OrderIdDTO`(orderId) | `Result<Void>` |

### 3.6 评价 `/mini/review`

| 方法 | 路径 | 说明 | 参数 | 返回 |
|------|------|------|------|------|
| POST | `/mini/review/submit` | 提交评价 | `ReviewSubmitDTO` | `Result<Void>` |
| GET | `/mini/review/detail/{orderId}` | 查看订单评价 | `orderId`(path) | `Result<ReviewDetailVO>` |

### 3.7 收藏 `/mini/favorite`

| 方法 | 路径 | 说明 | 参数 | 返回 |
|------|------|------|------|------|
| POST | `/mini/favorite/add` | 收藏商品 | `FavoriteDTO`(productId) | `Result<Void>` |
| POST | `/mini/favorite/cancel` | 取消收藏 | `FavoriteDTO`(productId) | `Result<Void>` |
| GET | `/mini/favorite/list` | 收藏列表 | `page`(默认1), `pageSize`(默认10) | `Result<IPage<FavoriteListVO>>` |
| GET | `/mini/favorite/check/{productId}` | 是否已收藏 | `productId`(path) | `Result<Boolean>` |

### 3.8 关注 `/mini/follow`

| 方法 | 路径 | 说明 | 参数 | 返回 |
|------|------|------|------|------|
| POST | `/mini/follow/follow` | 关注用户 | `FollowUserDTO`(userId) | `Result<Void>` |
| POST | `/mini/follow/unfollow` | 取消关注 | `FollowUserDTO`(userId) | `Result<Void>` |
| GET | `/mini/follow/check/{userId}` | 是否已关注 | `userId`(path) | `Result<Boolean>` |
| GET | `/mini/follow/stats/{userId}` | 关注/粉丝统计 | `userId`(path) | `Result<FollowStatsVO>` |

### 3.9 消息通知 `/mini/notification`

| 方法 | 路径 | 说明 | 参数 | 返回 |
|------|------|------|------|------|
| GET | `/mini/notification/list` | 通知列表 | `page`(默认1), `pageSize`(默认10), `category`(可选:1交易/2系统) | `Result<IPage<NotificationVO>>` |
| GET | `/mini/notification/favorite-list` | 收藏通知列表 | `page`(默认1), `pageSize`(默认20) | `Result<IPage<FavoriteNotificationVO>>` |
| GET | `/mini/notification/follower-list` | 关注通知列表 | `page`(默认1), `pageSize`(默认20) | `Result<IPage<FollowerNotificationVO>>` |
| POST | `/mini/notification/read` | 标记单条已读 | `NotificationReadDTO`(id) | `Result<Void>` |
| POST | `/mini/notification/read-batch` | 批量标记已读 | `NotificationReadBatchDTO`(ids) | `Result<Void>` |
| POST | `/mini/notification/read-type` | 按类型全部已读 | `type`(query) | `Result<Void>` |
| POST | `/mini/notification/read-all` | 全部已读 | — | `Result<Void>` |
| GET | `/mini/notification/unread-count` | 未读数量 | — | `Result<UnreadCountVO>` |

### 3.10 聊天 `/mini/chat`

| 方法 | 路径 | 说明 | 参数 | 返回 |
|------|------|------|------|------|
| POST | `/mini/chat/session/create` | 创建会话 | `ChatSessionCreateDTO` | `Result<ChatSessionVO>` |
| GET | `/mini/chat/sessions` | 会话列表 | — | `Result<List<ChatSessionVO>>` |
| GET | `/mini/chat/list` | 会话列表（含统计） | — | `Result<Map<String,Object>>` |
| POST | `/mini/chat/session/delete` | 删除会话 | `{sessionId}`(body) | `Result<Void>` |
| POST | `/mini/chat/delete` | 删除会话（别名） | `{sessionId}`(body) | `Result<Void>` |
| POST | `/mini/chat/session/top` | 置顶/取消置顶 | `{sessionId}`(body) | `Result<Void>` |
| GET | `/mini/chat/unread-total` | 总未读消息数 | — | `Result<Integer>` |
| GET | `/mini/chat/messages` | 消息历史记录 | `sessionKey`, `page`, `pageSize`(query) | `Result<Map<String,Object>>` |
| POST | `/mini/chat/read` | 标记会话已读 | `{sessionKey}`(body) | `Result<Void>` |
| POST | `/mini/chat/message/send` | 发送消息 | `ChatMessageSendDTO` | `Result<Long>` |

### 3.11 举报 `/mini/report`

| 方法 | 路径 | 说明 | 参数 | 返回 |
|------|------|------|------|------|
| POST | `/mini/report/submit` | 提交举报 | `ReportSubmitDTO` | `Result<Void>` |
| GET | `/mini/report/detail/{id}` | 举报详情 | `id`(path) | `Result<ReportDetailVO>` |

### 3.12 搜索 `/mini/search`

| 方法 | 路径 | 说明 | 参数 | 返回 |
|------|------|------|------|------|
| GET | `/mini/search/hot-keywords` | 热门搜索词 | — | `Result<List<HotKeywordVO>>` |

### 3.13 分类 `/mini/category`

| 方法 | 路径 | 说明 | 参数 | 返回 |
|------|------|------|------|------|
| GET | `/mini/category/list` | 分类列表 | — | `Result<List<CategoryVO>>` |

### 3.14 校区 `/mini/campus`

| 方法 | 路径 | 说明 | 参数 | 返回 |
|------|------|------|------|------|
| GET | `/mini/campus/list` | 校区列表 | — | `Result<List<CampusVO>>` |
| GET | `/mini/campus/meeting-points/{campusId}` | 校区交易点列表 | `campusId`(path) | `Result<List<MeetingPointVO>>` |

### 3.15 学院 `/mini/college`

| 方法 | 路径 | 说明 | 参数 | 返回 |
|------|------|------|------|------|
| GET | `/mini/college/list` | 学院列表 | — | `Result<List<CollegeVO>>` |

### 3.16 轮播图 `/mini/banner`

| 方法 | 路径 | 说明 | 参数 | 返回 |
|------|------|------|------|------|
| GET | `/mini/banner/list` | 轮播图列表 | `campusId`(query) | `Result<List<BannerVO>>` |


---

## 四、枚举值速查

| 字段 | 值 | 含义 |
|------|----|------|
| `user.gender` | 0/1/2 | 未知/男/女 |
| `user.status` | 0/1/2 | 封禁/正常/注销中 |
| `user.auth_status` | 0/1/2/3 | 未认证/审核中/已认证/已驳回 |
| `product.status` | 0/1/2/3/4 | 待审核/在售/已下架/已售出/审核驳回 |
| `product.condition_level` | 1/2/3/4/5 | 全新/几乎全新/9成新/8成新/7成新及以下 |
| `trade_order.status` | 1/2/3/4/5 | 待面交/预留/已完成/已评价/已取消 |
| `campus_auth.status` | 0/1/2 | 待审核/通过/驳回 |
| `notification.type` | 1-10 | 交易成功/新消息/审核通过/审核驳回/系统公告/被收藏/订单取消/认证通过/认证驳回/评价提醒 |
| `notification.category` | 1/2 | 交易/系统 |
| `report.target_type` | 1/2 | 商品/用户 |
| `report.reason_type` | 1/2/3/4/5 | 虚假商品/违禁物品/价格异常/骚扰信息/其他 |
| `report.status` | 0/1/2 | 待处理/已处理/已忽略 |
| `employee.role` | 1/2 | 超级管理员/普通管理员 |
| `banner.link_type` | 1/2/3 | 商品详情/活动页/外部链接 |
| `notice.type` | 1/2 | 系统公告/活动公告 |

---

## 五、接口统计

| 模块 | 接口数 |
|------|--------|
| 公共 (common) | 1 |
| 管理端 (admin) | 51 |
| 小程序端 (mini) | 67 |
| **合计** | **119** |
