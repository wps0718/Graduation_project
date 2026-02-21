package com.qingyuan.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qingyuan.secondhand.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}
