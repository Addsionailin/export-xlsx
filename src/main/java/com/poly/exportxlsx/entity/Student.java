package com.poly.exportxlsx.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class Student {

    @TableId(type = IdType.ASSIGN_ID)
    @ExcelProperty(value = "编号")
    private String id;

    @ExcelProperty(value = "名称")
    private String name;

    @ExcelProperty(value = "年龄")
    private Integer age;

    @ExcelProperty(value = "性别")
    private String gender;

    @ExcelProperty(value = "住址")
    private String address;

    @ExcelProperty(value = "家庭联系电话")
    private String phone;
}
