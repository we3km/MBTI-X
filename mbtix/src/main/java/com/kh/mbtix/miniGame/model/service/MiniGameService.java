package com.kh.mbtix.miniGame.model.service;

import java.util.List;
import java.util.Map;

import com.kh.mbtix.miniGame.model.dto.GameRoom;
import com.kh.mbtix.miniGame.model.dto.GameRoomInfo;
import com.kh.mbtix.miniGame.model.dto.Gamer;
import com.kh.mbtix.miniGame.model.dto.Quiz;

public interface MiniGameService {

	public List<Quiz> selectQuiz();

	public void insertPoint(Map<String, Object> point);

	public List<Map<String, Object>> getRank();

	public List<Map<String, Object>> getUserMBTI();

	public String getQuizTitle();

	public List<GameRoom> selectGameRoomList();

	public int createGameRoom(Map<String, Object> map);

	public List<Gamer> selectGamers(int roomId);

	public void joinGameRoom(Map<String, Object> map);

	public GameRoomInfo selectGameRoomInfo(int roomId);

	public int leaveRoom(Map<String, Object> map);

	public int deleteRoom(int roomId);
	
	public String getGameRoomStatus(Map<String, Object> map);
	
	public void setGameState(Map<String, Object> map);

	public void changeCaptain(Map<String, Object> captainInfo);

	public List<String> selectCathMindWords();

	public void changeRoomInfo(Map<String, Object> map);
}
