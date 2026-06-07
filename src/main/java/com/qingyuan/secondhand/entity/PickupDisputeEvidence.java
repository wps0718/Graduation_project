package com.qingyuan.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("pickup_dispute_evidence")
public class PickupDisputeEvidence {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long disputeId;
    private Integer side;
    private Integer type;
    private String url;
    private String content;
    private String description;
    private Integer sortOrder;
    private LocalDateTime createTime;
}
