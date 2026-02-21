package com.qingyuan.secondhand.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("review")
public class Review {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long reviewerId;
    private Long targetId;
    private Integer scoreDesc;
    private Integer scoreAttitude;
    private Integer scoreExperience;
    private String content;
    private Integer isAuto;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
