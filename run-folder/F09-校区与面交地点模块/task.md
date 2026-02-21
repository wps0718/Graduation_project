# Feature F09：校区与面交地点模块

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
