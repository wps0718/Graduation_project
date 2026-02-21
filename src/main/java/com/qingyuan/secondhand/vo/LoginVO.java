package com.qingyuan.secondhand.vo;

import lombok.Data;

@Data
public class LoginVO {

    private String token;
    private Long userId;
    private Boolean isNew;
    private Integer authStatus;
    private String nickName;
    private String avatarUrl;
    private Boolean deactivating;
}
