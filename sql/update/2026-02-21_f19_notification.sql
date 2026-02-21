ALTER TABLE notification
    ADD COLUMN IF NOT EXISTS category tinyint(4) NOT NULL DEFAULT 1 COMMENT '消息分类 1-交易 2-系统';
