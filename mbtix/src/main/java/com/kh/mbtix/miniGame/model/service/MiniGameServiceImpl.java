package com.kh.mbtix.miniGame.model.service;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.kh.mbtix.miniGame.model.dao.MiniGameDao;
import com.kh.mbtix.miniGame.model.dto.GameRoom;
import com.kh.mbtix.miniGame.model.dto.GameRoomInfo;
import com.kh.mbtix.miniGame.model.dto.Gamer;
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
	@Override
	public List<GameRoom> selectGameRoomList() {
		return dao.selectGameRoomList();
	}
	@Override
	public int createGameRoom(Map<String, Object> map) {
		return dao.createGameRoom(map);
	}
	@Override
	public List<Gamer> selectGamers(int roomId) {
		return dao.selectGamers(roomId);
	}
	@Override
	public void joinGameRoom(Map<String, Object> map) {
		dao.joinGameRoom(map);
	}
	@Override
	public GameRoomInfo selectGameRoomInfo(int roomId) {
		return dao.selectGameRoomInfo(roomId);
	}
	@Override
	public int leaveRoom(Map<String, Object> map) {
		return dao.leaveRoom(map);
	}
	@Override
	public int deleteRoom(int roomId) {
		return dao.deleteRoom(roomId);
	}
	@Override
	public String getGameRoomStatus(Map<String, Object> map) {
		return dao.getGameRoomStatus(map);
	}
	@Override
	public void setGameState(Map<String, Object> map) {
		dao.setGameState(map);
	}
	@Override
	public void changeCaptain(Map<String, Object> captainInfo) {
		dao.changeCaptain(captainInfo);
	}
	@Override
	public List<String> selectCathMindWords() {
		return dao.selectCathMindWords();
	}
	@Override
	public void changeRoomInfo(Map<String, Object> map) {
		dao.changeRoomInfo(map);
	}
	@Override
	public void kickOut(Map<String, Object> map) {
		dao.kickOut(map);
	}
	@Override
	public void deleteKickOut(int roomId) {
		dao.deleteKickOut(roomId);
	}
	@Override
	public int checkKickOut(Map<String, Object> map) {
		return dao.checkKickOut(map);
	}
}