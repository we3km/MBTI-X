package com.kh.mbtix.board.model.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.kh.mbtix.board.model.dao.BoardDao;
import com.kh.mbtix.board.model.vo.Board;
import com.kh.mbtix.board.model.vo.Board.BoardLike;
import com.kh.mbtix.board.model.vo.BoardComment;
//import com.kh.mbtix.board.model.vo.Report;
import com.kh.mbtix.common.util.FileStorage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardService {
	
	private final BoardDao dao;
	private final FileStorage fileStorage;
	
	public List<Board> selectBoardList(Map<String, Object> param) {
		return dao.selectBoardList(param);
	}

	public Board selectBoard(int boardId) {
		return dao.selectBoard(boardId);
	}
	public void incrementView(int boardId) {
		dao.incrementView(boardId);
	}
	
	@Transactional
	public int insertBoard(Board b, List<MultipartFile> images) {
		int result = dao.insertBoard(b);
		
		// b안에 mbti정보가 있으면 mbtiboard에도 데이터 추가하기.
		if(b.getCategoryId() == 3 && result > 0) {
			dao.inserMbtiBoard(b);
		}
		
		// 궁금해 게시판의 경우, 질문대상 mbti정보가 저장될 수 있도록 설정
		if(b.getCategoryId() == 1 && result > 0) {
			b.setMbtiName(b.getBoardMbti());
			dao.inserMbtiBoard(b);
		}
		
		if (result > 0 && images != null && !images.isEmpty()) {
	        for (MultipartFile file : images) {
	            // 파일 저장 로직 (ex. 로컬 디렉토리 / AWS S3 / DB)
	            String savedPath = fileStorage.save(file);
	            HashMap<String, Object> map = new HashMap<>();
		   		map.put("fileName", savedPath);
		   		map.put("refId", b.getBoardId());
		   		map.put("categoryId", b.getCategoryId());
	            dao.insertBoardImage(map);
	        }
	    }
		
		return result;		
	}

	/*public int insertReport(Report r) {
		return dao.insertReport(r);
	}
	*/
	public void insertComment(BoardComment c) {
		int comment = dao.insertComment(c);
		
	}

	public List<BoardComment> getComments(int boardId) {
		return dao.getComments(boardId);
	}

	public boolean deleteBoard(int boardId, long userId) {
		return dao.deleteBoard(boardId, userId) > 0;
	}

	public int insertLike(BoardLike map) {
//		처음 좋아요 싫어요 -> insert
//		두번째부터 -> 좋아요를 누른 상태에서 다시 좋아요 -> 취소.
//		                  좋아요를 누른 상태에서 싫어요 -> 좋아요에서 싫어요로 변경
		String savedStatus = dao.selectBoardLike(map);
		if(savedStatus != null) {
			// 이미 좋아요 혹은 싫어요를 누른 경우
			String status = map.getStatus(); // '1' 좋아요, '2' 싫어요			
			System.out.println(status+"----------------"+savedStatus);
			if(status.equals(savedStatus)) {
				// 좋아요요청인데 좋아요상태값이 저장된경우
				// 삭제
				return dao.deleteLike(map);
			}else {
				// 업데이트
				return dao.updateLike(map);
			}
		}
		
		return dao.insertLike(map);
	}

	public int deleteComment(BoardComment comment) {
		return dao.deleteComment(comment);
	}

	public boolean deleteComment(int commentId, Long userId) {
		// TODO Auto-generated method stub
		return false;
	}
}
