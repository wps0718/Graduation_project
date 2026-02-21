package com.qingyuan.secondhand.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingyuan.secondhand.dto.CategoryDTO;
import com.qingyuan.secondhand.entity.Category;
import com.qingyuan.secondhand.vo.CategoryVO;

import java.util.List;

public interface CategoryService extends IService<Category> {
    List<CategoryVO> getMiniList();

    Page<CategoryVO> getAdminPage(Integer page, Integer pageSize, String name);

    List<CategoryVO> getAdminList();

    void addCategory(CategoryDTO dto);

    void updateCategory(CategoryDTO dto);

    void deleteCategory(Long id);
}
