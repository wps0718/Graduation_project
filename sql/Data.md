# 轻院二手交易平台 - 数据库设计文档

基于 `init.sql` 生成的数据库结构摘要。

---

## 1. 用户表（user）

存储微信小程序注册用户的核心信息。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键，自增，起始 10000 |
| open_id | varchar(64) | 微信 openid（唯一索引） |
| session_key | varchar(255) | 微信会话密钥 |
| nick_name | varchar(32) | 昵称 |
| username | varchar(50) | 用户名（手机号） |
| password | varchar(255) | 密码（BCrypt 加密） |
| avatar_url | varchar(255) | 头像 URL |
| bio | varchar(200) | 个人简介 |
| ip_region | varchar(64) | IP 属地 |
| gender | tinyint | 性别：0-未知 / 1-男 / 2-女 |
| phone | varchar(11) | 手机号（唯一索引） |
| campus_id | bigint | 所在校区 ID |
| auth_status | tinyint | 认证状态：0-未认证 / 1-审核中 / 2-已认证 / 3-已驳回 |
| score | decimal(3,1) | 综合评分（默认 5.0） |
| status | tinyint | 账号状态：0-封禁 / 1-正常 / 2-注销中 |
| ban_reason | varchar(255) | 封禁原因 |
| deactivate_time | datetime | 注销申请时间 |
| agreement_accepted | tinyint | 是否同意用户协议 |
| last_login_time | datetime | 最后登录时间 |

**索引：** 主键、open_id（唯一）、phone（唯一）、campus_id、status

---

## 1.1 用户关注表（user_follow）

记录用户之间的关注关系。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| follower_id | bigint | 关注者用户 ID |
| followee_id | bigint | 被关注用户 ID |

**索引：** 关注者+被关注者联合唯一、被关注者 ID、关注者 ID

---

## 2. 管理员表（employee）

存储管理后台的运营人员账号。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| username | varchar(32) | 用户名（唯一） |
| password | varchar(255) | 密码（BCrypt 加密） |
| name | varchar(32) | 姓名 |
| phone | varchar(11) | 手机号 |
| role | tinyint | 角色：1-超级管理员 / 2-普通管理员 |
| status | tinyint | 状态：0-禁用 / 1-启用 |

**默认数据：** admin / 123456（超级管理员）

**索引：** 主键、username（唯一）

---

## 3. 学院表（college）

存储学校内的学院/系所信息，用于校园认证。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| name | varchar(64) | 学院名称 |
| sort | int | 排序号 |
| status | tinyint | 状态：0-禁用 / 1-启用 |

**索引：** 主键

---

## 4. 校区表（campus）

存储学校的不同校区信息。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| name | varchar(32) | 校区名称 |
| code | varchar(32) | 校区编码（唯一） |
| status | tinyint | 状态：0-禁用 / 1-启用 |
| sort | int | 排序号 |

**默认数据：** 南海北、南海南、新港

**索引：** 主键、code（唯一）

---

## 5. 面交地点表（meeting_point）

存储各校区内预设的面交地点。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| campus_id | bigint | 所属校区 ID |
| name | varchar(64) | 地点名称 |
| status | tinyint | 状态：0-禁用 / 1-启用 |
| sort | int | 排序号 |

**默认数据（7 条）：** 南海北 3 个、南海南 2 个、新港 2 个

**索引：** 主键、campus_id

---

## 6. 商品分类表（category）

存储商品的分类体系。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| name | varchar(32) | 分类名称 |
| icon | varchar(255) | 分类图标 URL |
| sort | int | 排序号 |
| status | tinyint | 状态：0-禁用 / 1-启用 |

**默认数据（6 条）：** 书籍、服饰、生活、电子设备、运动设备、潮玩娱乐

**索引：** 主键

---

## 7. 校园认证表（campus_auth）

