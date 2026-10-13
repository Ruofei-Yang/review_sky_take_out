package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @Override
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // TODO 后期需要进行md5加密，然后再进行比对
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    //新增员工
    @Override
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO , employee);
        //补全剩余字段
        employee.setStatus(1);
        employee.setPassword("RDEFAULT_PASSWOD");
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        employee.setCreateUser(10L);
        employee.setUpdateUser(10L);

        employeeMapper.save(employee);

    }

    //分页查询
    @Override
    public PageResult page(EmployeePageQueryDTO employeePageQueryDTO){
        //开启分页查询
        PageHelper.startPage(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());
        //强转，因为前端还需要总数，要把员工数据和总数装到一起(Page是分页器自带！！total是拦截器自动加)
        Page<Employee> page=(Page<Employee>)employeeMapper.getPage(employeePageQueryDTO);

        Long total = page.getTotal();   //注意，不是你觉得是int就是，你得看分页器定义的total字段是什么类型
        List<Employee> records = new ArrayList< >(page.getResult());  //getResult返回的是取到的数据
        return new PageResult(total,records);

    }


    @Override
    public void setStatus(Integer status,Long id){
        employeeMapper.setStatus(status,id);

    }

    @Override
    public Employee getById(Long id){
        return employeeMapper.getById(id);
    }

    @Override
    public void update(EmployeeDTO employeeDTO){
        Employee employee = new Employee();

        BeanUtils.copyProperties(employeeDTO, employee);

        //你是直接覆盖，所以往employee里装的只需要有可能修改的字段就可以，不改的用默认值也行！
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(10L);
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setCreateUser(10L);          这两个字段不用复制！！status也不用

        employeeMapper.update(employee);
    }




}
