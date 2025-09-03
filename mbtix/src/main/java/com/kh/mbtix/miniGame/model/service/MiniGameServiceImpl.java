package com.kh.mbtix.miniGame.model.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kh.mbtix.miniGame.model.dao.MiniGameDao;
import com.kh.mbtix.miniGame.model.dto.Quiz;

@Service
public class MiniGameServiceImpl implements MiniGameService {

	@Autowired
	private MiniGameDao dao;

	@Override
	public List<Quiz> selectQuiz() {
		return dao.selectQuiz();
	}

	@Override
	public void insertPoint(Map<String, Object> point) {
		dao.insertPoint(point);
	}

	@Override
	public List<Map<String, Object>> getRank() {
		return dao.getRank();
	}

	@Override
	public List<Map<String, Object>> getUserMBTI() {
		return dao.getUserMBTI();
	}

	@Override
	public String getQuizTitle() {
		return dao.getQuizTitle();
	}
}
