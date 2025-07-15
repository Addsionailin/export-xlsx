package com.poly.exportxlsx;

import com.github.javafaker.Faker;
import com.poly.exportxlsx.controller.StudentController;
import com.poly.exportxlsx.entity.Student;
import com.poly.exportxlsx.service.IStudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SpringBootTest
class ExportXlsxApplicationTests {
	@Autowired
	private IStudentService studentService;

	@Test
	void batchInsertStudents() {
		Faker faker = new Faker(Locale.CHINA); // 指定中文语境
		int batchSize = 2000; // 生成测试数据数量
		List<Student> studentList = new ArrayList<>();

		for (int i = 0; i < batchSize; i++) {
			Student student = new Student();
			// 使用Faker生成逼真数据
			student.setName(faker.name().fullName());
			student.setAge(faker.number().numberBetween(18, 25)); // 18~24岁
			student.setGender(faker.options().option("男", "女"));
			student.setPhone(faker.phoneNumber().cellPhone());
			student.setAddress(
					faker.address().state() +        // 省
							faker.address().cityName() +     // 市
							faker.address().streetAddress()  // 街道
			);

			studentList.add(student);
		}

		// 批量保存
		studentService.saveBatch(studentList);
		System.out.println("✅ 已生成并插入 " + batchSize + " 条学生数据");
	}

	@Test
	public void list() {
		Student student = new Student();

		System.out.println(studentService.list());
	}
}
