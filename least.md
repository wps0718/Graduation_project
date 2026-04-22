# 轻院二手交易平台 - 数据库设计总结

## 一、概述

该数据库用于轻院二手交易平台（高校二手物品交易小程序），基于 MySQL 5.7，包含用户体系、商品交易、即时通讯、校园认证等核心功能。

## 二、核心表结构

### 1. 用户体系

| 表名 | 说明 |
|------|------|
| user | 用户表：基本信息、微信openid、认证状态、评分等 |
| user_follow | 用户关注表：关注者与被关注者关系 |
| employee | 管理员表：后台管理账号 |

### 2. 校园组织

| 表名 | 说明 |
|------|------|
| college | 学院表 |
| campus | 校区表：南海北、南海南、新港三个校区 |
| meeting_point | 面交地点表：各校区的默认面交地点 |
| campus_auth | 校园认证表：学生认证信息 |
| campus_auth_history | 校园认证历史表 |

### 3. 商品模块

| 表名 | 说明 |
|------|------|
| category | 商品分类：书籍、服饰、生活、电子设备、运动设备、潮玩娱乐 |
| product | 商品表：标题、描述、价格、成色、图片、状态等 |
| favorite | 收藏表 |
| product_comment | 商品留言表：支持回复嵌套 |

### 4. 交易订单

| 表名 | 说明 |
|------|------|
| trade_order | 订单表：订单号、买家、卖家、成交价、面交地点、状态 |
| review | 评价表：描述相符、沟通态度、交易体验评分 |

### 5. 消息通知

| 表名 | 说明 |
|------|------|
| notification | 消息通知表：交易成功、审核通过、系统公告等 |
| chat_session | 会话表：买家与卖家的会话关联 |
| chat_message | 聊天消息表：支持文本、商品卡片、订单卡片 |

### 6. 内容管理

| 表名 | 说明 |
|------|------|
| banner | Banner表：首页轮播图 |
| search_keyword | 搜索热词表 |
| notice | 系统公告表 |
| report | 举报表 |

## 三、表关系说明

```
user --(发布)--> product
user --(下单)--> trade_order --(属于)--> product
user --(关注)--> user_follow
user --(认证)--> campus_auth --(属于)--> campus
product --(留言)--> product_comment
chat_session --(关联)--> product
chat_message --(归属)--> chat_session
```

## 四、状态字段说明

### 用户状态
- **用户状态(user.status)**：0-封禁 1-正常 2-注销中
- **认证状态(user.auth_status)**：0-未认证 1-审核中 2-已认证 3-已驳回
- **性别(user.gender)**：0-未知 1-男 2-女

### 商品状态
- **商品状态(product.status)**：0-待审核 1-在售 2-已下架 3-已售出 4-审核驳回
- **成色(product.condition_level)**：1-全新 2-几乎全新 3-9成新 4-8成新 5-7成新及以下

### 订单状态
- **订单状态(trade_order.status)**：1-待面交 2-预留 3-已完成 4-已评价 5-已取消

### 认证状态
- **认证状态(campus_auth.status)**：0-待审核 1-通过 2-驳回

### 举报状态
- **举报处理状态(report.status)**：0-待处理 1-已处理 2-已忽略
- **举报目标类型(report.target_type)**：1-商品 2-用户
- **举报原因(report.reason_type)**：1-虚假商品 2-违禁物品 3-价格异常 4-骚扰信息 5-其他

### 会话与消息
- **最后消息类型(chat_session.last_msg_type)**：1-文本 2-商品卡片 3-订单卡片 4-系统
- **消息类型(chat_message.msg_type)**：1-文本 2-商品卡片 3-订单卡片 4-系统提示 5-快捷回复
- **消息通知类型(notification.type)**：1-交易成功 2-新消息 3-审核通过 4-审核驳回 5-系统公告 6-被收藏 7-订单取消 8-认证通过 9-认证驳回 10-评价提醒

### 管理员
- **管理员角色(employee.role)**：1-超级管理员 2-普通管理员
- **管理员状态(employee.status)**：0-禁用 1-启用

## 五、更新日志

| 日期 | 脚本 | 说明 |
|------|------|------|
| 2026-02-21 | f19_notification | 为notification表添加category字段 |
| 2026-02-21 | f21_banner_search | 创建banner和search_keyword表 |
| 2026-02-22 | f_im_02_chat_session | 扩展chat_session表：添加last_msg_type、is_top、is_deleted |
| 2026-02-23 | f_im_03_chat_message | 创建chat_message表 |
| 2026-04-06 | f06_auth_real_name | 为campus_auth添加real_name字段，创建campus_auth_history |
| 2026-04-08 | f07_user_follow_profile | 添加bio、ip_region字段，创建user_follow表 |
| 2026-04-10 | f08_product_comment | 创建product_comment表 |

## 六、索引优化

- product表复合索引：(status, campus_id, category_id, create_time)
- product表复合索引：(status, price)
- chat_message表索引：(session_key, create_time)
- chat_message表索引：(receiver_id, is_read)

## 七、初始数据

### 校区初始化数据
| name | code | sort |
|------|------|------|
| 南海北 | nanhai_north | 1 |
| 南海南 | nanhai_south | 2 |
| 新港 | xingang | 3 |

### 商品分类初始化数据
| name | sort |
|------|------|
| 书籍 | 1 |
| 服饰 | 2 |
| 生活 | 3 |
| 电子设备 | 4 |
| 运动设备 | 5 |
| 潮玩娱乐 | 6 |

### 重要用户字段说明
- **ban_reason**：封禁原因
- **deactivate_time**：注销申请时间
- **agreement_accepted**：是否同意用户协议（0-否 1-是）
- **last_login_time**：最后登录时间
- **score**：用户综合评分（默认5.0）
- **auto_off_time**：商品自动下架时间（发布后90天）

### 默认管理员
- 用户名：admin，密码：123456（BCrypt加密）

### 面交地点初始化数据
- 南海北校区：宿舍楼A、图书馆、食堂
- 南海南校区：宿舍楼B、食堂
- 新港校区：教学楼、图书馆