存储用户的校园认证申请记录（审核通过后用户获得"已认证"状态）。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| user_id | bigint | 用户 ID |
| college_id | bigint | 学院 ID |
| real_name | varchar(32) | 真实姓名 |
| student_no | varchar(32) | 学号（唯一） |
| class_name | varchar(64) | 班级 |
| cert_image | varchar(255) | 认证材料图片 URL |
| status | tinyint | 审核状态：0-待审核 / 1-通过 / 2-驳回 |
| reject_reason | varchar(255) | 驳回原因 |
| review_time | datetime | 审核时间 |
| reviewer_id | bigint | 审核人 ID（管理员） |

**索引：** 主键、user_id、status、student_no（唯一）

---

## 8. 校园认证历史表（campus_auth_history）

记录校园认证的每次变更历史。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| user_id | bigint | 用户 ID |
| auth_id | bigint | 认证主表 ID |
| college_id | bigint | 学院 ID |
| real_name | varchar(32) | 真实姓名 |
| student_no | varchar(32) | 学号 |
| class_name | varchar(64) | 班级 |
| cert_image | varchar(255) | 认证材料图片 URL |
| status | tinyint | 审核状态：0-待审核 / 1-通过 / 2-驳回 |
| reject_reason | varchar(255) | 驳回原因 |
| review_time | datetime | 审核时间 |
| reviewer_id | bigint | 审核人 ID（管理员） |

**索引：** 主键、user_id、auth_id、status

---

## 9. 商品表（product）

核心业务表，存储用户发布的二手商品。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| user_id | bigint | 发布者用户 ID |
| title | varchar(50) | 商品标题 |
| description | varchar(500) | 商品描述 |
| price | decimal(10,2) | 二手价格 |
| original_price | decimal(10,2) | 原价 |
| category_id | bigint | 分类 ID |
| condition_level | tinyint | 成色：1-全新 / 2-几乎全新 / 3-9成新 / 4-8成新 / 5-7成新及以下 |
| campus_id | bigint | 交易校区 ID |
| meeting_point_id | bigint | 面交地点 ID（预设） |
| meeting_point_text | varchar(100) | 面交地点（自定义） |
| images | varchar(2000) | 图片 JSON 数组 |
| view_count | int | 浏览量 |
| favorite_count | int | 收藏量 |
| status | tinyint | 状态：0-待审核 / 1-在售 / 2-已下架 / 3-已售出 / 4-审核驳回 |
| reject_reason | varchar(255) | 驳回原因 |
| review_time | datetime | 审核时间 |
| reviewer_id | bigint | 审核人 ID |
| auto_off_time | datetime | 自动下架时间（发布后 90 天） |
| is_deleted | tinyint | 逻辑删除：0-否 / 1-是 |

**索引：** 主键、user_id、category_id、campus_id、status、create_time、auto_off_time，以及复合索引（status+campus_id+category_id+create_time）、（status+price）

---

## 10. 收藏表（favorite）

记录用户收藏的商品。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| user_id | bigint | 用户 ID |
| product_id | bigint | 商品 ID |

**索引：** 用户+商品联合唯一、product_id

---

## 11. 订单表（trade_order）

存储交易订单信息，记录买卖双方的交易流程。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| order_no | varchar(32) | 订单号（唯一） |
| product_id | bigint | 商品 ID |
| buyer_id | bigint | 买家用户 ID |
| seller_id | bigint | 卖家用户 ID |
| price | decimal(10,2) | 成交价格 |
| campus_id | bigint | 面交校区 ID |
| meeting_point | varchar(100) | 面交地点 |
| status | tinyint | 状态：1-待面交 / 2-预留 / 3-已完成 / 4-已评价 / 5-已取消 |
| cancel_reason | varchar(255) | 取消原因 |
| cancel_by | bigint | 取消操作人 ID（0=系统自动） |
| expire_time | datetime | 过期时间（创建后 72 小时） |
| confirm_deadline | datetime | 自动确认收货截止（创建后 7 天） |
| complete_time | datetime | 交易完成时间 |
| is_deleted_buyer | tinyint | 买家是否删除 |
| is_deleted_seller | tinyint | 卖家是否删除 |

