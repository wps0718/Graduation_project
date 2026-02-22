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
