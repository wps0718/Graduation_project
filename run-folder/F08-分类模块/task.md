# Feature F08：分类模块

### 任务规划

**[监督者] 2026-02-19 规划任务：**

该功能是独立模块，无前置依赖。实现商品分类的增删改查，小程序端查询带 Redis 缓存，管理端支持完整 CRUD 操作。

#### 步骤 1：创建 Category 实体类（entity/Category.java）

**字段定义（参考数据库 category 表）：**
- id (Long) - 主键，@TableId(type = IdType.AUTO)
- name (String) - 分类名称
- icon (String) - 分类图标URL
- sort (Integer) - 排序号，默认0
- status (Integer) - 状态 0-禁用 1-启用，默认1
- createTime (LocalDateTime) - 创建时间，@TableField(fill = FieldFill.INSERT)
- updateTime (LocalDateTime) - 更新时间，@TableField(fill = FieldFill.INSERT_UPDATE)

**MyBatis-Plus 注解要求：**
- 类上添加 @TableName("category")
- id 字段添加 @TableId(type = IdType.AUTO)
- createTime 添加 @TableField(fill = FieldFill.INSERT)
- updateTime 添加 @TableField(fill = FieldFill.INSERT_UPDATE)

#### 步骤 2：创建 CategoryMapper 接口（mapper/CategoryMapper.java）

**要求：**
- 继承 BaseMapper<Category>
- 无需额外方法（使用 MyBatis-Plus 内置方法）

#### 步骤 3：创建 DTO 和 VO

**CategoryDTO.java（dto/CategoryDTO.java）：**
- id (Long) - 分类ID（更新时必需）
- name (String) - 分类名称，@NotBlank，@Size(max = 32)
- icon (String) - 分类图标URL，@Size(max = 255)
- sort (Integer) - 排序号，@NotNull
- status (Integer) - 状态，@NotNull

**CategoryVO.java（vo/CategoryVO.java）：**
- id (Long) - 分类ID
- name (String) - 分类名称
- icon (String) - 分类图标URL
- sort (Integer) - 排序号
- status (Integer) - 状态

#### 步骤 4：创建 CategoryService 接口和实现类

**CategoryService.java（service/CategoryService.java）：**
- 继承 IService<Category>
- 添加方法：
  - List<CategoryVO> getMiniList() - 小程序端分类列表（公开接口，Redis 缓存）
  - Page<CategoryVO> getAdminPage(Integer page, Integer pageSize, String name) - 管理端分页查询（支持名称搜索）
  - List<CategoryVO> getAdminList() - 管理端全量列表
  - void addCategory(CategoryDTO dto) - 添加分类
  - void updateCategory(CategoryDTO dto) - 更新分类
  - void deleteCategory(Long id) - 删除分类

**CategoryServiceImpl.java（service/impl/CategoryServiceImpl.java）：**
- 继承 ServiceImpl<CategoryMapper, Category>
- 实现 CategoryService 接口
- 注入 StringRedisTemplate、ProductMapper（用于检查商品引用）

**getMiniList 方法业务逻辑：**
1. Redis key: `category:list`
2. 先从 Redis 获取缓存：`stringRedisTemplate.opsForValue().get(key)`
3. 如果缓存存在：反序列化为 List<CategoryVO> 并返回
4. 如果缓存不存在：
   - 查询数据库：`list(new LambdaQueryWrapper<Category>().eq(Category::getStatus, 1).orderByAsc(Category::getSort))`
   - 转换为 List<CategoryVO>
   - 序列化为 JSON 存入 Redis，TTL=1小时：`stringRedisTemplate.opsForValue().set(key, json, 1, TimeUnit.HOURS)`
   - 返回结果

**getAdminPage 方法业务逻辑：**
1. 构造分页对象：`Page<Category> pageObj = new Page<>(page, pageSize)`
2. 构造查询条件：
   ```java
   LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
   if (StringUtils.hasText(name)) {
       wrapper.like(Category::getName, name);
   }
   wrapper.orderByAsc(Category::getSort);
   ```
3. 执行分页查询：`page(pageObj, wrapper)`
4. 转换为 Page<CategoryVO> 并返回

**getAdminList 方法业务逻辑：**
1. 查询所有分类：`list(new LambdaQueryWrapper<Category>().orderByAsc(Category::getSort))`
2. 转换为 List<CategoryVO> 并返回

**addCategory 方法业务逻辑：**
1. 创建 Category 对象，设置字段
2. 调用 save(category)
3. 清除 Redis 缓存：`stringRedisTemplate.delete("category:list")`

**updateCategory 方法业务逻辑：**
1. 根据 dto.getId() 查询分类是否存在，不存在抛出 BusinessException("分类不存在")
2. 更新分类信息
3. 调用 updateById(category)
4. 清除 Redis 缓存：`stringRedisTemplate.delete("category:list")`

**deleteCategory 方法业务逻辑：**
1. 检查是否有商品使用该分类：
   - 查询 product 表：`productMapper.selectCount(new LambdaQueryWrapper<Product>().eq(Product::getCategoryId, id))`
   - 如果 count > 0：抛出 BusinessException("该分类下有商品，无法删除")
2. 调用 removeById(id)
3. 清除 Redis 缓存：`stringRedisTemplate.delete("category:list")`

**依赖注入：**
- StringRedisTemplate stringRedisTemplate
- ProductMapper productMapper（用于检查商品引用）
- ObjectMapper objectMapper（用于 JSON 序列化/反序列化）

#### 步骤 5：创建 MiniCategoryController（controller/mini/MiniCategoryController.java）

