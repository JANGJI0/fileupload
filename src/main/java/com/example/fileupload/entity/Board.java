package com.example.fileupload.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Data
@Entity
@Table(name="board")
public class Board {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int bno;
	@Column(name="title")
	private String title;
	@Column(name="pw")
	private String pw;
	
	// 🔽 DB에 저장되지 않지만 화면에 보여줄 용도
    @Transient
    private boolean hasFile;

    @Transient
    private String thumbnailFileName;
	
	// 사용하지 않는 연관 관계 설정 주석처리
	// Boardfile 관계설정 X
	// 양방양 아니므로 List 참조안함
	
}
