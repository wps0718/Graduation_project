package com.qingyuan.secondhand.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class FootprintDeleteDTO {
    @NotEmpty(message = "请选择要删除的足迹")
    private List<Long> ids;
}
