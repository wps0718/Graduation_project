## 一、tasksWeb.md AI实现提示词模板

```markdown
# IM WebSocket 自建 - 任务构建与审查记录

## 文档说明
- 本文档记录 IM WebSocket 自建功能的任务规划、执行记录、审查反馈
- 任务ID格式：F-IM-{子编号}
- 记录前缀：[Kiro] = 规划/审查方，[trae] = 编码方

## 项目现有架构关键信息（执行者必读）
- JWT鉴权：JwtUtil.parseToken(token) 返回 Claims，claims.get("type") 区分 mini/admin，claims.getSubject() 为 userId(String→Long)
- 用户上下文：UserContext.getCurrentUserId() 获取当前登录用户ID（由 JwtInterceptor 写入）
- 拦截器注册：WebMvcConfig.java 中 /mini/** 走 JwtInterceptor，/admin/** 走 AdminJwtInterceptor
- Entity风格：@TableName("xxx")、@TableId(type=IdType.AUTO)、时间字段 @TableField(fill=FieldFill.INSERT/UPDATE)
- Mapper风格：继承 BaseMapper<T>，查询用 LambdaQueryWrapper 或注解SQL
- Controller风格：统一返回 Result.success(data)/Result.error(msg)，路径 /mini/... 开头
- 前端请求：request.js 中 USE_MOCK=true 控制Mock开关，真实请求 header 带 Authorization: Bearer ${token}
- 前端聊天列表(list.vue)绑定字段：userId/nickName/avatarUrl/authStatus/lastMessage/lastTime/unread/productId/productTitle/productPrice/productImage
- 前端聊天详情(detail.vue)消息字段：id/time/type/from/content，自己/对方用 from===selfId 区分
- 前端目前无聊天专用store，只有 store/app.js 和 store/user.js

---

## F-IM-01：WebSocket基础通信

### 任务状态：待规划

---

## F-IM-02：会话管理

### 任务状态：待规划

---

## F-IM-03：消息存储与查询

### 任务状态：待规划

---

## F-IM-04：业务卡片消息与系统集成

### 任务状态：待规划
```

---

## 二、提示词

---

### F-IM-01：WebSocket基础通信

#### F-IM-01-A：Kiro 规划任务

```
请开始你的监督者工作，规划 Feature F-IM-01：WebSocket基础通信。

⚠️ 项目现有架构约束（必须遵循，不可偏离）：
- JWT工具类已有：JwtUtil.java，提供 parseToken(String token) 返回 Claims
  - claims.get("type") 区分 "mini"/"admin"
  - claims.getSubject() 返回 userId 字符串，需 Long.parseLong() 转换
- 用户上下文已有：UserContext.java，提供 setCurrentUserId(Long) 和 getCurrentUserId()
  - WebSocket握手拦截器不走 JwtInterceptor（那是HTTP拦截器），需自行在握手阶段调用 JwtUtil
- 拦截器注册在 WebMvcConfig.java，WebSocket不走MVC拦截器链
- Entity风格：@TableName + @TableId(type=IdType.AUTO) + @TableField(fill=FieldFill.INSERT)
- 统一返回：Result.success(data) / Result.error(msg)，code=1成功，code=0失败
- Redis操作使用项目已有的 StringRedisTemplate

1. 读取 feature_list.json，找到 id 为 "F-IM-01" 的功能
2. 分析该功能需要涉及的所有文件：
   - config/WebSocketConfig.java
   - websocket/ChatHandshakeInterceptor.java
   - websocket/WebSocketServer.java
   - websocket/WebSocketSessionManager.java
   - websocket/handler/MessageDispatcher.java
   - websocket/handler/PingMessageHandler.java
   - websocket/handler/ChatMessageHandler.java（空实现占位）
   - websocket/handler/ReadMessageHandler.java（空实现占位）
   - websocket/protocol/WebSocketMessage.java
   - websocket/protocol/MessageType.java
   - task/WebSocketHeartbeatTask.java
   - common/constant/RedisConstant.java（追加常量）
3. 在 tasksWeb.md 的 F-IM-01 部分规划详细的开发步骤，包括：

   **Step 1: pom.xml 依赖确认**
   - 确认项目 pom.xml 已有 spring-boot-starter-websocket 依赖
   - 没有则添加：
     ```xml
     <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-websocket</artifactId>
     </dependency>
     ```

   **Step 2: Redis Key 常量补充**
   - 在已有的 common/constant/RedisConstant.java 中追加（不要新建文件，不要删除已有常量）：
     ```java
     public static final String IM_ONLINE = "im:online:";
     public static final String IM_HEARTBEAT = "im:heartbeat:";
     public static final String IM_UNREAD = "im:unread:";
     public static final String IM_SESSION_UNREAD = "im:session:unread:";
     ```

   **Step 3: 消息协议定义**
   - MessageType.java 枚举类，放在 websocket/protocol/ 包下
   - 枚举值：CHAT, READ, READ_ACK, PING, PONG, SYSTEM, FORCE_OFFLINE
   - 每个枚举值有 value(String) 属性，值为枚举名的小写或原样
   - 提供 fromValue(String) 静态方法用于反序列化匹配（忽略大小写）

   - WebSocketMessage.java 泛型类，放在 websocket/protocol/ 包下
   - 字段：type(String), data(T)
   - 使用 @Data @NoArgsConstructor @AllArgsConstructor（Lombok）
   - 提供 static toJson(WebSocketMessage<?>) 方法：使用 Jackson ObjectMapper 序列化
   - 提供 static fromJson(String json) 方法：反序列化为 WebSocketMessage<Object>（data先为LinkedHashMap，由各Handler自行转换）
   - ObjectMapper 声明为 private static final，直接 new ObjectMapper() 并配置 FAIL_ON_UNKNOWN_PROPERTIES=false
   - toJson/fromJson 异常时记录日志并返回 null / 抛出 RuntimeException

   **Step 4: WebSocket配置类**
   - WebSocketConfig.java 放在 config/ 包下
   - 实现 WebSocketConfigurer 接口
   - 加 @Configuration @EnableWebSocket 注解
   - registerWebSocketHandlers 方法中：
     a. 注册 WebSocketServer 处理器到路径 "/ws/chat"
     b. 添加 ChatHandshakeInterceptor 拦截器
     c. setAllowedOrigins("*")（开发阶段）
   - WebSocketServer 和 ChatHandshakeInterceptor 通过构造器注入（@RequiredArgsConstructor）

   **Step 5: 握手鉴权拦截器**
   - ChatHandshakeInterceptor.java 放在 websocket/ 包下
   - 实现 HandshakeInterceptor 接口
   - 通过构造器注入 JwtUtil 和 UserMapper（项目已有 UserMapper 继承 BaseMapper<User>）
   - beforeHandshake 方法逻辑：
     a. 从 ServerHttpRequest 获取 URI
     b. 解析 URI 查询参数获取 token（格式：/ws/chat?token=xxx）
     c. token 为空 → log.warn + return false
     d. 调用 jwtUtil.parseToken(token) 获取 Claims
     e. 校验 claims.get("type") 必须等于 "mini"（只有小程序用户能连WS）
     f. Long userId = Long.parseLong(claims.getSubject())
     g. 通过 userMapper.selectById(userId) 查询用户
     h. 用户不存在 → return false
     i. 用户 status==0（封禁）→ return false
     j. 用户 status==2（注销中）→ return false
     k. attributes.put("userId", userId) — 存入 WebSocketSession 的 attributes
     l. return true
     m. 整体 try-catch，任何异常 → log.error + return false
   - afterHandshake 方法：空实现

   **Step 6: 在线会话管理器**
   - WebSocketSessionManager.java 放在 websocket/ 包下
   - @Component + @Slf4j
   - 注入 StringRedisTemplate
   - 核心数据结构：private final ConcurrentHashMap<Long, WebSocketSession> onlineSessions = new ConcurrentHashMap<>()
   - 方法：
     a. addSession(Long userId, WebSocketSession session):
        - 检查是否已有旧连接：oldSession = onlineSessions.get(userId)
        - 如果 oldSession != null 且 oldSession.isOpen()：
          * 构建 FORCE_OFFLINE 消息：WebSocketMessage<Map> msg，type="FORCE_OFFLINE"，data=Map.of("reason","账号在其他设备登录")
          * oldSession.sendMessage(new TextMessage(WebSocketMessage.toJson(msg)))
          * oldSession.close()
        - onlineSessions.put(userId, session)
        - stringRedisTemplate.opsForValue().set(RedisConstant.IM_ONLINE + userId, "1")
        - log.info("用户{}上线，当前在线{}人", userId, onlineSessions.size())

     b. removeSession(Long userId):
        - WebSocketSession session = onlineSessions.remove(userId)
        - if (session != null && session.isOpen()) session.close()
        - stringRedisTemplate.delete(RedisConstant.IM_ONLINE + userId)
        - log.info("用户{}下线，当前在线{}人", userId, onlineSessions.size())

     c. getSession(Long userId) → 返回 onlineSessions.get(userId)

     d. isOnline(Long userId) → 返回 onlineSessions.containsKey(userId) && session.isOpen()

     e. sendToUser(Long userId, WebSocketMessage<?> message) → boolean:
        - WebSocketSession session = onlineSessions.get(userId)
        - if (session == null || !session.isOpen()) return false
        - synchronized(session) { session.sendMessage(new TextMessage(WebSocketMessage.toJson(message))) }
        - return true
        - catch IOException → log.error + removeSession(userId) + return false

     f. getOnlineCount() → return onlineSessions.size()

     g. getAllOnlineUserIds() → return onlineSessions.keySet()（心跳检测用）

   **Step 7: WebSocket处理器（核心入口）**
   - WebSocketServer.java 放在 websocket/ 包下
   - 继承 TextWebSocketHandler（Spring 原生，不是 @ServerEndpoint）
   - @Component + @Slf4j
   - 构造器注入 WebSocketSessionManager 和 MessageDispatcher

   - afterConnectionEstablished(WebSocketSession session):
     a. Long userId = (Long) session.getAttributes().get("userId")
     b. if (userId == null) { session.close(); return; }
     c. sessionManager.addSession(userId, session)

   - handleTextMessage(WebSocketSession session, TextMessage message):
     a. Long userId = (Long) session.getAttributes().get("userId")
     b. String payload = message.getPayload()
     c. log.debug("收到用户{}消息: {}", userId, payload)
     d. try { WebSocketMessage<?> wsMsg = WebSocketMessage.fromJson(payload) }
     e. if (wsMsg == null || wsMsg.getType() == null) { log.warn("消息格式错误"); return; }
     f. messageDispatcher.dispatch(userId, wsMsg)
     g. catch (Exception e) { log.error("处理用户{}消息异常", userId, e) }
     ※ 注意：异常必须捕获，不能让异常往上抛导致连接断开

   - afterConnectionClosed(WebSocketSession session, CloseStatus status):
     a. Long userId = (Long) session.getAttributes().get("userId")
     b. if (userId != null) sessionManager.removeSession(userId)
     c. log.info("用户{}连接关闭, status={}", userId, status)

   - handleTransportError(WebSocketSession session, Throwable exception):
     a. Long userId = (Long) session.getAttributes().get("userId")
     b. log.error("用户{}传输异常", userId, exception)
     c. if (userId != null) sessionManager.removeSession(userId)

   **Step 8: 消息分发器**
   - MessageDispatcher.java 放在 websocket/handler/ 包下
   - @Component + @Slf4j
   - 构造器注入 PingMessageHandler
   - 暂时不注入 ChatMessageHandler 和 ReadMessageHandler（F-IM-01阶段它们是空实现）
   - dispatch(Long senderId, WebSocketMessage<?> message):
     a. String type = message.getType().toUpperCase()
     b. switch (type):
        case "PING" → pingMessageHandler.handle(senderId)
        case "CHAT" → log.info("CHAT消息，待F-IM-03实现") // TODO: chatMessageHandler.handle(senderId, message.getData())
        case "READ" → log.info("READ消息，待F-IM-03实现") // TODO: readMessageHandler.handle(senderId, message.getData())
        default → log.warn("未知消息类型: {}", type)

   **Step 9: 心跳处理器**
   - PingMessageHandler.java 放在 websocket/handler/ 包下
   - @Component + @Slf4j
   - 构造器注入 WebSocketSessionManager 和 StringRedisTemplate
   - handle(Long userId):
     a. stringRedisTemplate.opsForValue().set(RedisConstant.IM_HEARTBEAT + userId, String.valueOf(System.currentTimeMillis()), 60, TimeUnit.SECONDS)
     b. WebSocketMessage<Void> pong = new WebSocketMessage<>("PONG", null)
     c. sessionManager.sendToUser(userId, pong)
     d. log.debug("用户{}心跳PING，已回复PONG", userId)

   **Step 10: 空实现占位Handler**
   - ChatMessageHandler.java 放在 websocket/handler/ 包下
   - @Component + @Slf4j
   - public void handle(Long senderId, Object data):
     ```java
     // TODO [F-IM-03] 实现CHAT消息处理：参数校验→入库→更新会话→推送/离线通知
     log.info("ChatMessageHandler.handle() 待实现, senderId={}", senderId);
     ```

   - ReadMessageHandler.java 放在 websocket/handler/ 包下
   - @Component + @Slf4j
   - public void handle(Long senderId, Object data):
     ```java
     // TODO [F-IM-03] 实现READ消息处理：标记已读→重置未读数→推送READ_ACK
     log.info("ReadMessageHandler.handle() 待实现, senderId={}", senderId);
     ```

   **Step 11: 心跳超时检测定时任务**
   - WebSocketHeartbeatTask.java 放在 task/ 包下
   - @Component + @Slf4j + @EnableScheduling（或项目启动类已有）
   - 构造器注入 WebSocketSessionManager 和 StringRedisTemplate
   - @Scheduled(fixedRate = 30000) 方法 checkHeartbeat():
     a. Set<Long> onlineUserIds = sessionManager.getAllOnlineUserIds()
     b. for (Long userId : onlineUserIds):
        String heartbeat = stringRedisTemplate.opsForValue().get(RedisConstant.IM_HEARTBEAT + userId)
        if (heartbeat == null) {
          log.warn("用户{}心跳超时，强制断开", userId)
          sessionManager.removeSession(userId)
        }

   **Step 12: 测试用例要求**
   - WebSocketSessionManagerTest.java：
     a. 测试 addSession 正常添加（Mock WebSocketSession，验证 onlineSessions.size 和 Redis SET）
     b. 测试 addSession 重复添加踢旧连接（验证旧Session收到FORCE_OFFLINE并被close）
     c. 测试 removeSession 正常移除（验证 Redis DEL）
     d. 测试 isOnline 在线/离线判断
     e. 测试 sendToUser 在线发送成功（验证 session.sendMessage 被调用）
     f. 测试 sendToUser 离线返回false（不抛异常）
   - Mock 对象：WebSocketSession 用 Mockito.mock()，StringRedisTemplate 用 @MockBean 或 Mockito.mock()

4. 步骤要足够具体，让执行者可以直接按步骤编写代码
5. 明确标注以下注意事项：
   - 使用 Spring 原生 WebSocket（TextWebSocketHandler），不使用 javax.websocket @ServerEndpoint
   - 握手拦截器中复用项目已有的 JwtUtil.parseToken()，不要自己写JWT解析逻辑
   - 握手拦截器中查询用户状态用项目已有的 UserMapper.selectById()
   - 不要在握手拦截器中调用 UserContext.setCurrentUserId()（UserContext 是基于 ThreadLocal 的，WebSocket 生命周期跨线程，userId 应存在 session.getAttributes() 中）
   - WebSocketSession 的 sendMessage 需要 synchronized(session) 防止并发写入
   - 所有 WebSocket 相关类放在 websocket/ 包下（ChatHandshakeInterceptor 也放在 websocket/ 包下而非 config/）
   - F-IM-01 阶段 ChatMessageHandler 和 ReadMessageHandler 只是空实现占位
   - 日志使用 @Slf4j（Lombok）
   - 构造器注入使用 @RequiredArgsConstructor（Lombok），不使用 @Autowired

规划完成后，更新 tasksWeb.md 中 F-IM-01 部分的状态为"已规划"，并告诉我你规划的内容摘要。
```

