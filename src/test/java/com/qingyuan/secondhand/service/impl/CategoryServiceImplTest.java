package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingyuan.secondhand.common.constant.RedisConstant;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.dto.CategoryDTO;
import com.qingyuan.secondhand.entity.Category;
import com.qingyuan.secondhand.mapper.CategoryMapper;
import com.qingyuan.secondhand.vo.CategoryVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Test
    void testGetMiniList_CacheHit() throws Exception {
        CategoryMapper categoryMapper = Mockito.mock(CategoryMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);

        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        String json = "[{\"id\":1,\"name\":\"数码\"}]";
        Mockito.when(valueOperations.get(RedisConstant.CATEGORY_LIST)).thenReturn(json);

        List<CategoryVO> cachedList = List.of(buildVO(1L, "数码"));
        Mockito.when(objectMapper.readValue(Mockito.eq(json), Mockito.any(TypeReference.class))).thenReturn(cachedList);

        CategoryServiceImpl service = new CategoryServiceImpl(categoryMapper, stringRedisTemplate, objectMapper);
        List<CategoryVO> result = service.getMiniList();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("数码", result.get(0).getName());
        Mockito.verifyNoInteractions(categoryMapper);
    }

    @Test
    void testGetMiniList_CacheMiss() throws Exception {
        CategoryMapper categoryMapper = Mockito.mock(CategoryMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);
        ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);

        Mockito.when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        Mockito.when(valueOperations.get(RedisConstant.CATEGORY_LIST)).thenReturn(null);

        Category category = new Category();
        category.setId(2L);
        category.setName("图书");
        category.setIcon("b.png");
        category.setSort(1);
        category.setStatus(1);
        Mockito.when(categoryMapper.selectList(Mockito.any(LambdaQueryWrapper.class))).thenReturn(List.of(category));

        Mockito.when(objectMapper.writeValueAsString(Mockito.any())).thenReturn("[{\"id\":2,\"name\":\"图书\"}]");

        CategoryServiceImpl service = new CategoryServiceImpl(categoryMapper, stringRedisTemplate, objectMapper);
        List<CategoryVO> result = service.getMiniList();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals(2L, result.get(0).getId());

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> ttlCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<TimeUnit> unitCaptor = ArgumentCaptor.forClass(TimeUnit.class);
        Mockito.verify(valueOperations).set(keyCaptor.capture(), jsonCaptor.capture(), ttlCaptor.capture(), unitCaptor.capture());

        Assertions.assertEquals(RedisConstant.CATEGORY_LIST, keyCaptor.getValue());
        Assertions.assertTrue(StringUtils.hasText(jsonCaptor.getValue()));
        Assertions.assertEquals(1L, ttlCaptor.getValue());
        Assertions.assertEquals(TimeUnit.HOURS, unitCaptor.getValue());
    }

    @Test
    void testGetAdminPage_WithNameFilter() {
        CategoryMapper categoryMapper = Mockito.mock(CategoryMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        Page<Category> pageResult = new Page<>(1, 10);
        Category category = new Category();
        category.setId(3L);
        category.setName("服饰");
        category.setSort(1);
        category.setStatus(1);
        pageResult.setRecords(List.of(category));
        pageResult.setTotal(1);

        Mockito.when(categoryMapper.selectPage(Mockito.any(Page.class), Mockito.any(LambdaQueryWrapper.class)))
                .thenReturn(pageResult);

        CategoryServiceImpl service = new CategoryServiceImpl(categoryMapper, stringRedisTemplate, objectMapper);
        Page<CategoryVO> result = service.getAdminPage(1, 10, "服");

        Assertions.assertEquals(1, result.getTotal());
        Assertions.assertEquals("服饰", result.getRecords().get(0).getName());

        Mockito.verify(categoryMapper).selectPage(Mockito.any(Page.class), Mockito.any(LambdaQueryWrapper.class));
    }

    @Test
    void testGetAdminPage_WithoutNameFilter() {
        CategoryMapper categoryMapper = Mockito.mock(CategoryMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        Page<Category> pageResult = new Page<>(1, 10);
        pageResult.setRecords(Collections.emptyList());
        pageResult.setTotal(0);
        Mockito.when(categoryMapper.selectPage(Mockito.any(Page.class), Mockito.any(LambdaQueryWrapper.class)))
                .thenReturn(pageResult);

        CategoryServiceImpl service = new CategoryServiceImpl(categoryMapper, stringRedisTemplate, objectMapper);
        Page<CategoryVO> result = service.getAdminPage(1, 10, null);

        Assertions.assertEquals(0, result.getTotal());
        Mockito.verify(categoryMapper).selectPage(Mockito.any(Page.class), Mockito.any(LambdaQueryWrapper.class));
    }

    @Test
    void testGetAdminList() {
        CategoryMapper categoryMapper = Mockito.mock(CategoryMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        Category category = new Category();
        category.setId(4L);
        category.setName("家居");
        category.setSort(1);
        category.setStatus(1);
        Mockito.when(categoryMapper.selectList(Mockito.any(LambdaQueryWrapper.class))).thenReturn(List.of(category));

        CategoryServiceImpl service = new CategoryServiceImpl(categoryMapper, stringRedisTemplate, objectMapper);
        List<CategoryVO> result = service.getAdminList();

        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("家居", result.get(0).getName());
    }

    @Test
    void testAddCategory() {
        CategoryMapper categoryMapper = Mockito.mock(CategoryMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        Mockito.when(categoryMapper.insert(Mockito.any(Category.class))).thenReturn(1);

        CategoryServiceImpl service = new CategoryServiceImpl(categoryMapper, stringRedisTemplate, objectMapper);
        CategoryDTO dto = new CategoryDTO();
        dto.setName("运动");
        dto.setIcon("a.png");
        dto.setSort(1);
        dto.setStatus(1);
        service.addCategory(dto);

        Mockito.verify(categoryMapper).insert(Mockito.any(Category.class));
        Mockito.verify(stringRedisTemplate).delete(RedisConstant.CATEGORY_LIST);
    }

    @Test
    void testUpdateCategory_Success() {
        CategoryMapper categoryMapper = Mockito.mock(CategoryMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        Category existing = new Category();
        existing.setId(10L);
        Mockito.when(categoryMapper.selectById(10L)).thenReturn(existing);
        Mockito.when(categoryMapper.updateById(Mockito.any(Category.class))).thenReturn(1);

        CategoryServiceImpl service = new CategoryServiceImpl(categoryMapper, stringRedisTemplate, objectMapper);
        CategoryDTO dto = new CategoryDTO();
        dto.setId(10L);
        dto.setName("数码");
        dto.setIcon("a.png");
        dto.setSort(1);
        dto.setStatus(1);
        service.updateCategory(dto);

        Mockito.verify(categoryMapper).updateById(Mockito.any(Category.class));
        Mockito.verify(stringRedisTemplate).delete(RedisConstant.CATEGORY_LIST);
    }

    @Test
    void testUpdateCategory_NotFound() {
        CategoryMapper categoryMapper = Mockito.mock(CategoryMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        Mockito.when(categoryMapper.selectById(11L)).thenReturn(null);

        CategoryServiceImpl service = new CategoryServiceImpl(categoryMapper, stringRedisTemplate, objectMapper);
        CategoryDTO dto = new CategoryDTO();
        dto.setId(11L);
        dto.setName("数码");
        dto.setSort(1);
        dto.setStatus(1);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.updateCategory(dto));
        Assertions.assertEquals("分类不存在", ex.getMsg());
    }

    @Test
    void testDeleteCategory_Success() {
        CategoryMapper categoryMapper = Mockito.mock(CategoryMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        Mockito.when(categoryMapper.countProductByCategoryId(12L)).thenReturn(0L);
        Mockito.when(categoryMapper.deleteById(12L)).thenReturn(1);

        CategoryServiceImpl service = new CategoryServiceImpl(categoryMapper, stringRedisTemplate, objectMapper);
        service.deleteCategory(12L);

        Mockito.verify(categoryMapper).deleteById(12L);
        Mockito.verify(stringRedisTemplate).delete(RedisConstant.CATEGORY_LIST);
    }

    @Test
    void testDeleteCategory_HasProducts() {
        CategoryMapper categoryMapper = Mockito.mock(CategoryMapper.class);
        StringRedisTemplate stringRedisTemplate = Mockito.mock(StringRedisTemplate.class);
        ObjectMapper objectMapper = Mockito.mock(ObjectMapper.class);

        Mockito.when(categoryMapper.countProductByCategoryId(13L)).thenReturn(5L);

        CategoryServiceImpl service = new CategoryServiceImpl(categoryMapper, stringRedisTemplate, objectMapper);
        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.deleteCategory(13L));
        Assertions.assertEquals("该分类下有商品，无法删除", ex.getMsg());

        Mockito.verify(categoryMapper, Mockito.never()).deleteById(Mockito.anyLong());
    }

    private CategoryVO buildVO(Long id, String name) {
        CategoryVO vo = new CategoryVO();
        vo.setId(id);
        vo.setName(name);
        vo.setSort(1);
        vo.setStatus(1);
        return vo;
    }
}
