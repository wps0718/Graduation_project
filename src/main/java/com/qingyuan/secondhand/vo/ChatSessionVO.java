package com.qingyuan.secondhand.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionVO {
    @JsonProperty("id")
    private Long sessionId;
    private String sessionKey;
    @JsonProperty("userId")
    private Long peerId;
    @JsonProperty("nickName")
    private String peerName;
    @JsonProperty("avatarUrl")
    private String peerAvatar;
    @JsonProperty("authStatus")
    private Integer peerAuthStatus;
    private Long productId;
    private String productTitle;
    private String productImage;
    private BigDecimal productPrice;
    private Integer productStatus;
    @JsonProperty("lastMessage")
    private String lastMsg;
    private Integer lastMsgType;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastTime;
    private Integer unread;
    private Boolean isTop;
    private Boolean isNew;
}
