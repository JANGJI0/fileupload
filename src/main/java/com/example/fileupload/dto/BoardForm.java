package com.example.fileupload.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class BoardForm {
	private String title;
	private String pw;
	// 하나가아니라 여러개 넘어오기때문
	private List<MultipartFile> fileList; // spring 에서 input type="file"
}
