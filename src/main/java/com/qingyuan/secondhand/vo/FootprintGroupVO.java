package com.qingyuan.secondhand.vo;

import lombok.Data;

import java.util.List;

@Data
public class FootprintGroupVO {
    private String date;
    private List<FootprintItemVO> items;
}
