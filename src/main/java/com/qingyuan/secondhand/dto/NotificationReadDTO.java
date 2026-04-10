package com.qingyuan.secondhand.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationReadDTO {
    @NotNull
    private Long id;
}