**要求：**
- @RestController
- @RequestMapping("/mini/category")
- 注入 CategoryService

**接口定义：**
```java
@GetMapping("/list")
public Result<List<CategoryVO>> list() {
    List<CategoryVO> list = categoryService.getMiniList();
    return Result.success(list);
}
```

**注意：**
- 该接口为公开接口（无需登录），需要在 WebMvcConfig 中放行 "/mini/category/list"

#### 步骤 6：创建 AdminCategoryController（controller/admin/AdminCategoryController.java）

**要求：**
- @RestController
- @RequestMapping("/admin/category")
- 注入 CategoryService

**接口定义：**

```java
@GetMapping("/page")
public Result<Page<CategoryVO>> page(
    @RequestParam(defaultValue = "1") Integer page,
    @RequestParam(defaultValue = "10") Integer pageSize,
    @RequestParam(required = false) String name) {
    Page<CategoryVO> pageResult = categoryService.getAdminPage(page, pageSize, name);
    return Result.success(pageResult);
}

@GetMapping("/list")
public Result<List<CategoryVO>> list() {
    List<CategoryVO> list = categoryService.getAdminList();
    return Result.success(list);
}

@PostMapping("/add")
public Result<Void> add(@RequestBody @Valid CategoryDTO dto) {
    categoryService.addCategory(dto);
    return Result.success();
}

@PostMapping("/update")
public Result<Void> update(@RequestBody @Valid CategoryDTO dto) {
    categoryService.updateCategory(dto);
    return Result.success();
}

@PostMapping("/delete")
public Result<Void> delete(@RequestParam Long id) {
    categoryService.deleteCategory(id);
    return Result.success();
}
```

#### 步骤 7：更新 WebMvcConfig

**操作：**
- 在 excludePathPatterns 中添加 "/mini/category/list"（公开接口，无需登录）

#### 步骤 8：更新 RedisConstant（如果常量不存在）

**检查并添加常量：**
- CATEGORY_LIST = "category:list"

#### 步骤 9：编写测试用例（test/java/.../service/impl/CategoryServiceImplTest.java）

**测试场景：**

1. **testGetMiniList_CacheHit** - 缓存命中
   - Mock Redis 返回缓存的 JSON 数据
   - 验证未查询数据库
   - 验证返回的 List<CategoryVO> 正确

2. **testGetMiniList_CacheMiss** - 缓存未命中
   - Mock Redis 返回 null（缓存不存在）
   - Mock CategoryMapper 返回分类列表
   - 验证查询了数据库
   - 验证 Redis set 被调用（存入缓存，TTL=1小时）
   - 验证返回的 List<CategoryVO> 正确

3. **testGetAdminPage_WithNameFilter** - 管理端分页查询（带名称搜索）
   - Mock CategoryMapper.selectPage 返回分页结果
   - 验证 LambdaQueryWrapper 包含 like 条件
   - 验证返回的 Page<CategoryVO> 正确

4. **testGetAdminPage_WithoutNameFilter** - 管理端分页查询（无搜索条件）
   - Mock CategoryMapper.selectPage 返回分页结果
   - 验证 LambdaQueryWrapper 不包含 like 条件
   - 验证返回的 Page<CategoryVO> 正确

5. **testGetAdminList** - 管理端全量列表
   - Mock CategoryMapper.selectList 返回所有分类
   - 验证按 sort 排序
   - 验证返回的 List<CategoryVO> 正确

6. **testAddCategory** - 添加分类
   - 验证 save 方法被调用
   - 验证 Redis delete 被调用（清除缓存）

7. **testUpdateCategory_Success** - 更新分类成功
   - Mock CategoryMapper.selectById 返回已有分类
   - 验证 updateById 方法被调用
   - 验证 Redis delete 被调用（清除缓存）

8. **testUpdateCategory_NotFound** - 更新不存在的分类
   - Mock CategoryMapper.selectById 返回 null
   - 验证抛出 BusinessException("分类不存在")

9. **testDeleteCategory_Success** - 删除分类成功（无商品引用）
   - Mock ProductMapper.selectCount 返回 0（无商品使用该分类）
   - 验证 removeById 方法被调用
   - 验证 Redis delete 被调用（清除缓存）

10. **testDeleteCategory_HasProducts** - 删除分类失败（有商品引用）
    - Mock ProductMapper.selectCount 返回 5（有5个商品使用该分类）
    - 验证抛出 BusinessException("该分类下有商品，无法删除")
    - 验证 removeById 方法未被调用

**测试要求：**
- 使用 @ExtendWith(MockitoExtension.class)
- Mock CategoryMapper、ProductMapper、StringRedisTemplate、ObjectMapper
- 使用 ArgumentCaptor 验证 Redis key 和 TTL 设置
- 验证 JSON 序列化/反序列化逻辑

#### 步骤 10：运行测试并生成证据包

**操作：**
1. 在终端运行：`mvn test -Dtest=CategoryServiceImplTest`
2. 将输出保存到：`run-folder/F08-分类模块/test_output.log`
3. 创建 `run-folder/F08-分类模块/run.sh`，内容：
   ```bash
   #!/bin/bash
   mvn test -Dtest=CategoryServiceImplTest
   ```
4. 复制本任务规划到：`run-folder/F08-分类模块/task.md`

#### 步骤 11：创建审查信号文件

**操作：**
- 在项目根目录创建 `.ready-for-review` 文件
- 文件内容：
  ```
  Feature: F08 分类模块
  Status: 待审查
  Timestamp: [当前时间]
  ```
