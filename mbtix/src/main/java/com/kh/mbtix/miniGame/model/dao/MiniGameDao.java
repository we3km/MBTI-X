package com.kh.mbtix.miniGame.model.dao;
import java.util.List;
import java.util.Map;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.kh.mbtix.miniGame.model.dto.GameRoom;
import com.kh.mbtix.miniGame.model.dto.GameRoomInfo;
import com.kh.mbtix.miniGame.model.dto.Gamer;
import com.kh.mbtix.miniGame.model.dto.Quiz;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Repository
public class MiniGameDao {
	@Autowired
	private SqlSessionTemplate session;
	public List<Quiz> selectQuiz() {
		return session.selectList("minimapper.selectQuiz");
	}
	@Transactional
	public void insertPoint(Map<String, Object> point) {
		session.insert("minimapper.insertPoint", point);
		session.update("minimapper.updateUserPoint", point);
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
		session.insert("minimapper.createGameRoom", map);
		Integer roomId = (Integer) map.get("roomId"); // 생성된 방 번호
		session.insert("minimapper.joinGameRoom", map);
		return roomId;
	}
	public void joinGameRoom(Map<String, Object> map) {
		session.update("minimapper.increasePlayerCount", map);
		session.insert("minimapper.joinGameRoom", map);
	}
	public List<Gamer> selectGamers(int roomId) {
		List<Gamer> gamers = session.selectList("minimapper.selectGamers", roomId);
		return gamers;
	}
	public GameRoomInfo selectGameRoomInfo(int roomId) {
		return session.selectOne("minimapper.selectGameRoomInfo", roomId);
	}
	public int leaveRoom(Map<String, Object> map) {
		session.update("minimapper.decreasePlayerCount", map);
		return session.delete("minimapper.leaveRoom", map);
	}
	public int deleteRoom(int roomId) {
		return session.delete("minimapper.deleteRoom", roomId);
	}
	public String getGameRoomStatus(Map<String, Object> map) {
		return session.selectOne("minimapper.getGameRoomStatus", map);
	}
	public void setGameState(Map<String, Object> map) {
		session.update("minimapper.setGameState", map);
	}
	public void changeCaptain(Map<String, Object> captainInfo) {
		session.update("minimapper.changeCaptain", captainInfo);
	}
	public List<String> selectCathMindWords() {
		return session.selectList("minimapper.selectCathMindWords");
	}
	public void changeRoomInfo(Map<String, Object> map) {
		session.update("minimapper.changeRoomInfo", map);
	}
	public void kickOut(Map<String, Object> map) {
		session.insert("minimapper.kickOut", map);
	}
	public void deleteKickOut(int roomId) {
		session.delete("minimapper.deleteKickOut", roomId);
	}
	public int checkKickOut(Map<String, Object> map) {		
		Integer result = session.selectOne("minimapper.checkKickOut", map);
	    return result != null ? result : 0;
	}
}

