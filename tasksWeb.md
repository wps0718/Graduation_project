# WebSocket IM 功能开发任务清单（续）

## [监督者] F-IM-03 消息存储与查询 - 审查结果

**审查时间**：2026-02-23 00:30

### 审查详情

#### 1. Entity 类审查 - ChatMessage.java ✅
- ✅ 使用 @TableName("chat_message") 注解
- ✅ 主键使用 @TableId(type = IdType.AUTO)
- ✅ createTime 使用 @TableField(fill = FieldFill.INSERT)
- ✅ 没有 updateTime 字段（消息不修改，符合业务逻辑）
- ✅ 没有使用 @TableLogic（chat_message 不需要逻辑删除）
- ✅ 字段与数据库表一致

#### 2. Mapper 审查 - ChatMessageMapper ✅
- ✅ 继承 BaseMapper<ChatMessage>
- ✅ 自定义方法使用 @Param 注解
- ✅ XML 中所有参数使用 #{} 而非 ${}
- ✅ selectMessagesBySessionKey 按 create_time DESC 排序
- ✅ markAsRead 只更新 is_read=0 的消息（避免重复更新）
- ✅ SQL 语句正确，无安全隐患

#### 3. 协议类审查 ✅
- ✅ ChatPayload 包含 receiverId, productId, msgType, content 字段
- ✅ ReadPayload 包含 sessionKey 字段
- ✅ 字段命名规范

#### 4. VO 审查 - ChatMessageVO ✅
- ✅ 字段对齐前端需求（msgId→id, senderId→from, msgType→type, createTime→time）
- ✅ 包含 isSelf 字段（senderId == currentUserId）
- ✅ 使用 @JsonFormat 格式化时间
- ✅ 字段完整

#### 5. Service 实现审查 - ChatMessageServiceImpl ✅

**saveAndPushMessage 方法逐步审查**：
- ✅ a. 参数校验完整（receiverId/content/msgType/不能给自己发）
- ✅ b. sessionKey 使用 SessionKeyUtil.buildSessionKey
- ✅ c. 调用 ensureSessionExists
- ✅ d. ChatMessage 入库设置所有字段
- ✅ e. lastMsg 截取 ≤50 字符
- ✅ f. 卡片消息 lastMsg 为 "[商品卡片]"/"[订单信息]"
- ✅ g. 发送方 chat_session 只更新 last_msg/last_time，不改 unread
- ✅ h. 接收方 chat_session 更新 last_msg/last_time 且 unread+1
- ✅ i. Redis INCR im:unread:{receiverId}
- ✅ j. 推送消息体包含 senderName/senderAvatar
- ✅ k. WS 推送在事务提交后执行（使用 @Transactional 注解）
- ✅ l. 离线时写 notification 表（type=2）

**getMessageHistory 方法审查**：
- ✅ a. 权限校验 isParticipant
- ✅ b. isSelf 正确填充（senderId.equals(currentUserId)）
- ✅ c. 分页参数计算正确（offset = (page-1) * pageSize）
- ✅ d. 返回 {total, records} 格式

**markSessionReadByUserId 方法审查**：
- ✅ a. 存在此重载方法（供 WS ReadMessageHandler 用）
- ✅ b. 与 markSessionRead 逻辑一致，userId 由参数传入
- ✅ c. 批量更新 is_read
- ✅ d. chat_session.unread 置 0
- ✅ e. Redis 重新计算（不是简单 DEL）

**sendSystemMessage 方法审查**：
- ✅ a. 正确入库和推送
- ✅ b. 参数校验完整

#### 6. WebSocket Handler 审查

**ChatMessageHandler.java 审查**：
- ❌ **严重问题**：异常未全部捕获！
- ❌ 只有参数校验的 BusinessException，但 saveAndPushMessage 可能抛出其他异常
- ❌ 如果 saveAndPushMessage 抛出未捕获的异常，会导致 WebSocket 连接断开
- ❌ 缺少 try-catch 包裹整个 handle 方法
- ❌ 缺少错误消息回送给发送者的逻辑

**ReadMessageHandler.java 审查**：
- ❌ **严重问题**：异常未全部捕获！
- ❌ 只有参数校验的 BusinessException，但 markSessionReadByUserId 可能抛出其他异常
- ❌ 缺少 try-catch 包裹整个 handle 方法

**MessageDispatcher.java 审查**：
- ✅ CHAT/READ 路由已替换 TODO
- ✅ 注入了 ChatMessageHandler 和 ReadMessageHandler
- ✅ 传递 message.getData() 给 Handler

#### 7. Controller 审查 - MiniChatController ✅
- ✅ GET /mini/chat/messages 参数正确（sessionKey, page, pageSize）
- ✅ POST /mini/chat/read 参数正确（sessionKey）
- ✅ 返回 Result.success()

#### 8. 工具类审查 - SessionKeyUtil ✅
- ✅ 补充了 getPeerId 方法
- ✅ 补充了 getProductId 方法
- ✅ 方法实现正确

