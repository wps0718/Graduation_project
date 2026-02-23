# [监督者] F-IM-04 业务卡片消息与系统集成 - 任务规划

**规划时间**：2026-02-23 01:00
**前置条件**：F-IM-01、F-IM-02、F-IM-03 已全部通过审查

## 功能概述

实现商品卡片消息、订单卡片消息、系统提示消息、快捷回复消息，并与订单创建/价格修改/订单取消等业务模块集成。

## 架构约束分析

### 现有实体类字段
- **Product**: id, title, price, images(JSON数组字符串), status 等
- **TradeOrder**: id, orderNo, price, status 等
- **ChatMessage**: msg_type 字段已存在，但 MsgType 枚举类尚未创建

### 循环依赖风险
- ChatSessionService 需要调用 ChatMessageService.sendProductCard
- ChatMessageService 已注入 ChatSessionService
- 解决方案：使用 @Lazy 注解延迟加载

### 业务集成原则
- IM 调用失败不影响主业务流程（防御性编程）
- 所有 IM 调用使用 try-catch 包裹
- OrderServiceImpl / ProductServiceImpl 可能已实现也可能未实现

## 详细开发步骤

### Step 1: 创建 MsgType 枚举类

**文件**: `src/main/java/com/qingyuan/secondhand/common/enums/MsgType.java`

**要求**:

1. 枚举值定义：
   - TEXT(1, "文本消息")
   - PRODUCT_CARD(2, "商品卡片")
   - ORDER_CARD(3, "订单卡片")
   - SYSTEM_TIP(4, "系统提示")
   - QUICK_REPLY(5, "快捷回复")

2. 属性：
   - private final int code
   - private final String desc

3. 方法：
   - 构造函数：MsgType(int code, String desc)
   - public int getCode()
   - public String getDesc()
   - public static MsgType fromCode(int code) — 根据 code 返回枚举，找不到返回 null
   - public String getLastMsgPreview() — 返回会话列表中的预览文本：
     * TEXT/QUICK_REPLY → null（由 content 截取）
     * PRODUCT_CARD → "[商品卡片]"
     * ORDER_CARD → "[订单信息]"
     * SYSTEM_TIP → "[系统消息]"

**测试要求**:
- 测试 fromCode 方法（有效 code 和无效 code）
- 测试 getLastMsgPreview 方法（各种消息类型）

---

### Step 2: ChatMessageServiceImpl 扩展卡片方法

**文件**: `src/main/java/com/qingyuan/secondhand/service/impl/ChatMessageServiceImpl.java`


**新增方法 1: sendProductCard**

```java
public void sendProductCard(String sessionKey, Long senderId, Long receiverId, Long productId)
```

**实现逻辑**:
1. 参数校验：sessionKey、senderId、receiverId、productId 不能为空
2. 查询商品：Product product = productMapper.selectById(productId)
3. 商品不存在时：log.warn("商品不存在: {}", productId); return; （不抛异常）
4. 构建 content JSON：
   ```java
   Map<String, Object> cardData = new HashMap<>();
   cardData.put("productId", product.getId());
   cardData.put("title", product.getTitle());
   cardData.put("price", product.getPrice());
   // 从 product.getImages() JSON数组取首图
   String image = parseFirstImage(product.getImages());
   cardData.put("image", image);
   cardData.put("status", product.getStatus());
   String content = objectMapper.writeValueAsString(cardData);
   ```
5. 构建 ChatMessage 对象，msgType = MsgType.PRODUCT_CARD.getCode()
6. 入库、更新会话、推送（复用现有逻辑或调用 sendSystemMessage）

**新增方法 2: sendOrderCard**

```java
public void sendOrderCard(String sessionKey, Long buyerId, Long sellerId, Long orderId)
```


**实现逻辑**:
1. 参数校验：sessionKey、buyerId、sellerId、orderId 不能为空
2. 查询订单：TradeOrder order = tradeOrderMapper.selectById(orderId)
3. 订单不存在时：log.warn("订单不存在: {}", orderId); return;
4. 构建 content JSON：
   ```java
   Map<String, Object> cardData = new HashMap<>();
   cardData.put("orderId", order.getId());
   cardData.put("orderNo", order.getOrderNo());
   cardData.put("price", order.getPrice());
   cardData.put("status", order.getStatus());
   cardData.put("statusText", getOrderStatusText(order.getStatus()));
   String content = objectMapper.writeValueAsString(cardData);
   ```
5. msgType = MsgType.ORDER_CARD.getCode()
6. 分别发送给买家和卖家（两条消息）：
   - 买家消息：senderId=sellerId, receiverId=buyerId
   - 卖家消息：senderId=buyerId, receiverId=sellerId

**新增方法 3: sendSystemTip**

```java
public void sendSystemTip(String sessionKey, Long triggerId, Long receiverId, String tipContent)
```

**实现逻辑**:
1. 参数校验：sessionKey、receiverId、tipContent 不能为空
2. triggerId 可以为 null（系统自动触发时）
3. 构建 ChatMessage，msgType = MsgType.SYSTEM_TIP.getCode()
4. content = tipContent
5. senderId = triggerId != null ? triggerId : 0L
6. 入库、更新会话、推送


**辅助方法**:
- `parseFirstImage(String imagesJson)` — 从 JSON 数组字符串解析首图
- `getOrderStatusText(Integer status)` — 订单状态码转中文文本

**注意事项**:
- 需要注入 ProductMapper 和 TradeOrderMapper
- 需要注入 ObjectMapper
- 所有方法使用 @Transactional 注解

**测试要求**:
- 测试 sendProductCard 正常场景（验证 JSON content 包含正确字段）
- 测试 sendProductCard 商品不存在场景（不抛异常，只 log）
- 测试 sendOrderCard 正常场景（验证买家和卖家都收到）
- 测试 sendSystemTip 正常场景