**索引：** 主键、order_no（唯一）、buyer_id、seller_id、product_id、status、expire_time、confirm_deadline

---

## 12. 评价表（review）

存储交易完成后买卖双方的互评记录。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| order_id | bigint | 订单 ID |
| reviewer_id | bigint | 评价人 ID |
| target_id | bigint | 被评价人 ID |
| score_desc | tinyint | 商品描述相符评分（1-5） |
| score_attitude | tinyint | 沟通态度评分（1-5） |
| score_experience | tinyint | 交易体验评分（1-5） |
| content | varchar(200) | 评价内容 |
| is_auto | tinyint | 是否自动评价 |

**索引：** 订单+评价人联合唯一、target_id

---

## 13. 举报表（report）

存储用户对商品或他人的举报记录。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| reporter_id | bigint | 举报人 ID |
| target_type | tinyint | 举报目标：1-商品 / 2-用户 |
| target_id | bigint | 被举报目标 ID |
| reason_type | tinyint | 举报原因：1-虚假商品 / 2-违禁物品 / 3-价格异常 / 4-骚扰信息 / 5-其他 |
| description | varchar(255) | 补充说明 |
| status | tinyint | 处理状态：0-待处理 / 1-已处理 / 2-已忽略 |
| handle_result | varchar(255) | 处理结果 |
| handler_id | bigint | 处理人 ID |
| handle_time | datetime | 处理时间 |

**索引：** 主键、reporter_id、target（target_type+target_id）、status，以及举报人+目标联合唯一

---

## 14. 消息通知表（notification）

存储发送给用户的通知消息。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| user_id | bigint | 接收用户 ID |
| type | tinyint | 消息类型：1-交易成功 / 2-新消息 / 3-审核通过 / 4-审核驳回 / 5-系统公告 / 6-被收藏 / 7-订单取消 / 8-认证通过 / 9-认证驳回 / 10-评价提醒 |
| title | varchar(64) | 消息标题 |
| content | varchar(255) | 消息内容 |
| related_id | bigint | 关联业务 ID |
| related_type | tinyint | 关联类型：1-商品 / 2-订单 / 3-认证 / 4-系统 |
| is_read | tinyint | 是否已读 |
| category | tinyint | 分类：1-交易 / 2-系统 |

**索引：** 主键、user_id+is_read+category、create_time

---

## 15. Banner 表（banner）

存储首页轮播图配置。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| title | varchar(64) | Banner 标题 |
| image | varchar(255) | Banner 图片 URL |
| link_type | tinyint | 跳转类型：1-商品详情 / 2-活动页 / 3-外部链接 |
| link_url | varchar(255) | 跳转地址 |
| campus_id | bigint | 展示校区（NULL=全校区） |
| sort | int | 排序号 |
| status | tinyint | 状态：0-下架 / 1-上架 |
| start_time | datetime | 生效开始时间 |
| end_time | datetime | 生效结束时间 |

**索引：** 主键、status、campus_id

---

## 16. 搜索热词表（search_keyword）

存储搜索热词和高频搜索记录。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| keyword | varchar(32) | 关键词（唯一） |
| search_count | int | 搜索次数 |
| is_hot | tinyint | 是否热门推荐 |
| sort | int | 排序号 |
| status | tinyint | 状态：0-禁用 / 1-启用 |

**索引：** 主键、keyword（唯一）、is_hot

---

## 17. 系统公告表（notice）

存储管理后台发布的系统公告。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| title | varchar(64) | 公告标题 |
| content | varchar(500) | 公告内容 |
| type | tinyint | 类型：1-系统公告 / 2-活动公告 |
| status | tinyint | 状态：0-下架 / 1-上架 |
| publisher_id | bigint | 发布人 ID |

