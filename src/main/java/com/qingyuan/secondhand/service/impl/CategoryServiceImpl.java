package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qingyuan.secondhand.common.constant.RedisConstant;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.dto.CategoryDTO;
import com.qingyuan.secondhand.entity.Category;
import com.qingyuan.secondhand.mapper.CategoryMapper;
import com.qingyuan.secondhand.service.CategoryService;
import com.qingyuan.secondhand.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public List<CategoryVO> getMiniList() {
        String cacheKey = RedisConstant.CATEGORY_LIST;
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.hasText(cached)) {
            try {
                return objectMapper.readValue(cached, new TypeReference<List<CategoryVO>>() {
                });
            } catch (Exception e) {
                stringRedisTemplate.delete(cacheKey);
            }
        }

        List<Category> list = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSort));
        List<CategoryVO> result = list == null ? List.of() : list.stream().map(this::toCategoryVO).toList();

        try {
            String json = objectMapper.writeValueAsString(result);
            stringRedisTemplate.opsForValue().set(cacheKey, json, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            return result;
        }

        return result;
    }

    @Override
    public Page<CategoryVO> getAdminPage(Integer page, Integer pageSize, String name) {
        Page<Category> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            wrapper.like(Category::getName, name);
        }
        wrapper.orderByAsc(Category::getSort);

        Page<Category> result = categoryMapper.selectPage(pageObj, wrapper);
        Page<CategoryVO> voPage = new Page<>(result.getCurrent(), result.getSize());
        voPage.setTotal(result.getTotal());
        List<CategoryVO> records = result.getRecords() == null ? List.of() : result.getRecords().stream().map(this::toCategoryVO).toList();
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    public List<CategoryVO> getAdminList() {
        List<Category> list = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSort));
        return list == null ? List.of() : list.stream().map(this::toCategoryVO).toList();
    }

    @Override
    public void addCategory(CategoryDTO dto) {
        Category category = new Category();
        category.setName(dto.getName());
        category.setIcon(dto.getIcon());
        category.setSort(dto.getSort());
        category.setStatus(dto.getStatus());
        int inserted = categoryMapper.insert(category);
        if (inserted <= 0) {
            throw new BusinessException("新增分类失败");
        }
        stringRedisTemplate.delete(RedisConstant.CATEGORY_LIST);
    }

    @Override
    public void updateCategory(CategoryDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("分类ID不能为空");
        }
        Category existing = categoryMapper.selectById(dto.getId());
        if (existing == null) {
            throw new BusinessException("分类不存在");
        }
        Category category = new Category();
        category.setId(dto.getId());
        category.setName(dto.getName());
        category.setIcon(dto.getIcon());
        category.setSort(dto.getSort());
        category.setStatus(dto.getStatus());
        category.setUpdateTime(LocalDateTime.now());
        int updated = categoryMapper.updateById(category);
        if (updated <= 0) {
            throw new BusinessException("更新分类失败");
        }
        stringRedisTemplate.delete(RedisConstant.CATEGORY_LIST);
    }

    @Override
    public void deleteCategory(Long id) {
        if (id == null) {
            throw new BusinessException("分类ID不能为空");
        }
        Long count = categoryMapper.countProductByCategoryId(id);
        if (count != null && count > 0) {
            throw new BusinessException("该分类下有商品，无法删除");
        }
        int deleted = categoryMapper.deleteById(id);
        if (deleted <= 0) {
            throw new BusinessException("删除分类失败");
        }
        stringRedisTemplate.delete(RedisConstant.CATEGORY_LIST);
    }

    private CategoryVO toCategoryVO(Category category) {
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setIcon(category.getIcon());
        vo.setSort(category.getSort());
        vo.setStatus(category.getStatus());
        return vo;
    }
}
