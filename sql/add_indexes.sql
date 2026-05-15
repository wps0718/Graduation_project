-- =====================================================
-- 索引补充脚本（增量更新，已有的索引会自动跳过）
-- =====================================================

-- 用户表：补充 auth_status 索引（管理端审核按认证状态筛选）
ALTER TABLE `user` ADD INDEX IF NOT EXISTS `idx_auth_status` (`auth_status`);

-- 订单表：补充 create_time 索引（订单列表排序字段）
ALTER TABLE `trade_order` ADD INDEX IF NOT EXISTS `idx_create_time` (`create_time`);

-- Banner表：补充 (status, campus_id) 复合索引（按校区查询上架 banner）
ALTER TABLE `banner` ADD INDEX IF NOT EXISTS `idx_status_campus` (`status`, `campus_id`);

-- 公告表：补充 type 索引（按公告类型筛选）
ALTER TABLE `notice` ADD INDEX IF NOT EXISTS `idx_type` (`type`);
