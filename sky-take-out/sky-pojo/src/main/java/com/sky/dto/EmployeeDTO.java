package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
//public class EmployeeDTO implements Serializable {    只有用到Redis之类的才写这个序列化
public class EmployeeDTO  {


    private Long id;

    private String username;

    private String name;

    private String phone;

    private String sex;

    private String idNumber;

}
