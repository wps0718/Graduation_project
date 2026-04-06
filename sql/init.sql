-- =====================================================
-- 轻院二手交易平台 - 完整数据库初始化脚本
-- 适用于 MySQL 5.7
-- =====================================================
-- -----------------------------------------------------
-- 1. 用户表（user）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `open_id` varchar(64) DEFAULT NULL COMMENT '微信openid',
  `session_key` varchar(255) DEFAULT NULL COMMENT '会话密钥',
  `nick_name` varchar(32) DEFAULT NULL COMMENT '昵称',
  `username` varchar(50) DEFAULT NULL COMMENT '用户名（手机号）',
  `password` varchar(255) DEFAULT NULL COMMENT '密码（BCrypt加密）',
  `avatar_url` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `gender` tinyint(2) DEFAULT 0 COMMENT '性别 0-未知 1-男 2-女',
  `phone` varchar(11) DEFAULT NULL COMMENT '手机号',
  `campus_id` bigint(20) DEFAULT NULL COMMENT '所在校区ID',
  `auth_status` tinyint(4) DEFAULT 0 COMMENT '认证状态 0-未认证 1-审核中 2-已认证 3-已驳回',
  `score` decimal(3,1) DEFAULT 5.0 COMMENT '用户综合评分',
  `status` tinyint(4) DEFAULT 1 COMMENT '账号状态 0-封禁 1-正常 2-注销中',
  `ban_reason` varchar(255) DEFAULT NULL COMMENT '封禁原因',
  `deactivate_time` datetime DEFAULT NULL COMMENT '注销申请时间',
  `agreement_accepted` tinyint(1) DEFAULT 0 COMMENT '是否同意用户协议 0-否 1-是',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_open_id` (`open_id`),
  UNIQUE KEY `idx_phone` (`phone`),
  KEY `idx_campus_id` (`campus_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=10000 DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
-- -----------------------------------------------------
-- 2. 管理员表（employee）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `employee` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(32) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `name` varchar(32) NOT NULL COMMENT '姓名',
  `phone` varchar(11) DEFAULT NULL COMMENT '手机号',
  `role` tinyint(4) NOT NULL DEFAULT 2 COMMENT '角色 1-超级管理员 2-普通管理员',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';
-- 默认管理员账号 admin / 123456
INSERT INTO `employee` (`id`, `username`, `password`, `name`, `role`, `status`, `create_time`, `update_time`)
VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '超级管理员', 1, 1, NOW(), NOW());
-- -----------------------------------------------------
-- 3. 学院表（college）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `college` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(64) NOT NULL COMMENT '学院名称',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学院表';
-- -----------------------------------------------------
-- 4. 校区表（campus）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `campus` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) NOT NULL COMMENT '校区名称',
  `code` varchar(32) NOT NULL COMMENT '校区编码',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序号',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校区表';
INSERT INTO `campus` (`name`, `code`, `sort`, `status`, `create_time`, `update_time`) VALUES
('南海北', 'nanhai_north', 1, 1, NOW(), NOW()),
('南海南', 'nanhai_south', 2, 1, NOW(), NOW()),
('新港', 'xingang', 3, 1, NOW(), NOW());
-- -----------------------------------------------------
-- 5. 面交地点表（meeting_point）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `meeting_point` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `campus_id` bigint(20) NOT NULL COMMENT '校区ID',
  `name` varchar(64) NOT NULL COMMENT '地点名称',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序号',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_campus_id` (`campus_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='面交地点表';
INSERT INTO `meeting_point` (`campus_id`, `name`, `sort`, `status`, `create_time`, `update_time`) VALUES
(1, '南海北校区-宿舍楼A', 1, 1, NOW(), NOW()),
(1, '南海北校区-图书馆', 2, 1, NOW(), NOW()),
(1, '南海北校区-食堂', 3, 1, NOW(), NOW()),
(2, '南海南校区-宿舍楼B', 1, 1, NOW(), NOW()),
(2, '南海南校区-食堂', 2, 1, NOW(), NOW()),
(3, '新港校区-教学楼', 1, 1, NOW(), NOW()),
(3, '新港校区-图书馆', 2, 1, NOW(), NOW());
-- -----------------------------------------------------
-- 6. 商品分类表（category）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(32) NOT NULL COMMENT '分类名称',
  `icon` varchar(255) DEFAULT NULL COMMENT '分类图标URL',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';
