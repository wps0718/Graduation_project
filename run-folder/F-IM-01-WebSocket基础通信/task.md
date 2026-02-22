# F-IM-01：WebSocket基础通信

## 目标
- 建立 WebSocket 基础通信能力
- 完成 JWT 握手鉴权、在线连接管理、心跳保活与超时清理
- 提供消息协议与分发框架，占位后续消息处理

## 开发步骤
1. pom.xml 依赖确认（spring-boot-starter-websocket）
2. RedisConstant 追加 IM 常量
3. MessageType 枚举
4. WebSocketMessage 协议类
5. ChatHandshakeInterceptor 握手鉴权
6. WebSocketSessionManager 会话管理
7. WebSocketServer 入口处理器
8. MessageDispatcher 消息分发
9. PingMessageHandler 心跳处理
10. ChatMessageHandler / ReadMessageHandler 空实现占位
11. WebSocketHeartbeatTask 心跳超时检测
12. WebSocketConfig 端点配置
13. WebSocketSessionManagerTest 单元测试

## 验收要点
- /ws/chat 端点可连接，JWT 校验通过才允许握手
- 用户状态校验：封禁/注销中拒绝连接
- 心跳 PING/PONG 正常工作，60 秒未心跳判定断线
- 重复连接踢旧连接并推送 FORCE_OFFLINE
