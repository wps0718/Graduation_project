-- =====================================================
-- 增量脚本：举报表增加被举报目标快照字段
-- 功能：保存举报时的商品/用户信息，避免目标被删除后无法展示
-- =====================================================

ALTER TABLE `report`
  ADD COLUMN `target_title` varchar(255) DEFAULT NULL COMMENT '被举报目标快照标题（商品标题/用户昵称）';

ALTER TABLE `report`
  ADD COLUMN `target_cover_image` varchar(255) DEFAULT NULL COMMENT '被举报目标快照封面（商品首图/用户头像）';