#### F-IM-01-B：Trae 开始编码

```
请开始你的执行者工作，实现 Feature F-IM-01：WebSocket基础通信。

⚠️ 项目现有架构约束（必须遵循）：
- JWT：JwtUtil.parseToken(token) → Claims，claims.get("type")区分mini/admin，claims.getSubject()为userId字符串
- 用户上下文：UserContext.getCurrentUserId()，但WebSocket中不使用UserContext，userId存在session.getAttributes()中
- Entity风格：@TableName + @TableId(type=IdType.AUTO) + @TableField(fill=FieldFill.INSERT)
- 统一返回：Result.success(data) / Result.error(msg)
- Redis：使用已有的 StringRedisTemplate
- Lombok：@Data @Slf4j @RequiredArgsConstructor @Builder

1. 读取 tasksWeb.md，找到 F-IM-01 WebSocket基础通信的任务步骤
2. 读取 feature_list.json 中 id="F-IM-01" 的 acceptance_criteria
3. 按照 tasksWeb.md 中的步骤，逐步编写以下代码（严格按顺序）：

   Step 1: 确认/添加 pom.xml 依赖 spring-boot-starter-websocket

   Step 2: 在已有的 common/constant/RedisConstant.java 中追加 IM 常量
   ※ 只追加，不修改不删除已有常量

   Step 3: 消息协议
   - websocket/protocol/MessageType.java
   - websocket/protocol/WebSocketMessage.java

   Step 4: WebSocket配置
   - config/WebSocketConfig.java（@Configuration @EnableWebSocket，实现WebSocketConfigurer）

   Step 5: 握手拦截器
   - websocket/ChatHandshakeInterceptor.java
   ※ 必须复用 JwtUtil.parseToken()，不要自己写JWT解析
   ※ 必须用 UserMapper.selectById() 查用户状态
   ※ userId 存入 session.getAttributes().put("userId", userId)
   ※ 不要调用 UserContext.setCurrentUserId()

   Step 6: 会话管理器
   - websocket/WebSocketSessionManager.java
   ※ sendToUser 中 synchronized(session) 同步块

   Step 7: WebSocket处理器
   - websocket/WebSocketServer.java（继承 TextWebSocketHandler）
   ※ handleTextMessage 必须 try-catch 全包裹，异常不能断连

   Step 8: 消息分发器
   - websocket/handler/MessageDispatcher.java
   ※ CHAT 和 READ 用 log.info + TODO 注释占位

   Step 9: 心跳处理器
   - websocket/handler/PingMessageHandler.java

   Step 10: 空实现占位
   - websocket/handler/ChatMessageHandler.java（方法体只有TODO注释和日志）
   - websocket/handler/ReadMessageHandler.java（方法体只有TODO注释和日志）

   Step 11: 心跳检测定时任务
   - task/WebSocketHeartbeatTask.java

4. 每完成一个 Step，在 tasksWeb.md 中追加 [执行者] 前缀的记录，格式如下：
   [执行者] Step X 完成 - {文件名} - {简要说明} - {时间}

5. 编码完成后，编写测试：
   - test/.../WebSocketSessionManagerTest.java
     a. Mock WebSocketSession（Mockito）
     b. Mock StringRedisTemplate
     c. 测试 addSession 正常添加
     d. 测试 addSession 重复添加踢旧连接（验证旧Session.sendMessage和close被调用）
     e. 测试 removeSession
     f. 测试 isOnline
     g. 测试 sendToUser 在线成功
     h. 测试 sendToUser 离线返回false

6. 在终端运行测试：
   mvn test -Dtest=WebSocketSessionManagerTest
   将输出保存到 run-folder/F-IM-01-WebSocket基础通信/test_output.log

7. 生成 run-folder/F-IM-01-WebSocket基础通信/ 证据包：
   - task.md（从 tasksWeb.md 中提取 F-IM-01 部分）
   - test_output.log
   - file_list.txt（本次创建/修改的所有文件清单）

8. 创建 .ready-for-review 信号文件

9. 完成后将详细内容写入tasksWeb.md，并告诉我结果摘要

关键约束（违反则审查必驳回）：
- ❌ 不使用 @ServerEndpoint
- ❌ 不自己写 JWT 解析逻辑（必须用 JwtUtil.parseToken）
- ❌ 不在 WebSocket 中使用 UserContext（ThreadLocal 跨线程不安全）
- ❌ sendToUser 不加 synchronized 会被驳回
- ❌ handleTextMessage 不 try-catch 会被驳回
- ✅ 使用 TextWebSocketHandler
- ✅ userId 存 session.getAttributes()
- ✅ 复用项目已有的 JwtUtil、UserMapper、StringRedisTemplate
- ✅ @Slf4j + @RequiredArgsConstructor
```

#### F-IM-01-C：Kiro 执行审查