---

### Step 3: 更新 saveAndPushMessage 中的 lastMsg 逻辑

**文件**: `src/main/java/com/qingyuan/secondhand/service/impl/ChatMessageServiceImpl.java`

**修改位置**: saveAndPushMessage 方法中计算 lastMsg 的部分

**原逻辑**:
```java
String lastMsg;
if (payload.getMsgType() == 2) {
    lastMsg = "[商品卡片]";
} else if (payload.getMsgType() == 3) {
    lastMsg = "[订单信息]";
} else {
    lastMsg = content.length() > 50 ? content.substring(0, 50) + "..." : content;
}
```


**新逻辑**:
```java
MsgType type = MsgType.fromCode(payload.getMsgType());
String lastMsg;
if (type != null && type.getLastMsgPreview() != null) {
    lastMsg = type.getLastMsgPreview();  // "[商品卡片]" / "[订单信息]" / "[系统消息]"
} else {
    // TEXT / QUICK_REPLY 或未知类型，使用 content 截取
    lastMsg = content.length() > 50 ? content.substring(0, 50) + "..." : content;
}
```

**优点**:
- 使用枚举统一管理消息类型预览文本
- 易于扩展新的消息类型
- 代码更清晰

---

### Step 4: 补全 ChatSessionServiceImpl.createSession 的商品卡片发送

**文件**: `src/main/java/com/qingyuan/secondhand/service/impl/ChatSessionServiceImpl.java`

**修改位置**: createSession 方法中，创建新会话后

**当前代码**:
```java
if (session == null) {
    createDualSessions(userId, dto.getPeerId(), dto.getProductId());
    isNew = true;
}
```

**新增代码**:
```java
if (session == null) {
    createDualSessions(userId, dto.getPeerId(), dto.getProductId());
    isNew = true;
    
    // 新会话自动发送商品卡片
    if (dto.getProductId() != null) {
        try {
            String sessionKey = SessionKeyUtil.buildSessionKey(userId, dto.getPeerId(), dto.getProductId());
            chatMessageService.sendProductCard(sessionKey, userId, dto.getPeerId(), dto.getProductId());
        } catch (Exception e) {
            log.error("创建会话时发送商品卡片失败", e);
            // 不影响会话创建主流程
        }
    }
}
```


**循环依赖解决方案**:

ChatSessionServiceImpl 已经注入了 ChatMessageService，但需要确保不会造成循环依赖。

**方案 1（推荐）**: 使用 @Lazy 注解
```java
@Service
@RequiredArgsConstructor
public class ChatSessionServiceImpl implements ChatSessionService {
    
    @Lazy
    private final ChatMessageService chatMessageService;
    // ...
}
```

**方案 2**: 将 sendProductCard 调用移到 Controller 层
```java
// MiniChatController.createSession
ChatSessionVO vo = chatSessionService.createSession(dto);
if (vo.getIsNew() && dto.getProductId() != null) {
    try {
        chatMessageService.sendProductCard(vo.getSessionKey(), userId, dto.getPeerId(), dto.getProductId());
    } catch (Exception e) {
        log.error("发送商品卡片失败", e);
    }
}
```

**推荐使用方案 1**，因为业务逻辑应该在 Service 层完成。

**测试要求**:
- 测试 createSession 新建会话时自动发送商品卡片
- 测试 createSession 新建会话但 productId 为 null 时不发送卡片
- 测试 sendProductCard 失败不影响会话创建

---

### Step 5: OrderServiceImpl 集成（视实现情况）

**文件**: `src/main/java/com/qingyuan/secondhand/service/impl/TradeOrderServiceImpl.java`


**集成点 1: 订单创建成功后发送订单卡片**

**方法**: createOrder

**集成代码**:
```java
// 订单创建成功后
try {
    String sessionKey = SessionKeyUtil.buildSessionKey(buyerId, sellerId, productId);
    chatMessageService.sendOrderCard(sessionKey, buyerId, sellerId, order.getId());
} catch (Exception e) {
    log.error("订单创建后发送订单卡片失败, orderId={}", order.getId(), e);
    // 不影响订单创建主流程
}
```

**集成点 2: 订单取消后发送系统提示**

**方法**: cancelOrder

**集成代码**:
```java
// 订单取消成功后
try {
    String sessionKey = SessionKeyUtil.buildSessionKey(buyerId, sellerId, productId);
    Long otherUserId = cancelUserId.equals(buyerId) ? sellerId : buyerId;
    String tipContent = "交易已取消";
    if (StringUtils.hasText(cancelReason)) {
        tipContent += "，原因：" + cancelReason;
    }
    chatMessageService.sendSystemTip(sessionKey, cancelUserId, otherUserId, tipContent);
} catch (Exception e) {
    log.error("订单取消后发送系统提示失败, orderId={}", orderId, e);
}
```

**如果 TradeOrderServiceImpl 未实现或方法不存在**:

添加 TODO 注释：
```java
// TODO [F-IM-04] 订单创建成功后，发送订单卡片到聊天会话
// String sessionKey = SessionKeyUtil.buildSessionKey(buyerId, sellerId, productId);
// chatMessageService.sendOrderCard(sessionKey, buyerId, sellerId, order.getId());

// TODO [F-IM-04] 订单取消后，发送系统提示到聊天会话
// String sessionKey = SessionKeyUtil.buildSessionKey(buyerId, sellerId, productId);
// Long otherUserId = cancelUserId.equals(buyerId) ? sellerId : buyerId;
// chatMessageService.sendSystemTip(sessionKey, cancelUserId, otherUserId, "交易已取消，原因：" + cancelReason);
```
