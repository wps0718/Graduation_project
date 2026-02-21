# 轻院二手 - 开发任务记录

## 项目信息
- 项目名称：二手交易平台（毕业设计）
- 项目路径：G:\Code\Graduation_project
- 根包名：com.qingyuan.secondhand
- 技术栈：Spring Boot 3.x + MyBatis-Plus + MySQL 5.7 + Redis
- 构建工具：Maven

---

## Feature F09：校区与面交地点模块

### 任务规划

**[监督者] 2026-02-19 规划任务：**

该功能是独立模块，无前置依赖。实现校区和面交地点的管理，小程序端查询带 Redis 缓存，管理端支持完整 CRUD 操作。

#### 步骤 1：创建 Campus 实体类（entity/Campus.java）

**字段定义（参考数据库 campus 表）：**
- id (Long) - 主键，@TableId(type = IdType.AUTO)
- name (String) - 校区名称
- address (String) - 校区地址
- sort (Integer) - 排序号，默认0
- status (Integer) - 状态 0-禁用 1-启用，默认1
- createTime (LocalDateTime) - 创建时间，@TableField(fill = FieldFill.INSERT)
- updateTime (LocalDateTime) - 更新时间，@TableField(fill = FieldFill.INSERT_UPDATE)

**MyBatis-Plus 注解要求：**
- 类上添加 @TableName("campus")
- id 字段添加 @TableId(type = IdType.AUTO)
- createTime 添加 @TableField(fill = FieldFill.INSERT)
- updateTime 添加 @TableField(fill = FieldFill.INSERT_UPDATE)

#### 步骤 2：创建 MeetingPoint 实体类（entity/MeetingPoint.java）

**字段定义（参考数据库 meeting_point 表）：**
- id (Long) - 主键，@TableId(type = IdType.AUTO)
- campusId (Long) - 所属校区ID
- name (String) - 面交地点名称
- description (String) - 地点描述（可选）
- sort (Integer) - 排序号，默认0
- status (Integer) - 状态 0-禁用 1-启用，默认1
- createTime (LocalDateTime) - 创建时间，@TableField(fill = FieldFill.INSERT)
- updateTime (LocalDateTime) - 更新时间，@TableField(fill = FieldFill.INSERT_UPDATE)

**MyBatis-Plus 注解要求：**
- 类上添加 @TableName("meeting_point")
- id 字段添加 @TableId(type = IdType.AUTO)
- createTime 添加 @TableField(fill = FieldFill.INSERT)
- updateTime 添加 @TableField(fill = FieldFill.INSERT_UPDATE)

#### 步骤 3：创建 CampusMapper 接口（mapper/CampusMapper.java）

**要求：**
- 继承 BaseMapper<Campus>
- 无需额外方法（使用 MyBatis-Plus 内置方法）

#### 步骤 4：创建 MeetingPointMapper 接口（mapper/MeetingPointMapper.java）

**要求：**
- 继承 BaseMapper<MeetingPoint>
- 无需额外方法（使用 MyBatis-Plus 内置方法）

#### 步骤 5：创建 DTO 和 VO

**CampusDTO.java（dto/CampusDTO.java）：**
- id (Long) - 校区ID（更新时必需）
- name (String) - 校区名称，@NotBlank，@Size(max = 64)
- address (String) - 校区地址，@Size(max = 255)
- sort (Integer) - 排序号，@NotNull
- status (Integer) - 状态，@NotNull

**MeetingPointDTO.java（dto/MeetingPointDTO.java）：**
- id (Long) - 面交地点ID（更新时必需）
- campusId (Long) - 所属校区ID，@NotNull
- name (String) - 面交地点名称，@NotBlank，@Size(max = 64)
- description (String) - 地点描述，@Size(max = 255)
- sort (Integer) - 排序号，@NotNull
- status (Integer) - 状态，@NotNull

**CampusVO.java（vo/CampusVO.java）：**
- id (Long) - 校区ID
- name (String) - 校区名称
- address (String) - 校区地址
- sort (Integer) - 排序号
- status (Integer) - 状态

**MeetingPointVO.java（vo/MeetingPointVO.java）：**
- id (Long) - 面交地点ID
- campusId (Long) - 所属校区ID
- name (String) - 面交地点名称
- description (String) - 地点描述
- sort (Integer) - 排序号
- status (Integer) - 状态

#### 步骤 6：创建 CampusService 接口和实现类

**CampusService.java（service/CampusService.java）：**
- 继承 IService<Campus>
- 添加方法：
  - List<CampusVO> getMiniList() - 小程序端校区列表（公开接口，Redis 缓存）
  - List<CampusVO> getAdminList() - 管理端全量列表
  - void addCampus(CampusDTO dto) - 添加校区
  - void updateCampus(CampusDTO dto) - 更新校区

**CampusServiceImpl.java（service/impl/CampusServiceImpl.java）：**
- 继承 ServiceImpl<CampusMapper, Campus>
- 实现 CampusService 接口
- 注入 StringRedisTemplate、ObjectMapper

**getMiniList 方法业务逻辑：**
1. Redis key: `campus:list`
2. 先从 Redis 获取缓存：`stringRedisTemplate.opsForValue().get(key)`
3. 如果缓存存在：反序列化为 List<CampusVO> 并返回
4. 如果缓存不存在：
   - 查询数据库：`list(new LambdaQueryWrapper<Campus>().eq(Campus::getStatus, 1).orderByAsc(Campus::getSort))`
   - 转换为 List<CampusVO>
   - 序列化为 JSON 存入 Redis，TTL=1小时：`stringRedisTemplate.opsForValue().set(key, json, 1, TimeUnit.HOURS)`
   - 返回结果

**getAdminList 方法业务逻辑：**
1. 查询所有校区：`list(new LambdaQueryWrapper<Campus>().orderByAsc(Campus::getSort))`
2. 转换为 List<CampusVO> 并返回

**addCampus 方法业务逻辑：**
1. 创建 Campus 对象，设置字段
2. 调用 save(campus)
3. 清除 Redis 缓存：`stringRedisTemplate.delete("campus:list")`

**updateCampus 方法业务逻辑：**
1. 根据 dto.getId() 查询校区是否存在，不存在抛出 BusinessException("校区不存在")
2. 更新校区信息
3. 调用 updateById(campus)
4. 清除 Redis 缓存：`stringRedisTemplate.delete("campus:list")`

**依赖注入：**
- StringRedisTemplate stringRedisTemplate
- ObjectMapper objectMapper（用于 JSON 序列化/反序列化）

#### 步骤 7：创建 MeetingPointService 接口和实现类

**MeetingPointService.java（service/MeetingPointService.java）：**
- 继承 IService<MeetingPoint>
- 添加方法：
  - List<MeetingPointVO> getMiniListByCampusId(Long campusId) - 小程序端按校区查询面交地点
  - List<MeetingPointVO> getAdminListByCampusId(Long campusId) - 管理端按校区查询面交地点
  - void addMeetingPoint(MeetingPointDTO dto) - 添加面交地点
  - void updateMeetingPoint(MeetingPointDTO dto) - 更新面交地点
  - void deleteMeetingPoint(Long id) - 删除面交地点

**MeetingPointServiceImpl.java（service/impl/MeetingPointServiceImpl.java）：**
- 继承 ServiceImpl<MeetingPointMapper, MeetingPoint>
- 实现 MeetingPointService 接口
- 注入 StringRedisTemplate、ObjectMapper

**getMiniListByCampusId 方法业务逻辑：**
1. Redis key: `meeting_point:campus:{campusId}`
2. 先从 Redis 获取缓存
3. 如果缓存存在：反序列化为 List<MeetingPointVO> 并返回
4. 如果缓存不存在：
   - 查询数据库：`list(new LambdaQueryWrapper<MeetingPoint>().eq(MeetingPoint::getCampusId, campusId).eq(MeetingPoint::getStatus, 1).orderByAsc(MeetingPoint::getSort))`
   - 转换为 List<MeetingPointVO>
   - 序列化为 JSON 存入 Redis，TTL=1小时
   - 返回结果

**getAdminListByCampusId 方法业务逻辑：**
1. 查询指定校区的所有面交地点：`list(new LambdaQueryWrapper<MeetingPoint>().eq(MeetingPoint::getCampusId, campusId).orderByAsc(MeetingPoint::getSort))`
2. 转换为 List<MeetingPointVO> 并返回

**addMeetingPoint 方法业务逻辑：**
1. 创建 MeetingPoint 对象，设置字段
2. 调用 save(meetingPoint)
3. 清除 Redis 缓存：`stringRedisTemplate.delete("meeting_point:campus:" + dto.getCampusId())`

**updateMeetingPoint 方法业务逻辑：**
1. 根据 dto.getId() 查询面交地点是否存在，不存在抛出 BusinessException("面交地点不存在")
2. 更新面交地点信息
3. 调用 updateById(meetingPoint)
4. 清除 Redis 缓存：`stringRedisTemplate.delete("meeting_point:campus:" + meetingPoint.getCampusId())`

**deleteMeetingPoint 方法业务逻辑：**
1. 根据 id 查询面交地点是否存在，不存在抛出 BusinessException("面交地点不存在")
2. 调用 removeById(id)
3. 清除 Redis 缓存：`stringRedisTemplate.delete("meeting_point:campus:" + meetingPoint.getCampusId())`

**注意：**
- 删除面交地点前无需检查商品引用（根据 acceptance_criteria）

**依赖注入：**
- StringRedisTemplate stringRedisTemplate
- ObjectMapper objectMapper

#### 步骤 8：创建 MiniCampusController（controller/mini/MiniCampusController.java）

**要求：**
- @RestController
- @RequestMapping("/mini/campus")
- 注入 CampusService、MeetingPointService

**接口定义：**
```java
@GetMapping("/list")
public Result<List<CampusVO>> getCampusList() {
    List<CampusVO> list = campusService.getMiniList();
    return Result.success(list);
}

@GetMapping("/meeting-points/{campusId}")
public Result<List<MeetingPointVO>> getMeetingPoints(@PathVariable Long campusId) {
    List<MeetingPointVO> list = meetingPointService.getMiniListByCampusId(campusId);
    return Result.success(list);
}
```

**注意：**
- 这两个接口为公开接口（无需登录），需要在 WebMvcConfig 中放行 "/mini/campus/list" 和 "/mini/campus/meeting-points/**"

#### 步骤 9：创建 AdminCampusController（controller/admin/AdminCampusController.java）

