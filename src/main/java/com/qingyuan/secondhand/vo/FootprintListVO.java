package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.util.List;

@Data
public class FootprintListVO {
    private List<FootprintGroupVO> records;
    private long total;
}
