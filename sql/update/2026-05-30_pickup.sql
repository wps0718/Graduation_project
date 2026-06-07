-- 代拿快递功能建表
-- 日期：2026-05-30

-- 1. 代拿订单表
CREATE TABLE IF NOT EXISTS pickup_order (
    id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no              VARCHAR(32)   NOT NULL COMMENT '订单号，格式: PD + yyyyMMddHHmmss + 4位随机数',
    requester_id          BIGINT        NOT NULL COMMENT '需求者用户ID',
    picker_id             BIGINT        DEFAULT NULL COMMENT '代拿者用户ID',
    campus_id             BIGINT        DEFAULT NULL COMMENT '需求者校区ID（冗余，用于需求池筛选）',

    pickup_code           VARCHAR(255)  DEFAULT NULL COMMENT '取件码（AES加密存储）',
    pickup_location       VARCHAR(128)  NOT NULL COMMENT '取件地点（驿站名称）',
    pickup_detail         VARCHAR(256)  DEFAULT NULL COMMENT '取件详细说明',
    delivery_location     VARCHAR(128)  NOT NULL COMMENT '送达地点',

    proposed_price        DECIMAL(10,2) NOT NULL COMMENT '需求者报价（当前金额）',
    agreed_price          DECIMAL(10,2) DEFAULT NULL COMMENT '双方确认后的最终报酬',

    price_proposer_id     BIGINT        DEFAULT NULL COMMENT '最后一次发起价格修改的用户ID',
    picker_price_confirmed TINYINT      DEFAULT 0  COMMENT '代拿者价格确认：0未确认/1已确认',
    price_deadline        DATETIME      DEFAULT NULL COMMENT '价格确认截止时间',

    expected_delivery_time DATETIME     DEFAULT NULL COMMENT '期望送达时间',
    delivery_deadline     DATETIME      DEFAULT NULL COMMENT '送达截止时间（期望时间+1h）',
    evidence_images       TEXT          DEFAULT NULL COMMENT '代拿证据图片URL（JSON数组）',

    requester_confirmed   TINYINT       DEFAULT 0 COMMENT '需求者确认完成：0未确认/1已确认',
    picker_confirmed      TINYINT       DEFAULT 0 COMMENT '代拿者确认送达：0未确认/1已确认',

    status                TINYINT       NOT NULL DEFAULT 0 COMMENT '订单状态：0待接单/1已接单/2价格已确认/3代拿中/4已代拿/5已完成/6已评价/7已取消/8纠纷中',
    cancel_reason         VARCHAR(256)  DEFAULT NULL COMMENT '取消原因',
    cancel_by             BIGINT        DEFAULT NULL COMMENT '取消方用户ID',

    confirm_deadline      DATETIME      DEFAULT NULL COMMENT '需求者确认截止时间（完成后3天）',
    complete_time         DATETIME      DEFAULT NULL COMMENT '完成时间',
    create_time           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    payment_status        TINYINT       DEFAULT 0 COMMENT '支付状态：0未支付/1已冻结/2已释放/3已结算',
    payment_order_id      VARCHAR(64)   DEFAULT NULL COMMENT '支付系统订单号',

    is_deleted            TINYINT       DEFAULT 0 COMMENT '逻辑删除',

    KEY idx_requester_id (requester_id),
    KEY idx_picker_id (picker_id),
    KEY idx_status (status),
    KEY idx_price_deadline (price_deadline),
    KEY idx_delivery_deadline (delivery_deadline),
    KEY idx_campus_id (campus_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代拿快递订单表';

-- 2. 纠纷表
CREATE TABLE IF NOT EXISTS pickup_dispute (
    id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id              BIGINT        NOT NULL COMMENT '关联代拿订单ID',
    order_no              VARCHAR(32)   NOT NULL COMMENT '订单号冗余',

    initiator_id          BIGINT        NOT NULL COMMENT '申诉发起人ID',
    initiator_role        TINYINT       NOT NULL COMMENT '发起人角色：1需求者/2代拿者',
    dispute_type          TINYINT       NOT NULL COMMENT '1未送达/2物品损坏/3超时未完成/4价格争议/5其他',
    description           VARCHAR(500)  NOT NULL COMMENT '申诉描述',
    submit_time           DATETIME      NOT NULL COMMENT '申诉提交时间',

    responder_id          BIGINT        DEFAULT NULL COMMENT '响应人ID',
    response_description  VARCHAR(500)  DEFAULT NULL COMMENT '回应描述',
    response_time         DATETIME      DEFAULT NULL COMMENT '回应提交时间',
    response_deadline     DATETIME      NOT NULL COMMENT '响应截止时间（提交后7天）',
    last_reminder_time    DATETIME      DEFAULT NULL COMMENT '最后提醒时间',
    reminder_count        TINYINT       DEFAULT 0 COMMENT '已提醒次数',

    status                TINYINT       NOT NULL DEFAULT 0 COMMENT '0待响应/1已响应待裁决/2自动胜诉/3已裁决/4已撤销',
    admin_id              BIGINT        DEFAULT NULL COMMENT '裁决管理员ID',
    judgment_result       TINYINT       DEFAULT NULL COMMENT '裁决结果：1支持申诉方/2支持被申诉方/3双方协商',
    judgment_detail       VARCHAR(500)  DEFAULT NULL COMMENT '裁决说明',
    penalty_user_id       BIGINT        DEFAULT NULL COMMENT '被处罚用户ID',
    penalty_score         DECIMAL(3,1)  DEFAULT NULL COMMENT '扣除信用分',
    resolve_time          DATETIME      DEFAULT NULL COMMENT '裁决完成时间',

    create_time           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    KEY idx_order_id (order_id),
    KEY idx_status_response_deadline (status, response_deadline)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代拿快递纠纷表';

-- 3. 纠纷证据表
CREATE TABLE IF NOT EXISTS pickup_dispute_evidence (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    dispute_id  BIGINT        NOT NULL COMMENT '关联纠纷ID',
    side        TINYINT       NOT NULL COMMENT '1申诉方/2被申诉方',
    type        TINYINT       NOT NULL COMMENT '1图片/2文字',
    url         VARCHAR(255)  DEFAULT NULL COMMENT '图片URL（type=1时必填）',
    content     VARCHAR(500)  DEFAULT NULL COMMENT '文字内容（type=2时必填）',
    description VARCHAR(255)  DEFAULT NULL COMMENT '图片说明（type=1时可选）',
    sort_order  INT           DEFAULT 0  COMMENT '排序序号',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,

    KEY idx_dispute_id_side (dispute_id, side)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='纠纷证据表';

-- 4. 修改user表：新增代拿信用分
ALTER TABLE user ADD COLUMN pickup_score DECIMAL(3,1) DEFAULT 5.0 COMMENT '代拿信用分';

-- 5. 修改notification表注释
ALTER TABLE notification MODIFY COLUMN type TINYINT NOT NULL COMMENT '1-交易成功 2-新消息 ... 25-纠纷裁决完成';
ALTER TABLE notification MODIFY COLUMN category TINYINT NOT NULL DEFAULT 1 COMMENT '1-交易 2-系统 3-代拿订单';
