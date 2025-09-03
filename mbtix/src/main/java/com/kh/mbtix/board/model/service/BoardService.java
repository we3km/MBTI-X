package com.kh.mbtix.board.model.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.mbtix.board.model.dao.BoardDao;
import com.kh.mbtix.board.model.vo.Board;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardService {
	
	private final BoardDao dao;
	
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
	public int insertBoard(Board b) {
		int result = dao.insertBoard(b);
		
		// b안에 mbti정보가 있으면 mbtiboard에도 데이터 추가하기.
		if(b.getCategoryId() == 2 && result > 0) {
			dao.inserMbtiBoard(b);
		}
		
		return result;		
	}



}
