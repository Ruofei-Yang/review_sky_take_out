package com.sky.mapper;

import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    //新增员工
    @Insert("insert into employee(name,username,password,phone,sex,id_number,status,create_time,update_time,create_user,update_user) values (#{name}, #{username},#{password}, #{phone}, #{sex},#{idNumber}, #{status}, #{createTime},#{updateTime}, #{createUser}, #{updateUser})")
    void save(Employee employee);

    //分页查询
    List<Employee> getPage(EmployeePageQueryDTO employeePageQueryDTO);

    //设置状态
    @Update("update employee set status = #{status} where id=#{id}")
    void setStatus(@Param("status") Integer status ,@Param("id") Long id);

    //根据id获取数据
    @Select("select * from employee where id=#{id}")
    Employee getById(@Param("id") Long id);

    //修改员工
    //注意：不传username，账号不能修改！
    @Update("update employee set name = #{name}, phone = #{phone}, sex = #{sex}, id_number = #{idNumber}, update_time = #{updateTime}, update_user = #{updateUser} where id = #{id}")
    void update(Employee employee);

}
