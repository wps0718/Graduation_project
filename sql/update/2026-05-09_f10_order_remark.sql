ALTER TABLE `trade_order` ADD COLUMN `remark` varchar(255) DEFAULT NULL COMMENT '买家备注（面交时间、协商说明等）' AFTER `meeting_point`;
