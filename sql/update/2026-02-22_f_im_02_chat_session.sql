ALTER TABLE `chat_session`
ADD COLUMN `last_msg_type` tinyint(4) DEFAULT 1 COMMENT '最后消息类型 1-文本 2-商品卡片 3-订单卡片 4-系统' AFTER `last_msg`,
ADD COLUMN `is_top` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否置顶 0-否 1-是' AFTER `last_time`,
ADD COLUMN `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是' AFTER `is_top`;
