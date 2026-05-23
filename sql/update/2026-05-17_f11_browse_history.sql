-- -----------------------------------------------------
-- 浏览历史表（browse_history）
-- 记录用户浏览过的商品，同一用户浏览同一商品只保留一条记录
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `browse_history` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) NOT NULL COMMENT '浏览用户ID',
  `product_id` bigint(20) NOT NULL COMMENT '浏览的商品ID',
  `is_deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
  `create_time` datetime DEFAULT NULL COMMENT '浏览时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='浏览历史表';