**索引：** 主键、status

---

## 18. 会话表（chat_session）

记录用户之间的聊天会话（按商品维度）。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| user_id | bigint | 当前用户 ID |
| peer_id | bigint | 对话方用户 ID |
| product_id | bigint | 关联商品 ID |
| last_msg | varchar(255) | 最后一条消息摘要 |
| last_msg_type | tinyint | 最后消息类型：1-文本 / 2-商品卡片 / 3-订单卡片 / 4-系统 |
| unread | int | 未读消息数 |
| last_time | datetime | 最后消息时间 |
| is_top | tinyint | 是否置顶 |
| is_deleted | tinyint | 是否删除 |

**索引：** 用户+对话方+商品联合唯一、user_id+last_time、product_id

---

## 19. 聊天消息表（chat_message）

存储聊天会话的具体消息内容。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| session_key | varchar(64) | 会话标识 |
| sender_id | bigint | 发送者 ID |
| receiver_id | bigint | 接收者 ID |
| msg_type | tinyint | 类型：1-文本 / 2-商品卡片 / 3-订单卡片 / 4-系统提示 / 5-快捷回复 |
| content | varchar(1000) | 消息内容 |
| product_id | bigint | 关联商品 ID |
| order_id | bigint | 关联订单 ID |
| is_read | tinyint | 是否已读 |
| create_time | datetime | 发送时间 |

**索引：** session_key+create_time、receiver_id+is_read、sender_id、create_time

---

## 20. 商品留言表（product_comment）

*来源：`sql/update/2026-04-10_f08_product_comment.sql`*

记录用户在商品详情页的留言和回复（楼层式评论）。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| product_id | bigint | 商品 ID |
| user_id | bigint | 留言用户 ID |
| parent_id | bigint | 父留言 ID（回复哪条留言） |
| root_id | bigint | 根留言 ID（所属的第一层留言） |
| reply_to_user_id | bigint | 被回复用户 ID |
| content | varchar(500) | 留言内容 |
| is_read | tinyint | 是否已读（针对被回复人） |
| is_deleted | tinyint | 是否逻辑删除 |

**索引：** 主键、product_id、user_id、parent_id、root_id、reply_to_user_id+is_read

---

## 数据库表变更日志

以下是项目开发过程中的增量变更脚本，记录了每个版本的数据库改动：

| 日期 | 脚本 | 变更内容 |
|------|------|----------|
| 2026-02-21 | `f19_notification.sql` | notification 表新增 `category` 字段（消息分类：1-交易 / 2-系统） |
| 2026-02-21 | `f21_banner_search.sql` | 新增 banner 表和 search_keyword 表 |
| 2026-02-22 | `f_im_02_chat_session.sql` | chat_session 表新增 `last_msg_type`、`is_top`、`is_deleted` 字段 |
| 2026-02-23 | `f_im_03_chat_message.sql` | 新增 chat_message 表 |
| 2026-04-06 | `f06_auth_real_name.sql` | campus_auth 表新增 `real_name` 字段；新增 campus_auth_history 表 |
| 2026-04-08 | `f07_user_follow_profile.sql` | user 表新增 `bio`、`ip_region` 字段；新增 user_follow 表 |
| 2026-04-10 | `f08_product_comment.sql` | 新增 product_comment 表 |

---

## 核心业务关系概览

```
用户（user）
 ├── 发布商品（product） → 订单（trade_order） → 评价（review）
 ├── 收藏商品（favorite）
 ├── 发起会话（chat_session） → 聊天消息（chat_message）
 ├── 校园认证（campus_auth） → 认证历史（campus_auth_history）
 ├── 关注他人（user_follow）
 ├── 收到通知（notification）
 └── 举报（report）

管理员（employee）
 ├── 审核商品（product.reviewer_id）
 ├── 审核认证（campus_auth.reviewer_id）
 └── 发布公告（notice）
```
