package com.example.fileupload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class FileuploadApplication implements WebMvcConfigurer {

	public static void main(String[] args) {
		SpringApplication.run(FileuploadApplication.class, args);
	}
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/upload/**").addResourceLocations("file:///C:/project/upload/");
		// / **  "특정 경로로 들어오는 요청을 전부 처리할 때"
	}
}
