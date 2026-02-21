CREATE TABLE IF NOT EXISTS `banner` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Banner标题',
  `image` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Banner图片URL',
  `link_type` tinyint(4) DEFAULT NULL COMMENT '跳转类型 1-商品详情 2-活动页 3-外部链接',
  `link_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '跳转地址',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Banner表';

CREATE TABLE IF NOT EXISTS `search_keyword` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `keyword` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '关键词',
  `search_count` int(11) NOT NULL DEFAULT 0 COMMENT '搜索次数',
  `is_hot` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否热门推荐 0-否 1-是',
  `sort` int(11) NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_keyword` (`keyword`),
  KEY `idx_is_hot` (`is_hot`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='搜索热词表';
