package com.kh.mbtix.balgame.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.kh.mbtix.balgame.model.dto.BalGameDtos.GameDto;
import com.kh.mbtix.balgame.model.dto.BalGameDtos.OptionDto;

@Mapper
public interface BalGameMapper {
    // ===== 오늘의 게임 =====
    List<GameDto> selectTodayGamesPaged(@Param("offset") int offset, @Param("size") int size);
    int countTodayGames();

    // 옵션
    List<OptionDto> selectOptionsByGameId(Long gameId);
    List<Map<String,Object>> selectOptionVoteCounts(Long gameId);
    String selectUserVoteLabel(@Param("gameId") long gameId, @Param("userId") Long userId);

    // ===== 과거 게임 =====
    List<Map<String,Object>> selectPastGamesPaged(@Param("date") String date, @Param("offset") int offset, @Param("size") int size);
    int countPastGames(@Param("date") String date);
    List<String> selectPastDates();
    List<Map<String,Object>> selectPastGamesByDate(@Param("date") String date);

    // ===== 투표 =====
    int hasUserVoted(@Param("gameId") Long gameId, @Param("userId") Long userId);
    String selectUserMbtiCode(Long userId);
    void insertVote(@Param("gameId") Long gameId, @Param("optionId") Long optionId, @Param("userId") Long userId, @Param("snapMbti") String snapMbti);

    // ===== 통계 =====
    List<Map<String,Object>> selectOptionMbti(@Param("gameId") Long gameId);

    // ===== 게임 생성 =====
    void deactivateAllActiveGames();
    void deactivateTodayGame();
    void insertGame(@Param("title") String title);
    Long selectLastGameId();
    void insertOption(@Param("gameId") Long gameId, @Param("label") String label, @Param("textContent") String textContent);
}