**要求：**
- @RestController
- @RequestMapping("/admin/campus")
- 注入 CampusService、MeetingPointService

**接口定义：**
```java
// 校区管理
@GetMapping("/list")
public Result<List<CampusVO>> getCampusList() {
    List<CampusVO> list = campusService.getAdminList();
    return Result.success(list);
}

@PostMapping("/add")
public Result<Void> addCampus(@RequestBody @Valid CampusDTO dto) {
    campusService.addCampus(dto);
    return Result.success();
}

@PostMapping("/update")
public Result<Void> updateCampus(@RequestBody @Valid CampusDTO dto) {
    campusService.updateCampus(dto);
    return Result.success();
}

// 面交地点管理
@GetMapping("/meeting-point/list/{campusId}")
public Result<List<MeetingPointVO>> getMeetingPointList(@PathVariable Long campusId) {
    List<MeetingPointVO> list = meetingPointService.getAdminListByCampusId(campusId);
    return Result.success(list);
}

@PostMapping("/meeting-point/add")
public Result<Void> addMeetingPoint(@RequestBody @Valid MeetingPointDTO dto) {
    meetingPointService.addMeetingPoint(dto);
    return Result.success();
}

@PostMapping("/meeting-point/update")
public Result<Void> updateMeetingPoint(@RequestBody @Valid MeetingPointDTO dto) {
    meetingPointService.updateMeetingPoint(dto);
    return Result.success();
}

@PostMapping("/meeting-point/delete")
public Result<Void> deleteMeetingPoint(@RequestParam Long id) {
    meetingPointService.deleteMeetingPoint(id);
    return Result.success();
}
```

#### 步骤 10：更新 WebMvcConfig

**操作：**
- 在 excludePathPatterns 中添加 "/mini/campus/list" 和 "/mini/campus/meeting-points/**"（公开接口，无需登录）

#### 步骤 11：更新 RedisConstant（如果常量不存在）

**检查并添加常量：**
- CAMPUS_LIST = "campus:list"
- MEETING_POINT_CAMPUS = "meeting_point:campus:"

#### 步骤 12：编写测试用例（test/java/.../service/impl/CampusServiceImplTest.java）

**测试场景：**

1. **testGetMiniList_CacheHit** - 缓存命中
   - Mock Redis 返回缓存的 JSON 数据
   - 验证未查询数据库
   - 验证返回的 List<CampusVO> 正确

2. **testGetMiniList_CacheMiss** - 缓存未命中
   - Mock Redis 返回 null（缓存不存在）
   - Mock CampusMapper.selectList 返回校区列表
   - 验证查询了数据库
   - 验证 Redis set 被调用（存入缓存，TTL=1小时）
   - 验证返回的 List<CampusVO> 正确

3. **testGetAdminList** - 管理端全量列表
   - Mock CampusMapper.selectList 返回所有校区
   - 验证按 sort 排序
   - 验证返回的 List<CampusVO> 正确

4. **testAddCampus** - 添加校区
   - 验证 save 方法被调用
   - 验证 Redis delete 被调用（清除缓存）

5. **testUpdateCampus_Success** - 更新校区成功
   - Mock CampusMapper.selectById 返回已有校区
   - 验证 updateById 方法被调用
   - 验证 Redis delete 被调用（清除缓存）

6. **testUpdateCampus_NotFound** - 更新不存在的校区
   - Mock CampusMapper.selectById 返回 null
   - 验证抛出 BusinessException("校区不存在")

#### 步骤 13：编写测试用例（test/java/.../service/impl/MeetingPointServiceImplTest.java）

**测试场景：**

1. **testGetMiniListByCampusId_CacheHit** - 缓存命中
   - Mock Redis 返回缓存的 JSON 数据
   - 验证未查询数据库
   - 验证返回的 List<MeetingPointVO> 正确

2. **testGetMiniListByCampusId_CacheMiss** - 缓存未命中
   - Mock Redis 返回 null
   - Mock MeetingPointMapper.selectList 返回面交地点列表
   - 验证查询了数据库
   - 验证 Redis set 被调用（TTL=1小时）
   - 验证返回的 List<MeetingPointVO> 正确

3. **testGetAdminListByCampusId** - 管理端按校区查询
   - Mock MeetingPointMapper.selectList 返回指定校区的面交地点
   - 验证按 sort 排序
   - 验证返回的 List<MeetingPointVO> 正确

4. **testAddMeetingPoint** - 添加面交地点
   - 验证 save 方法被调用
   - 验证 Redis delete 被调用（清除指定校区的缓存）

5. **testUpdateMeetingPoint_Success** - 更新面交地点成功
   - Mock MeetingPointMapper.selectById 返回已有面交地点
   - 验证 updateById 方法被调用
   - 验证 Redis delete 被调用

6. **testUpdateMeetingPoint_NotFound** - 更新不存在的面交地点
   - Mock MeetingPointMapper.selectById 返回 null
   - 验证抛出 BusinessException("面交地点不存在")

7. **testDeleteMeetingPoint_Success** - 删除面交地点成功
   - Mock MeetingPointMapper.selectById 返回已有面交地点
   - 验证 removeById 方法被调用
   - 验证 Redis delete 被调用

8. **testDeleteMeetingPoint_NotFound** - 删除不存在的面交地点
   - Mock MeetingPointMapper.selectById 返回 null
   - 验证抛出 BusinessException("面交地点不存在")

**测试要求：**
- 使用 @ExtendWith(MockitoExtension.class)
- Mock CampusMapper、MeetingPointMapper、StringRedisTemplate、ObjectMapper
- 使用 ArgumentCaptor 验证 Redis key 和 TTL 设置
- 验证 JSON 序列化/反序列化逻辑

#### 步骤 14：运行测试并生成证据包

**操作：**
1. 在终端运行：`mvn test -Dtest=CampusServiceImplTest,MeetingPointServiceImplTest`
2. 将输出保存到：`run-folder/F09-校区与面交地点模块/test_output.log`
3. 创建 `run-folder/F09-校区与面交地点模块/run.sh`，内容：
   ```bash
   #!/bin/bash
   mvn test -Dtest=CampusServiceImplTest,MeetingPointServiceImplTest
   ```
4. 复制本任务规划到：`run-folder/F09-校区与面交地点模块/task.md`

#### 步骤 15：创建审查信号文件

**操作：**
- 在项目根目录创建 `.ready-for-review` 文件
- 文件内容：
  ```
  Feature: F09 校区与面交地点模块
  Status: 待审查
  Timestamp: [当前时间]
  ```

---

### 关键业务规则

1. **小程序端接口为公开接口**：校区列表和面交地点列表无需登录即可访问
2. **Redis 缓存策略**：
   - 校区列表：key=campus:list，TTL=1小时
   - 面交地点列表：key=meeting_point:campus:{campusId}，TTL=1小时
3. **查询条件**：
   - 小程序端只查询 status=1（启用）的数据
   - 管理端查询所有数据（包含禁用状态）
4. **排序规则**：所有列表查询按 sort 字段升序排序
5. **缓存清除**：增删改操作后清除对应的 Redis 缓存
6. **删除限制**：删除面交地点前无需检查商品引用（与分类模块不同）

### 数据库字段映射

| 数据库字段 | Java 类型 | 说明 |
|-----------|----------|------|
| campus.status | Integer | 0-禁用 1-启用 |
| campus.sort | Integer | 排序号（升序） |
| meeting_point.campus_id | Long | 所属校区ID |
| meeting_point.status | Integer | 0-禁用 1-启用 |
| meeting_point.sort | Integer | 排序号（升序） |

### 验收标准（来自 feature_list.json）

- [ ] 小程序端校区列表为公开接口，Redis 缓存
- [ ] 面交地点按 campusId 查询
- [ ] 管理端支持校区和面交地点的增删改查
- [ ] 删除面交地点前无特殊限制
- [ ] 编写 Service 层单元测试

---

### 开发进展

（执行者在此追加 [执行者] 前缀的工作记录）

[执行者] 2026-02-20 完成 Campus/MeetingPoint 实体、DTO、VO、Mapper、Service、Controller 与 Redis 常量
[执行者] 2026-02-20 完成 CampusServiceImplTest 与 MeetingPointServiceImplTest
[执行者] 2026-02-20 运行 mvn test -Dtest=CampusServiceImplTest,MeetingPointServiceImplTest 并生成 run-folder/F09-校区与面交地点模块 证据包与 .ready-for-review

---

### 审查记录

（监督者在此追加审查意见）

---


---

### 审查记录

**[监督者] 2026-02-20 00:23: ✅ 功能「F09 校区与面交地点模块」验收通过。**

#### 4.1 MyBatis-Plus 规范审查：✅
- ✅ Campus 和 MeetingPoint 实体使用了 @TableName 注解
- ✅ 主键字段使用了 @TableId(type = IdType.AUTO)
- ✅ createTime 使用了 @TableField(fill = FieldFill.INSERT)
- ✅ updateTime 使用了 @TableField(fill = FieldFill.INSERT_UPDATE)
- ✅ CampusMapper 和 MeetingPointMapper 继承了 BaseMapper
- ✅ 简单 CRUD 直接使用 MyBatis-Plus 内置方法（insert、updateById、deleteById、selectList）
- ✅ CampusService 和 MeetingPointService 继承了 IService
- ✅ CampusServiceImpl 和 MeetingPointServiceImpl 继承了 ServiceImpl
- ✅ 条件查询使用了 LambdaQueryWrapper（类型安全）
- ✅ 无需分页查询（全量列表）

#### 4.2 功能正确性审查：✅
- ✅ MiniCampusController 只做参数接收和 Service 调用，无业务逻辑
- ✅ AdminCampusController 只做参数接收和 Service 调用，无业务逻辑
- ✅ Controller 路径前缀正确：/mini/campus 和 /admin/campus
- ✅ CampusServiceImpl 正确实现了所有业务逻辑：
  - getMiniList：Redis 缓存1小时，只查询 status=1 的校区，按 sort 排序
  - getAdminList：全量列表，按 sort 排序
  - addCampus：插入校区，清除 Redis 缓存
  - updateCampus：校验校区存在，更新校区，清除 Redis 缓存
- ✅ MeetingPointServiceImpl 正确实现了所有业务逻辑：
  - getMiniListByCampusId：按 campusId 查询，Redis 缓存1小时，只查询 status=1，按 sort 排序
  - getAdminListByCampusId：按 campusId 查询全量列表，按 sort 排序
  - addMeetingPoint：插入面交地点，清除对应校区的 Redis 缓存
  - updateMeetingPoint：校验存在，更新，清除缓存
  - deleteMeetingPoint：校验存在，删除，清除缓存（无需检查商品引用）
