# 2026-04-06 开发记录

## 模块：小程序（miniapp）- 校园认证

- 校园认证表单新增字段：姓名（realName），提交字段与后端保持一致（realName / studentNo / className / certImage）。
- 修复提交失败：前端字段名与后端 DTO 不一致导致校验失败的问题，并透出后端返回 msg 便于定位。
- 修复认证状态展示：对齐 status / rejectReason 字段，避免状态与原因显示异常。
- 新增功能：认证历史入口、认证历史列表页、认证历史详情页、认证内容对比页。
- 支持审核中/已认证状态下修改资料：状态卡片提供“修改资料”，回填并重新提交后进入待审核。
- 优化认证历史列表：最新记录置顶，其余记录增加“已失效”标签并弱化展示。
- 修复个人中心认证状态不一致：个人中心 onShow 额外请求 /mini/auth/status 校正展示。

## 模块：后端（src）- 校园认证与用户信息

- 数据结构新增：campus_auth 表新增 real_name 字段；新增 campus_auth_history 表用于记录每次提交快照（含审核结果与驳回原因）。
- 校园认证提交：每次提交写入/更新 campus_auth，并追加一条 campus_auth_history（status=0）。
- 审核通过/驳回：更新 campus_auth，同时同步更新“最新一条”历史记录（以 history.id 倒序定位）。
- 新增接口：
  - GET /mini/auth/history、GET /mini/auth/history/{id}
  - GET /admin/auth/history/{authId}
- 修复一致性：查询与排序统一按 id 倒序，避免 create_time 异常导致“最新记录”错位。
- 修复个人中心状态滞后：
  - 提交/通过/驳回时清理 Redis：user:info:{userId}、user:stats:{userId}
  - /mini/user/info 在缓存命中时仍实时以 campus_auth.status 映射覆盖 authStatus（保证与认证页一致）。

## 模块：管理后台（admin）- 认证审核

- 新增认证审核页面：支持列表筛选、详情弹窗展示。
- 认证详情弹窗新增“历史时间线”：点击时间线节点切换右侧详情，便于审核老师查看全提交轨迹。

## 数据库（sql）

- init.sql：补充 campus_auth.real_name 与 campus_auth_history 表结构。
- 增量脚本：sql/update/2026-04-06_f06_auth_real_name.sql 增加对应变更。

## 测试与验证

- 后端：mvn test 全量通过。
- 小程序：现有 node --test 用例通过。

