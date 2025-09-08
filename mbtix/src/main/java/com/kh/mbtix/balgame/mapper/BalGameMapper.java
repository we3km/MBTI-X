package com.kh.mbtix.balgame.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kh.mbtix.balgame.model.dto.BalGameDtos.GameDto;
import com.kh.mbtix.balgame.model.dto.BalGameDtos.OptionDto;

@Mapper
public interface BalGameMapper {
	 // game
	 GameDto selectTodayGame();
	 List<OptionDto> selectOptionsByGameId(Long gameId);
	  List<Map<String,Object>> selectPastGamesPaged(@Param("offset") int offset, @Param("size") int size);
	  int countPastGames();
	  
	  // 특정 게임에서 유저가 이미 투표했는지 확인
	  int hasUserVoted(@Param("gameId") Long gameId, @Param("userId") Long userId);

	// 유저 MBTI 코드 조회
	    String selectUserMbtiCode(Long userId);
	    

	    // 투표 저장
	    void insertVote(@Param("gameId") Long gameId,
	                    @Param("optionId") Long optionId,
	                    @Param("userId") Long userId,
	                    @Param("snapMbti") String snapMbti);

	  // stats
	    List<Map<String,Object>> selectOptionVoteCounts(Long gameId);

	  // 유저 MBTI 조회 (스냅샷용)
//	  String selectUserMbtiCode(@Param("userId") long userId);
	}