- ✅ CampusDTO 和 MeetingPointDTO 字段正确，带完整参数校验注解
- ✅ CampusVO 和 MeetingPointVO 字段正确（5个和6个字段）
- ✅ 使用 Result<T> 统一响应封装

#### 4.3 安全性审查：✅
- ✅ LambdaQueryWrapper 使用类型安全的方式
- ✅ WebMvcConfig 正确放行了 /mini/campus/list 和 /mini/campus/meeting-points/** 接口（公开接口）

#### 4.4 代码质量审查：✅
- ✅ 分层合理：Controller → Service → Mapper
- ✅ 命名规范：Campus、MeetingPoint、CampusDTO、MeetingPointDTO、CampusVO、MeetingPointVO 语义清晰
- ✅ 异常通过 BusinessException 抛出（校区不存在、面交地点不存在、新增/更新/删除失败）
- ✅ Redis 缓存逻辑健壮：
  - 反序列化失败时自动删除缓存（防止脏数据）
  - 序列化失败时直接返回结果（不影响主业务）
  - 缓存 key 格式正确：campus:list 和 meeting_point:campus:{campusId}
- ✅ 无 N+1 查询问题

#### 4.5 测试审查（反作弊）：✅
- ✅ 测试文件存在：CampusServiceImplTest.java 和 MeetingPointServiceImplTest.java
- ✅ CampusServiceImplTest 覆盖6个场景：
  1. testGetMiniList_CacheHit - 缓存命中
  2. testGetMiniList_CacheMiss - 缓存未命中
  3. testGetAdminList - 管理端全量列表
  4. testAddCampus - 添加校区
  5. testUpdateCampus_Success - 更新校区成功
  6. testUpdateCampus_NotFound - 更新不存在的校区
- ✅ MeetingPointServiceImplTest 覆盖8个场景：
  1. testGetMiniListByCampusId_CacheHit - 缓存命中
  2. testGetMiniListByCampusId_CacheMiss - 缓存未命中
  3. testGetAdminListByCampusId - 管理端按校区查询
  4. testAddMeetingPoint - 添加面交地点
  5. testUpdateMeetingPoint_Success - 更新面交地点成功
  6. testUpdateMeetingPoint_NotFound - 更新不存在的面交地点
  7. testDeleteMeetingPoint_Success - 删除面交地点成功
  8. testDeleteMeetingPoint_NotFound - 删除不存在的面交地点
- ✅ 断言有实际意义：
  - 验证 VO 字段值（id、name、address、sort、status、campusId、description）
  - 验证 Redis 缓存被设置（TTL=1小时）
  - 验证增删改操作后缓存被删除
  - 验证异常消息内容（"校区不存在"、"面交地点不存在"）
  - 使用 ArgumentCaptor 验证 Redis key 和 TTL
- ✅ Mock 配置正确：
  - Mock CampusMapper、MeetingPointMapper、StringRedisTemplate、ObjectMapper
  - 正确模拟 Redis 的 get、set、delete 操作
  - 使用 @ExtendWith(MockitoExtension.class)
- ✅ 无 @Disabled 跳过测试

#### 4.6 数据库一致性审查：✅
- ✅ Campus Entity 字段与 campus 表一致（7个字段）
- ✅ MeetingPoint Entity 字段与 meeting_point 表一致（8个字段）
- ✅ 字段类型映射正确（tinyint→Integer, datetime→LocalDateTime）
- ✅ status 枚举值正确（0-禁用 1-启用）

#### 4.7 证据包审查：✅
- ✅ run-folder/F09-校区与面交地点模块/ 目录完整
- ✅ run.sh 存在且执行 mvn test -Dtest=CampusServiceImplTest,MeetingPointServiceImplTest
- ✅ test_output.log 包含 BUILD SUCCESS
- ✅ task.md 包含任务规划

#### 4.8 独立复跑验证：✅
- ✅ 在 Kiro 终端独立运行 mvn test "-Dtest=CampusServiceImplTest,MeetingPointServiceImplTest"
- ✅ 测试结果：Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
- ✅ BUILD SUCCESS

#### 4.9 Acceptance Criteria 逐项验证：✅
1. ✅ 小程序端校区列表为公开接口，Redis 缓存（WebMvcConfig 放行 /mini/campus/list，缓存 TTL=1小时）
2. ✅ 面交地点按 campusId 查询（getMiniListByCampusId 方法，LambdaQueryWrapper.eq(MeetingPoint::getCampusId, campusId)）
3. ✅ 管理端支持校区和面交地点的增删改查（AdminCampusController 7个接口全部实现）
4. ✅ 删除面交地点前无特殊限制（deleteMeetingPoint 方法只校验存在性，无需检查商品引用）
5. ✅ 编写 Service 层单元测试（CampusServiceImplTest 6个测试 + MeetingPointServiceImplTest 8个测试）

#### 4.10 重点检查项：✅
- ✅ 小程序端接口为公开接口：
  - WebMvcConfig 正确放行 /mini/campus/list 和 /mini/campus/meeting-points/**
  - 无需登录即可访问
- ✅ Redis 缓存策略正确：
  - 校区列表：key=campus:list，TTL=1小时
  - 面交地点列表：key=meeting_point:campus:{campusId}，TTL=1小时
  - 增删改操作后清除对应缓存
- ✅ 面交地点按 campusId 正确关联：
  - 查询条件：.eq(MeetingPoint::getCampusId, campusId)
  - 缓存 key 包含 campusId
  - 删除时清除对应校区的缓存
- ✅ Controller 正确分包：
  - MiniCampusController 在 controller/mini 包下
  - AdminCampusController 在 controller/admin 包下
- ✅ RedisConstant 正确添加了常量：
  - CAMPUS_LIST = "campus:list"
  - MEETING_POINT_CAMPUS = "meeting_point:campus:"
- ✅ 删除面交地点无需检查商品引用（与分类模块不同，符合验收标准）

### 亮点
1. **Redis 缓存健壮性**：反序列化失败时自动删除缓存，序列化失败时直接返回结果，不影响主业务
2. **缓存 key 设计合理**：面交地点缓存按 campusId 分组，避免缓存粒度过大
3. **测试覆盖全面**：14个测试场景覆盖所有业务分支和异常情况
4. **代码简洁高效**：使用 List.of() 和 stream().map().toList() 简化代码
5. **参数校验完整**：DTO 使用 @NotBlank、@NotNull、@Size 注解，Controller 使用 @Valid 校验
6. **异常处理精准**：insert/update/delete 返回值校验，失败时抛出明确的 BusinessException

### 统计
- 累计通过功能：9/26（F01-F09）
- 累计测试数量：62 个（F01: 4 + F02: 8 + F03: 10 + F04: 9 + F05: 8 + F06: 9 + F07: 10 + F08: 10 + F09: 14）
- 全部测试通过率：100%

已更新 feature_list.json：F09 passes=true
已创建 .review-passed 信号文件

---


---

## [监督者] 验收流程完成 - 2026-02-20

✅ 已更新 `feature_list.json`：F09 的 `passes` 字段已设为 `true`
✅ 已创建 `.review-passed` 信号文件
✅ 已删除 `.ready-for-review` 信号文件

F09 功能「校区与面交地点模块」正式验收通过，可进入下一功能开发。


---

# Feature F10: 学院管理模块

## [监督者] 任务规划 - 2026-02-20

### 功能概述
管理端学院的增删改查功能。F06 已创建 College 实体、CollegeMapper、CollegeService 和 CollegeServiceImpl（含 getCollegeList 方法），本功能只需补充管理端的增删改查接口。

### 依赖关系
- 依赖 F06（校园认证-小程序端）：已创建 College 实体、CollegeService、CampusAuth 实体
- CampusAuth 表中有 college_id 字段，删除学院前需检查是否有认证记录引用

### 数据库表结构（已存在）
```sql
college 表：
- id (bigint, 主键自增)
- name (varchar(50), 学院名称)
- sort (int, 排序)
- status (tinyint, 状态: 0-禁用 1-启用)
- create_time (datetime)
- update_time (datetime)

campus_auth 表（关联）：
- college_id (bigint, 外键关联 college.id)
```

### Redis 缓存策略
- Key: `RedisConstant.COLLEGE_LIST` (已在 F06 中定义)
- TTL: 1小时
- 清除时机：添加、更新、删除学院后

---

## 实现步骤

### 步骤 1: 创建 CollegeDTO
**文件**: `src/main/java/com/qingyuan/secondhand/dto/CollegeDTO.java`

**字段**:
```java
- Long id (更新时必填)
- @NotBlank String name (学院名称，1-50字)
- Integer sort (排序，默认0)
- Integer status (状态: 0-禁用 1-启用，默认1)
```

**校验注解**:
- name: @NotBlank(message = "学院名称不能为空")
- name: @Length(max = 50, message = "学院名称不能超过50字")

---

### 步骤 2: 在 CollegeService 中补充方法
**文件**: `src/main/java/com/qingyuan/secondhand/service/CollegeService.java`

**新增方法签名**:
```java
void addCollege(CollegeDTO dto);
void updateCollege(CollegeDTO dto);
void deleteCollege(Long id);
```

---

### 步骤 3: 在 CollegeServiceImpl 中实现方法
**文件**: `src/main/java/com/qingyuan/secondhand/service/impl/CollegeServiceImpl.java`

**实现要点**:

1. **addCollege(CollegeDTO dto)**:
   - 创建 College 实体，设置 name、sort、status
   - 使用 MyBatis-Plus 的 save() 方法保存
   - 保存成功后清除 Redis 缓存（`stringRedisTemplate.delete(RedisConstant.COLLEGE_LIST)`）

2. **updateCollege(CollegeDTO dto)**:
   - 校验 id 不能为空
   - 根据 id 查询学院是否存在，不存在抛出 BusinessException("学院不存在")
   - 更新 name、sort、status
   - 使用 MyBatis-Plus 的 updateById() 方法更新
   - 更新成功后清除 Redis 缓存

3. **deleteCollege(Long id)**:
   - 校验 id 不能为空
   - 根据 id 查询学院是否存在，不存在抛出 BusinessException("学院不存在")
   - **关键**：检查是否有认证记录使用该学院
     - 使用 CampusAuthMapper 查询 `count(college_id = id)`
     - 如果 count > 0，抛出 BusinessException("该学院下存在认证记录，无法删除")
   - 使用 MyBatis-Plus 的 removeById() 方法删除
   - 删除成功后清除 Redis 缓存

**依赖注入**:
- 需要注入 CampusAuthMapper（用于检查认证记录）

---

### 步骤 4: 创建 AdminCollegeController
**文件**: `src/main/java/com/qingyuan/secondhand/controller/admin/AdminCollegeController.java`

**接口列表**:

1. **GET /admin/college/list** - 学院列表
   - 无参数
   - 调用 collegeService.list() 查询所有学院（不分页）
   - 返回 `Result<List<College>>`
   - 注意：这里返回完整的 College 实体（包含 status、sort），与小程序端的 CollegeVO 不同

2. **POST /admin/college/add** - 添加学院
   - 参数：@RequestBody @Valid CollegeDTO dto
   - 调用 collegeService.addCollege(dto)
   - 返回 `Result<Void>`

3. **POST /admin/college/update** - 更新学院
   - 参数：@RequestBody @Valid CollegeDTO dto
   - 校验 dto.getId() 不能为空
   - 调用 collegeService.updateCollege(dto)
   - 返回 `Result<Void>`

4. **POST /admin/college/delete** - 删除学院
   - 参数：@RequestParam Long id
   - 调用 collegeService.deleteCollege(id)
   - 返回 `Result<Void>`

**注解**:
- @RestController
- @RequestMapping("/admin/college")
- @RequiredArgsConstructor

---

### 步骤 5: 编写单元测试
**文件**: `src/test/java/com/qingyuan/secondhand/service/impl/CollegeServiceImplTest.java`

**测试场景**:

1. **testAddCollege_Success** - 正常添加学院
   - 创建 CollegeDTO，设置 name、sort、status
   - 调用 addCollege()
   - 验证 collegeMapper.insert() 被调用
   - 验证 Redis 缓存被清除

2. **testUpdateCollege_Success** - 正常更新学院
   - Mock collegeMapper.selectById() 返回已存在的学院
   - 调用 updateCollege()
   - 验证 collegeMapper.updateById() 被调用
   - 验证 Redis 缓存被清除

3. **testUpdateCollege_NotFound** - 更新不存在的学院
   - Mock collegeMapper.selectById() 返回 null
   - 调用 updateCollege()
   - 断言抛出 BusinessException("学院不存在")

4. **testDeleteCollege_Success** - 正常删除学院
   - Mock collegeMapper.selectById() 返回已存在的学院
   - Mock campusAuthMapper.selectCount() 返回 0（无认证记录）
   - 调用 deleteCollege()
   - 验证 collegeMapper.deleteById() 被调用
   - 验证 Redis 缓存被清除

5. **testDeleteCollege_HasAuthRecords** - 删除有认证记录的学院
   - Mock collegeMapper.selectById() 返回已存在的学院
   - Mock campusAuthMapper.selectCount() 返回 1（有认证记录）
   - 调用 deleteCollege()
   - 断言抛出 BusinessException("该学院下存在认证记录，无法删除")
   - 验证 collegeMapper.deleteById() 未被调用

6. **testDeleteCollege_NotFound** - 删除不存在的学院
   - Mock collegeMapper.selectById() 返回 null
   - 调用 deleteCollege()
   - 断言抛出 BusinessException("学院不存在")

**Mock 对象**:
- @Mock CollegeMapper collegeMapper
- @Mock CampusAuthMapper campusAuthMapper
- @Mock StringRedisTemplate stringRedisTemplate
- @Mock ObjectMapper objectMapper
- @InjectMocks CollegeServiceImpl collegeService

---

## 验收标准（Acceptance Criteria）

- [ ] 学院列表查询返回所有学院（包含禁用状态的学院）
- [ ] 添加学院成功后清除学院列表 Redis 缓存
- [ ] 更新学院成功后清除学院列表 Redis 缓存
- [ ] 删除学院前检查是否有认证记录使用该学院
- [ ] 有认证记录时抛出 BusinessException 禁止删除
- [ ] 删除成功后清除学院列表 Redis 缓存
- [ ] 编写 Service 层单元测试，覆盖所有场景（6个测试用例）
- [ ] 所有测试断言有实际意义，正确 mock MyBatis-Plus 方法

---

## 文件清单

### 需要创建的文件
1. `src/main/java/com/qingyuan/secondhand/dto/CollegeDTO.java`
2. `src/main/java/com/qingyuan/secondhand/controller/admin/AdminCollegeController.java`
3. `src/test/java/com/qingyuan/secondhand/service/impl/CollegeServiceImplTest.java`

### 需要修改的文件
1. `src/main/java/com/qingyuan/secondhand/service/CollegeService.java` - 新增 3 个方法签名
2. `src/main/java/com/qingyuan/secondhand/service/impl/CollegeServiceImpl.java` - 实现 3 个方法，注入 CampusAuthMapper

---

## 技术要点

1. **MyBatis-Plus 规范**:
   - 使用 save()、updateById()、removeById() 内置方法
   - 使用 LambdaQueryWrapper 查询认证记录数量
   - 不需要编写 XML SQL

2. **Redis 缓存管理**:
   - 使用已有的 RedisConstant.COLLEGE_LIST 常量
   - 增删改操作后都要清除缓存

3. **业务校验**:
   - 删除前必须检查外键引用（campus_auth.college_id）
   - 使用 BusinessException 抛出业务异常

4. **测试要点**:
   - Mock CampusAuthMapper.selectCount() 方法
   - 验证 Redis delete() 方法被调用
   - 覆盖成功和失败场景

---

## 注意事项

1. 管理端列表接口返回完整的 College 实体（包含 status、sort），与小程序端的 CollegeVO 不同
2. 删除学院的检查逻辑是核心业务规则，必须在测试中重点覆盖
3. CollegeServiceImpl 已经注入了 collegeMapper、stringRedisTemplate、objectMapper，只需补充注入 campusAuthMapper
4. 所有 Redis 缓存操作失败不应影响主业务流程（已在 getCollegeList 中处理）

---

**规划完成时间**: 2026-02-20
**规划人**: 监督者（Kiro IDE）

### 开发进展

[执行者] 2026-02-20 完成学院管理模块 DTO/Service/Controller 实现
[执行者] 2026-02-20 完成 CollegeServiceImplTest 并通过测试，生成 F10 证据包与 .ready-for-review


---

## [监督者] F10 代码审查 - 2026-02-20

### 审查信号
- ✅ 检测到 `.ready-for-review` 信号文件
- ✅ 执行者已完成 F10 学院管理模块实现

---

### 4.1 MyBatis-Plus 规范审查
- ✅ College Entity 使用了 `@TableName("college")` 注解（F06 已有，未修改）
- ✅ 主键字段使用了 `@TableId(type = IdType.AUTO)`（F06 已有，未修改）
- ✅ createTime/updateTime 使用了 `@TableField(fill = FieldFill.INSERT/INSERT_UPDATE)`（F06 已有，未修改）
- ✅ CollegeMapper 继承了 `BaseMapper<College>`（F06 已有，未修改）
- ✅ 简单 CRUD 使用了 MyBatis-Plus 内置方法（insert、updateById、deleteById、selectById）
- ✅ CollegeService 接口继承了 `IService<College>`（F06 已有，新增 3 个方法签名）
- ✅ CollegeServiceImpl 继承了 `ServiceImpl<CollegeMapper, College>`（F06 已有，新增 3 个方法实现）
- ✅ 条件查询使用了 `LambdaQueryWrapper`（删除前检查认证记录）
- ✅ 未使用分页（管理端列表返回所有学院，符合需求）
- ✅ MyBatisPlusConfig 已配置（F06 之前已配置）
- ✅ MetaObjectHandler 已配置（F06 之前已配置）

**评分**: ✅ 完全符合 MyBatis-Plus 规范

---

### 4.2 功能正确性审查

#### CollegeDTO (新增文件)
- ✅ 字段完整：id、name、sort、status
- ✅ 参数校验注解正确：
  - `@NotBlank` + `@Size(max=50)` 用于 name
  - `@NotNull` 用于 sort 和 status
- ✅ 使用 `@Size` 而非 `@Length`（Jakarta Validation 标准注解）

#### CollegeService (修改文件)
- ✅ 新增 3 个方法签名：addCollege、updateCollege、deleteCollege
- ✅ 保留了 F06 的 getCollegeList() 方法

#### CollegeServiceImpl (修改文件)
- ✅ **addCollege()**: 
  - 创建 College 实体并设置字段
  - 使用 `collegeMapper.insert()` 保存
  - 保存成功后清除 Redis 缓存
  - 插入失败抛出 BusinessException
  
- ✅ **updateCollege()**:
  - 校验 id 不为空
  - 查询学院是否存在
  - 使用 `collegeMapper.updateById()` 更新
  - 更新成功后清除 Redis 缓存
  - 手动设置 updateTime（虽然 MetaObjectHandler 会自动填充，但显式设置更清晰）
  
- ✅ **deleteCollege()**:
  - 校验 id 不为空
  - 查询学院是否存在
  - **关键业务逻辑**：使用 `campusAuthMapper.selectCount()` + `LambdaQueryWrapper` 检查认证记录
  - 有认证记录时抛出 BusinessException("该学院下存在认证记录，无法删除")
  - 使用 `collegeMapper.deleteById()` 删除
  - 删除成功后清除 Redis 缓存

- ✅ 正确注入了 CampusAuthMapper（用于检查外键引用）
- ✅ 保留了 F06 的 getCollegeList() 方法实现（未修改）

#### AdminCollegeController (新增文件)
- ✅ 路径前缀正确：`@RequestMapping("/admin/college")`
- ✅ 4 个接口实现：
  1. `GET /list` - 调用 `collegeService.list()` 返回所有学院（包含禁用状态）
  2. `POST /add` - 参数校验 `@Valid`，调用 addCollege()
  3. `POST /update` - 参数校验 `@Valid`，调用 updateCollege()
  4. `POST /delete` - 接收 `@RequestParam Long id`，调用 deleteCollege()
- ✅ 所有接口返回 `Result<T>` 统一响应
- ✅ Controller 只做参数接收和 Service 调用，无业务逻辑

**评分**: ✅ 功能实现完全正确

---

### 4.3 安全性审查
- ✅ 无密码字段（不涉及）
- ✅ 无 XML SQL（不涉及）
- ✅ LambdaQueryWrapper 使用类型安全方式（`CampusAuth::getCollegeId`）
- ✅ 无 JWT 操作（不涉及）
- ✅ 无敏感字段泄露

**评分**: ✅ 无安全问题

---

### 4.4 代码质量审查
- ✅ 分层合理：Controller → Service → Mapper
- ✅ 命名规范：addCollege、updateCollege、deleteCollege、CollegeDTO
- ✅ 异常通过 BusinessException 抛出（学院不存在、有认证记录等）
- ✅ 无多表操作（不需要 @Transactional）
- ✅ 无 N+1 查询问题
- ✅ 代码简洁，逻辑清晰

**评分**: ✅ 代码质量优秀

---

### 4.5 测试审查（反作弊）

#### CollegeServiceImplTest (新增文件)
测试场景覆盖：

1. ✅ **testAddCollege_Success** - 正常添加学院
   - Mock `collegeMapper.insert()` 返回 1
   - 验证 insert() 被调用
   - 验证 Redis 缓存被清除
   - 断言有实际意义

2. ✅ **testUpdateCollege_Success** - 正常更新学院
   - Mock `selectById()` 返回已存在的学院
   - Mock `updateById()` 返回 1
   - 验证 updateById() 被调用
   - 验证 Redis 缓存被清除
   - 断言有实际意义

3. ✅ **testUpdateCollege_NotFound** - 更新不存在的学院
   - Mock `selectById()` 返回 null
   - 断言抛出 BusinessException("学院不存在")
   - 断言异常消息正确
   - 断言有实际意义

4. ✅ **testDeleteCollege_Success** - 正常删除学院
   - Mock `selectById()` 返回已存在的学院
   - Mock `campusAuthMapper.selectCount()` 返回 0L（无认证记录）
   - Mock `deleteById()` 返回 1
   - 验证 deleteById() 被调用
   - 验证 Redis 缓存被清除
   - 断言有实际意义

5. ✅ **testDeleteCollege_HasAuthRecords** - 删除有认证记录的学院（核心场景）
   - Mock `selectById()` 返回已存在的学院
   - Mock `campusAuthMapper.selectCount()` 返回 1L（有认证记录）
   - 断言抛出 BusinessException("该学院下存在认证记录，无法删除")
   - 验证 deleteById() 未被调用（`Mockito.never()`）
   - 断言有实际意义

6. ✅ **testDeleteCollege_NotFound** - 删除不存在的学院
   - Mock `selectById()` 返回 null
   - 断言抛出 BusinessException("学院不存在")
   - 断言有实际意义

**Mock 配置**:
- ✅ 正确 Mock CollegeMapper、CampusAuthMapper、StringRedisTemplate、ObjectMapper
- ✅ 使用构造函数注入创建 CollegeServiceImpl 实例
- ✅ Mock 返回值合理（insert/update/delete 返回 1，selectCount 返回 0L/1L）

**测试覆盖**:
- ✅ 覆盖了所有 acceptance_criteria
- ✅ 覆盖了成功和失败场景
- ✅ 重点测试了删除前检查认证记录的核心业务逻辑
- ✅ 无 `@Disabled` 跳过测试
- ✅ 无假测试（如 `assertTrue(true)`）

**评分**: ✅ 测试覆盖完整，断言有实际意义

---

### 4.6 数据库一致性审查
- ✅ College Entity 字段与 college 表一致（F06 已验证）
- ✅ 字段类型映射正确：
  - id → Long
  - name → String
  - sort → Integer
  - status → Integer
  - createTime/updateTime → LocalDateTime
- ✅ 删除前检查 campus_auth.college_id 外键引用（核心业务规则）

**评分**: ✅ 数据库一致性正确

---

### 4.7 证据包审查
- ✅ `run-folder/F10-学院管理模块/` 目录存在
- ✅ `run.sh` 文件存在，内容为 `mvn test -Dtest=CollegeServiceImplTest`
- ✅ `test_output.log` 包含 `BUILD SUCCESS`
- ✅ 测试结果：`Tests run: 6, Failures: 0, Errors: 0, Skipped: 0`

**评分**: ✅ 证据包完整

---

### 4.8 F06 代码完整性审查（重点）
- ✅ College Entity 未被修改（@TableName、@TableId、@TableField 注解完整）
- ✅ CollegeMapper 未被修改（继承 BaseMapper）
- ✅ CollegeService 只新增了 3 个方法签名，未修改 getCollegeList()
- ✅ CollegeServiceImpl 只新增了 3 个方法实现和 CampusAuthMapper 注入，未修改 getCollegeList()
- ✅ CollegeVO 未被修改（F06 已有）
- ✅ F06 已通过的代码完全未受影响

**评分**: ✅ 正确复用 F06 代码，未破坏已有功能

---

### 第五步：独立复跑验证

**执行命令**:
```bash
cd G:\Code\Graduation_project
mvn test -Dtest=CollegeServiceImplTest
```

**执行结果**:
```
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
[INFO] Total time:  8.079 s
[INFO] Finished at: 2026-02-20T00:54:20+08:00
```

**评分**: ✅ 独立复跑验证通过

---

### 验收标准逐项验证

根据 feature_list.json 中 F10 的 acceptance_criteria：

1. ✅ **学院列表查询返回所有学院**
   - AdminCollegeController.getCollegeList() 调用 collegeService.list()
   - 返回所有学院（包含禁用状态），不过滤 status

2. ✅ **删除学院前检查是否有认证记录使用该学院，有则抛异常禁止删除**
   - CollegeServiceImpl.deleteCollege() 中使用 campusAuthMapper.selectCount() 查询
   - count > 0 时抛出 BusinessException("该学院下存在认证记录，无法删除")
   - 测试用例 testDeleteCollege_HasAuthRecords 覆盖此场景

3. ✅ **添加/更新学院后清除学院列表Redis缓存**
   - addCollege() 和 updateCollege() 都调用 stringRedisTemplate.delete(RedisConstant.COLLEGE_LIST)
   - 测试用例验证了 Redis delete() 被调用

4. ✅ **编写Service层单元测试，覆盖删除有认证引用的学院场景**
   - CollegeServiceImplTest 包含 6 个测试用例
   - testDeleteCollege_HasAuthRecords 专门测试删除有认证记录的场景
   - 验证了 deleteById() 未被调用

---

## 审查结论

✅ **功能「F10 学院管理模块」验收通过**

### 审查评分汇总
- MyBatis-Plus 规范：✅ 完全符合
- 功能正确性：✅ 实现完整
- 安全性：✅ 无问题
- 代码质量：✅ 优秀
- 测试覆盖：✅ 完整且有实际意义
- 数据库一致性：✅ 正确
- 证据包：✅ 完整
- 独立复跑：✅ 通过
- F06 代码完整性：✅ 未破坏已有功能

### 亮点
1. 正确复用了 F06 已有的 College 实体和 CollegeMapper，避免重复创建
2. 删除前检查认证记录的业务逻辑实现正确，使用 LambdaQueryWrapper 类型安全
3. 测试用例覆盖全面，特别是 testDeleteCollege_HasAuthRecords 验证了核心业务规则
4. 增删改操作后都正确清除了 Redis 缓存
5. 异常处理规范，使用 BusinessException 抛出业务异常

### 无问题项
- 无安全漏洞
- 无假测试
- 无代码重复
- 无 N+1 查询
- 无破坏已有功能

**审查时间**: 2026-02-20 00:54
**审查人**: 监督者（Kiro IDE）


---

## [监督者] 验收流程完成 - 2026-02-20 00:54

✅ 已更新 `feature_list.json`：F10 的 `passes` 字段已设为 `true`
✅ 已创建 `.review-passed` 信号文件
✅ 已删除 `.ready-for-review` 信号文件

F10 功能「学院管理模块」正式验收通过，可进入下一功能开发。


---

# Feature F11: 商品发布与编辑

## [监督者] 任务规划 - 2026-02-20

### 功能概述
小程序端商品发布、编辑、修改价格功能。这是商品模块的第一个功能，需要创建 Product 实体和完整的基础架构。

### 依赖关系
- 依赖 F01（微信登录）：需要 UserContext 获取当前用户 ID
- 依赖 F08（分类模块）：需要 category_id 关联分类
- 依赖 F09（校区与面交地点模块）：需要 campus_id 和 meeting_point_id

### 数据库表结构（product 表）
```sql
id (bigint, 主键自增)
user_id (bigint, 发布者用户ID)
title (varchar(50), 商品标题)
description (varchar(500), 商品描述)
price (decimal(10,2), 二手价格)
original_price (decimal(10,2), 原价, 可选)
category_id (bigint, 分类ID)
condition_level (tinyint, 成色: 1-全新 2-几乎全新 3-9成新 4-8成新 5-7成新及以下)
campus_id (bigint, 交易校区ID)
meeting_point_id (bigint, 面交地点ID, 可选)
meeting_point_text (varchar(100), 面交地点自定义文字, 可选)
images (varchar(2000), 商品图片URL, JSON数组格式)
view_count (int, 浏览量, 默认0)
favorite_count (int, 收藏量, 默认0)
status (tinyint, 商品状态: 0-待审核 1-在售 2-已下架 3-已售出 4-审核驳回, 默认0)
reject_reason (varchar(255), 驳回原因, 可选)
review_time (datetime, 审核时间, 可选)
reviewer_id (bigint, 审核人ID, 可选)
auto_off_time (datetime, 自动下架时间, 发布后90天)
is_deleted (tinyint, 逻辑删除: 0-否 1-是, 默认0, @TableLogic)
create_time (datetime, 创建时间, 自动填充)
update_time (datetime, 更新时间, 自动填充)
```

### 索引
- idx_user_id (user_id)
- idx_category_id (category_id)
- idx_campus_id (campus_id)
- idx_status (status)
- idx_create_time (create_time)
- idx_auto_off_time (auto_off_time)
- idx_status_campus_category_create (status, campus_id, category_id, create_time) - 复合索引
- idx_status_price (status, price)

---

## 实现步骤

### 步骤 1: 创建 Product 实体
**文件**: `src/main/java/com/qingyuan/secondhand/entity/Product.java`

**字段**:
```java
@TableId(type = IdType.AUTO)
private Long id;

private Long userId;

private String title;

private String description;

private BigDecimal price;

private BigDecimal originalPrice;

private Long categoryId;

private Integer conditionLevel;

private Long campusId;

private Long meetingPointId;

private String meetingPointText;

private String images;  // JSON 数组字符串

private Integer viewCount;

private Integer favoriteCount;

private Integer status;

private String rejectReason;

private LocalDateTime reviewTime;

private Long reviewerId;

private LocalDateTime autoOffTime;

@TableLogic
private Integer isDeleted;

@TableField(fill = FieldFill.INSERT)
private LocalDateTime createTime;

@TableField(fill = FieldFill.INSERT_UPDATE)
private LocalDateTime updateTime;
```

**注解**:
- @Data
- @TableName("product")
- @TableId(type = IdType.AUTO) 用于 id
- @TableLogic 用于 isDeleted
- @TableField(fill = FieldFill.INSERT) 用于 createTime
- @TableField(fill = FieldFill.INSERT_UPDATE) 用于 updateTime

**字段类型映射**:
- bigint → Long
- varchar → String
- decimal(10,2) → BigDecimal
- tinyint → Integer
- int → Integer
- datetime → LocalDateTime

---

### 步骤 2: 创建 ProductMapper
**文件**: `src/main/java/com/qingyuan/secondhand/mapper/ProductMapper.java`

**内容**:
```java
public interface ProductMapper extends BaseMapper<Product> {
}
```

**说明**: 简单 CRUD 使用 MyBatis-Plus 内置方法，不需要 XML

---

### 步骤 3: 创建 ProductPublishDTO
**文件**: `src/main/java/com/qingyuan/secondhand/dto/ProductPublishDTO.java`

**字段**:
```java
@NotBlank(message = "商品标题不能为空")
@Size(min = 1, max = 50, message = "商品标题长度为1-50字")
private String title;

@NotBlank(message = "商品描述不能为空")
@Size(min = 1, max = 500, message = "商品描述长度为1-500字")
private String description;

@NotNull(message = "价格不能为空")
@DecimalMin(value = "0.01", message = "价格必须大于0")
@Digits(integer = 8, fraction = 2, message = "价格格式不正确")
private BigDecimal price;

@DecimalMin(value = "0.01", message = "原价必须大于0")
@Digits(integer = 8, fraction = 2, message = "原价格式不正确")
private BigDecimal originalPrice;  // 可选

@NotNull(message = "分类不能为空")
private Long categoryId;

@NotNull(message = "成色不能为空")
@Min(value = 1, message = "成色值范围1-5")
@Max(value = 5, message = "成色值范围1-5")
private Integer conditionLevel;

@NotNull(message = "交易校区不能为空")
private Long campusId;

private Long meetingPointId;  // 可选

@Size(max = 100, message = "面交地点文字不能超过100字")
private String meetingPointText;  // 可选

@NotNull(message = "商品图片不能为空")
@Size(min = 1, max = 9, message = "商品图片数量为1-9张")
private List<String> images;
```

**校验注解**:
- @NotBlank: title, description
- @NotNull: price, categoryId, conditionLevel, campusId, images
- @Size: title(1-50), description(1-500), meetingPointText(max 100), images(1-9)
- @DecimalMin: price(>0), originalPrice(>0)
- @Digits: price, originalPrice (integer=8, fraction=2)
- @Min/@Max: conditionLevel(1-5)

---

### 步骤 4: 创建 ProductUpdateDTO
**文件**: `src/main/java/com/qingyuan/secondhand/dto/ProductUpdateDTO.java`

**字段**: 与 ProductPublishDTO 相同，额外增加：
```java
@NotNull(message = "商品ID不能为空")
private Long productId;
```

**说明**: 可以继承 ProductPublishDTO 并添加 productId 字段，或者独立定义

---

### 步骤 5: 创建 ProductService 接口
**文件**: `src/main/java/com/qingyuan/secondhand/service/ProductService.java`

**方法签名**:
```java
public interface ProductService extends IService<Product> {
    
    /**
     * 发布商品
     */
    void publishProduct(ProductPublishDTO dto);
    
    /**
     * 编辑商品
     */
    void updateProduct(ProductUpdateDTO dto);
    
    /**
     * 修改价格
     */
    void updatePrice(Long productId, BigDecimal newPrice);
}
```

---

### 步骤 6: 实现 ProductServiceImpl
**文件**: `src/main/java/com/qingyuan/secondhand/service/impl/ProductServiceImpl.java`

**实现要点**:

#### 6.1 publishProduct(ProductPublishDTO dto)
1. 从 UserContext 获取当前用户 ID
2. 创建 Product 实体，设置字段：
   - userId = 当前用户 ID
   - title, description, price, originalPrice, categoryId, conditionLevel
   - campusId, meetingPointId, meetingPointText
   - images = 将 List<String> 转换为 JSON 字符串（使用 ObjectMapper）
   - viewCount = 0
   - favoriteCount = 0
   - status = 0（待审核）
   - isDeleted = 0
   - autoOffTime = LocalDateTime.now().plusDays(90)
3. 使用 productMapper.insert() 保存
4. 保存失败抛出 BusinessException("发布商品失败")

#### 6.2 updateProduct(ProductUpdateDTO dto)
1. 从 UserContext 获取当前用户 ID
2. 根据 productId 查询商品是否存在
   - 不存在抛出 BusinessException("商品不存在")
3. 校验是否是自己的商品
   - product.getUserId() != 当前用户 ID 抛出 BusinessException("无权编辑该商品")
4. 更新商品字段：
   - title, description, price, originalPrice, categoryId, conditionLevel
   - campusId, meetingPointId, meetingPointText
   - images = 将 List<String> 转换为 JSON 字符串
   - status = 0（重置为待审核）
   - rejectReason = null（清空驳回原因）
   - autoOffTime = LocalDateTime.now().plusDays(90)（重新设置）
5. 使用 productMapper.updateById() 更新
6. 更新失败抛出 BusinessException("更新商品失败")

#### 6.3 updatePrice(Long productId, BigDecimal newPrice)
1. 从 UserContext 获取当前用户 ID
2. 校验 newPrice > 0
   - 不满足抛出 BusinessException("价格必须大于0")
3. 根据 productId 查询商品是否存在
   - 不存在抛出 BusinessException("商品不存在")
4. 校验是否是自己的商品
   - product.getUserId() != 当前用户 ID 抛出 BusinessException("无权修改该商品")
5. 只更新 price 字段
   - 创建新的 Product 对象，设置 id 和 price
   - 使用 productMapper.updateById() 更新
6. 更新失败抛出 BusinessException("修改价格失败")

**依赖注入**:
- ProductMapper productMapper
- ObjectMapper objectMapper（用于 JSON 转换）

**注意事项**:
- images 字段存储为 JSON 数组字符串，例如：`["url1", "url2", "url3"]`
- 使用 ObjectMapper.writeValueAsString() 将 List<String> 转换为 JSON
- 编辑商品时状态重置为待审核，清空驳回原因，重设自动下架时间
- 修改价格不需要重新审核，只更新 price 字段

---

### 步骤 7: 创建 MiniProductController
**文件**: `src/main/java/com/qingyuan/secondhand/controller/mini/MiniProductController.java`

**接口列表**:

1. **POST /mini/product/publish** - 发布商品
   - 参数：@RequestBody @Valid ProductPublishDTO dto
   - 调用 productService.publishProduct(dto)
   - 返回 `Result<Void>`

2. **POST /mini/product/update** - 编辑商品
   - 参数：@RequestBody @Valid ProductUpdateDTO dto
   - 调用 productService.updateProduct(dto)
   - 返回 `Result<Void>`

3. **POST /mini/product/update-price** - 修改价格
   - 参数：@RequestParam Long productId, @RequestParam BigDecimal newPrice
   - 调用 productService.updatePrice(productId, newPrice)
   - 返回 `Result<Void>`

**注解**:
- @RestController
- @RequestMapping("/mini/product")
- @RequiredArgsConstructor

---

### 步骤 8: 编写单元测试
**文件**: `src/test/java/com/qingyuan/secondhand/service/impl/ProductServiceImplTest.java`

**测试场景**:

1. **testPublishProduct_Success** - 正常发布商品
   - Mock UserContext.getCurrentUserId() 返回 10001L
   - Mock productMapper.insert() 返回 1
   - 创建 ProductPublishDTO，设置所有必填字段
   - 调用 publishProduct()
   - 验证 productMapper.insert() 被调用
   - 验证插入的 Product 对象字段正确：
     - userId = 10001L
     - status = 0
     - isDeleted = 0
     - viewCount = 0
     - favoriteCount = 0
     - autoOffTime 约等于 now + 90天
     - images 是 JSON 字符串

2. **testPublishProduct_ImagesJsonConversion** - 图片 JSON 转换
   - 验证 images List<String> 正确转换为 JSON 数组字符串
   - 断言 images 字段包含 `["url1","url2"]` 格式

3. **testUpdateProduct_Success** - 正常编辑商品
   - Mock UserContext.getCurrentUserId() 返回 10001L
   - Mock productMapper.selectById() 返回已存在的商品（userId=10001L）
   - Mock productMapper.updateById() 返回 1
   - 调用 updateProduct()
   - 验证 productMapper.updateById() 被调用
   - 验证更新的 Product 对象：
     - status = 0（重置为待审核）
     - rejectReason = null（清空）
     - autoOffTime 重新设置为 now + 90天

4. **testUpdateProduct_NotOwner** - 编辑非自己的商品
   - Mock UserContext.getCurrentUserId() 返回 10001L
   - Mock productMapper.selectById() 返回商品（userId=10002L，不是自己的）
   - 调用 updateProduct()
   - 断言抛出 BusinessException("无权编辑该商品")
   - 验证 productMapper.updateById() 未被调用

5. **testUpdateProduct_NotFound** - 编辑不存在的商品
   - Mock productMapper.selectById() 返回 null
   - 调用 updateProduct()
   - 断言抛出 BusinessException("商品不存在")

6. **testUpdatePrice_Success** - 正常修改价格
   - Mock UserContext.getCurrentUserId() 返回 10001L
   - Mock productMapper.selectById() 返回已存在的商品（userId=10001L）
   - Mock productMapper.updateById() 返回 1
   - 调用 updatePrice(1L, new BigDecimal("99.99"))
   - 验证 productMapper.updateById() 被调用
   - 验证只更新了 price 字段（不更新 status）

7. **testUpdatePrice_NotOwner** - 修改非自己商品的价格
   - Mock UserContext.getCurrentUserId() 返回 10001L
   - Mock productMapper.selectById() 返回商品（userId=10002L）
   - 调用 updatePrice()
   - 断言抛出 BusinessException("无权修改该商品")

8. **testUpdatePrice_InvalidPrice** - 价格小于等于0
   - 调用 updatePrice(1L, new BigDecimal("0"))
   - 断言抛出 BusinessException("价格必须大于0")

**Mock 对象**:
- @Mock ProductMapper productMapper
- @Mock ObjectMapper objectMapper
- @InjectMocks ProductServiceImpl productService
- MockedStatic<UserContext> userContextMock（使用 Mockito.mockStatic）

**注意事项**:
- 需要 mock UserContext.getCurrentUserId() 静态方法
- 需要 mock ObjectMapper.writeValueAsString() 返回 JSON 字符串
- 验证 autoOffTime 时使用时间范围断言（允许几秒误差）

---

## 验收标准（Acceptance Criteria）

- [x] 参数校验：标题1-50字、价格>0、图片1-9张、描述1-500字
- [x] 发布商品默认 status=0（待审核）、is_deleted=0
- [x] auto_off_time 设置为当前时间+90天
- [x] images 字段存储为 JSON 数组字符串
- [x] 编辑商品时校验是否是自己的商品
- [x] 编辑后状态重置为待审核(0)，清空 reject_reason，重新设置 auto_off_time
- [x] 修改价格时校验是否是自己的商品，直接更新价格（不需要重新审核）
- [x] 编写 Service 层单元测试，覆盖正常发布、参数校验失败、编辑非自己商品场景

## 完成记录 - 2026-02-20
- 已创建 Product 实体、DTO、Mapper、Service、Controller、测试
- 已运行 mvn test -Dtest=ProductServiceImplTest，输出保存至 run-folder/F11-商品发布与编辑/test_output.log
- 已运行 mvn compile -q

---

## 文件清单

### 需要创建的文件
1. `src/main/java/com/qingyuan/secondhand/entity/Product.java`
2. `src/main/java/com/qingyuan/secondhand/mapper/ProductMapper.java`
3. `src/main/java/com/qingyuan/secondhand/service/ProductService.java`
4. `src/main/java/com/qingyuan/secondhand/service/impl/ProductServiceImpl.java`
5. `src/main/java/com/qingyuan/secondhand/dto/ProductPublishDTO.java`
6. `src/main/java/com/qingyuan/secondhand/dto/ProductUpdateDTO.java`
7. `src/main/java/com/qingyuan/secondhand/controller/mini/MiniProductController.java`
8. `src/test/java/com/qingyuan/secondhand/service/impl/ProductServiceImplTest.java`

---

## 技术要点

### 1. MyBatis-Plus 规范
- Product 实体使用 @TableName、@TableId、@TableField、@TableLogic 注解
- ProductMapper 继承 BaseMapper<Product>
- ProductService 继承 IService<Product>
- ProductServiceImpl 继承 ServiceImpl<ProductMapper, Product>
- 使用 insert()、updateById()、selectById() 内置方法
- isDeleted 使用 @TableLogic 自动处理逻辑删除

### 2. JSON 处理
- images 字段存储为 JSON 数组字符串
- 使用 ObjectMapper.writeValueAsString(List<String>) 转换
- 示例：`["https://example.com/img1.jpg", "https://example.com/img2.jpg"]`

### 3. 时间计算
- autoOffTime = LocalDateTime.now().plusDays(90)
- 编辑商品时重新设置 autoOffTime

### 4. 权限校验
- 使用 UserContext.getCurrentUserId() 获取当前用户
- 编辑和修改价格前校验 product.getUserId() == 当前用户 ID

### 5. 状态管理
- 发布商品：status = 0（待审核）
- 编辑商品：status = 0（重置为待审核），rejectReason = null
- 修改价格：不改变 status（不需要重新审核）

### 6. 参数校验
- 使用 Jakarta Validation 注解（@NotBlank, @NotNull, @Size, @DecimalMin, @Digits, @Min, @Max）
- Controller 使用 @Valid 触发校验
- 价格使用 BigDecimal 类型，避免精度问题

---

## 注意事项

1. **Product 实体字段必须与 product 表完全一致**
2. **isDeleted 必须使用 @TableLogic 注解**（这是 MyBatis-Plus 逻辑删除的关键）
3. **images 存储为 JSON 字符串，不是逗号分隔的字符串**
4. **编辑商品时必须重置状态为待审核，清空驳回原因**
5. **修改价格不重新审核，只更新 price 字段**
6. **autoOffTime 在发布和编辑时都要设置为当前时间+90天**
7. **测试时需要 mock UserContext 静态方法**
8. **测试时需要 mock ObjectMapper 的 JSON 转换**
9. **价格使用 BigDecimal，避免使用 double 或 float**
10. **所有业务异常使用 BusinessException 抛出**

---

**规划完成时间**: 2026-02-20
**规划人**: 监督者（Kiro IDE）


---

## [监督者] F11 代码审查 - 2026-02-20

### 审查信号
- ✅ 检测到 `.ready-for-review` 信号文件
- ✅ 执行者已完成 F11 商品发布与编辑模块实现

---

### 4.1 MyBatis-Plus 规范审查
- ✅ Product Entity 使用了 `@TableName("product")` 注解
- ✅ 主键字段使用了 `@TableId(type = IdType.AUTO)`
- ✅ createTime/updateTime 使用了 `@TableField(fill = FieldFill.INSERT/INSERT_UPDATE)`
- ✅ **isDeleted 使用了 `@TableLogic` 注解**（关键：逻辑删除标记）
- ✅ ProductMapper 继承了 `BaseMapper<Product>`
- ✅ 简单 CRUD 使用了 MyBatis-Plus 内置方法（insert、updateById、selectById）
- ✅ ProductService 接口继承了 `IService<Product>`
- ✅ ProductServiceImpl 继承了 `ServiceImpl<ProductMapper, Product>`
- ✅ 未使用条件查询（本功能不涉及）
- ✅ 未使用分页（本功能不涉及）
- ✅ MyBatisPlusConfig 已配置（之前功能已配置）
- ✅ MetaObjectHandler 已配置（之前功能已配置）

**评分**: ✅ 完全符合 MyBatis-Plus 规范

---

### 4.2 功能正确性审查

#### Product Entity (新增文件)
- ✅ 所有字段与 product 表完全一致（18个字段）
- ✅ 字段类型映射正确：
  - bigint → Long
  - varchar → String
  - decimal(10,2) → BigDecimal（**关键：价格精度处理**）
  - tinyint → Integer
  - int → Integer
  - datetime → LocalDateTime
- ✅ images 字段类型为 String（存储 JSON 字符串）
- ✅ isDeleted 使用 @TableLogic 注解

#### ProductPublishDTO (新增文件)
- ✅ 参数校验注解完整：
  - title: @NotBlank + @Size(min=1, max=50)
  - description: @NotBlank + @Size(min=1, max=500)
  - price: @NotNull + @DecimalMin("0.01") + @Digits(integer=8, fraction=2)
  - originalPrice: @DecimalMin("0.01") + @Digits(integer=8, fraction=2)（可选）
  - categoryId: @NotNull
  - conditionLevel: @NotNull + @Min(1) + @Max(5)
  - campusId: @NotNull
  - meetingPointText: @Size(max=100)（可选）
  - images: @NotNull + @Size(min=1, max=9)
- ✅ images 字段类型为 List<String>（前端传入，后端转 JSON）
- ✅ 价格使用 BigDecimal 类型（避免精度问题）

#### ProductUpdateDTO (新增文件)
- ✅ 继承 ProductPublishDTO
- ✅ 新增 productId 字段（@NotNull）

#### ProductServiceImpl (新增文件)
- ✅ **publishProduct()** 实现正确：
  - 从 UserContext 获取当前用户 ID
  - 调用 fillProduct() 填充基本字段
  - 设置 viewCount = 0, favoriteCount = 0
  - 设置 status = 0（待审核）
  - 设置 isDeleted = 0
  - 设置 autoOffTime = LocalDateTime.now().plusDays(90)
  - 使用 productMapper.insert() 保存
  - 插入失败抛出 BusinessException

- ✅ **updateProduct()** 实现正确：
  - 从 UserContext 获取当前用户 ID
  - 查询商品是否存在
  - 校验是否是自己的商品（userId.equals(existing.getUserId())）
  - 调用 fillProduct() 填充基本字段
  - **状态重置为待审核**：status = 0
  - **清空驳回原因**：rejectReason = null
  - **重新设置自动下架时间**：autoOffTime = LocalDateTime.now().plusDays(90)
  - 使用 productMapper.updateById() 更新

- ✅ **updatePrice()** 实现正确：
  - 从 UserContext 获取当前用户 ID
  - 校验 newPrice > 0
  - 查询商品是否存在
  - 校验是否是自己的商品
  - **只更新 price 字段**（不更新 status，不需要重新审核）
  - 使用 productMapper.updateById() 更新

- ✅ **fillProduct()** 辅助方法：
  - 填充所有基本字段
  - 调用 toJson() 将 images List<String> 转换为 JSON 字符串

- ✅ **toJson()** JSON 转换方法：
  - 使用 ObjectMapper.writeValueAsString() 转换
  - 转换失败抛出 BusinessException("图片处理失败")
  - **安全性**：使用 try-catch 捕获异常，避免程序崩溃

#### MiniProductController (新增文件)
- ✅ 路径前缀正确：`@RequestMapping("/mini/product")`
- ✅ 3 个接口实现：
  1. `POST /publish` - 参数校验 @Valid，调用 publishProduct()
  2. `POST /update` - 参数校验 @Valid，调用 updateProduct()
  3. `POST /update-price` - 接收 productId 和 newPrice，调用 updatePrice()
- ✅ 所有接口返回 `Result<Void>` 统一响应
- ✅ Controller 只做参数接收和 Service 调用，无业务逻辑

**评分**: ✅ 功能实现完全正确

---

### 4.3 安全性审查
- ✅ 无密码字段（不涉及）
- ✅ 无 XML SQL（不涉及）
- ✅ 无 LambdaQueryWrapper（不涉及）
- ✅ 无 JWT 操作（不涉及）
- ✅ **JSON 序列化安全**：使用 ObjectMapper，异常处理完善
- ✅ **价格精度安全**：使用 BigDecimal 而非 Double/Float
- ✅ **权限校验**：编辑和修改价格前校验商品归属

**评分**: ✅ 无安全问题

---

### 4.4 代码质量审查
- ✅ 分层合理：Controller → Service → Mapper
- ✅ 命名规范：publishProduct、updateProduct、updatePrice、fillProduct、toJson
- ✅ 异常通过 BusinessException 抛出（未登录、商品不存在、无权操作、价格无效等）
- ✅ 无多表操作（不需要 @Transactional）
- ✅ 无 N+1 查询问题
- ✅ 代码简洁，逻辑清晰
- ✅ 使用辅助方法 fillProduct() 和 toJson() 避免代码重复

**评分**: ✅ 代码质量优秀

---

### 4.5 测试审查（反作弊）

#### ProductServiceImplTest (新增文件)
测试场景覆盖（9个测试用例）：

1. ✅ **testPublishProduct_Success** - 正常发布商品
   - Mock UserContext.setCurrentUserId(10001L)
   - Mock ObjectMapper.writeValueAsString() 返回 JSON 字符串
   - Mock productMapper.insert() 返回 1
   - 使用 ArgumentCaptor 捕获插入的 Product 对象
   - 断言 userId = 10001L
   - 断言 status = 0（待审核）
   - 断言 isDeleted = 0
   - 断言 viewCount = 0, favoriteCount = 0
   - 断言 images = "[\"url1\",\"url2\"]"（JSON 字符串）
   - 断言 autoOffTime 在 89-90 天范围内（使用 Duration 计算）
   - 断言有实际意义

2. ✅ **testPublishProduct_ParamValidationFailed** - 参数校验失败
   - 使用 Hibernate Validator 验证 DTO
   - 设置无效参数（空标题、空描述、价格=0、空图片列表）
   - 断言校验失败（validator.validate(dto).isEmpty() == false）
   - 断言有实际意义

3. ✅ **testPublishProduct_ImagesJsonConversion** - 图片 JSON 转换
   - 验证 images List<String> 正确转换为 JSON 字符串
   - 断言 images = "[\"url1\",\"url2\"]"
   - 断言有实际意义

4. ✅ **testUpdateProduct_Success** - 正常编辑商品
   - Mock 商品存在且属于当前用户
   - Mock productMapper.updateById() 返回 1
   - 使用 ArgumentCaptor 捕获更新的 Product 对象
   - 断言 status = 0（重置为待审核）
   - 断言 rejectReason = null（清空）
   - 断言 autoOffTime 不为 null（重新设置）
   - 断言有实际意义

5. ✅ **testUpdateProduct_NotOwner** - 编辑非自己的商品
   - Mock 商品存在但属于其他用户（userId=10002L）
   - 当前用户 ID = 10001L
   - 断言抛出 BusinessException("无权编辑该商品")
   - 验证 productMapper.updateById() 未被调用（Mockito.never()）
   - 断言有实际意义

6. ✅ **testUpdateProduct_NotFound** - 编辑不存在的商品
   - Mock productMapper.selectById() 返回 null
   - 断言抛出 BusinessException("商品不存在")
   - 断言有实际意义

7. ✅ **testUpdatePrice_Success** - 正常修改价格
   - Mock 商品存在且属于当前用户
   - Mock productMapper.updateById() 返回 1
   - 使用 ArgumentCaptor 捕获更新的 Product 对象
   - 断言 price = 99.99
   - 断言 status = null（**关键：不更新 status，不重新审核**）
   - 断言有实际意义

8. ✅ **testUpdatePrice_NotOwner** - 修改非自己商品的价格
   - Mock 商品存在但属于其他用户
   - 断言抛出 BusinessException("无权修改该商品")
   - 断言有实际意义

9. ✅ **testUpdatePrice_InvalidPrice** - 价格小于等于0
   - 调用 updatePrice(1L, new BigDecimal("0"))
   - 断言抛出 BusinessException("价格必须大于0")
   - 断言有实际意义

**Mock 配置**:
- ✅ 正确 Mock ProductMapper、ObjectMapper
- ✅ 使用 UserContext.setCurrentUserId() 设置当前用户（静态方法）
- ✅ 使用 @AfterEach 清理 UserContext（避免测试间干扰）
- ✅ 使用 ArgumentCaptor 捕获实际参数进行断言
- ✅ Mock 返回值合理（insert/update 返回 1，JSON 返回字符串）

**测试覆盖**:
- ✅ 覆盖了所有 acceptance_criteria
- ✅ 覆盖了成功和失败场景
- ✅ 重点测试了 JSON 转换、权限校验、状态管理
- ✅ 无 `@Disabled` 跳过测试
- ✅ 无假测试（如 `assertTrue(true)`）
- ✅ 所有断言都测试了具体业务数据

**评分**: ✅ 测试覆盖完整，断言有实际意义

---

### 4.6 数据库一致性审查
- ✅ Product Entity 字段与 product 表完全一致（18个字段）
- ✅ 字段类型映射正确：
  - id, userId, categoryId, campusId, meetingPointId, reviewerId → Long
  - title, description, meetingPointText, images, rejectReason → String
  - price, originalPrice → BigDecimal（**关键：decimal(10,2) 映射正确**）
  - conditionLevel, viewCount, favoriteCount, status, isDeleted → Integer
  - reviewTime, autoOffTime, createTime, updateTime → LocalDateTime
- ✅ 枚举值与 SQL 注释一致：
  - status: 0-待审核 1-在售 2-已下架 3-已售出 4-审核驳回
  - conditionLevel: 1-全新 2-几乎全新 3-9成新 4-8成新 5-7成新及以下
- ✅ isDeleted 使用 @TableLogic 自动处理逻辑删除
- ✅ autoOffTime 正确设置为发布时间+90天

**评分**: ✅ 数据库一致性正确

---

### 4.7 证据包审查
- ✅ `run-folder/F11-商品发布与编辑/` 目录存在
- ✅ `run.sh` 文件存在，内容为 `mvn test -Dtest=ProductServiceImplTest`
- ✅ `test_output.log` 包含 `BUILD SUCCESS`
- ✅ 测试结果：`Tests run: 9, Failures: 0, Errors: 0, Skipped: 0`

**评分**: ✅ 证据包完整

---

### 第五步：独立复跑验证

**执行命令**:
```bash
cd G:\Code\Graduation_project
mvn test -Dtest=ProductServiceImplTest
```

**执行结果**:
```
[INFO] Tests run: 9, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
[INFO] Total time:  10.541 s
[INFO] Finished at: 2026-02-20T14:34:19+08:00
```

**评分**: ✅ 独立复跑验证通过

---

### 验收标准逐项验证

根据 feature_list.json 中 F11 的 acceptance_criteria：

1. ✅ **参数校验：标题1-50字、价格>0、图片1-9张、描述1-500字**
   - ProductPublishDTO 使用完整的校验注解
   - title: @Size(min=1, max=50)
   - description: @Size(min=1, max=500)
   - price: @DecimalMin("0.01")
   - images: @Size(min=1, max=9)
   - testPublishProduct_ParamValidationFailed 测试覆盖

2. ✅ **发布商品默认status=0（待审核）、is_deleted=0**
   - publishProduct() 中设置 status = 0, isDeleted = 0
   - testPublishProduct_Success 断言验证

3. ✅ **auto_off_time设置为当前时间+90天**
   - publishProduct() 中设置 autoOffTime = LocalDateTime.now().plusDays(90)
   - testPublishProduct_Success 使用 Duration 验证时间范围

4. ✅ **images字段存储为JSON数组字符串**
   - toJson() 方法使用 ObjectMapper.writeValueAsString() 转换
   - testPublishProduct_ImagesJsonConversion 验证 JSON 格式

5. ✅ **编辑商品时校验是否是自己的商品**
   - updateProduct() 中校验 userId.equals(existing.getUserId())
   - testUpdateProduct_NotOwner 测试覆盖

6. ✅ **编辑后状态重置为待审核(0)，清空reject_reason，重新设置auto_off_time**
   - updateProduct() 中设置 status = 0, rejectReason = null, autoOffTime = now+90天
   - testUpdateProduct_Success 断言验证

7. ✅ **修改价格时校验是否是自己的商品，直接更新价格（不需要重新审核）**
   - updatePrice() 中校验权限
   - 只更新 price 字段，不更新 status
   - testUpdatePrice_Success 断言 status = null（未更新）

8. ✅ **编写Service层单元测试，覆盖正常发布、参数校验失败、编辑非自己商品场景**
   - 9 个测试用例覆盖所有场景
   - testPublishProduct_Success、testPublishProduct_ParamValidationFailed、testUpdateProduct_NotOwner 等

---

## 审查结论

✅ **功能「F11 商品发布与编辑」验收通过**

### 审查评分汇总
- MyBatis-Plus 规范：✅ 完全符合
- 功能正确性：✅ 实现完整
- 安全性：✅ 无问题
- 代码质量：✅ 优秀
- 测试覆盖：✅ 完整且有实际意义
- 数据库一致性：✅ 正确
- 证据包：✅ 完整
- 独立复跑：✅ 通过

### 亮点
1. **Product Entity 完整**：18个字段与数据库表完全一致，所有 MyBatis-Plus 注解正确
2. **isDeleted 使用 @TableLogic**：正确实现逻辑删除标记
3. **价格使用 BigDecimal**：避免精度问题，符合金融数据处理规范
4. **JSON 序列化安全**：使用 ObjectMapper，异常处理完善
5. **参数校验完整**：使用 Jakarta Validation 注解，覆盖所有字段
6. **权限校验严格**：编辑和修改价格前校验商品归属
7. **状态管理正确**：
   - 发布：status=0（待审核）
   - 编辑：status=0（重置），rejectReason=null（清空），autoOffTime 重设
   - 改价：不更新 status（不重新审核）
8. **测试覆盖全面**：9个测试用例，覆盖所有业务场景和边界条件
9. **测试断言有实际意义**：使用 ArgumentCaptor 捕获实际参数，验证具体业务数据
10. **代码质量高**：使用辅助方法避免重复，异常处理规范

### 无问题项
- 无安全漏洞
- 无假测试
- 无代码重复
- 无 N+1 查询
- 无精度问题（使用 BigDecimal）
- 无 JSON 注入风险（使用 ObjectMapper）

**审查时间**: 2026-02-20 14:34
**审查人**: 监督者（Kiro IDE）


---

## [监督者] 验收流程完成 - 2026-02-20 14:34

✅ 已更新 `feature_list.json`：F11 的 `passes` 字段已设为 `true`
✅ 已创建 `.review-passed` 信号文件
✅ 已删除 `.ready-for-review` 信号文件

F11 功能「商品发布与编辑」正式验收通过，可进入下一功能开发。
