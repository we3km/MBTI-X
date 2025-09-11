package com.kh.mbtix.board.model.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.mbtix.board.model.vo.Board;
import com.kh.mbtix.board.model.vo.BoardComment;
import com.kh.mbtix.board.model.vo.Report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
@RequiredArgsConstructor
public class BoardDao {
	
	private final SqlSessionTemplate template;
	
	public List<Board> selectBoardList(Map<String, Object> param) {
		return template.selectList("board.selectBoardList", param);
	}

	public Board selectBoard(int boardId) {
		return template.selectOne("board.selectBoard", boardId);
	}

	public int insertBoard(Board b) {
		return template.insert("board.insertBoard", b);
	}

	public void incrementView(int boardId) {
		template.update("board.incrementView", boardId);
	}

	public void inserMbtiBoard(Board b) {
		template.insert("board.insertMbtiBoard", b);
	}

	public int insertReport(Report r) {
		return template.insert("board.insertReport", r);
	}
	
	public int insertComment(BoardComment comment) {
	    return template.insert("board.insertComment", comment);
	}

	public void insertBoardImage(HashMap<String, Object> map) {		
		 template.insert("board.insertBoardImage", map);
	}

	public List<BoardComment> getComments(int boardId) {
		return  template.selectList("board.getComments", boardId);
	}

}