```
执行者已提交 F-IM-01 WebSocket基础通信的审查请求。请执行完整的审查流程：

1. 读取 .ready-for-review 文件内容
2. 读取 tasksWeb.md 中执行者关于 F-IM-01 的 [执行者] 工作记录
3. 逐一审查以下文件（对照项目已有架构）：

   **pom.xml**
   - 是否有 spring-boot-starter-websocket 依赖

   **common/constant/RedisConstant.java**
   - 是否只是追加了 IM_ONLINE/IM_HEARTBEAT/IM_UNREAD/IM_SESSION_UNREAD 四个常量
   - 是否保留了原有常量（如 sms:code、sms:limit 等）

   **websocket/protocol/MessageType.java**
   - 是否包含 CHAT, READ, READ_ACK, PING, PONG, SYSTEM, FORCE_OFFLINE 七个值
   - 是否有 fromValue(String) 方法

   **websocket/protocol/WebSocketMessage.java**
   - 是否为泛型类
   - ObjectMapper 是否为 private static final（不是每次 new）
   - FAIL_ON_UNKNOWN_PROPERTIES 是否配置为 false
   - toJson/fromJson 异常处理是否合理

   **config/WebSocketConfig.java**
   - 是否实现 WebSocketConfigurer（不是 WebSocketMessageBrokerConfigurer）
   - 是否注册到 /ws/chat
   - 是否添加了 ChatHandshakeInterceptor
   - 是否 setAllowedOrigins("*")

   **websocket/ChatHandshakeInterceptor.java**
   ※ 核心审查项：
   a. 是否复用 JwtUtil.parseToken(token)（不是自己解析JWT）
   b. 是否检查 claims.get("type").equals("mini")
   c. 是否用 Long.parseLong(claims.getSubject()) 获取userId
   d. 是否用 UserMapper.selectById(userId) 查询用户
   e. 是否校验 status==0(封禁) 和 status==2(注销中) 返回false
   f. userId 是否存入 attributes.put("userId", userId)
   g. 是否有 try-catch 全包裹
   h. ❌ 是否没有调用 UserContext.setCurrentUserId()（WebSocket中禁止使用ThreadLocal的UserContext）

   **websocket/WebSocketSessionManager.java**
   a. ConcurrentHashMap 类型是否为 <Long, WebSocketSession>
   b. addSession 是否处理踢旧连接（发FORCE_OFFLINE + close）
   c. sendToUser 是否有 synchronized(session) 同步块
   d. sendToUser 发送 IOException 是否 removeSession
   e. 是否操作 Redis IM_ONLINE key
   f. 是否有 getAllOnlineUserIds() 方法（心跳检测用）

   **websocket/WebSocketServer.java**
   a. ❌ 是否没有使用 @ServerEndpoint（必须是继承 TextWebSocketHandler）
   b. afterConnectionEstablished 是否从 attributes 取 userId
   c. handleTextMessage 是否有完整 try-catch（异常不能断连）
   d. handleTextMessage 中 WebSocketMessage.fromJson 是否正确调用
   e. afterConnectionClosed 和 handleTransportError 是否清理 Session

   **websocket/handler/MessageDispatcher.java**
   a. PING 是否路由到 PingMessageHandler
   b. CHAT 和 READ 是否有 TODO 注释占位（不是空白，要有明确的 TODO [F-IM-03] 标记）
   c. 未知类型是否有 log.warn

   **websocket/handler/PingMessageHandler.java**
   a. 是否 SET Redis 心跳key 且 TTL 60秒
   b. 是否回复 PONG 消息

   **websocket/handler/ChatMessageHandler.java + ReadMessageHandler.java**
   a. 是否只有 TODO 注释和日志，无业务逻辑

   **task/WebSocketHeartbeatTask.java**
   a. @Scheduled fixedRate 是否 30000
   b. 是否遍历在线用户检查 Redis 心跳key
   c. 超时是否调用 removeSession

4. 审查测试文件 WebSocketSessionManagerTest.java：
   - Mock 对象是否正确（WebSocketSession, StringRedisTemplate）
   - 是否覆盖 6 个场景
   - 断言是否有实际意义

5. 审查 run-folder/F-IM-01-WebSocket基础通信/ 证据包完整性

6. 在终端运行 mvn test -Dtest=WebSocketSessionManagerTest 独立复跑验证

7. 对比运行结果与执行者的 test_output.log

8. 在 tasksWeb.md 中记录 [监督者] 审查结果，格式：
   [监督者] F-IM-01 审查结果：通过/驳回
   [监督者] 审查详情：
   - ✅ xxx 符合要求
   - ❌ xxx 不符合要求，原因：...

9. 根据审查结果执行通过或驳回流程

10. 告诉我审查结果

必须驳回的情况（红线）：
- 使用了 @ServerEndpoint 而非 TextWebSocketHandler
- 自己写了 JWT 解析逻辑而非复用 JwtUtil.parseToken
- WebSocket 中使用了 UserContext.setCurrentUserId()
- sendToUser 没有 synchronized
- handleTextMessage 没有 try-catch
- 修改了 RedisConstant.java 中的原有常量
```

#### F-IM-01-D：Trae 修正驳回（仅驳回时使用）

```
监督者已驳回 F-IM-01 WebSocket基础通信的审查。请：
1. 读取 tasksWeb.md 中监督者关于 F-IM-01 的 [监督者] 反馈
2. 逐一理解每个驳回问题（特别注意标注 ❌ 的项）
3. 按照反馈修正对应的代码文件
4. 修正时注意不要引入新问题（不要改动其他无关文件）
5. 重新运行 mvn test -Dtest=WebSocketSessionManagerTest
6. 更新 run-folder/F-IM-01-WebSocket基础通信/ 证据包
7. 重新创建 .ready-for-review 信号文件
8. 在 tasksWeb.md 中记录 [执行者] 修正内容：
   [执行者] F-IM-01 修正 - 针对驳回问题X：修改了xxx文件的xxx方法 - {时间}
```

---

### F-IM-02：会话管理

#### F-IM-02-A：Kiro 规划任务

