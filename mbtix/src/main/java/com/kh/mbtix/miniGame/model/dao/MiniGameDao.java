package com.kh.mbtix.miniGame.model.dao;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.kh.mbtix.miniGame.model.dto.GameRoom;
import com.kh.mbtix.miniGame.model.dto.Gamer;
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

	public List<GameRoom> selectGameRoomList() {
		return session.selectList("minimapper.selectGameRoomList");
	}

	public int createGameRoom(Map<String, Object> map) {
		session.insert("minimapper.createGameRoom", map); // insert 행 수 반환
		Integer roomId = (Integer) map.get("roomId");     // 생성된 방 번호
		session.insert("minimapper.joinGameRoom", map);
		return roomId;
	}

	public void leaveRoom(Map<String, Object> map) {
		session.update("minimapper.leaveRoom", map);
	}

	public void joinGameRoom(Map<String, Object> map) {
		session.insert("minimapper.joinGameRoom", map);
	}

	// 게임방 내 플레이어
	public List<Gamer> selectGamers(int roomId) {
		return session.selectList("minimapper.selectGamers", roomId);
	}
}