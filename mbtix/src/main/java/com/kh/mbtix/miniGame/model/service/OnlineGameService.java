package com.kh.mbtix.miniGame.model.service;

public interface OnlineGameService {
	public void startGame(int roomId);
	
	public void waitingGame(int roomId);
	
	public void resultGame(int roomId);
	
	public void drawingGame(int roomId);
	
	public void finalGame(int roomId);

}
