package com.example.fileupload.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.fileupload.dto.BoardForm;
import com.example.fileupload.entity.Board;
import com.example.fileupload.entity.BoardMapping;
import com.example.fileupload.entity.Boardfile;
import com.example.fileupload.repository.BoardRepository;
import com.example.fileupload.repository.BoardfileRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class BoardController {
	
	@Autowired BoardRepository boardRepository;
	@Autowired BoardfileRepository boardfileRepository;
	
	@GetMapping("/boardOne")
	public String boardOne(Model model, @RequestParam(value = "bno") int bno) {
		BoardMapping boardMapping = boardRepository.findByBno(bno);
		
		List<Boardfile> fileList = boardfileRepository.findByBno(bno);
		log.debug("size: " + fileList.size());
		
		model.addAttribute("boardMapping", boardMapping);
		model.addAttribute("fileList", fileList);
		return "boardOne";
	}
	
	@GetMapping({"/","/boardList"})
	public String boardList(Model model) {
		// 페이징 ( 마지막에 올라온것을 위로)
		// Sort : bno DESC
		// PageRequest : 1, 10
		// Page<BoardMapping>
		List<BoardMapping> list = boardRepository.findAllBy();
		model.addAttribute("list", list);
		return "boardList";
	}
	
	// 입력폼
	@GetMapping("/addBoard")
	public String addBoard() {
		return "addBoard";
	}
	
	
	// 입력 액션
	@PostMapping("/addBoard")
	public String addBoard(BoardForm boardForm) {
		log.debug(boardForm.toString());
		// 파일을 첨부하지 않아도 fileSize는 1이다.
		log.debug("MultipartFile Size: " + boardForm.getFileList().size());
		
		Board board = new Board();
		board.setTitle(boardForm.getTitle());
		board.setPw(boardForm.getPw());
		boardRepository.save(board); // board 저장
		int bno = board.getBno(); // board insert 후 bno 변경되었는지
		log.debug("bno: " + bno);
		
		// 파일 분리
		List<MultipartFile> fileList = boardForm.getFileList();
		long firstFileSize = fileList.get(0).getSize();
		log.debug("firstFileSize: " + firstFileSize);
		
		// 이슈: 파일을 첨부하지 않아도 fileSize는 1이다
		if(firstFileSize > 0) { // 첫번째 파일 사이즈가 0이상이다 -> 첨부된 파일이 있다.
			// 업로드 파일 유효성 검증 코드 구현
			for(MultipartFile f : fileList) {
				if(f.getContentType().equals("application/octet-stream") || f.getSize() > 10 * 1024 * 1024) { // 1KB = 1024byte, 1MB = 1024kB 
					log.debug("실행파일이나 10MB이상 파일은 업로드가 안됩니다.");
					return "redirect:/addBoard"; //Msg 추가
				}
			}
			
			// 파일 업로드 진행 코드
			for(MultipartFile f : fileList) {
				log.debug("파일타입: " +  f.getContentType());
				log.debug("원본이름: " +  f.getOriginalFilename());
				log.debug("파일크기: " +  f.getSize());
				// 확장자 추출
				String ext = f.getOriginalFilename().substring(f.getOriginalFilename().lastIndexOf(".") + 1); // 점은 필요없으니 + 1
				log.debug("확장자: " + ext);
				String fname = UUID.randomUUID().toString().replace("-", "");
				log.debug("저장파일이름: " + fname);
				
				File emptyFile = new File("c:/project/upload/" + fname + "." + ext);
				// f 의 byte -> emptyFile 복사
				try {
					f.transferTo(emptyFile);
				} catch (Exception e) {
					log.error("파일저장 실패!");
					e.printStackTrace();
				}
				
				// boardFile테이블에도 파일정보 저장
				Boardfile boardfile = new Boardfile();
				boardfile.setBno(bno);
				boardfile.setFext(ext);
				boardfile.setFname(fname);
				boardfile.setForiginname(f.getOriginalFilename());
				boardfile.setFsize(f.getSize());
				boardfile.setFtype(f.getContentType());
				boardfileRepository.save(boardfile);
				
			}
			
		}
		
		return "redirect:/";
	}
}
