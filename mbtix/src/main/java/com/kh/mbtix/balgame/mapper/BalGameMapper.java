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
	 
	 // ===== 과거 게임 목록(페이징) =====
	 // 주의: 서비스에서 Map 기반으로 꺼내 쓰므로 resultType=map 형태를 권장
	 List<Map<String,Object>> selectPastGamesPaged(@Param("offset") int offset, @Param("size") int size);
	  int countPastGames();
	  
	  
	  // ===== 투표 관련 =====
	  // 중복 투표 방지용
	  int hasUserVoted(@Param("gameId") Long gameId, @Param("userId") Long userId);

	// 유저 MBTI 코드 조회
	    String selectUserMbtiCode(Long userId);
	    

	 // 실제 투표 insert
	    void insertVote(@Param("gameId") Long gameId,
	                    @Param("optionId") Long optionId,
	                    @Param("userId") Long userId,
	                    @Param("snapMbti") String snapMbti);
	    
	 // 내가 투표한 라벨(A/B) 반환 (오늘의 게임 조회 시 myVote 채우기 용)
	 String selectUserVoteLabel(long gameId, Long userId);

	    // ===== 통계 =====
	    // 옵션별 총 득표수 LABEL, CNT 를 담은 맵 리스트
	    List<Map<String,Object>> selectOptionVoteCounts(Long gameId);
	    List<Map<String,Object>> selectOptionMbti(@Param("gameId") Long gameId);
	    
	    
		

		// ===== 스케줄러(자정) =====
		void deactivateTodayGame();
		void activateNewGame();

	  // 유저 MBTI 조회 (스냅샷용)
//	  String selectUserMbtiCode(@Param("userId") long userId);
		
		
		// 게임생성
		void insertGame(@Param("title") String title);
	    Long selectLastGameId();
	    void insertOption(@Param("gameId") Long gameId,
	                      @Param("label") String label,
	                      @Param("textContent") String textContent);
		
		
		
		
		
		
		
		
		
		
		
		
	}


