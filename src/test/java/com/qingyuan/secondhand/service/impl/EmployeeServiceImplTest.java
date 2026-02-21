package com.qingyuan.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingyuan.secondhand.common.context.UserContext;
import com.qingyuan.secondhand.common.exception.BusinessException;
import com.qingyuan.secondhand.common.util.JwtUtil;
import com.qingyuan.secondhand.dto.EmployeeDTO;
import com.qingyuan.secondhand.dto.EmployeeLoginDTO;
import com.qingyuan.secondhand.entity.Employee;
import com.qingyuan.secondhand.mapper.EmployeeMapper;
import com.qingyuan.secondhand.vo.EmployeeLoginVO;
import com.qingyuan.secondhand.vo.EmployeeVO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @AfterEach
    void clearContext() {
        UserContext.removeCurrentUserId();
        UserContext.removeCurrentUserType();
    }

    @Test
    void testLoginSuccess() {
        EmployeeMapper employeeMapper = Mockito.mock(EmployeeMapper.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        EmployeeServiceImpl service = new EmployeeServiceImpl(employeeMapper, passwordEncoder, jwtUtil);

        Employee employee = buildEmployee(1L, "admin", "hash", "管理员", "13800000000", 1, 1);
        Mockito.when(employeeMapper.selectOne(Mockito.any(LambdaQueryWrapper.class))).thenReturn(employee);
        Mockito.when(passwordEncoder.matches("123456", "hash")).thenReturn(true);
        Mockito.when(jwtUtil.createAdminToken(Mockito.eq(1L), Mockito.anyMap())).thenReturn("token-1");

        EmployeeLoginDTO dto = new EmployeeLoginDTO();
        dto.setUsername("admin");
        dto.setPassword("123456");
        EmployeeLoginVO vo = service.login(dto);

        Assertions.assertEquals(1L, vo.getId());
        Assertions.assertEquals("admin", vo.getUsername());
        Assertions.assertEquals("管理员", vo.getName());
        Assertions.assertEquals(1, vo.getRole());
        Assertions.assertEquals("token-1", vo.getToken());

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(jwtUtil).createAdminToken(Mockito.eq(1L), captor.capture());
        Assertions.assertEquals("admin", captor.getValue().get("type"));
    }

    @Test
    void testLoginPasswordError() {
        EmployeeMapper employeeMapper = Mockito.mock(EmployeeMapper.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        EmployeeServiceImpl service = new EmployeeServiceImpl(employeeMapper, passwordEncoder, jwtUtil);

        Employee employee = buildEmployee(2L, "admin", "hash", "管理员", "13800000000", 1, 1);
        Mockito.when(employeeMapper.selectOne(Mockito.any(LambdaQueryWrapper.class))).thenReturn(employee);
        Mockito.when(passwordEncoder.matches("bad", "hash")).thenReturn(false);

        EmployeeLoginDTO dto = new EmployeeLoginDTO();
        dto.setUsername("admin");
        dto.setPassword("bad");

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.login(dto));
        Assertions.assertEquals("用户名或密码错误", ex.getMsg());
        Mockito.verifyNoInteractions(jwtUtil);
    }

    @Test
    void testLoginDisabled() {
        EmployeeMapper employeeMapper = Mockito.mock(EmployeeMapper.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        EmployeeServiceImpl service = new EmployeeServiceImpl(employeeMapper, passwordEncoder, jwtUtil);

        Employee employee = buildEmployee(3L, "admin", "hash", "管理员", "13800000000", 1, 0);
        Mockito.when(employeeMapper.selectOne(Mockito.any(LambdaQueryWrapper.class))).thenReturn(employee);

        EmployeeLoginDTO dto = new EmployeeLoginDTO();
        dto.setUsername("admin");
        dto.setPassword("123456");

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.login(dto));
        Assertions.assertEquals("账号已被禁用", ex.getMsg());
    }

    @Test
    void testAddEmployeePermissionDenied() {
        EmployeeMapper employeeMapper = Mockito.mock(EmployeeMapper.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        EmployeeServiceImpl service = new EmployeeServiceImpl(employeeMapper, passwordEncoder, jwtUtil);

        UserContext.setCurrentUserId(9L);
        Employee operator = buildEmployee(9L, "ops", "hash", "普通", null, 2, 1);
        Mockito.when(employeeMapper.selectById(9L)).thenReturn(operator);

        EmployeeDTO dto = new EmployeeDTO();
        dto.setUsername("newuser");
        dto.setName("新员工");
        dto.setRole(2);
        dto.setStatus(1);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.addEmployee(dto));
        Assertions.assertEquals("无权限操作", ex.getMsg());
    }

    @Test
    void testAddEmployeeSuccess() {
        EmployeeMapper employeeMapper = Mockito.mock(EmployeeMapper.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        EmployeeServiceImpl service = new EmployeeServiceImpl(employeeMapper, passwordEncoder, jwtUtil);

        UserContext.setCurrentUserId(1L);
        Employee admin = buildEmployee(1L, "admin", "hash", "超级管理员", null, 1, 1);
        Mockito.when(employeeMapper.selectById(1L)).thenReturn(admin);
        Mockito.when(employeeMapper.selectOne(Mockito.any(LambdaQueryWrapper.class))).thenReturn(null);
        Mockito.when(passwordEncoder.encode("123456")).thenReturn("encoded");
        Mockito.when(employeeMapper.insert(Mockito.any(Employee.class))).thenReturn(1);

        EmployeeDTO dto = new EmployeeDTO();
        dto.setUsername("newuser");
        dto.setName("新员工");
        dto.setPhone("13800000001");
        dto.setRole(2);
        dto.setStatus(1);
        service.addEmployee(dto);

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        Mockito.verify(employeeMapper).insert(captor.capture());
        Assertions.assertEquals("newuser", captor.getValue().getUsername());
        Assertions.assertEquals("encoded", captor.getValue().getPassword());
    }

    @Test
    void testUpdateEmployeeUsernameExists() {
        EmployeeMapper employeeMapper = Mockito.mock(EmployeeMapper.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        EmployeeServiceImpl service = new EmployeeServiceImpl(employeeMapper, passwordEncoder, jwtUtil);

        UserContext.setCurrentUserId(1L);
        Employee admin = buildEmployee(1L, "admin", "hash", "超级管理员", null, 1, 1);
        Employee existing = buildEmployee(5L, "old", "hash", "旧员工", null, 2, 1);
        Employee duplicate = buildEmployee(6L, "dup", "hash", "重复", null, 2, 1);
        Mockito.when(employeeMapper.selectById(1L)).thenReturn(admin);
        Mockito.when(employeeMapper.selectById(5L)).thenReturn(existing);
        Mockito.when(employeeMapper.selectOne(Mockito.any(LambdaQueryWrapper.class))).thenReturn(duplicate);

        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(5L);
        dto.setUsername("dup");
        dto.setName("旧员工");
        dto.setRole(2);
        dto.setStatus(1);

        BusinessException ex = Assertions.assertThrows(BusinessException.class, () -> service.updateEmployee(dto));
        Assertions.assertEquals("用户名已存在", ex.getMsg());
    }

    @Test
    void testResetPasswordSuccess() {
        EmployeeMapper employeeMapper = Mockito.mock(EmployeeMapper.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        EmployeeServiceImpl service = new EmployeeServiceImpl(employeeMapper, passwordEncoder, jwtUtil);

        UserContext.setCurrentUserId(1L);
        Employee admin = buildEmployee(1L, "admin", "hash", "超级管理员", null, 1, 1);
        Employee target = buildEmployee(10L, "user", "hash", "员工", null, 2, 1);
        Mockito.when(employeeMapper.selectById(1L)).thenReturn(admin);
        Mockito.when(employeeMapper.selectById(10L)).thenReturn(target);
        Mockito.when(passwordEncoder.encode("123456")).thenReturn("encoded");
        Mockito.when(employeeMapper.updateById(Mockito.any(Employee.class))).thenReturn(1);

        service.resetPassword(10L);

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        Mockito.verify(employeeMapper).updateById(captor.capture());
        Assertions.assertEquals("encoded", captor.getValue().getPassword());
    }

    @Test
    void testGetEmployeeInfoSuccess() {
        EmployeeMapper employeeMapper = Mockito.mock(EmployeeMapper.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        EmployeeServiceImpl service = new EmployeeServiceImpl(employeeMapper, passwordEncoder, jwtUtil);

        UserContext.setCurrentUserId(7L);
        Employee employee = buildEmployee(7L, "ops", "hash", "员工", "13800000002", 2, 1);
        Mockito.when(employeeMapper.selectById(7L)).thenReturn(employee);

        EmployeeVO vo = service.getEmployeeInfo();

        Assertions.assertEquals(7L, vo.getId());
        Assertions.assertEquals("ops", vo.getUsername());
    }

    @Test
    void testGetEmployeePageSuccess() {
        EmployeeMapper employeeMapper = Mockito.mock(EmployeeMapper.class);
        BCryptPasswordEncoder passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        JwtUtil jwtUtil = Mockito.mock(JwtUtil.class);
        EmployeeServiceImpl service = new EmployeeServiceImpl(employeeMapper, passwordEncoder, jwtUtil);

        Page<Employee> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(List.of(buildEmployee(8L, "user", "hash", "员工", null, 2, 1)));
        Mockito.when(employeeMapper.selectPage(Mockito.any(Page.class), Mockito.any(LambdaQueryWrapper.class))).thenReturn(page);

        Page<EmployeeVO> result = service.getEmployeePage(1, 10, "user");

        Assertions.assertEquals(1, result.getTotal());
        Assertions.assertEquals("user", result.getRecords().get(0).getUsername());
    }

    private Employee buildEmployee(Long id, String username, String password, String name, String phone, Integer role, Integer status) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setUsername(username);
        employee.setPassword(password);
        employee.setName(name);
        employee.setPhone(phone);
        employee.setRole(role);
        employee.setStatus(status);
        return employee;
    }
}