#### 9. 测试审查 - ChatMessageServiceImplTest ✅
- ✅ 测试覆盖 saveAndPushMessage 正常发送场景
- ✅ 测试覆盖 saveAndPushMessage 接收方离线场景
- ✅ 测试覆盖 saveAndPushMessage 参数校验场景（receiverId为空、content为空、不能给自己发消息）
- ✅ 测试覆盖 getMessageHistory 正常分页场景
- ✅ 测试覆盖 getMessageHistory 越权访问场景
- ✅ 测试覆盖 markSessionReadByUserId 正常标记场景
- ✅ 测试覆盖 markSessionReadByUserId 越权操作场景
- ✅ 所有断言有实际意义（assertEquals, assertTrue, assertFalse, assertThrows）
- ✅ Mock 配置正确
- ✅ 测试通过：Tests run: 9, Failures: 0, Errors: 0, Skipped: 0

#### 10. 证据包审查 ✅
- ✅ run-folder/F-IM-03-消息存储与查询/ 目录存在
- ✅ test_output.log 显示 BUILD SUCCESS
- ✅ Tests run: 9, Failures: 0, Errors: 0, Skipped: 0

#### 11. 独立复跑验证 ✅
- ✅ 在 Kiro 终端独立执行 mvn test -Dtest=ChatMessageServiceImplTest
- ✅ 测试结果：Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
- ✅ BUILD SUCCESS
- ✅ 与执行者的测试结果一致

### 红线问题（必须修复）

#### 🔴 红线 1：ChatMessageHandler 异常未全部捕获
**文件**：src/main/java/com/qingyuan/secondhand/websocket/handler/ChatMessageHandler.java

**问题**：
- 只有参数校验的 BusinessException，但 saveAndPushMessage 可能抛出其他异常
- 如果抛出未捕获的异常，会导致 WebSocket 连接断开
- 缺少 try-catch 包裹整个 handle 方法
- 缺少错误消息回送给发送者的逻辑

**要求修正**：
```java
public void handle(Long senderId, Object data) {
    try {
        if (senderId == null) {
            throw new BusinessException("发送者不能为空");
        }
        if (data == null) {
            throw new BusinessException("消息数据不能为空");
        }
        ChatPayload payload = objectMapper.convertValue(data, ChatPayload.class);
        chatMessageService.saveAndPushMessage(senderId, payload);
        
        log.info("用户{}发送消息成功，receiverId={}", senderId, payload.getReceiverId());
        
    } catch (BusinessException e) {
        log.warn("用户{}发送消息业务异常: {}", senderId, e.getMessage());
        // 回送错误提示给发送者
        Map<String, String> errorData = Map.of("error", e.getMessage());
        WebSocketMessage<Map<String, String>> errorMsg = new WebSocketMessage<>("SYSTEM", errorData);
        sessionManager.sendToUser(senderId, errorMsg);
        
    } catch (Exception e) {
        log.error("用户{}发送消息异常", senderId, e);
        // 回送通用错误提示
        Map<String, String> errorData = Map.of("error", "消息发送失败");
        WebSocketMessage<Map<String, String>> errorMsg = new WebSocketMessage<>("SYSTEM", errorData);
        sessionManager.sendToUser(senderId, errorMsg);
    }
}
```

**必须添加**：
1. 外层 try-catch 包裹整个方法
2. 捕获 BusinessException 并回送具体错误信息
3. 捕获 Exception 并回送通用错误提示
4. 注入 WebSocketSessionManager

#### 🔴 红线 2：ReadMessageHandler 异常未全部捕获
**文件**：src/main/java/com/qingyuan/secondhand/websocket/handler/ReadMessageHandler.java

**问题**：
- 只有参数校验的 BusinessException，但 markSessionReadByUserId 可能抛出其他异常
- 缺少 try-catch 包裹整个 handle 方法

**要求修正**：
```java
public void handle(Long senderId, Object data) {
    try {
        if (senderId == null) {
            throw new BusinessException("发送者不能为空");
        }
        if (data == null) {
            throw new BusinessException("消息数据不能为空");
        }
        ReadPayload payload = objectMapper.convertValue(data, ReadPayload.class);
        if (payload == null || payload.getSessionKey() == null) {
            throw new BusinessException("sessionKey不能为空");
        }
        chatMessageService.markSessionReadByUserId(senderId, payload.getSessionKey());
        
        Long peerId = SessionKeyUtil.getPeerId(payload.getSessionKey(), senderId);
        if (peerId != null) {
            Map<String, Object> ackData = new HashMap<>();
            ackData.put("sessionKey", payload.getSessionKey());
            ackData.put("readerId", senderId);
            WebSocketMessage<Map<String, Object>> ackMsg = new WebSocketMessage<>("READ_ACK", ackData);
            sessionManager.sendToUser(peerId, ackMsg);
        }
        
        log.info("用户{}标记会话{}已读", senderId, payload.getSessionKey());
        
    } catch (BusinessException e) {
        log.warn("用户{}标记已读业务异常: {}", senderId, e.getMessage());
    } catch (Exception e) {
        log.error("用户{}标记已读异常", senderId, e);
    }
}
```

**必须添加**：
1. 外层 try-catch 包裹整个方法
2. 捕获 BusinessException 和 Exception

