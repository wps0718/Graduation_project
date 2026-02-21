package com.qingyuan.secondhand.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingyuan.secondhand.common.result.Result;
import com.qingyuan.secondhand.dto.CategoryDTO;
import com.qingyuan.secondhand.service.CategoryService;
import com.qingyuan.secondhand.vo.CategoryVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/category")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

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
}