```
请开始你的监督者工作，规划 Feature F-IM-02：会话管理。

前置条件：F-IM-01 已通过审查。

⚠️ 项目现有架构约束（必须遵循）：
- Entity风格：@TableName("chat_session") + @TableId(type=IdType.AUTO) + @TableField(fill=FieldFill.INSERT) 用于 createTime，@TableField(fill=FieldFill.INSERT_UPDATE) 用于 updateTime
- Mapper风格：继承 BaseMapper<ChatSession>，简单查询用 LambdaQueryWrapper，复杂联表用 XML
- Controller风格：返回 Result.success(data)，路径 /mini/chat/...，用 UserContext.getCurrentUserId() 获取当前用户
- VO字段必须对齐前端 list.vue 绑定：userId(peerId), nickName(peerName), avatarUrl(peerAvatar), authStatus(peerAuthStatus), lastMessage(lastMsg), lastTime, unread, productId, productTitle, productPrice, productImage

1. 读取 feature_list.json，找到 id 为 "F-IM-02" 的功能
2. 分析该功能需要涉及的所有文件：
   - entity/ChatSession.java
   - mapper/ChatSessionMapper.java
   - resources/mapper/ChatSessionMapper.xml
   - service/ChatSessionService.java
   - service/impl/ChatSessionServiceImpl.java
   - controller/mini/MiniChatController.java
   - dto/ChatSessionCreateDTO.java
   - vo/ChatSessionVO.java
   - common/util/SessionKeyUtil.java
3. 在 tasksWeb.md 的 F-IM-02 部分规划详细的开发步骤，包括：

   **Step 1: 数据库建表**
   - 在 sql/ 目录下创建或追加 chat_session 建表 DDL：
   ```sql
   CREATE TABLE IF NOT EXISTS `chat_session` (
     `id`            bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '主键',
     `user_id`       bigint(20)    NOT NULL COMMENT '当前用户ID',
     `peer_id`       bigint(20)    NOT NULL COMMENT '对话方用户ID',
     `product_id`    bigint(20)    DEFAULT NULL COMMENT '关联商品ID',
     `last_msg`      varchar(255)  DEFAULT NULL COMMENT '最后一条消息摘要',
     `last_msg_type`  tinyint(4)   DEFAULT 1 COMMENT '最后消息类型 1-文本 2-商品卡片 3-订单卡片 4-系统',
     `unread`        int(11)       NOT NULL DEFAULT 0 COMMENT '未读数',
     `last_time`     datetime      DEFAULT NULL COMMENT '最后消息时间',
     `is_top`        tinyint(1)    NOT NULL DEFAULT 0 COMMENT '是否置顶 0-否 1-是',
     `is_deleted`    tinyint(1)    NOT NULL DEFAULT 0 COMMENT '是否删除 0-否 1-是',
     `create_time`   datetime      DEFAULT NULL COMMENT '创建时间',
     `update_time`   datetime      DEFAULT NULL COMMENT '更新时间',
     PRIMARY KEY (`id`),
     UNIQUE KEY `idx_user_peer_product` (`user_id`, `peer_id`, `product_id`),
     KEY `idx_user_last_time` (`user_id`, `last_time`),
     KEY `idx_product_id` (`product_id`)
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话表';
   ```

   **Step 2: SessionKeyUtil 工具类**
   - common/util/SessionKeyUtil.java
   - public static String buildSessionKey(Long userA, Long userB, Long productId):
     long min = Math.min(userA, userB);
     long max = Math.max(userA, userB);
     return min + "_" + max + "_" + (productId != null ? productId : 0);
   - public static boolean isParticipant(String sessionKey, Long userId):
     解析 sessionKey 判断 userId 是否是参与方（用于权限校验）

   **Step 3: ChatSession 实体类**
   - entity/ChatSession.java
   - 注解风格参考项目已有的 User.java：
     @Data @Builder @NoArgsConstructor @AllArgsConstructor
     @TableName("chat_session")
   - @TableId(type = IdType.AUTO) private Long id;
   - private Long userId, peerId, productId;
   - private String lastMsg;
   - private Integer lastMsgType;
   - private Integer unread;
   - private LocalDateTime lastTime;
   - private Integer isTop;  // 0/1
   - private Integer isDeleted;  // 0/1
   - @TableField(fill = FieldFill.INSERT) private LocalDateTime createTime;
   - @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updateTime;
   ※ is_top 和 is_deleted 用 Integer 而非 Boolean，与项目 product 表的 is_deleted 风格保持一致

   **Step 4: ChatSessionCreateDTO**
   - dto/ChatSessionCreateDTO.java
   - @Data
   - @NotNull(message = "对方用户ID不能为空") private Long peerId;
   - private Long productId;  // 可选

   **Step 5: ChatSessionVO**
   - vo/ChatSessionVO.java
   - @Data @Builder @NoArgsConstructor @AllArgsConstructor
   - 字段（注意：命名要方便前端 list.vue 直接使用或简单映射）：
     Long sessionId;
     String sessionKey;
     Long peerId;          // 前端 list.vue 中对应 userId
     String peerName;      // 前端 list.vue 中对应 nickName
     String peerAvatar;    // 前端 list.vue 中对应 avatarUrl
     Integer peerAuthStatus; // 前端 list.vue 中对应 authStatus
     Long productId;
     String productTitle;
     String productImage;
     BigDecimal productPrice;
     Integer productStatus;
     String lastMsg;       // 前端 list.vue 中对应 lastMessage
     Integer lastMsgType;
     LocalDateTime lastTime;
     Integer unread;
     Boolean isTop;        // VO中可用Boolean方便前端
     Boolean isNew;        // 仅 createSession 返回时用

   **Step 6: ChatSessionMapper**
   - mapper/ChatSessionMapper.java 继承 BaseMapper<ChatSession>
   - 自定义方法（在XML中实现）：
     List<ChatSessionVO> selectSessionListByUserId(@Param("userId") Long userId);
   - XML关联查询：
     LEFT JOIN user ON user.id = chat_session.peer_id  → 取 nick_name, avatar_url, auth_status
     LEFT JOIN product ON product.id = chat_session.product_id → 取 title, images(JSON取首图), price, status
     WHERE chat_session.user_id = #{userId} AND chat_session.is_deleted = 0
     ORDER BY chat_session.is_top DESC, chat_session.last_time DESC
   - 注意 product.images 是 JSON 数组字符串（如 '["/a.jpg","/b.jpg"]'），取首图逻辑：
     可以在SQL中用 SUBSTRING_INDEX 截取，或者在 Java 层处理
     建议 Java 层处理（VO映射后端处理），SQL 查出原始 images 字段

   **Step 7: ChatSessionService 接口**
   - service/ChatSessionService.java
   - 方法签名：
     ChatSessionVO createSession(ChatSessionCreateDTO dto);
     List<ChatSessionVO> getSessionList();
     void deleteSession(Long sessionId);
     void toggleTop(Long sessionId);
     Integer getUnreadTotal();
     // 内部方法，供 ChatMessageHandler 调用
     void ensureSessionExists(Long userA, Long userB, Long productId);
     void updateSessionLastMsg(Long userId, Long peerId, Long productId, String lastMsg, Integer lastMsgType);
     void incrementUnread(Long userId, Long peerId, Long productId);
     void resetUnread(Long userId, Long peerId, Long productId);

   **Step 8: ChatSessionServiceImpl 实现**
   - service/impl/ChatSessionServiceImpl.java
   - @Service + @Slf4j + @RequiredArgsConstructor
   - 注入：ChatSessionMapper, UserMapper（校验用户存在）, ProductMapper（可选，查商品信息）, StringRedisTemplate

   - createSession(ChatSessionCreateDTO dto):
     a. Long currentUserId = UserContext.getCurrentUserId()
     b. if (dto.getPeerId().equals(currentUserId)) throw new BusinessException("不能与自己创建会话")
     c. 校验 peerId 用户存在：userMapper.selectById(dto.getPeerId())，不存在则抛异常
     d. String sessionKey = SessionKeyUtil.buildSessionKey(currentUserId, dto.getPeerId(), dto.getProductId())
     e. 查询已有会话：
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>()
          .eq(ChatSession::getUserId, currentUserId)
          .eq(ChatSession::getPeerId, dto.getPeerId())
          .eq(dto.getProductId() != null, ChatSession::getProductId, dto.getProductId());
        ChatSession existing = chatSessionMapper.selectOne(wrapper);
     f. if (existing != null):
        - if (existing.getIsDeleted() == 1) → 恢复：existing.setIsDeleted(0); chatSessionMapper.updateById(existing);
        - 构建 ChatSessionVO 返回，isNew=false
     g. if (existing == null):
        - 创建两条记录（@Transactional）：
          ChatSession sessionA = ChatSession.builder()
            .userId(currentUserId).peerId(dto.getPeerId()).productId(dto.getProductId())
            .unread(0).isTop(0).isDeleted(0).lastTime(LocalDateTime.now()).build();
          ChatSession sessionB = ChatSession.builder()
            .userId(dto.getPeerId()).peerId(currentUserId).productId(dto.getProductId())
            .unread(0).isTop(0).isDeleted(0).lastTime(LocalDateTime.now()).build();
          chatSessionMapper.insert(sessionA);
          chatSessionMapper.insert(sessionB);
        - // TODO [F-IM-04] 新会话创建成功后，如果 productId 不为空，自动发送商品卡片消息
        - // chatMessageService.sendProductCard(sessionKey, currentUserId, dto.getPeerId(), dto.getProductId());
        - 构建 ChatSessionVO 返回，isNew=true

   - getSessionList():
     Long currentUserId = UserContext.getCurrentUserId();
     List<ChatSessionVO> list = chatSessionMapper.selectSessionListByUserId(currentUserId);
     // 处理 product.images → productImage（取JSON数组首个元素）
     for (ChatSessionVO vo : list) {
       vo.setSessionKey(SessionKeyUtil.buildSessionKey(currentUserId, vo.getPeerId(), vo.getProductId()));
       // 处理 productImage 首图逻辑
     }
     return list;

   - deleteSession(Long sessionId):
     ChatSession session = chatSessionMapper.selectById(sessionId);
     if (session == null || !session.getUserId().equals(UserContext.getCurrentUserId()))
       throw new BusinessException("会话不存在");
     session.setIsDeleted(1);
     chatSessionMapper.updateById(session);

   - toggleTop(Long sessionId):
     ChatSession session = chatSessionMapper.selectById(sessionId);
     if (session == null || !session.getUserId().equals(UserContext.getCurrentUserId()))
       throw new BusinessException("会话不存在");
     session.setIsTop(session.getIsTop() == 1 ? 0 : 1);
     chatSessionMapper.updateById(session);

   - getUnreadTotal():
     Long currentUserId = UserContext.getCurrentUserId();
     // 优先读 Redis
     String cached = stringRedisTemplate.opsForValue().get(RedisConstant.IM_UNREAD + currentUserId);
     if (cached != null) return Integer.parseInt(cached);
     // Redis 不存在则查数据库
     LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>()
       .eq(ChatSession::getUserId, currentUserId)
       .eq(ChatSession::getIsDeleted, 0);
     List<ChatSession> sessions = chatSessionMapper.selectList(wrapper);
     int total = sessions.stream().mapToInt(ChatSession::getUnread).sum();
     stringRedisTemplate.opsForValue().set(RedisConstant.IM_UNREAD + currentUserId, String.valueOf(total));
     return total;

   - ensureSessionExists / updateSessionLastMsg / incrementUnread / resetUnread:
     这些是供 ChatMessageServiceImpl（F-IM-03）调用的内部方法
     按名称实现对应的数据库操作
     incrementUnread 必须使用 SQL: UPDATE chat_session SET unread = unread + 1 WHERE ...

   **Step 9: MiniChatController**
   - controller/mini/MiniChatController.java
   - @RestController @RequestMapping("/mini/chat") @Slf4j @RequiredArgsConstructor
   - 注入 ChatSessionService
   - 接口定义：

     @PostMapping("/session/create")
     public Result<ChatSessionVO> createSession(@RequestBody @Valid ChatSessionCreateDTO dto) {
       return Result.success(chatSessionService.createSession(dto));
     }

     @GetMapping("/sessions")
     public Result<List<ChatSessionVO>> getSessionList() {
       return Result.success(chatSessionService.getSessionList());
     }

     @PostMapping("/session/delete")
     public Result<Void> deleteSession(@RequestBody Map<String, Long> params) {
       chatSessionService.deleteSession(params.get("sessionId"));
       return Result.success();
     }

     @PostMapping("/session/top")
     public Result<Void> toggleTop(@RequestBody Map<String, Long> params) {
       chatSessionService.toggleTop(params.get("sessionId"));
       return Result.success();
     }

     @GetMapping("/unread-total")
     public Result<Integer> getUnreadTotal() {
       return Result.success(chatSessionService.getUnreadTotal());
     }

   ※ 所有接口需登录鉴权（/mini/** 已被 WebMvcConfig 中的 JwtInterceptor 拦截）
   ※ 注意：这些 REST 接口走 HTTP，经过 JwtInterceptor → UserContext 可用

   **Step 10: 测试用例**
   - ChatSessionServiceImplTest.java：
     a. 测试 createSession 新建会话 — 验证两条记录都创建、返回 isNew=true
     b. 测试 createSession 已有会话 — 返回 isNew=false，不重复创建
     c. 测试 createSession 自己跟自己 — 抛 BusinessException
     d. 测试 createSession 已删除的会话恢复 — is_deleted 从 1 变 0
     e. 测试 deleteSession 软删除 — is_deleted=1
     f. 测试 deleteSession 别人的会话 — 抛异常
     g. 测试 toggleTop 切换 — 0→1 / 1→0
     h. 测试 getUnreadTotal — 验证 Redis 优先和数据库降级

4. 步骤足够具体，让执行者可直接编码
5. 明确标注：
   - REST 接口走 HTTP → JwtInterceptor → UserContext.getCurrentUserId() 可用
   - WebSocket 中不使用 UserContext（回顾 F-IM-01 约束）
   - F-IM-02 中 sendProductCard 的调用先用 TODO 注释占位
   - createSession 中创建两条记录需要 @Transactional
   - ChatSessionVO 的字段命名要考虑前端 list.vue 的字段映射

规划完成后，更新 tasksWeb.md 中 F-IM-02 部分的状态为"已规划"，并告诉我你规划的内容摘要。
```

#### F-IM-02-B：Trae 开始编码

```
请开始你的执行者工作，实现 Feature F-IM-02：会话管理。

前置条件：F-IM-01 已通过审查，WebSocket基础通信代码已就绪。

⚠️ 项目现有架构约束（必须遵循）：
- Entity：@TableName + @TableId(type=IdType.AUTO) + @TableField(fill=FieldFill.INSERT/INSERT_UPDATE)
- Mapper：继承 BaseMapper<T>，简单查询用 LambdaQueryWrapper，联表用XML
- Controller：Result.success(data) / Result.error(msg)，路径 /mini/chat/...
- 当前用户ID：UserContext.getCurrentUserId()（REST接口中可用，因为走了JwtInterceptor）
- 项目已有 UserMapper、ProductMapper 可直接注入使用
- BusinessException 是项目已有的业务异常类

1. 读取 tasksWeb.md，找到 F-IM-02 会话管理的任务步骤
2. 读取 feature_list.json 中 id="F-IM-02" 的 acceptance_criteria
3. 按照 tasksWeb.md 中的步骤，逐步编写以下代码：

   Step 1: sql/chat_session.sql（建表DDL）
   Step 2: common/util/SessionKeyUtil.java
   Step 3: entity/ChatSession.java（参考项目已有 Entity 风格）
   Step 4: dto/ChatSessionCreateDTO.java
   Step 5: vo/ChatSessionVO.java（字段对齐前端 list.vue 绑定）
   Step 6: mapper/ChatSessionMapper.java + resources/mapper/ChatSessionMapper.xml
   Step 7: service/ChatSessionService.java（接口）
   Step 8: service/impl/ChatSessionServiceImpl.java（实现）
   Step 9: controller/mini/MiniChatController.java

4. 每完成一个 Step，在 tasksWeb.md 中追加 [执行者] 记录

5. 编写测试 ChatSessionServiceImplTest.java，覆盖 8 个场景

6. 运行测试：
   mvn test -Dtest=ChatSessionServiceImplTest
   保存到 run-folder/F-IM-02-会话管理/test_output.log

7. 生成证据包 + .ready-for-review

8. 完成后将详细内容写入tasksWeb.md，并告诉我结果摘要

关键约束（违反则审查必驳回）：
- ❌ createSession 不创建两条记录（双方各一条）会被驳回
- ❌ incrementUnread 使用先查后改（不是SQL unread=unread+1）会被驳回
- ❌ deleteSession 影响了对方的会话记录会被驳回
- ❌ Controller 不返回 Result.success() 会被驳回
- ❌ ChatSessionVO 缺少前端 list.vue 需要的字段会被驳回
- ✅ createSession 两条记录的创建需要 @Transactional
- ✅ 联表查询 SQL 写在 XML 中
- ✅ sendProductCard 调用用 TODO 占位
- ✅ 复用已有的 UserMapper、ProductMapper、BusinessException
```

#### F-IM-02-C：Kiro 执行审查

```
执行者已提交 F-IM-02 会话管理的审查请求。请执行完整的审查流程：

1. 读取 .ready-for-review 和 tasksWeb.md 中 [执行者] 记录
2. 逐一审查以下文件：

   **entity/ChatSession.java**
   - @TableName 是否为 "chat_session"
   - @TableId 是否为 IdType.AUTO
   - 时间字段是否有 @TableField(fill=...) 注解
   - is_top / is_deleted 是否用 Integer 类型（与项目 Product.isDeleted 风格一致）

   **common/util/SessionKeyUtil.java**
   - buildSessionKey 是否 min_max_productId 格式
   - productId 为 null 时是否用 0
   - isParticipant 方法是否正确解析

   **dto/ChatSessionCreateDTO.java**
   - peerId 是否有 @NotNull 校验注解
   - 是否使用 @Data

   **vo/ChatSessionVO.java — 重点审查**
   - 核对前端 list.vue 需要的每个字段：
     peerId (→前端userId) ✓
     peerName (→前端nickName) ✓
     peerAvatar (→前端avatarUrl) ✓
     peerAuthStatus (→前端authStatus) ✓
     lastMsg (→前端lastMessage) ✓
     lastTime ✓
     unread ✓
     productId ✓
     productTitle ✓
     productPrice ✓
     productImage ✓
   - 是否还有 sessionId, sessionKey, lastMsgType, isTop, productStatus, isNew

   **mapper/ChatSessionMapper.xml**
   - 联表 JOIN user 和 product 是否正确
   - WHERE 条件是否包含 is_deleted=0
   - ORDER BY 是否 is_top DESC, last_time DESC
   - product.images 字段的处理方式是否合理

   **service/impl/ChatSessionServiceImpl.java — 核心审查**
   - createSession:
     a. ❌ peerId==currentUserId 校验是否存在
     b. ❌ peerId 用户是否校验存在（userMapper.selectById）
     c. sessionKey 生成是否使用 SessionKeyUtil
     d. 查询已有会话的 LambdaQueryWrapper 是否正确
     e. 已有且 is_deleted=1 的会话是否恢复
     f. 新建时是否创建两条记录（A→B 和 B→A）
     g. @Transactional 注解是否存在
     h. sendProductCard 是否有 TODO [F-IM-04] 占位
   - deleteSession:
     a. 是否校验 sessionId 属于 currentUserId
     b. 是否只修改 is_deleted=1，不影响对方
   - toggleTop:
     a. 是否校验 sessionId 属于 currentUserId
   - getUnreadTotal:
     a. 是否优先读 Redis IM_UNREAD key
     b. Redis 不存在时是否查数据库并回写 Redis
   - incrementUnread:
     a. ❌ 是否使用 SQL SET unread=unread+1（而非先查后改）

   **controller/mini/MiniChatController.java**
   - 路径前缀是否为 /mini/chat
   - 是否所有方法返回 Result.success(...)
   - 是否没有业务逻辑（只做参数接收和 Service 调用）

3. 审查测试文件 ChatSessionServiceImplTest.java
4. 运行 mvn test -Dtest=ChatSessionServiceImplTest 复跑
5. 在 tasksWeb.md 记录 [监督者] 审查结果
6. 通过或驳回

重点检查（红线）：
- 双条记录创建的原子性（@Transactional）
- incrementUnread 必须是 SQL 层面 +1
- 软删除只影响当前用户
- ChatSessionVO 字段必须覆盖前端 list.vue 所有绑定字段
- Controller 返回 Result.success()
```

#### F-IM-02-D：Trae 修正驳回（仅驳回时使用）

```
监督者已驳回 F-IM-02 会话管理的审查。请：
1. 读取 tasksWeb.md 中监督者关于 F-IM-02 的 [监督者] 反馈
2. 逐一理解每个驳回问题
3. 按照反馈修正对应的代码文件
4. 重新运行 mvn test -Dtest=ChatSessionServiceImplTest
5. 更新 run-folder/F-IM-02-会话管理/ 证据包
6. 重新创建 .ready-for-review 信号文件
7. 在 tasksWeb.md 中记录 [执行者] 修正内容和重试结果
```

---

### F-IM-03：消息存储与查询

#### F-IM-03-A：Kiro 规划任务

```
请开始你的监督者工作，规划 Feature F-IM-03：消息存储与查询。

前置条件：F-IM-01、F-IM-02 已通过审查。

⚠️ 项目现有架构约束（必须遵循）：
- Entity：@TableName + @TableId(type=IdType.AUTO) + @TableField(fill=FieldFill.INSERT)
- Mapper：BaseMapper<T> + XML
- Controller：Result.success(data)
- 分页响应格式：{ total: xx, records: [...] }（参考项目已有的商品列表接口）
- 前端 detail.vue 消息字段：id, time, type, from, content，自己/对方用 from===selfId 区分
- 项目已有 Notification 实体和 NotificationService（或 NotificationMapper），用于离线通知
- 用户信息（nickName, avatarUrl）可通过 UserMapper.selectById() 获取

1. 读取 feature_list.json，找到 id 为 "F-IM-03" 的功能
2. 分析该功能需要涉及的所有文件：
   - entity/ChatMessage.java
   - mapper/ChatMessageMapper.java
   - resources/mapper/ChatMessageMapper.xml
   - service/ChatMessageService.java
   - service/impl/ChatMessageServiceImpl.java
   - websocket/handler/ChatMessageHandler.java（替换空实现）
   - websocket/handler/ReadMessageHandler.java（替换空实现）
   - websocket/handler/MessageDispatcher.java（更新路由）
   - websocket/protocol/ChatPayload.java
   - websocket/protocol/ReadPayload.java
   - vo/ChatMessageVO.java
   - controller/mini/MiniChatController.java（追加接口）
3. 在 tasksWeb.md 的 F-IM-03 部分规划详细的开发步骤，包括：

   **Step 1: 数据库建表**
   ```sql
   CREATE TABLE IF NOT EXISTS `chat_message` (
     `id`            bigint(20)    NOT NULL AUTO_INCREMENT COMMENT '主键',
     `session_key`   varchar(64)   NOT NULL COMMENT '会话标识',
     `sender_id`     bigint(20)    NOT NULL COMMENT '发送者ID',
     `receiver_id`   bigint(20)    NOT NULL COMMENT '接收者ID',
     `msg_type`      tinyint(4)    NOT NULL DEFAULT 1 COMMENT '消息类型 1-文本 2-商品卡片 3-订单卡片 4-系统提示 5-快捷回复',
     `content`       varchar(1000) NOT NULL COMMENT '消息内容',
     `product_id`    bigint(20)    DEFAULT NULL COMMENT '关联商品ID',
     `order_id`      bigint(20)    DEFAULT NULL COMMENT '关联订单ID',
     `is_read`       tinyint(1)    NOT NULL DEFAULT 0 COMMENT '是否已读',
     `create_time`   datetime      NOT NULL COMMENT '发送时间',
     PRIMARY KEY (`id`),
     KEY `idx_session_key` (`session_key`, `create_time`),
     KEY `idx_receiver_read` (`receiver_id`, `is_read`),
     KEY `idx_sender_id` (`sender_id`),
     KEY `idx_create_time` (`create_time`)
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';
   ```

   **Step 2: ChatMessage 实体类**
   - entity/ChatMessage.java
   - @TableName("chat_message") @Data @Builder @NoArgsConstructor @AllArgsConstructor
   - @TableId(type = IdType.AUTO) private Long id;
   - private String sessionKey, Long senderId, Long receiverId;
   - private Integer msgType;
   - private String content;
   - private Long productId, orderId;
   - private Integer isRead;
   - @TableField(fill = FieldFill.INSERT) private LocalDateTime createTime;
   ※ chat_message 没有 update_time（消息不修改）

   **Step 3: 消息协议 Payload**
   - websocket/protocol/ChatPayload.java
   - @Data：Long receiverId, Long productId, Integer msgType, String content
   - websocket/protocol/ReadPayload.java
   - @Data：String sessionKey

   **Step 4: ChatMessageVO**
   - vo/ChatMessageVO.java
   - @Data @Builder @NoArgsConstructor @AllArgsConstructor
   - 字段（对齐前端 detail.vue 的消息结构）：
     Long msgId;           // → 前端 item.id
     Long senderId;        // → 前端 item.from
     Long receiverId;
     Integer msgType;      // → 前端 item.type
     String content;       // → 前端 item.content
     Long productId;
     Long orderId;
     Boolean isRead;
     Boolean isSelf;       // senderId == currentUserId
     LocalDateTime createTime; // → 前端 item.time
   ※ 前端 detail.vue 用 from===selfId 判断自己/对方，后端提供 isSelf 和 senderId 双保险

   **Step 5: ChatMessageMapper**
   - mapper/ChatMessageMapper.java 继承 BaseMapper<ChatMessage>
   - XML中自定义方法：
     a. selectMessagesBySessionKey(@Param("sessionKey") String sessionKey, @Param("offset") int offset, @Param("limit") int limit):
        SELECT * FROM chat_message WHERE session_key = #{sessionKey} ORDER BY create_time DESC LIMIT #{offset}, #{limit}
     b. selectMessageCount(@Param("sessionKey") String sessionKey):
        SELECT COUNT(*) FROM chat_message WHERE session_key = #{sessionKey}
     c. markAsRead(@Param("receiverId") Long receiverId, @Param("sessionKey") String sessionKey):
        UPDATE chat_message SET is_read = 1 WHERE receiver_id = #{receiverId} AND session_key = #{sessionKey} AND is_read = 0

   **Step 6: ChatMessageService 接口**
   - service/ChatMessageService.java
   - Long saveAndPushMessage(Long senderId, ChatPayload payload);  // 返回 msgId
   - Map<String, Object> getMessageHistory(String sessionKey, Integer page, Integer pageSize);  // 返回 {total, records}
   - void markSessionRead(String sessionKey);  // 当前用户标记已读
   - void sendSystemMessage(String sessionKey, Long senderId, Long receiverId, Integer msgType, String content);  // 系统/卡片消息

   **Step 7: ChatMessageServiceImpl 核心实现**
   ※ 这是整个 IM 的核心方法，必须极其详细

   - @Service @Slf4j @RequiredArgsConstructor
   - 注入：ChatMessageMapper, ChatSessionService, WebSocketSessionManager, UserMapper, NotificationMapper(或NotificationService), StringRedisTemplate, ObjectMapper

   - **saveAndPushMessage(Long senderId, ChatPayload payload):**
     整体标注 @Transactional 仅用于数据库操作部分，WS推送在事务外

     a. 参数校验：
        - payload.getReceiverId() 不为空，否则抛 BusinessException("接收者不能为空")
        - payload.getContent() 不为空且 length ≤ 1000
        - payload.getMsgType() 在 1~5 范围内
        - senderId != payload.getReceiverId()，不能给自己发消息

     b. 生成 sessionKey：
        String sessionKey = SessionKeyUtil.buildSessionKey(senderId, payload.getReceiverId(), payload.getProductId())

     c. 确保会话存在：
        chatSessionService.ensureSessionExists(senderId, payload.getReceiverId(), payload.getProductId())

     d. 构建消息实体并入库：
        ChatMessage message = ChatMessage.builder()
          .sessionKey(sessionKey)
          .senderId(senderId)
          .receiverId(payload.getReceiverId())
          .msgType(payload.getMsgType())
          .content(payload.getContent())
          .productId(payload.getProductId())
          .isRead(0)
          .createTime(LocalDateTime.now())
          .build();
        chatMessageMapper.insert(message);

     e. 计算 lastMsg 摘要（content 前50字符）：
        String lastMsg = payload.getContent().length() > 50 ? payload.getContent().substring(0, 50) + "..." : payload.getContent();
        ※ 如果是卡片消息(msgType=2/3)，lastMsg 设为 "[商品卡片]" / "[订单信息]"

     f. 更新发送方的 chat_session：
        chatSessionService.updateSessionLastMsg(senderId, payload.getReceiverId(), payload.getProductId(), lastMsg, payload.getMsgType())
        ※ 发送方的 unread 不变

     g. 更新接收方的 chat_session：
        chatSessionService.updateSessionLastMsg(payload.getReceiverId(), senderId, payload.getProductId(), lastMsg, payload.getMsgType())
        chatSessionService.incrementUnread(payload.getReceiverId(), senderId, payload.getProductId())
        ※ 如果接收方的会话 is_deleted=1，ensureSessionExists 已处理恢复

     h. Redis 更新接收方总未读数：
        stringRedisTemplate.opsForValue().increment(RedisConstant.IM_UNREAD + payload.getReceiverId())

     i. 构建 S2C 推送消息体：
        // 查询发送者信息（考虑缓存或批量查询）
        User sender = userMapper.selectById(senderId);
        Map<String, Object> pushData = new HashMap<>();
        pushData.put("msgId", message.getId());
        pushData.put("sessionKey", sessionKey);
        pushData.put("senderId", senderId);
        pushData.put("senderName", sender.getNickName());
        pushData.put("senderAvatar", sender.getAvatarUrl());
        pushData.put("receiverId", payload.getReceiverId());
        pushData.put("productId", payload.getProductId());
        pushData.put("msgType", payload.getMsgType());
        pushData.put("content", payload.getContent());
        pushData.put("createTime", message.getCreateTime().toString());
        WebSocketMessage<Map<String, Object>> wsMsg = new WebSocketMessage<>("CHAT", pushData);

     j. WebSocket 推送（事务提交后）：
        boolean sent = sessionManager.sendToUser(payload.getReceiverId(), wsMsg);

     k. 离线通知：
        if (!sent) {
          // 接收方离线，写 notification 表
          Notification notification = Notification.builder()
            .userId(payload.getReceiverId())
            .type(2)  // 新消息提醒
            .title("新消息")
            .content(sender.getNickName() + "：" + lastMsg)
            .relatedId(payload.getProductId())
            .relatedType(1) // 商品
            .isRead(0)
            .category(1)  // 交易类
            .build();
          notificationMapper.insert(notification);
        }

     l. return message.getId();

   ※ 关于事务与推送分离的实现方式：
     方案A：将数据库操作提取为 private 方法加 @Transactional，推送逻辑在无事务的公开方法中调用
     方案B：使用 TransactionSynchronizationManager.registerSynchronization(afterCommit) 回调
     推荐方案A，更简单直观

   - **getMessageHistory(String sessionKey, Integer page, Integer pageSize):**
     a. Long currentUserId = UserContext.getCurrentUserId()
     b. 权限校验：SessionKeyUtil.isParticipant(sessionKey, currentUserId)，false 则抛 BusinessException("无权查看此会话")
     c. int offset = (page - 1) * pageSize;
     d. int total = chatMessageMapper.selectMessageCount(sessionKey);
     e. List<ChatMessage> messages = chatMessageMapper.selectMessagesBySessionKey(sessionKey, offset, pageSize);
     f. 转换为 ChatMessageVO 列表：
        List<ChatMessageVO> voList = messages.stream().map(msg -> ChatMessageVO.builder()
          .msgId(msg.getId())
          .senderId(msg.getSenderId())
          .receiverId(msg.getReceiverId())
          .msgType(msg.getMsgType())
          .content(msg.getContent())
          .productId(msg.getProductId())
          .orderId(msg.getOrderId())
          .isRead(msg.getIsRead() == 1)
          .isSelf(msg.getSenderId().equals(currentUserId))
          .createTime(msg.getCreateTime())
          .build()
        ).collect(Collectors.toList());
     g. return Map.of("total", total, "records", voList);
     ※ 前端 detail.vue 拿到后按 createTime 正序展示（后端按 DESC 分页以便取最新）

   - **markSessionRead(String sessionKey):**
     a. Long currentUserId = UserContext.getCurrentUserId()
     b. SessionKeyUtil.isParticipant(sessionKey, currentUserId) 校验
     c. chatMessageMapper.markAsRead(currentUserId, sessionKey)  // 批量更新 is_read=1
     d. chatSessionService.resetUnread(currentUserId, ...)  // chat_session.unread 置 0
     e. 重新计算 Redis 总未读数：
        int newTotal = chatSessionService.calculateUnreadTotal(currentUserId);
        stringRedisTemplate.opsForValue().set(RedisConstant.IM_UNREAD + currentUserId, String.valueOf(newTotal));
     f. 可选：向对方推送 READ_ACK（让对方知道消息已读）
        // 从 sessionKey 解析出对方 userId
        // sessionManager.sendToUser(peerId, new WebSocketMessage<>("READ_ACK", Map.of("sessionKey", sessionKey)))

   - **sendSystemMessage(...):**
     a. 构建 ChatMessage（senderId 参数传入，系统消息可以是 0 或操作人ID）
     b. 入库
     c. 更新双方 chat_session 的 last_msg
     d. 更新接收方 unread + Redis
     e. 尝试 WS 推送给接收方（甚至双方）

   **Step 8: ChatMessageHandler 实现（替换空实现）**
   - websocket/handler/ChatMessageHandler.java
   - @Component @Slf4j @RequiredArgsConstructor
   - 注入 ChatMessageService, ObjectMapper
   - handle(Long senderId, Object data):
     a. try {
          ChatPayload payload = objectMapper.convertValue(data, ChatPayload.class);
          chatMessageService.saveAndPushMessage(senderId, payload);
        } catch (BusinessException e) {
          log.warn("用户{}发送消息业务异常: {}", senderId, e.getMessage());
          // 回送错误提示给发送者
          sessionManager.sendToUser(senderId, new WebSocketMessage<>("SYSTEM", Map.of("error", e.getMessage())));
        } catch (Exception e) {
          log.error("用户{}发送消息异常", senderId, e);
        }
   ※ 异常必须全部捕获，绝不能往上抛

   **Step 9: ReadMessageHandler 实现（替换空实现）**
   - websocket/handler/ReadMessageHandler.java
   - @Component @Slf4j @RequiredArgsConstructor
   - 注入 ChatMessageService, ObjectMapper
   - handle(Long senderId, Object data):
     try {
       ReadPayload payload = objectMapper.convertValue(data, ReadPayload.class);
       // 注意：WS 中不能用 UserContext，需要手动传 userId
       chatMessageService.markSessionReadByUserId(senderId, payload.getSessionKey());
     } catch (Exception e) {
       log.error("用户{}标记已读异常", senderId, e);
     }
   ※ markSessionRead() 在 REST 中使用 UserContext，但 WS 调用需要额外提供一个接受 userId 参数的重载方法

   **Step 10: MessageDispatcher 更新**
   - 替换 F-IM-01 中的 TODO：
     case "CHAT" → chatMessageHandler.handle(senderId, message.getData())
     case "READ" → readMessageHandler.handle(senderId, message.getData())
   - 注入 ChatMessageHandler 和 ReadMessageHandler

   **Step 11: MiniChatController 追加接口**
   - 在已有的 MiniChatController.java 中追加：

     @GetMapping("/messages")
     public Result<Map<String, Object>> getMessages(
         @RequestParam String sessionKey,
         @RequestParam(defaultValue = "1") Integer page,
         @RequestParam(defaultValue = "30") Integer pageSize) {
       return Result.success(chatMessageService.getMessageHistory(sessionKey, page, pageSize));
     }

     @PostMapping("/read")
     public Result<Void> markRead(@RequestBody Map<String, String> params) {
       chatMessageService.markSessionRead(params.get("sessionKey"));
       return Result.success();
     }

   **Step 12: ChatMessageService 补充方法**
   - 因为 WS 中不能用 UserContext，需要增加一个方法：
     void markSessionReadByUserId(Long userId, String sessionKey);
   - 内部逻辑与 markSessionRead 相同，但 userId 由参数传入而非 UserContext 获取

   **Step 13: 测试用例**
   - ChatMessageServiceImplTest.java：
     a. 测试 saveAndPushMessage 正常发送 — 验证消息入库、会话更新、Redis更新
     b. 测试 saveAndPushMessage 接收方在线 — 验证 sendToUser 被调用
     c. 测试 saveAndPushMessage 接收方离线 — 验证 notification 表写入
     d. 测试 saveAndPushMessage 参数校验 — receiverId为空抛异常
     e. 测试 getMessageHistory 正常分页 — 验证返回 total 和 records
     f. 测试 getMessageHistory 越权访问 — 非会话参与方抛异常
     g. 测试 markSessionRead — 验证 is_read 更新、unread 置0、Redis 更新

4. 特别标注事务与推送分离的实现方式
5. 特别标注 WS 中不能用 UserContext 的问题和解决方案（markSessionReadByUserId 重载）
6. 明确 saveAndPushMessage 每一步的细节

规划完成后，更新 tasksWeb.md 中 F-IM-03 部分的状态为"已规划"，并告诉我你规划的内容摘要。
```

#### F-IM-03-B：Trae 开始编码

```
请开始你的执行者工作，实现 Feature F-IM-03：消息存储与查询。

前置条件：F-IM-01（WebSocket基础通信）和 F-IM-02（会话管理）已通过审查。

⚠️ 项目现有架构约束：
- Entity/Mapper/Controller 风格同前
- 分页响应格式：Map.of("total", xx, "records", list)
- 前端 detail.vue 消息字段：id/time/type/from/content
- 项目已有 Notification 实体（notification表）可直接使用
- REST 接口中用 UserContext.getCurrentUserId()
- WebSocket Handler 中不能用 UserContext，userId 从方法参数传入

1. 读取 tasksWeb.md，找到 F-IM-03 的任务步骤
2. 读取 feature_list.json 中 id="F-IM-03" 的 acceptance_criteria
3. 按步骤编写代码（Step 1 ~ Step 13）

4. 每完成一个 Step，在 tasksWeb.md 中追加 [执行者] 记录

5. 编写测试 ChatMessageServiceImplTest.java，覆盖 7 个场景

6. 运行测试：
   mvn test -Dtest=ChatMessageServiceImplTest
   保存到 run-folder/F-IM-03-消息存储与查询/test_output.log

7. 生成证据包 + .ready-for-review

8. 完成后将详细内容记录到tasksWeb.md中,并告诉我结果摘要

关键约束（违反则审查必驳回）：
- ❌ saveAndPushMessage 遗漏任何一步（入库/更新会话/Redis/推送/离线通知）
- ❌ 数据库操作和 WS 推送没有分离（推送在事务内）
- ❌ unread 更新使用先查后改
- ❌ getMessageHistory 没有权限校验
- ❌ WS Handler 中使用了 UserContext
- ❌ ChatMessageHandler 异常没有 try-catch（导致连接断开）
- ❌ MessageDispatcher 中 CHAT/READ 路由仍是 TODO
- ✅ 卡片消息(msgType=2/3)的 lastMsg 显示为 "[商品卡片]"/"[订单信息]"
- ✅ sendSystemMessage 可供 OrderService/ProductService 调用
- ✅ markSessionReadByUserId 重载方法供 WS ReadMessageHandler 使用
```

#### F-IM-03-C：Kiro 执行审查

```
执行者已提交 F-IM-03 消息存储与查询的审查请求。请执行完整的审查流程：

1. 读取 .ready-for-review 和 tasksWeb.md 中 [执行者] 记录
2. 逐一审查以下文件：

   **entity/ChatMessage.java**
   - 字段是否与 DDL 一致
   - @TableName 是否为 "chat_message"
   - create_time 是否有 @TableField(fill=FieldFill.INSERT)
   - 没有 update_time（消息不修改）

   **websocket/protocol/ChatPayload.java**
   - 字段：receiverId, productId, msgType, content

   **websocket/protocol/ReadPayload.java**
   - 字段：sessionKey

   **vo/ChatMessageVO.java**
   - 对齐前端 detail.vue：msgId(→id), senderId(→from), msgType(→type), content, createTime(→time)
   - 是否有 isSelf 字段

   **mapper/ChatMessageMapper.java + XML**
   - selectMessagesBySessionKey 分页 SQL 正确性
   - markAsRead 批量更新条件：receiver_id + session_key + is_read=0

   **service/impl/ChatMessageServiceImpl.java — 核心逐步审查**
   saveAndPushMessage:
   a. ❌ 参数校验是否完整（receiverId/content/msgType/不能给自己发）
   b. ❌ sessionKey 是否使用 SessionKeyUtil.buildSessionKey
   c. ❌ 是否调用 ensureSessionExists
   d. ❌ ChatMessage 入库是否设置所有字段
   e. ❌ lastMsg 截取是否 ≤50 字符
   f. ❌ 卡片消息 lastMsg 是否为 "[商品卡片]"/"[订单信息]"
   g. ❌ 发送方 chat_session 只更新 last_msg/last_time，不改 unread
   h. ❌ 接收方 chat_session 更新 last_msg/last_time 且 unread=unread+1
   i. ❌ Redis INCR im:unread:{receiverId}
   j. ❌ 推送消息体包含 senderName/senderAvatar
   k. ❌ WS 推送在事务提交后执行
   l. ❌ 离线时写 notification 表（type=2）

   getMessageHistory:
   a. ❌ 权限校验 isParticipant
   b. isSelf 正确填充
   c. 分页参数计算正确

   markSessionRead:
   a. 批量更新 is_read
   b. chat_session.unread 置 0
   c. Redis 重新计算（不是简单 DEL）

   markSessionReadByUserId:
   a. 是否存在此重载方法（供 WS ReadMessageHandler 用）
   b. 与 markSessionRead 逻辑一致，userId 由参数传入

   sendSystemMessage:
   a. 是否正确入库和推送

   **ChatMessageHandler.java**
   a. 是否替换了空实现
   b. data → ChatPayload 转换是否使用 objectMapper.convertValue
   c. try-catch 是否全包裹
   d. BusinessException 是否回送错误提示给发送者

   **ReadMessageHandler.java**
   a. 是否替换了空实现
   b. 是否调用 markSessionReadByUserId（不是 markSessionRead）
   c. ❌ 是否没有使用 UserContext

   **MessageDispatcher.java**
   a. CHAT/READ 路由是否指向真实 Handler（TODO 已替换）
   b. 是否注入了 ChatMessageHandler 和 ReadMessageHandler

   **MiniChatController.java**
   a. GET /mini/chat/messages 参数正确
   b. POST /mini/chat/read 参数正确
   c. 返回 Result.success()

3. 审查测试文件
4. 运行 mvn test 复跑
5. 在 tasksWeb.md 记录 [监督者] 审查结果
6. 通过或驳回

红线（任一不通过则驳回）：
- saveAndPushMessage 遗漏步骤
- 事务与推送未分离
- WS Handler 使用了 UserContext
- Handler 异常未 catch
- MessageDispatcher CHAT/READ 仍为 TODO
- getMessageHistory 无权限校验
```

#### F-IM-03-D：Trae 修正驳回（仅驳回时使用）

```
监督者已驳回 F-IM-03 消息存储与查询的审查。请：
1. 读取 tasksWeb.md 中监督者关于 F-IM-03 的 [监督者] 反馈
2. 逐一理解每个驳回问题（特别关注 saveAndPushMessage 的步骤完整性）
3. 按照反馈修正对应的代码文件
4. 重新运行 mvn test -Dtest=ChatMessageServiceImplTest
5. 更新 run-folder/F-IM-03-消息存储与查询/ 证据包
6. 重新创建 .ready-for-review 信号文件
7. 在 tasksWeb.md 中记录 [执行者] 修正内容和重试结果
```

---

### F-IM-04：业务卡片消息与系统集成

#### F-IM-04-A：Kiro 规划任务

```
请开始你的监督者工作，规划 Feature F-IM-04：业务卡片消息与系统集成。

前置条件：F-IM-01、F-IM-02、F-IM-03 已全部通过审查。

⚠️ 项目现有架构约束：
- Product 实体已有：id, title, price, images(JSON数组字符串), status 等字段
- TradeOrder 实体已有（如果实现了）：id, orderNo, price, status 等字段
- OrderServiceImpl / ProductServiceImpl 可能已实现也可能未实现
- 已有 MsgType 概念在 chat_message.msg_type 字段，但枚举类尚未创建

1. 读取 feature_list.json，找到 id 为 "F-IM-04" 的功能
2. 分析涉及的文件：
   - common/enums/MsgType.java
   - service/impl/ChatMessageServiceImpl.java（扩展方法）
   - service/impl/ChatSessionServiceImpl.java（补全TODO）
   - service/impl/OrderServiceImpl.java（视情况集成）
   - service/impl/ProductServiceImpl.java（视情况集成）
3. 在 tasksWeb.md 的 F-IM-04 部分规划详细的开发步骤，包括：

   **Step 1: MsgType 枚举**
   - common/enums/MsgType.java
   - 枚举值：
     TEXT(1, "文本消息"),
     PRODUCT_CARD(2, "商品卡片"),
     ORDER_CARD(3, "订单卡片"),
     SYSTEM_TIP(4, "系统提示"),
     QUICK_REPLY(5, "快捷回复")
   - 属性：int code, String desc
   - 提供 fromCode(int code) 静态方法
   - 提供 getLastMsgPreview() 方法：TEXT/QUICK_REPLY→null（由content截取），PRODUCT_CARD→"[商品卡片]"，ORDER_CARD→"[订单信息]"，SYSTEM_TIP→"[系统消息]"

   **Step 2: ChatMessageServiceImpl 扩展方法**

   - sendProductCard(String sessionKey, Long senderId, Long receiverId, Long productId):
     a. Product product = productMapper.selectById(productId)
     b. if (product == null) { log.warn("商品不存在: {}", productId); return; }
     c. 构建 content JSON：
        Map<String, Object> cardData = new HashMap<>();
        cardData.put("productId", product.getId());
        cardData.put("title", product.getTitle());
        cardData.put("price", product.getPrice());
        cardData.put("image", 从 product.getImages() JSON数组取首图);
        cardData.put("status", product.getStatus());
        String content = objectMapper.writeValueAsString(cardData);
     d. 调用内部方法保存消息（复用 saveAndPushMessage 的核心逻辑，或调用 sendSystemMessage）
     e. msgType = MsgType.PRODUCT_CARD.getCode()

   - sendOrderCard(String sessionKey, Long buyerId, Long sellerId, Long orderId):
     a. TradeOrder order = orderMapper.selectById(orderId)（如果 TradeOrder/OrderMapper 已存在）
     b. if (order == null) return;
     c. 构建 content JSON：
        orderId, orderNo, price, status, statusText(中文映射)
     d. 分别推送给买家和卖家（两个人都要收到订单卡片）
     e. msgType = MsgType.ORDER_CARD.getCode()

   - sendSystemTip(String sessionKey, Long triggerId, Long receiverId, String tipContent):
     a. 构建 ChatMessage，msgType = MsgType.SYSTEM_TIP.getCode()
     b. content = tipContent
     c. 入库 + 更新会话 + 推送
     d. triggerId 可以是卖家ID（修改价格时）或 0（系统自动）

   **Step 3: 补全 ChatSessionServiceImpl.createSession 的 TODO**
   - 找到 F-IM-02 中标注的 TODO [F-IM-04]
   - 替换为：
     ```java
     if (isNew && dto.getProductId() != null) {
       try {
         chatMessageService.sendProductCard(sessionKey, currentUserId, dto.getPeerId(), dto.getProductId());
       } catch (Exception e) {
         log.error("创建会话时发送商品卡片失败", e);
         // 不影响会话创建主流程
       }
     }
     ```
   ※ 注意循环依赖：ChatSessionService 注入 ChatMessageService，ChatMessageService 又注入 ChatSessionService
   ※ 解决方案：使用 @Lazy 注解，或将 sendProductCard 放在 Controller 层调用

   **Step 4: 更新 saveAndPushMessage 中的 lastMsg 逻辑**
   - 使用 MsgType 枚举：
     ```java
     MsgType type = MsgType.fromCode(payload.getMsgType());
     String lastMsg;
     if (type != null && type.getLastMsgPreview() != null) {
       lastMsg = type.getLastMsgPreview();  // "[商品卡片]" / "[订单信息]" / "[系统消息]"
     } else {
       lastMsg = content.length() > 50 ? content.substring(0, 50) + "..." : content;
     }
     ```

   **Step 5: OrderServiceImpl 集成（视实现情况）**
   - 如果 OrderServiceImpl.createOrder() 已实现：
     在订单创建成功后添加：
     ```java
     try {
       String sessionKey = SessionKeyUtil.buildSessionKey(buyerId, sellerId, productId);
       chatMessageService.sendOrderCard(sessionKey, buyerId, sellerId, order.getId());
     } catch (Exception e) {
       log.error("订单创建后发送订单卡片失败, orderId={}", order.getId(), e);
     }
     ```
   - 如果 OrderServiceImpl.cancelOrder() 已实现：
     在订单取消后添加：
     ```java
     try {
       String sessionKey = SessionKeyUtil.buildSessionKey(buyerId, sellerId, productId);
       chatMessageService.sendSystemTip(sessionKey, cancelUserId, otherUserId, "交易已取消，原因：" + cancelReason);
     } catch (Exception e) {
       log.error("订单取消后发送系统提示失败", e);
     }
     ```
   - 如果未实现，添加 TODO 注释：
     ```java
     // TODO [F-IM-04] 订单创建成功后，发送订单卡片到聊天会话
     // String sessionKey = SessionKeyUtil.buildSessionKey(buyerId, sellerId, productId);
     // chatMessageService.sendOrderCard(sessionKey, buyerId, sellerId, order.getId());
     ```

   **Step 6: ProductServiceImpl 集成（视实现情况）**
   - 如果 updatePrice() 已实现：
     价格修改成功后，查找该商品相关的活跃会话，发送系统提示
   - 如果未实现，添加 TODO 注释

   **Step 7: 测试用例**
   - ChatMessageServiceImplTest.java 追加：
     a. 测试 sendProductCard — 验证 JSON content 包含正确字段
     b. 测试 sendProductCard 商品不存在 — 不抛异常，只 log
     c. 测试 sendOrderCard — 验证双方都收到
     d. 测试 sendSystemTip — 验证入库和推送
   - ChatSessionServiceImplTest.java 追加：
     e. 测试 createSession 新建时调用 sendProductCard

4. 特别标注循环依赖的解决方案
5. 特别标注业务集成的防御性编程（try-catch，IM失败不影响主业务）
6. 标注哪些是视实现情况处理的

规划完成后，更新 tasksWeb.md 中 F-IM-04 部分的状态为"已规划"，并告诉我你规划的内容摘要。
```

#### F-IM-04-B：Trae 开始编码

```
请开始你的执行者工作，实现 Feature F-IM-04：业务卡片消息与系统集成。

前置条件：F-IM-01、F-IM-02、F-IM-03 已全部通过审查。

⚠️ 项目现有架构约束：
- Product 实体的 images 字段是 JSON 数组字符串（如 '["/a.jpg","/b.jpg"]'）
- 项目已有 ProductMapper 继承 BaseMapper<Product>
- 如果 OrderMapper / TradeOrder 实体已存在，直接使用；不存在则只写 TODO
- ObjectMapper 用于 JSON 序列化，不要手动拼 JSON 字符串
- 注意循环依赖问题（ChatSessionService ↔ ChatMessageService），用 @Lazy 解决

1. 读取 tasksWeb.md，找到 F-IM-04 的任务步骤
2. 读取 feature_list.json 中 id="F-IM-04" 的 acceptance_criteria
3. 按步骤编写代码：

   Step 1: common/enums/MsgType.java
   Step 2: 扩展 ChatMessageServiceImpl（sendProductCard / sendOrderCard / sendSystemTip）
   Step 3: 补全 ChatSessionServiceImpl.createSession 中的 TODO [F-IM-04]
   Step 4: 更新 saveAndPushMessage 中的 lastMsg 使用 MsgType 枚举
   Step 5: OrderServiceImpl 集成（视实现情况，已实现→添加代码，未实现→添加TODO）
   Step 6: ProductServiceImpl 集成（同上）

4. 每完成一个 Step，在 tasksWeb.md 中追加 [执行者] 记录

5. 运行全部 IM 相关测试：
   mvn test -Dtest="WebSocketSessionManagerTest,ChatSessionServiceImplTest,ChatMessageServiceImplTest"
   保存到 run-folder/F-IM-04-业务卡片消息/test_output.log

6. 生成证据包 + .ready-for-review

7. 完成后告诉我结果摘要

关键约束：
- ❌ JSON content 不能手动拼字符串（必须用 ObjectMapper）
- ❌ 商品不存在时不能抛异常（只 log.warn 并 return）
- ❌ 业务集成（Order/Product）IM调用失败不能影响主业务（必须 try-catch）
- ❌ F-IM-02 的 TODO [F-IM-04] 不能遗留
- ✅ Product.images 首图提取逻辑正确
- ✅ 循环依赖用 @Lazy 解决
- ✅ statusText 中文映射正确（1→待面交, 3→已完成, 4→已评价, 5→已取消）
```

#### F-IM-04-C：Kiro 执行审查

```
执行者已提交 F-IM-04 业务卡片消息与系统集成的审查请求。请执行完整的审查流程：

1. 读取 .ready-for-review 和 tasksWeb.md 中 [执行者] 记录
2. 逐一审查：

   **common/enums/MsgType.java**
   - 枚举值是否完整（1-5）
   - fromCode 方法是否正确
   - getLastMsgPreview 方法是否正确

   **ChatMessageServiceImpl 扩展方法**
   - sendProductCard：
     a. ❌ 是否查询了商品信息
     b. ❌ content JSON 是否使用 ObjectMapper 序列化（不是手动拼字符串）
     c. ❌ product.images 首图提取是否正确
     d. ❌ 商品不存在时是否只 log 不抛异常
   - sendOrderCard：
     a. ❌ content JSON 是否包含 orderId/orderNo/price/status/statusText
     b. ❌ statusText 中文映射是否正确
     c. ❌ 是否推送给双方（买家和卖家）
   - sendSystemTip：
     a. msgType 是否为 MsgType.SYSTEM_TIP.getCode()

   **saveAndPushMessage 中 lastMsg 逻辑**
   - ❌ 是否使用 MsgType.fromCode() 和 getLastMsgPreview()
   - ❌ 卡片消息 lastMsg 是否为 "[商品卡片]"/"[订单信息]"

   **ChatSessionServiceImpl.createSession**
   - ❌ 之前的 TODO [F-IM-04] 是否被替换为真实代码
   - ❌ sendProductCard 调用是否有 try-catch
   - 是否只在 isNew=true 且 productId!=null 时调用
   - 循环依赖是否用 @Lazy 解决

   **OrderServiceImpl / ProductServiceImpl**
   - 如果已实现：
     a. ❌ IM 调用是否有 try-catch
     b. IM 调用是否在主业务逻辑之后
   - 如果未实现：
     a. ❌ TODO 注释是否清晰（包含 [F-IM-04] 标签和完整示例代码）

3. 审查测试文件
4. 运行全部 IM 测试复跑
5. 在 tasksWeb.md 记录 [监督者] 审查结果
6. 通过或驳回

红线：
- JSON 手动拼字符串
- 商品不存在时抛异常
- 业务集成无 try-catch
- F-IM-02 的 TODO 未处理
- 循环依赖未解决（启动报错）
```

#### F-IM-04-D：Trae 修正驳回（仅驳回时使用）

```
监督者已驳回 F-IM-04 业务卡片消息与系统集成的审查。请：
1. 读取 tasksWeb.md 中监督者关于 F-IM-04 的 [监督者] 反馈
2. 逐一理解每个驳回问题
3. 按照反馈修正对应的代码文件
4. 重新运行全部 IM 测试
5. 更新 run-folder/F-IM-04-业务卡片消息/ 证据包
6. 重新创建 .ready-for-review 信号文件
7. 在 tasksWeb.md 中记录 [执行者] 修正内容和重试结果
```

---

## 四、完成后的最终集成验收

```
请执行 IM WebSocket 自建功能的最终集成验证：

1. 确认以下所有文件已创建且通过审查：
   □ config/WebSocketConfig.java
   □ websocket/ChatHandshakeInterceptor.java
   □ websocket/WebSocketServer.java（继承TextWebSocketHandler，不是@ServerEndpoint）
   □ websocket/WebSocketSessionManager.java（sendToUser有synchronized）
   □ websocket/handler/MessageDispatcher.java（CHAT/READ路由已实现，无TODO残留）
   □ websocket/handler/ChatMessageHandler.java（有完整try-catch）
   □ websocket/handler/ReadMessageHandler.java（使用markSessionReadByUserId，不用UserContext）
   □ websocket/handler/PingMessageHandler.java
   □ websocket/protocol/WebSocketMessage.java
   □ websocket/protocol/MessageType.java
   □ websocket/protocol/ChatPayload.java
   □ websocket/protocol/ReadPayload.java
   □ entity/ChatSession.java（@TableName + @TableId(type=IdType.AUTO)）
   □ entity/ChatMessage.java（同上）
   □ mapper/ChatSessionMapper.java + XML
   □ mapper/ChatMessageMapper.java + XML
   □ service/ChatSessionService.java
   □ service/impl/ChatSessionServiceImpl.java（createSession的TODO已补全）
   □ service/ChatMessageService.java（含markSessionReadByUserId重载）
   □ service/impl/ChatMessageServiceImpl.java（saveAndPushMessage完整实现）
   □ controller/mini/MiniChatController.java（返回Result.success）
   □ dto/ChatSessionCreateDTO.java
   □ vo/ChatSessionVO.java（字段覆盖前端list.vue绑定）
   □ vo/ChatMessageVO.java（字段覆盖前端detail.vue绑定，含isSelf）
   □ common/enums/MsgType.java
   □ common/util/SessionKeyUtil.java
   □ common/constant/RedisConstant.java（IM常量已追加，原有常量未被修改）
   □ task/WebSocketHeartbeatTask.java

2. 运行全部测试：
   mvn test -Dtest="WebSocketSessionManagerTest,ChatSessionServiceImplTest,ChatMessageServiceImplTest"

3. 验证关键架构约束：
   □ 整个websocket/包中没有使用UserContext（grep确认）
   □ 整个项目中没有@ServerEndpoint注解（grep确认）
   □ ChatHandshakeInterceptor中使用的是JwtUtil.parseToken()
   □ 所有Controller返回Result.success()/Result.error()
   □ 所有Entity使用@TableId(type=IdType.AUTO)

4. 检查SQL文件：chat_session和chat_message建表DDL存在于sql/目录

5. 检查pom.xml：spring-boot-starter-websocket依赖存在

6. 检查 tasksWeb.md：F-IM-01 到 F-IM-04 全部标记为"已通过审查"

7. 搜索残留TODO：
   grep -r "TODO \[F-IM-" --include="*.java" src/
   - F-IM-01/02/03 的 TODO 应全部清除
   - F-IM-04 中 OrderServiceImpl/ProductServiceImpl 的 TODO 允许存在（视实现进度）

8. 输出最终报告到 tasksWeb.md 底部：
   ## IM WebSocket 自建 - 最终验收报告
   - 完成时间：
   - 文件总数：
   - 新建文件数：
   - 修改文件数：
   - 测试用例数：
   - 测试通过率：
   - 架构约束验证：全部通过 / 有问题（列出）
   - 遗留事项：
     - OrderServiceImpl/ProductServiceImpl 集成TODO（待对应模块开发时补全）
     - 前端对接（待F-IM前端任务）
```

