# Feature F07：校园认证-管理端审核

### 任务规划

**[监督者] 2026-02-19 规划任务：**

该功能依赖 F06 已完成的 CampusAuth 和 College 实体、Mapper、Service。实现管理端的认证审核功能，包括分页查询、详情查询、通过和驳回操作。

#### 步骤 1：创建 AuthPageVO（vo/AuthPageVO.java）

**字段定义：**
```java
public class AuthPageVO {
    private Long id;                  // 认证记录ID
    private Long userId;              // 用户ID
    private String nickName;          // 用户昵称
    private String avatarUrl;         // 用户头像
    private Long collegeId;           // 学院ID
    private String collegeName;       // 学院名称
    private String studentNo;         // 学号
    private String className;         // 班级
    private String certImage;         // 认证材料图片URL
    private Integer status;           // 0-待审核 1-通过 2-驳回
    private String rejectReason;      // 驳回原因
    private LocalDateTime reviewTime; // 审核时间
    private LocalDateTime createTime; // 提交时间
}
```

**注意：**
- 该 VO 用于分页列表展示，包含用户基本信息和学院名称

#### 步骤 2：在 CampusAuthService 中新增方法

在 `service/CampusAuthService.java` 中新增：

```java
/**
 * 分页查询认证记录（管理端）
 * @param page 页码
 * @param size 每页大小
 * @param status 状态筛选（可选）
 * @param collegeId 学院ID筛选（可选）
 */
Page<AuthPageVO> pageAuth(Integer page, Integer size, Integer status, Long collegeId);

/**
 * 查询认证详情（管理端）
 * @param id 认证记录ID
 */
AuthPageVO getAuthDetail(Long id);

/**
 * 审核通过
 * @param id 认证记录ID
 */
void approveAuth(Long id);

/**
 * 审核驳回
 * @param id 认证记录ID
 * @param rejectReason 驳回原因
 */
void rejectAuth(Long id, String rejectReason);
```

#### 步骤 3：在 CampusAuthMapper 中新增方法

在 `mapper/CampusAuthMapper.java` 中新增：

```java
/**
 * 分页查询认证记录（关联用户和学院信息）
 * 使用 MyBatis XML 实现多表关联查询
 */
Page<AuthPageVO> pageAuthWithDetails(Page<AuthPageVO> page, 
                                     @Param("status") Integer status, 
                                     @Param("collegeId") Long collegeId);
```

**注意：**
- 该方法需要在 `resources/mapper/CampusAuthMapper.xml` 中实现
- 使用 LEFT JOIN 关联 user 表和 college 表

#### 步骤 4：创建 CampusAuthMapper.xml

在 `src/main/resources/mapper/CampusAuthMapper.xml` 中实现分页查询：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" 
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.qingyuan.secondhand.mapper.CampusAuthMapper">

    <select id="pageAuthWithDetails" resultType="com.qingyuan.secondhand.vo.AuthPageVO">
        SELECT 
            ca.id,
            ca.user_id AS userId,
            u.nick_name AS nickName,
            u.avatar_url AS avatarUrl,
            ca.college_id AS collegeId,
            c.name AS collegeName,
            ca.student_no AS studentNo,
            ca.class_name AS className,
            ca.cert_image AS certImage,
            ca.status,
            ca.reject_reason AS rejectReason,
            ca.review_time AS reviewTime,
            ca.create_time AS createTime
        FROM campus_auth ca
        LEFT JOIN user u ON ca.user_id = u.id
        LEFT JOIN college c ON ca.college_id = c.id
        <where>
            <if test="status != null">
                AND ca.status = #{status}
            </if>
            <if test="collegeId != null">
                AND ca.college_id = #{collegeId}
            </if>
        </where>
        ORDER BY ca.create_time DESC
    </select>

</mapper>
```

**注意：**
- 使用 `<where>` 和 `<if>` 标签实现动态 SQL
- 使用 `#{}` 参数占位符（而非 `${}`）
- 按提交时间倒序排序

