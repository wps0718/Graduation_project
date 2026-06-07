-- =====================================================
-- 轻院二手交易平台 - 增量更新脚本
-- 日期: 2026-05-24
-- 说明: 将已有数据库更新到最新版本（补充缺失的表和基础数据）
--       如果是全新部署，直接执行 init.sql 即可，不需要执行此文件
-- =====================================================

-- 1. 新增商品留言表（如果不存在）
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

-- 2. 新增浏览历史表（如果不存在）
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

-- 3. 补充学院基础数据（如果表为空）
INSERT IGNORE INTO `college` (`name`, `sort`, `status`, `create_time`, `update_time`) VALUES
('信息技术学院', 1, 1, NOW(), NOW()),
('轻化工技术学院', 2, 1, NOW(), NOW()),
('机电技术学院', 3, 1, NOW(), NOW()),
('汽车技术学院', 4, 1, NOW(), NOW()),
('艺术设计学院', 5, 1, NOW(), NOW()),
('经济管理学院', 6, 1, NOW(), NOW()),
('食品与生物技术学院', 7, 1, NOW(), NOW()),
('财贸学院', 8, 1, NOW(), NOW()),
('旅游管理学院', 9, 1, NOW(), NOW()),
('应用外语学院', 10, 1, NOW(), NOW());

-- 3. 新增登录日志表（如果不存在）
CREATE TABLE IF NOT EXISTS `login_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID（小程序端，管理员登录时为空）',
  `employee_id` bigint(20) DEFAULT NULL COMMENT '管理员ID（PC端，用户登录时为空）',
  `login_method` varchar(16) NOT NULL COMMENT '登录方式：wechat-微信登录 account-手机号密码 sms-短信验证码 pc-PC端管理后台',
  `login_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_employee_id` (`employee_id`),
  KEY `idx_login_method` (`login_method`),
  KEY `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- =====================================================
-- 执行完成！
-- =====================================================