INSERT INTO `category` (`name`, `sort`, `status`, `create_time`, `update_time`) VALUES
('书籍', 1, 1, NOW(), NOW()),
('服饰', 2, 1, NOW(), NOW()),
('生活', 3, 1, NOW(), NOW()),
('电子设备', 4, 1, NOW(), NOW()),
('运动设备', 5, 1, NOW(), NOW()),
('潮玩娱乐', 6, 1, NOW(), NOW());
-- -----------------------------------------------------
-- 7. 校园认证表（campus_auth）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `campus_auth` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `college_id` bigint(20) NOT NULL COMMENT '学院ID',
  `real_name` varchar(32) NOT NULL COMMENT '姓名',
  `student_no` varchar(32) NOT NULL COMMENT '学号',
  `class_name` varchar(64) NOT NULL COMMENT '班级',
  `cert_image` varchar(255) NOT NULL COMMENT '认证材料图片URL',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '审核状态 0-待审核 1-通过 2-驳回',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '驳回原因',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `reviewer_id` bigint(20) DEFAULT NULL COMMENT '审核人ID（管理员）',
  `create_time` datetime DEFAULT NULL COMMENT '提交时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  UNIQUE KEY `idx_student_no` (`student_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校园认证表';
-- -----------------------------------------------------
-- 8. 校园认证历史表（campus_auth_history）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `campus_auth_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `auth_id` bigint(20) NOT NULL COMMENT '认证主表ID',
  `college_id` bigint(20) NOT NULL COMMENT '学院ID',
  `real_name` varchar(32) NOT NULL COMMENT '姓名',
  `student_no` varchar(32) NOT NULL COMMENT '学号',
  `class_name` varchar(64) NOT NULL COMMENT '班级',
  `cert_image` varchar(255) NOT NULL COMMENT '认证材料图片URL',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '审核状态 0-待审核 1-通过 2-驳回',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '驳回原因',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `reviewer_id` bigint(20) DEFAULT NULL COMMENT '审核人ID（管理员）',
  `create_time` datetime DEFAULT NULL COMMENT '提交时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_auth_id` (`auth_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校园认证历史表';
-- -----------------------------------------------------
-- 9. 商品表（product）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) NOT NULL COMMENT '发布者用户ID',
  `title` varchar(50) NOT NULL COMMENT '商品标题',
  `description` varchar(500) NOT NULL COMMENT '商品描述',
  `price` decimal(10,2) NOT NULL COMMENT '二手价格',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '原价',
  `category_id` bigint(20) NOT NULL COMMENT '分类ID',
  `condition_level` tinyint(4) NOT NULL COMMENT '成色 1-全新 2-几乎全新 3-9成新 4-8成新 5-7成新及以下',
  `campus_id` bigint(20) NOT NULL COMMENT '交易校区ID',
  `meeting_point_id` bigint(20) DEFAULT NULL COMMENT '面交地点ID（预设）',
  `meeting_point_text` varchar(100) DEFAULT NULL COMMENT '面交地点（自定义文字）',
  `images` varchar(2000) NOT NULL COMMENT '商品图片URL，JSON数组格式',
  `view_count` int(11) NOT NULL DEFAULT 0 COMMENT '浏览量',
  `favorite_count` int(11) NOT NULL DEFAULT 0 COMMENT '收藏量',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '商品状态 0-待审核 1-在售 2-已下架 3-已售出 4-审核驳回',
  `reject_reason` varchar(255) DEFAULT NULL COMMENT '驳回原因',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `reviewer_id` bigint(20) DEFAULT NULL COMMENT '审核人ID',
  `auto_off_time` datetime DEFAULT NULL COMMENT '自动下架时间（发布后90天）',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_campus_id` (`campus_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_auto_off_time` (`auto_off_time`),
  KEY `idx_status_campus_category_create` (`status`, `campus_id`, `category_id`, `create_time`),
  KEY `idx_status_price` (`status`, `price`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';
-- -----------------------------------------------------
-- 10. 收藏表（favorite）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `favorite` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `product_id` bigint(20) NOT NULL COMMENT '商品ID',
  `create_time` datetime DEFAULT NULL COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_product` (`user_id`, `product_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';
-- -----------------------------------------------------
-- 11. 订单表（trade_order）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `trade_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no` varchar(32) NOT NULL COMMENT '订单号',
  `product_id` bigint(20) NOT NULL COMMENT '商品ID',
  `buyer_id` bigint(20) NOT NULL COMMENT '买家用户ID',
  `seller_id` bigint(20) NOT NULL COMMENT '卖家用户ID',
  `price` decimal(10,2) NOT NULL COMMENT '成交价格',
  `campus_id` bigint(20) DEFAULT NULL COMMENT '面交校区ID',
  `meeting_point` varchar(100) DEFAULT NULL COMMENT '面交地点',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '订单状态 1-待面交 2-预留 3-已完成 4-已评价 5-已取消',
  `cancel_reason` varchar(255) DEFAULT NULL COMMENT '取消原因',
  `cancel_by` bigint(20) DEFAULT NULL COMMENT '取消操作人ID（0为系统自动取消）',
  `expire_time` datetime DEFAULT NULL COMMENT '订单过期时间（创建后72小时）',
  `confirm_deadline` datetime DEFAULT NULL COMMENT '自动确认收货截止时间（创建后7天）',
  `complete_time` datetime DEFAULT NULL COMMENT '交易完成时间',
  `is_deleted_buyer` tinyint(1) NOT NULL DEFAULT 0 COMMENT '买家是否删除 0-否 1-是',
  `is_deleted_seller` tinyint(1) NOT NULL DEFAULT 0 COMMENT '卖家是否删除 0-否 1-是',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_order_no` (`order_no`),
  KEY `idx_buyer_id` (`buyer_id`),
  KEY `idx_seller_id` (`seller_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_status` (`status`),
  KEY `idx_expire_time` (`expire_time`),
  KEY `idx_confirm_deadline` (`confirm_deadline`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易订单表';
-- -----------------------------------------------------
-- 12. 评价表（review）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `review` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` bigint(20) NOT NULL COMMENT '订单ID',
  `reviewer_id` bigint(20) NOT NULL COMMENT '评价人ID',
  `target_id` bigint(20) NOT NULL COMMENT '被评价人ID',
  `score_desc` tinyint(4) NOT NULL DEFAULT 5 COMMENT '商品描述相符 1-5分',
  `score_attitude` tinyint(4) NOT NULL DEFAULT 5 COMMENT '沟通态度 1-5分',
  `score_experience` tinyint(4) NOT NULL DEFAULT 5 COMMENT '交易体验 1-5分',
  `content` varchar(200) DEFAULT NULL COMMENT '评价内容',
  `is_auto` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否自动评价 0-否 1-是',
  `create_time` datetime DEFAULT NULL COMMENT '评价时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_order_reviewer` (`order_id`, `reviewer_id`),
  KEY `idx_target_id` (`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评价表';
-- -----------------------------------------------------
-- 12. 举报表（report）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `report` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `reporter_id` bigint(20) NOT NULL COMMENT '举报人ID',
  `target_type` tinyint(4) NOT NULL COMMENT '举报目标类型 1-商品 2-用户',
  `target_id` bigint(20) NOT NULL COMMENT '被举报目标ID',
  `reason_type` tinyint(4) NOT NULL COMMENT '举报原因 1-虚假商品 2-违禁物品 3-价格异常 4-骚扰信息 5-其他',
  `description` varchar(255) DEFAULT NULL COMMENT '补充说明',
  `status` tinyint(4) NOT NULL DEFAULT 0 COMMENT '处理状态 0-待处理 1-已处理 2-已忽略',
  `handle_result` varchar(255) DEFAULT NULL COMMENT '处理结果',
  `handler_id` bigint(20) DEFAULT NULL COMMENT '处理人ID',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `create_time` datetime DEFAULT NULL COMMENT '举报时间',
  PRIMARY KEY (`id`),
  KEY `idx_reporter_id` (`reporter_id`),
  KEY `idx_target` (`target_type`, `target_id`),
  KEY `idx_status` (`status`),
  UNIQUE KEY `idx_reporter_target` (`reporter_id`, `target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报表';
-- -----------------------------------------------------
-- 13. 消息通知表（notification）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `notification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) NOT NULL COMMENT '接收用户ID',
  `type` tinyint(4) NOT NULL COMMENT '消息类型 1-交易成功 2-新消息 3-审核通过 4-审核驳回 5-系统公告 6-被收藏 7-订单取消 8-认证通过 9-认证驳回 10-评价提醒',
  `title` varchar(64) NOT NULL COMMENT '消息标题',
  `content` varchar(255) NOT NULL COMMENT '消息内容',
  `related_id` bigint(20) DEFAULT NULL COMMENT '关联业务ID（商品ID/订单ID等）',
  `related_type` tinyint(4) DEFAULT NULL COMMENT '关联业务类型 1-商品 2-订单 3-认证 4-系统',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读 0-未读 1-已读',
  `category` tinyint(4) NOT NULL DEFAULT 1 COMMENT '消息分类 1-交易 2-系统',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_read_category` (`user_id`, `is_read`, `category`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';
-- -----------------------------------------------------
-- 14. Banner表（banner）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `banner` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(64) NOT NULL COMMENT 'Banner标题',
  `image` varchar(255) NOT NULL COMMENT 'Banner图片URL',
  `link_type` tinyint(4) DEFAULT NULL COMMENT '跳转类型 1-商品详情 2-活动页 3-外部链接',
  `link_url` varchar(255) DEFAULT NULL COMMENT '跳转地址',
  `campus_id` bigint(20) DEFAULT NULL COMMENT '展示校区ID（NULL表示全校区展示）',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 0-下架 1-上架',
  `start_time` datetime DEFAULT NULL COMMENT '生效开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '生效结束时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_campus_id` (`campus_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Banner表';
-- -----------------------------------------------------
-- 15. 搜索热词表（search_keyword）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `search_keyword` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `keyword` varchar(32) NOT NULL COMMENT '关键词',
  `search_count` int(11) NOT NULL DEFAULT 0 COMMENT '搜索次数',
  `is_hot` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否热门推荐 0-否 1-是',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_keyword` (`keyword`),
  KEY `idx_is_hot` (`is_hot`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='搜索热词表';
-- -----------------------------------------------------
-- 16. 系统公告表（notice）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `notice` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(64) NOT NULL COMMENT '公告标题',
  `content` varchar(500) NOT NULL COMMENT '公告内容',
  `type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '公告类型 1-系统公告 2-活动公告',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 0-下架 1-上架',
  `publisher_id` bigint(20) DEFAULT NULL COMMENT '发布人ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统公告表';
-- -----------------------------------------------------
-- 17. 会话-商品关联表（chat_session）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `chat_session` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) NOT NULL COMMENT '当前用户ID',
  `peer_id` bigint(20) NOT NULL COMMENT '对话方用户ID',
  `product_id` bigint(20) DEFAULT NULL COMMENT '关联商品ID',
  `last_msg` varchar(255) DEFAULT NULL COMMENT '最后一条消息摘要',
  `last_msg_type` tinyint(4) DEFAULT 1 COMMENT '最后消息类型 1-文本 2-商品卡片 3-订单卡片 4-系统',
  `unread` int(11) NOT NULL DEFAULT 0 COMMENT '未读数',
  `last_time` datetime DEFAULT NULL COMMENT '最后消息时间',
  `is_top` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否置顶 0-否 1-是',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_peer_product` (`user_id`, `peer_id`, `product_id`),
  KEY `idx_user_last_time` (`user_id`, `last_time`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话-商品关联表';
-- -----------------------------------------------------
-- 18. 聊天消息表（chat_message）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `chat_message` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `session_key` varchar(64) NOT NULL COMMENT '会话标识',
  `sender_id` bigint(20) NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint(20) NOT NULL COMMENT '接收者ID',
  `msg_type` tinyint(4) NOT NULL DEFAULT 1 COMMENT '消息类型 1-文本 2-商品卡片 3-订单卡片 4-系统提示 5-快捷回复',
  `content` varchar(1000) NOT NULL COMMENT '消息内容',
  `product_id` bigint(20) DEFAULT NULL COMMENT '关联商品ID',
  `order_id` bigint(20) DEFAULT NULL COMMENT '关联订单ID',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读',
  `create_time` datetime NOT NULL COMMENT '发送时间',
  PRIMARY KEY (`id`),
  KEY `idx_session_key` (`session_key`, `create_time`),
  KEY `idx_receiver_read` (`receiver_id`, `is_read`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';
-- =====================================================
-- 执行完成！
-- =====================================================