#### 步骤 5：在 CampusAuthServiceImpl 中实现新增方法

**pageAuth 方法逻辑：**
1. 创建 MyBatis-Plus 的 Page 对象
2. 调用 Mapper 的 pageAuthWithDetails 方法
3. 返回分页结果

**getAuthDetail 方法逻辑：**
1. 调用 pageAuthWithDetails 查询单条记录（通过 id 筛选）
2. 如果记录不存在，抛出 BusinessException："认证记录不存在"
3. 返回 AuthPageVO

**approveAuth 方法逻辑：**
1. 查询认证记录，校验存在性
2. 校验当前状态是否为待审核（status=0），否则抛出异常："该认证已审核"
3. 使用 @Transactional 保证事务性
4. 更新认证记录：
   - status = 1（通过）
   - reviewTime = 当前时间
   - reviewerId = 当前管理员ID（从 UserContext 获取）
5. 更新用户表：auth_status = 2（已认证）
6. **预留通知调用**（注释掉）：
   ```java
   // TODO: 发送站内通知（F19 通知模块完成后启用）
   // notificationService.send(auth.getUserId(), NotificationType.AUTH_APPROVED, 
   //     "您的校园认证已通过", "", null, null, NotificationCategory.SYSTEM);
   ```

**rejectAuth 方法逻辑：**
1. 查询认证记录，校验存在性
2. 校验当前状态是否为待审核（status=0），否则抛出异常："该认证已审核"
3. 校验 rejectReason 非空
4. 使用 @Transactional 保证事务性
5. 更新认证记录：
   - status = 2（驳回）
   - rejectReason = 驳回原因
   - reviewTime = 当前时间
   - reviewerId = 当前管理员ID
6. 更新用户表：auth_status = 3（已驳回）
7. **预留通知调用**（注释掉）：
   ```java
   // TODO: 发送站内通知（F19 通知模块完成后启用）
   // notificationService.send(auth.getUserId(), NotificationType.AUTH_REJECTED, 
   //     "您的校园认证被驳回", rejectReason, null, null, NotificationCategory.SYSTEM);
   ```

**注意：**
- approveAuth 和 rejectAuth 都使用 @Transactional 保证事务性
- 通知调用先注释掉，等 F19 通知模块完成后再启用

#### 步骤 6：创建 AdminAuthController（controller/admin/AdminAuthController.java）

**接口定义：**
```java
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {
    private final CampusAuthService campusAuthService;
    
    /**
     * 分页查询认证记录
     * GET /admin/auth/page?page=1&size=10&status=0&collegeId=1
     */
    @GetMapping("/page")
    public Result<Page<AuthPageVO>> pageAuth(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long collegeId) {
        Page<AuthPageVO> result = campusAuthService.pageAuth(page, size, status, collegeId);
        return Result.success(result);
    }
    
    /**
     * 查询认证详情
     * GET /admin/auth/detail/{id}
     */
    @GetMapping("/detail/{id}")
    public Result<AuthPageVO> getAuthDetail(@PathVariable Long id) {
        AuthPageVO vo = campusAuthService.getAuthDetail(id);
        return Result.success(vo);
    }
    
    /**
     * 审核通过
     * POST /admin/auth/approve
     * Body: {"id": 1}
     */
    @PostMapping("/approve")
    public Result<Void> approveAuth(@RequestBody Map<String, Long> params) {
        Long id = params.get("id");
        if (id == null) {
            throw new BusinessException("认证记录ID不能为空");
        }
        campusAuthService.approveAuth(id);
        return Result.success();
    }
    
    /**
     * 审核驳回
     * POST /admin/auth/reject
     * Body: {"id": 1, "rejectReason": "材料不清晰"}
     */
    @PostMapping("/reject")
    public Result<Void> rejectAuth(@RequestBody Map<String, Object> params) {
        Long id = params.get("id") == null ? null : Long.valueOf(params.get("id").toString());
        String rejectReason = (String) params.get("rejectReason");
        
        if (id == null) {
            throw new BusinessException("认证记录ID不能为空");
        }
        if (rejectReason == null || rejectReason.trim().isEmpty()) {
            throw new BusinessException("驳回原因不能为空");
        }
        
        campusAuthService.rejectAuth(id, rejectReason);
        return Result.success();
    }
}
```

