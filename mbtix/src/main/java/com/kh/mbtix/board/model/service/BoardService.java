package com.kh.mbtix.board.model.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.kh.mbtix.board.model.dao.BoardDao;
import com.kh.mbtix.board.model.vo.Board;
import com.kh.mbtix.board.model.vo.BoardComment;
import com.kh.mbtix.board.model.vo.Report;
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
		if(b.getCategoryId() == 2 && result > 0) {
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

	public int insertReport(Report r) {
		return dao.insertReport(r);
	}
	
	public void insertComment(BoardComment c) {
		int comment = dao.insertComment(c);
		
	}

	public List<BoardComment> getComments(int boardId) {
		return dao.getComments(boardId);
	}



}
