package com.qingyuan.secondhand.vo;

import lombok.Data;

@Data
public class MeetingPointVO {
    private Long id;
    private Long campusId;
    private String name;
    private String description;
    private Integer sort;
    private Integer status;
}
