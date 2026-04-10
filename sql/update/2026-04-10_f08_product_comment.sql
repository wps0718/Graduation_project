-- -----------------------------------------------------
-- 19. 商品留言表（product_comment）
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `product_comment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `product_id` bigint(20) NOT NULL COMMENT '商品ID',
  `user_id` bigint(20) NOT NULL COMMENT '留言用户ID',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '父留言ID（回复哪条留言）',
  `root_id` bigint(20) DEFAULT NULL COMMENT '根留言ID（所属的第一层留言）',
  `reply_to_user_id` bigint(20) DEFAULT NULL COMMENT '被回复用户ID',
  `content` varchar(500) NOT NULL COMMENT '留言内容',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读（针对被回复人）',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否逻辑删除 0-否 1-是',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_root_id` (`root_id`),
  KEY `idx_reply_to_user_id_read` (`reply_to_user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品留言表';
