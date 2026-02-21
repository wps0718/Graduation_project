package com.qingyuan.secondhand.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingyuan.secondhand.common.result.Result;
import com.qingyuan.secondhand.dto.EmployeeDTO;
import com.qingyuan.secondhand.dto.EmployeeLoginDTO;
import com.qingyuan.secondhand.service.EmployeeService;
import com.qingyuan.secondhand.vo.EmployeeLoginVO;
import com.qingyuan.secondhand.vo.EmployeeVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/employee")
@RequiredArgsConstructor
public class AdminEmployeeController {

    private final EmployeeService employeeService;

    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@Valid @RequestBody EmployeeLoginDTO dto) {
        return Result.success(employeeService.login(dto));
    }

    @GetMapping("/info")
    public Result<EmployeeVO> getInfo() {
        return Result.success(employeeService.getEmployeeInfo());
    }

    @GetMapping("/page")
    public Result<Page<EmployeeVO>> getPage(@RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer pageSize,
                                            @RequestParam(required = false) String keyword) {
        return Result.success(employeeService.getEmployeePage(page, pageSize, keyword));
    }

    @PostMapping("/add")
    public Result<Void> addEmployee(@Valid @RequestBody EmployeeDTO dto) {
        employeeService.addEmployee(dto);
        return Result.success();
    }

    @PostMapping("/update")
    public Result<Void> updateEmployee(@Valid @RequestBody EmployeeDTO dto) {
        employeeService.updateEmployee(dto);
        return Result.success();
    }

    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestParam Long id) {
        employeeService.resetPassword(id);
        return Result.success();
    }
}
