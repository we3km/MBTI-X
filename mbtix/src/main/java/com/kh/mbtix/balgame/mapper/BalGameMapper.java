package com.kh.mbtix.balgame.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BalGameMapper {
	 // game
	  Map<String,Object> selectTodayGame();
	  List<Map<String,Object>> selectOptionsByGameId(@Param("gameId") long gameId);
	  List<Map<String,Object>> selectPastGamesPaged(@Param("offset") int offset, @Param("size") int size);
	  int countPastGames();

	  // vote insert (회원만)
	  int insertVote(@Param("gameId") long gameId,
	                 @Param("optionId") long optionId,
	                 @Param("userId") long userId,
	                 @Param("snapMbti") String snapMbti);

	  // stats
	  List<Map<String,Object>> selectOptionTotals(@Param("gameId") long gameId);
	  List<Map<String,Object>> selectOptionMbti(@Param("gameId") long gameId);

	  // 유저 MBTI 조회 (스냅샷용)
	  String selectUserMbtiCode(@Param("userId") long userId);
	}


