-- 为notification表添加category字段（MySQL 5.7兼容）
ALTER TABLE notification ADD COLUMN category tinyint(4) NOT NULL DEFAULT 1 COMMENT '消息分类 1-交易 2-系统';
