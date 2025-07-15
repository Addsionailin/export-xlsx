package com.poly.exportxlsx;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.poly.exportxlsx.mapper")
@SpringBootApplication
public class ExportXlsxApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExportXlsxApplication.class, args);
	}

}
