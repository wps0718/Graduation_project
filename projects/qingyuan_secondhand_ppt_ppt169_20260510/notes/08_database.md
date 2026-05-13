**标题**: 数据库设计与核心表结构

数据库共22张表（业务表14张 + 配置表8张），286个字段，45个组合索引。

ER关系核心设计：
- user表是中心节点，关联product（1:N）、trade_order（1:N）、campus_auth（1:1）
- trade_order关联review（1:2，买卖双方各一条评价）
- IM系统包含chat_session和chat_message两张表

核心表亮点：
- user表：15字段，包含综合评分score、认证状态auth_status
- trade_order表：17字段，设计了seller_confirmed和buyer_confirmed双向确认字段
- campus_auth：主表+auth_history历史表，支持全轨迹回溯和版本对比

当前数据：用户5247、商品1839、订单892、消息12345。

**时间控制**: 1分30秒