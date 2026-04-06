ALTER TABLE `campus_auth`
ADD COLUMN `real_name` varchar(32) NOT NULL DEFAULT '' COMMENT '姓名' AFTER `college_id`;

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
