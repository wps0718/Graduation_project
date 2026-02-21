package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qingyuan.secondhand.common.context.UserContext;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.common.util.JwtUtil;
import com.qingyuan.secondhand.dto.EmployeeDTO;
import com.qingyuan.secondhand.dto.EmployeeLoginDTO;
import com.qingyuan.secondhand.entity.Employee;
import com.qingyuan.secondhand.mapper.EmployeeMapper;
import com.qingyuan.secondhand.service.EmployeeService;
import com.qingyuan.secondhand.vo.EmployeeLoginVO;
import com.qingyuan.secondhand.vo.EmployeeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    private static final String DEFAULT_PASSWORD = "123456";
    private static final Integer SUPER_ADMIN_ROLE = 1;

    private final EmployeeMapper employeeMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public EmployeeLoginVO login(EmployeeLoginDTO dto) {
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getUsername, dto.getUsername());
        Employee employee = employeeMapper.selectOne(wrapper);
        if (employee == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (employee.getStatus() != null && employee.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        boolean matches = passwordEncoder.matches(dto.getPassword(), employee.getPassword());
        if (!matches) {
            throw new BusinessException("用户名或密码错误");
        }
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "admin");
        claims.put("adminId", employee.getId());
        String token = jwtUtil.createAdminToken(employee.getId(), claims);

        EmployeeLoginVO vo = new EmployeeLoginVO();
        vo.setId(employee.getId());
        vo.setUsername(employee.getUsername());
        vo.setName(employee.getName());
        vo.setRole(employee.getRole());
        vo.setToken(token);
        return vo;
    }

    @Override
    public EmployeeVO getEmployeeInfo() {
        Long currentId = UserContext.getCurrentUserId();
        if (currentId == null) {
            throw new BusinessException("未登录");
        }
        Employee employee = employeeMapper.selectById(currentId);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }
        return toEmployeeVO(employee);
    }

    @Override
    public Page<EmployeeVO> getEmployeePage(Integer page, Integer pageSize, String keyword) {
        Page<Employee> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Employee::getUsername, keyword)
                    .or()
                    .like(Employee::getName, keyword)
                    .or()
                    .like(Employee::getPhone, keyword));
        }
        wrapper.orderByDesc(Employee::getCreateTime);
        Page<Employee> result = employeeMapper.selectPage(pageObj, wrapper);
        Page<EmployeeVO> voPage = new Page<>(result.getCurrent(), result.getSize());
        voPage.setTotal(result.getTotal());
        List<EmployeeVO> records = result.getRecords() == null ? List.of() : result.getRecords().stream().map(this::toEmployeeVO).toList();
        voPage.setRecords(records);
        return voPage;
    }

    @Override
    public void addEmployee(EmployeeDTO dto) {
        Employee operator = getCurrentOperator();
        if (!SUPER_ADMIN_ROLE.equals(operator.getRole())) {
            throw new BusinessException("无权限操作");
        }
        ensureUsernameNotExists(dto.getUsername(), null);
        Employee employee = new Employee();
        employee.setUsername(dto.getUsername());
        employee.setName(dto.getName());
        employee.setPhone(dto.getPhone());
        employee.setRole(dto.getRole());
        employee.setStatus(dto.getStatus());
        employee.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        int inserted = employeeMapper.insert(employee);
        if (inserted <= 0) {
            throw new BusinessException("新增员工失败");
        }
    }

    @Override
    public void updateEmployee(EmployeeDTO dto) {
        Employee operator = getCurrentOperator();
        if (!SUPER_ADMIN_ROLE.equals(operator.getRole())) {
            throw new BusinessException("无权限操作");
        }
        if (dto.getId() == null) {
            throw new BusinessException("员工ID不能为空");
        }
        Employee existing = employeeMapper.selectById(dto.getId());
        if (existing == null) {
            throw new BusinessException("员工不存在");
        }
        ensureUsernameNotExists(dto.getUsername(), dto.getId());
        Employee employee = new Employee();
        employee.setId(dto.getId());
        employee.setUsername(dto.getUsername());
        employee.setName(dto.getName());
        employee.setPhone(dto.getPhone());
        employee.setRole(dto.getRole());
        employee.setStatus(dto.getStatus());
        employee.setUpdateTime(LocalDateTime.now());
        int updated = employeeMapper.updateById(employee);
        if (updated <= 0) {
            throw new BusinessException("更新员工失败");
        }
    }

    @Override
    public void resetPassword(Long id) {
        Employee operator = getCurrentOperator();
        if (!SUPER_ADMIN_ROLE.equals(operator.getRole())) {
            throw new BusinessException("无权限操作");
        }
        if (id == null) {
            throw new BusinessException("员工ID不能为空");
        }
        Employee existing = employeeMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("员工不存在");
        }
        Employee employee = new Employee();
        employee.setId(id);
        employee.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        employee.setUpdateTime(LocalDateTime.now());
        int updated = employeeMapper.updateById(employee);
        if (updated <= 0) {
            throw new BusinessException("重置密码失败");
        }
    }

    private Employee getCurrentOperator() {
        Long currentId = UserContext.getCurrentUserId();
        if (currentId == null) {
            throw new BusinessException("未登录");
        }
        Employee employee = employeeMapper.selectById(currentId);
        if (employee == null) {
            throw new BusinessException("员工不存在");
        }
        return employee;
    }

    private void ensureUsernameNotExists(String username, Long excludeId) {
        if (!StringUtils.hasText(username)) {
            throw new BusinessException("用户名不能为空");
        }
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getUsername, username);
        if (excludeId != null) {
            wrapper.ne(Employee::getId, excludeId);
        }
        Employee existing = employeeMapper.selectOne(wrapper);
        if (existing != null) {
            throw new BusinessException("用户名已存在");
        }
    }

    private EmployeeVO toEmployeeVO(Employee employee) {
        EmployeeVO vo = new EmployeeVO();
        vo.setId(employee.getId());
        vo.setUsername(employee.getUsername());
        vo.setName(employee.getName());
        vo.setPhone(employee.getPhone());
        vo.setRole(employee.getRole());
        vo.setStatus(employee.getStatus());
        vo.setCreateTime(employee.getCreateTime());
        vo.setUpdateTime(employee.getUpdateTime());
        return vo;
    }
}
