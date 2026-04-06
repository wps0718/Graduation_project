package com.qingyuan.secondhand.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("campus_auth_history")
public class CampusAuthHistory {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long authId;
    private Long collegeId;
    private String realName;
    private String studentNo;
    private String className;
    private String certImage;
    private Integer status;
    private String rejectReason;
    private LocalDateTime reviewTime;
    private Long reviewerId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

