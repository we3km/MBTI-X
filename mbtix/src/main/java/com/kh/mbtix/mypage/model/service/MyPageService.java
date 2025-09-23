package com.kh.mbtix.mypage.model.service;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.kh.mbtix.mypage.model.dao.MyPageDao;
import com.kh.mbtix.mypage.model.dto.MyPageDto.GameScore;
import com.kh.mbtix.mypage.model.dto.MyPageDto.UserBoard;
import com.kh.mbtix.mypage.model.dto.MyPageDto.UserProfileDto;
import com.kh.mbtix.security.model.dto.AuthDto.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyPageService {

    private final PasswordEncoder passwordEncoder;
	
	private final MyPageDao dao;

	
	public User updateNickname(Long userId, String newNickname) {
		
		//포인트 차감
		dao.minusPoint(userId,500);
		log.info("userID=({})",userId);
		
		dao.updateNickname(userId, newNickname);
		log.info("업데이트 userId=({}) newNickname({})",userId,newNickname);
		
		return dao.findUserByUserId(userId);
	}

	  public boolean checkPassword(Long userId, String currentPw) {
	        User user = dao.findUserById(userId);
	        if (user == null || user.getPassword() == null) {
	            return false;
	        }
	        return passwordEncoder.matches(currentPw, user.getPassword());
	    }
	  

	    public User updatePw(String newPw, Long userId) {
	        User user = dao.findUserById(userId);
	        if (user == null) {
	            throw new IllegalArgumentException("해당 ID의 사용자가 존재하지 않습니다. userId=" + userId);
	        }

	        String encodedPw = passwordEncoder.encode(newPw);
	        user.setPassword(encodedPw);

	        // DAO에 User 객체 전달해서 업데이트
	        dao.updatePassword(user);

	        // 업데이트된 사용자 다시 조회해서 반환 (옵션)
	        return dao.findUserById(userId);
	    }

		public User updateProfileImage(Long userId, String savedFileName) {
			
			User user = dao.findUserById(userId);
			if (user == null) {
		           throw new IllegalArgumentException("해당 ID의 사용자가 존재하지 않습니다. userId=" + userId);
		       }
			
			dao.updateProfileImg(userId,savedFileName);
			dao.updateProfileType(userId,500);
			
			return dao.findUserById(userId);
		}

		public GameScore getScore(Long userId) {
			return dao.getScore(userId);
		}

		public List<UserBoard> getBoardList(Long userId) {
			return dao.getBoardList(userId);
		}

		public Integer deductMbtiPoint(Long userId) {
		    int result = dao.deductMbtiPoint(userId);
		    if (result > 0) {
		       User user = dao.findUserById(userId);
		       return user.getPoint();
		    }
		    return null; // 실패
		}

		public UserProfileDto findUserProfile(Long userId) {
			return dao.findUserProfile(userId);
		}

		public GameScore findUserScores(Long userId) {
			return dao.findUserScores(userId);
		}

		public List<UserBoard> findUserBoards(Long userId) {
			return dao.findUserBoard(userId);
		}
}