**注意：**
- 使用 @RequestParam 接收查询参数，支持可选筛选
- 使用 @RequestBody Map 接收 JSON 参数（简化 DTO 定义）
- 参数校验在 Controller 层完成

#### 步骤 7：编写单元测试（test/.../service/impl/CampusAuthServiceImplTest.java）

在已有的 CampusAuthServiceImplTest 中追加测试方法：

**测试场景：**

1. **testPageAuth_NoFilter** - 分页查询无筛选
   - Mock pageAuthWithDetails 返回分页数据
   - 验证返回的 Page 对象正确

2. **testPageAuth_WithStatusFilter** - 分页查询按状态筛选
   - Mock pageAuthWithDetails 返回 status=0 的数据
   - 验证筛选参数正确传递

3. **testPageAuth_WithCollegeIdFilter** - 分页查询按学院筛选
   - Mock pageAuthWithDetails 返回指定学院的数据
   - 验证筛选参数正确传递

4. **testGetAuthDetail_Success** - 查询认证详情成功
   - Mock pageAuthWithDetails 返回单条记录
   - 验证返回的 AuthPageVO 字段正确

5. **testGetAuthDetail_NotFound** - 认证记录不存在
   - Mock pageAuthWithDetails 返回空结果
   - 断言抛出 BusinessException："认证记录不存在"

6. **testApproveAuth_Success** - 审核通过成功
   - Mock selectById 返回 status=0 的认证记录
   - Mock updateById 返回 1
   - 验证认证记录更新：status=1、reviewTime 不为空、reviewerId 正确
   - 验证用户 auth_status 更新为 2

7. **testApproveAuth_AlreadyReviewed** - 已审核的记录不能再次审核
   - Mock selectById 返回 status=1 的认证记录
   - 断言抛出 BusinessException："该认证已审核"

8. **testRejectAuth_Success** - 审核驳回成功
   - Mock selectById 返回 status=0 的认证记录
   - Mock updateById 返回 1
   - 验证认证记录更新：status=2、rejectReason 正确、reviewTime 不为空
   - 验证用户 auth_status 更新为 3

9. **testRejectAuth_AlreadyReviewed** - 已审核的记录不能再次驳回
   - Mock selectById 返回 status=2 的认证记录
   - 断言抛出 BusinessException："该认证已审核"

10. **testRejectAuth_EmptyReason** - 驳回原因为空
    - 断言抛出 BusinessException："驳回原因不能为空"

#### 步骤 8：生成证据包

**操作：**
1. 在终端运行：`mvn test -Dtest=CampusAuthServiceImplTest`
2. 将输出保存到：`run-folder/F07-校园认证-管理端审核/test_output.log`
3. 创建 `run-folder/F07-校园认证-管理端审核/run.sh`，内容：
   ```bash
   #!/bin/bash
   mvn test -Dtest=CampusAuthServiceImplTest
   ```
4. 复制本任务规划到：`run-folder/F07-校园认证-管理端审核/task.md`

#### 步骤 9：创建审查信号文件

创建 `.ready-for-review` 文件，内容：
```
Feature: F07 校园认证-管理端审核
Status: 待审查
Timestamp: [当前时间]
```

---

### 关键业务规则

1. **分页查询**：
   - 使用 MyBatis XML 实现多表关联查询（campus_auth LEFT JOIN user LEFT JOIN college）
   - 支持按 status 和 collegeId 动态筛选
   - 按 create_time 倒序排序

2. **审核权限**：
   - 只有待审核（status=0）的记录才能审核
   - 已审核的记录（status=1 或 2）不能再次审核

3. **事务保证**：
   - approveAuth 和 rejectAuth 使用 @Transactional 保证更新认证记录和用户表的原子性
