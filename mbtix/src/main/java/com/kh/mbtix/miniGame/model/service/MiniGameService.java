package com.kh.mbtix.miniGame.model.service;

import java.util.List;
import java.util.Map;

import com.kh.mbtix.miniGame.model.dto.GameRoom;
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

	public void leaveRoom(Map<String, Object> map);

	public void joinGameRoom(Map<String, Object> map);
}
