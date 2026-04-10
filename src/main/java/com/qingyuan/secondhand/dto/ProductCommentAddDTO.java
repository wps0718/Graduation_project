package com.qingyuan.secondhand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductCommentAddDTO {
    @NotNull(message = "productId不能为空")
    private Long productId;

    private Long parentId;

    @NotBlank(message = "content不能为空")
    @Size(max = 300, message = "content长度不能超过300")
    private String content;
}
