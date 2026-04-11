package com.qingyuan.secondhand.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class NotificationReadBatchDTO {
    @NotEmpty(message = "ID列表不能为空")
    private List<Long> ids;
}
