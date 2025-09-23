package com.kh.mbtix.mypage.model.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.mbtix.mypage.model.dto.MyPageDto.GameScore;
import com.kh.mbtix.mypage.model.dto.MyPageDto.UserBoard;
import com.kh.mbtix.mypage.model.dto.MyPageDto.UserProfileDto;
import com.kh.mbtix.security.model.dto.AuthDto.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MyPageDao {
	
	private final SqlSessionTemplate session; 
	
	public void minusPoint(Long userId, int i) {
		Map<String, Object> params = new HashMap<>();
	    params.put("userId", userId);
	    params.put("i", i);
		
		session.update("mypage.minusPoint",params);
	}

	public void updateNickname(Long userId, String newNickname) {
		Map<String, Object> params = new HashMap<>();
	    params.put("userId", userId);
	    params.put("newNickname", newNickname);
		
	    session.update("mypage.updateNickname",params);
	}

	public User findUserByUserId(Long userId) {
		return session.selectOne("mypage.findUserByUserId",userId);

	}

	public User findUserById(Long userId) {
		return session.selectOne("mypage.findUserById",userId);
	}

	public void updatePassword(User user) {
		session.update("mypage.updatePassword",user);
	}

	public void updateProfileImg(Long userId, String savedFileName) {
		Map<String, Object> params = new HashMap<>();
		params.put("userId", userId);
		params.put("savedFileName", savedFileName);
		session.update("mypage.updateProfileImg",params);
	}

	public void updateProfileType(Long userId, int i) {
		Map<String, Object> params = new HashMap<>();
	    params.put("userId", userId);
	    params.put("i", i);
	    session.update("mypage.updateProfileType",params);
		
	}

	public GameScore getScore(Long userId) {
		return session.selectOne("mypage.getScore",userId);
	}

	public UserBoard getBoard(Long userId) {
		return session.selectOne("mypage.getBoard",userId);
	}

	public List<UserBoard> getBoardList(Long userId) {
		return session.selectList("mypage.getBoardList",userId);
	}

	public int deductMbtiPoint(Long userId) {
		return session.update("mypage.deductMbtiPoint",userId);
	}

	public UserProfileDto findUserProfile(Long userId) {
		return session.selectOne("mypage.findUserProfile",userId);
	}

	public GameScore findUserScores(Long userId) {
		return session.selectOne("mypage.findUserScores",userId);
	}

	public List<UserBoard> findUserBoard(Long userId) {
		return session.selectList("mypage.findUserBoard",userId);
	}



}
