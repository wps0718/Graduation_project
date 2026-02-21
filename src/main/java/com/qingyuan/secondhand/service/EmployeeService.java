package com.qingyuan.secondhand.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qingyuan.secondhand.dto.EmployeeDTO;
import com.qingyuan.secondhand.dto.EmployeeLoginDTO;
import com.qingyuan.secondhand.entity.Employee;
import com.qingyuan.secondhand.vo.EmployeeLoginVO;
import com.qingyuan.secondhand.vo.EmployeeVO;

public interface EmployeeService extends IService<Employee> {
    EmployeeLoginVO login(EmployeeLoginDTO dto);

    EmployeeVO getEmployeeInfo();

    Page<EmployeeVO> getEmployeePage(Integer page, Integer pageSize, String keyword);

    void addEmployee(EmployeeDTO dto);

    void updateEmployee(EmployeeDTO dto);

    void resetPassword(Long id);
}
