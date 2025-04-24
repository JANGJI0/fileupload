package com.example.fileupload.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
	
	
	//board 삭제 폼
	@GetMapping("/removeBoard")
	public String removeBoard(Model model, @RequestParam("bno") int bno) {
		model.addAttribute("bno", bno); // 삭제 대상 게시글 번호 전달
		return "removeBoard"; // 삭제폼 페이지로 이동
	}
	
	// board 삭제 액션
	@PostMapping("/removeBoard")
	public String removeBoard(@RequestParam("bno") int bno // 삭제할 게시글 번호
							, @RequestParam("pw") String pw // 사용자가 입력한 비밀번호
							, RedirectAttributes rda) {		// 메세지 전달용
		
		// 1. 게시글 번호로 DB에서 게시글 조회
		Board board = boardRepository.findById(bno).orElse(null);
		
		// 2. 게시글이 존재하지 않으면 목록으로(예외 발생 대비 ex) 보고있는데 누가 삭제할 경우
		if(board == null) {
			rda.addFlashAttribute("msg", "존재하지 않는 게시글입니다.");
			return "redirect:/boardList";
		}
		
		// 3. 첨부된 파일이 있는지 확인 (boardfile 테이블에서 bno로 조회)
		List<Boardfile> fileList = boardfileRepository.findByBno(bno);
		
		// 4. 파일이 하나라도 있으면 삭제 막기
		if(fileList != null && !fileList.isEmpty()) {
			rda.addFlashAttribute("msg", "첨부파일이 있는 게시글은 삭제할 수 없습니다.");
			return "redirect:/boardOne?bno=" + bno; // 게시글 상세보기로 이동
		}
		
		// 5. 비밀번호가 맞는지 확인
		if(!board.getPw().equals(pw)) {
			rda.addFlashAttribute("msg", "비밀번호가 일치하지 않습니다.");
			return "redirect:/removeBoard?bno=" + bno; // 삭제 폼으로 다시 이동
		}
		
		// 6. 조건 다 만족하면 게시글 삭제 진행
		boardRepository.delete(board);
		
		// 7. 성공 메세지와 함께 목록으로 이동
		rda.addFlashAttribute("msg", "게시글이 성공적으로 삭제되었습니다.");
		return "redirect:/boardList";
		
		
	}
	
	// board 수정 폼
	@GetMapping("/modifyBoard")
	public String modifyBoard(Model model, @RequestParam("bno") int bno) { // 수정할 게시글 번호
		
		// 1. 게시글 번호(bno)로 해당 게시글을 DB에서 가져오기
		Board board = boardRepository.findById(bno).orElse(null);
		
		// 2. 해당 번호의 게시글이 없을 경우 목록으로 보낸다. ( 예외상황을 대비 ex) 동시에 삭제 or 클릭)
		if (board == null) {
			return "redirect:/boardList";
		}
		
		// 3. 게시글 정보가 있으면 모델에 담아서 수정폼으로 이동
		model.addAttribute("bno", board.getBno()); // 게시글 번호
		model.addAttribute("title", board.getTitle()); // 게시글 제목(수정용)
		
		// 4. 수정 폼으로 이동
		return "modifyBoard"; // 리다이렉트 아닌이유 : 서버에서 해당 뷰로 전달해서 보여줘야 하기 때문
	}
		// board 수정 액션
		@PostMapping("/modifyBoard")
		public String modifyBoard(@RequestParam("bno") int bno			// 수정할 게시글 번호
								, @RequestParam("title") String title	// 사용자가 입력한 새 제목
								, @RequestParam("pw") String pw			// 사용자가 입력한 비밀번호
								, RedirectAttributes rda) {				// 리다이렉트 시 메세지 전달
			
			// 1. 게시글 번호로 DB에서 해당 게시글 조회
			Board board = boardRepository.findById(bno).orElse(null);
			
			// 2. 게시글이 없으면 목록으로 보내고 오류 메세지 전달
			if(board == null) {
				rda.addFlashAttribute("msg", "게시글이 존재하지 않습니다.");
				return "redirect:/boardList";
			}
			
			// 3. 입력한 비밀번호가 다를 경우 수정실패
			if(!board.getPw().equals(pw)) {
				rda.addFlashAttribute("msg","비밀번호가 일치하지 않습니다.");
				return "redirect:/modifyBoard?bno=" + bno;
			}
			
			// 4. 비밀번호가 맞으면 제목만 수정하고 저장
			board.setTitle(title); // 기존 board 객체의 제목 변경
			boardRepository.save(board); // 수정된 객체 저장(UPDATE)
			
			// 5. 수정 완료되면 해당 게시글 상세보기 페이지로 이동
			return "redirect:/boardOne?bno=" + bno;
			
		}
		
	
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
	public String boardList(Model model
							, @RequestParam(value = "currentPage", defaultValue = "0") int currentPage
							, @RequestParam(value = "rowPerPage", defaultValue = "4") int rowPerPage
							, @RequestParam(value = "word", defaultValue = "") String word) {
		
		// 1. 정렬 기준 설정 (memberId 내림차순)
		Sort sort = Sort.by("bno").descending();
		
		// 2. pageable 생성(페이지 번호, 페이지당 글 수, 정렬)
		PageRequest Pageable = PageRequest.of(currentPage,rowPerPage, sort); // 0페이지, 3개
		
		// 3. 검색어 기준으로 페이징 된 id 가져오기
		Page<Board> list = boardRepository.findByTitleContaining(Pageable, word);
		
		// 4. 로그로 페이징 정보 확인
		log.debug("list.getToalElements(): " + list.getTotalElements()); //전체 행의 사이즈
		log.debug("list.getTotalPages(): " + list.getTotalPages()); // 전체 페이지 사이즈 lastPage
		log.debug("list.getNumber(): " + list.getNumber()); // 현재 페이지 사이즈
		log.debug("list.getSize(): " + list.getSize()); // rowPerPage
		log.debug("list.isFirst(): " + list.isFirst()); // 1페이지인지 : 이전 링크유무
		log.debug("list.hasNext(): " + list.hasNext()); // 다음이 있는지 : 다음링크유무
		
		// 5. view에 전달할 데이터 추가
		model.addAttribute("list", list);
		model.addAttribute("TotalPage", list.getTotalPages()); // 전체 페이지
		model.addAttribute("currentPage", list.getNumber() + 1); // 시작 페이지 무조건 0값을 가져와서 +1 해야 1페이지부터 시작
		model.addAttribute("prePage", list.getNumber() - 1);
		model.addAttribute("nextPage", list.getNumber() + 1);
		model.addAttribute("lastPage", list.getTotalPages() - 1); // 마지막으로 가는 페이지
		model.addAttribute("word", word); // 페이징 할 때 검색어 유지
		
		// 페이징 ( 마지막에 올라온것을 위로)
		// Sort : bno DESC
		// PageRequest : 1, 10
		// Page<BoardMapping>
		for (Board b : list.getContent()) {
		    List<Boardfile> files = boardfileRepository.findByBno(b.getBno());

		    if (files != null && !files.isEmpty()) {
		        b.setHasFile(true); // 🔹 Mustache에서 {{hasFile}} 표시용

		        // 첫 번째 파일을 썸네일로 사용
		        Boardfile first = files.get(0);
		        String fileName = first.getFname() + "." + first.getFext();
		        b.setThumbnailFileName(fileName); // 🔹 이미지 경로로 사용
		    }
		}
		// model.addAttribute("list", boardList);
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
