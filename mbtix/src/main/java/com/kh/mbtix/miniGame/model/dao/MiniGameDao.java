package com.kh.mbtix.miniGame.model.dao;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.mbtix.miniGame.model.dto.Quiz;

@Repository
public class MiniGameDao {

	@Autowired
	private SqlSessionTemplate session;

	public List<Quiz> selectQuiz() {
		return session.selectList("minimapper.selectQuiz");
	}

	public void insertPoint(Map<String, Object> point) {
		session.insert("minimapper.insertPoint", point);
	}

	public List<Map<String, Object>> getRank() {
		return session.selectList("minimapper.getRank");
	}

	public List<Map<String, Object>> getUserMBTI() {
		return session.selectList("minimapper.getUserMBTI");
	}

	public String getQuizTitle() {
		return session.selectOne("minimapper.getQuizTitle");
	}
}