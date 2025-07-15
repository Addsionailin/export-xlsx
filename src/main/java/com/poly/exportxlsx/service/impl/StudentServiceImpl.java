package com.poly.exportxlsx.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.poly.exportxlsx.entity.Student;
import com.poly.exportxlsx.mapper.StudentMapper;
import com.poly.exportxlsx.service.IStudentService;
import org.springframework.stereotype.Service;

@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements IStudentService {

}