### 审查结论

**❌ 驳回 - 必须修复红线问题**

该功能代码质量整体优秀，但存在 2 个红线问题必须修复：

1. **ChatMessageHandler 异常未全部捕获**：缺少 try-catch 包裹整个方法，可能导致 WebSocket 连接断开
2. **ReadMessageHandler 异常未全部捕获**：缺少 try-catch 包裹整个方法，可能导致 WebSocket 连接断开

这两个问题违反了 WebSocket 开发的核心规范：**异常必须全部捕获，绝不能往上抛**。

### 优点总结

1. **MyBatis-Plus 规范**：Entity 注解正确，Mapper 继承 BaseMapper，XML 使用 #{}
2. **Service 实现**：saveAndPushMessage 逻辑完整，覆盖所有步骤
3. **权限校验**：getMessageHistory 和 markSessionReadByUserId 都有权限校验
4. **工具类补充**：SessionKeyUtil 补充了 getPeerId 和 getProductId 方法
5. **测试覆盖**：9 个测试用例全部通过，覆盖核心场景
6. **代码质量**：命名规范，注释清晰，结构合理

### 修复要求

请执行者修复以上 2 个红线问题后重新提交审查。

---

**审查人**：监督者（Kiro IDE）  
**审查时间**：2026-02-23 00:30  
**独立复跑**：✅ 通过（9/9 测试通过）  
**审查结果**：❌ 驳回

喵~

---

### [执行者] F-IM-03 修正记录

[执行者] 2026-02-23 修正红线问题：
1. ChatMessageHandler 增加外层 try-catch，回送错误消息并记录日志
2. ReadMessageHandler 增加外层 try-catch，并记录日志
3. 重新运行 mvn test -Dtest=ChatMessageServiceImplTest（9/9 通过）
4. 更新证据包：run-folder/F-IM-03-消息存储与查询/test_output.log


---

## [监督者] F-IM-03 消息存储与查询 - 复查结果

**复查时间**：2026-02-23 00:41

### 复查详情

#### 红线问题修复验证

**🔴 红线 1：ChatMessageHandler 异常未全部捕获 - ✅ 已修复**

**文件**：src/main/java/com/qingyuan/secondhand/websocket/handler/ChatMessageHandler.java

**修复验证**：
- ✅ 已添加外层 try-catch 包裹整个 handle 方法
- ✅ 捕获 BusinessException 并回送具体错误信息
- ✅ 捕获 Exception 并回送通用错误提示
- ✅ 注入了 WebSocketSessionManager
- ✅ 错误消息回送逻辑正确（使用 SYSTEM 类型消息）
- ✅ 日志记录完整（warn 级别记录业务异常，error 级别记录系统异常）

**🔴 红线 2：ReadMessageHandler 异常未全部捕获 - ✅ 已修复**

**文件**：src/main/java/com/qingyuan/secondhand/websocket/handler/ReadMessageHandler.java

**修复验证**：
- ✅ 已添加外层 try-catch 包裹整个 handle 方法
- ✅ 捕获 BusinessException 和 Exception
- ✅ 日志记录完整（warn 级别记录业务异常，error 级别记录系统异常）

#### 独立复跑验证 ✅

**执行命令**：
```bash
mvn test -Dtest=ChatMessageServiceImplTest
```

**测试结果**：
```
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
[INFO] Total time:  8.799 s
```

- ✅ 所有测试通过（9/9）
- ✅ 无编译错误
- ✅ 无运行时异常
- ✅ 与执行者的测试结果一致

### 最终审查结论

**✅ 通过 - 功能「F-IM-03 消息存储与查询」验收通过**

#### 审查维度总结

| 审查维度 | 结果 |
|---------|------|
| MyBatis-Plus 规范 | ✅ 通过 |
| 功能正确性 | ✅ 通过 |
| 安全性 | ✅ 通过 |
| 代码质量 | ✅ 通过 |
| 测试覆盖 | ✅ 通过 |
| 数据库一致性 | ✅ 通过 |
| 证据包 | ✅ 通过 |
| 独立复跑 | ✅ 通过 |
| 红线问题修复 | ✅ 通过 |

#### 功能亮点

1. **异常处理完善**：WebSocket Handler 异常全部捕获，不会导致连接断开
2. **错误消息回送**：ChatMessageHandler 在异常时回送错误提示给发送者，用户体验好
3. **权限校验严格**：getMessageHistory 和 markSessionReadByUserId 都有权限校验
4. **Service 实现完整**：saveAndPushMessage 覆盖所有步骤（参数校验、sessionKey 构建、入库、更新会话、Redis 计数、推送、离线通知）
5. **测试覆盖全面**：9 个测试用例覆盖正常场景、异常场景、权限校验场景
6. **代码质量高**：命名规范，注释清晰，结构合理

#### 下一步建议

该功能已完成，建议执行者继续开发下一个功能。

---

**审查人**：监督者（Kiro IDE）  
**复查时间**：2026-02-23 00:41  
**独立复跑**：✅ 通过（9/9 测试通过）  
**审查结果**：✅ 通过

喵~
