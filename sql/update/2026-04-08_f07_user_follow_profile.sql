-- =====================================================
-- 增量脚本：用户关注体系 + 用户个人信息扩展
-- 功能：关注/粉丝、个人简介、IP属地、卖家主页展示扩展
-- =====================================================

ALTER TABLE `user`
  ADD COLUMN `bio` varchar(200) DEFAULT NULL COMMENT '个人简介';

ALTER TABLE `user`
  ADD COLUMN `ip_region` varchar(64) DEFAULT NULL COMMENT 'IP属地';

CREATE TABLE IF NOT EXISTS `user_follow` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `follower_id` bigint(20) NOT NULL COMMENT '关注者用户ID',
  `followee_id` bigint(20) NOT NULL COMMENT '被关注用户ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_follower_followee` (`follower_id`, `followee_id`),
  KEY `idx_followee_id` (`followee_id`),
  KEY `idx_follower_id` (`follower_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注表';

