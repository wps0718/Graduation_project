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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';
