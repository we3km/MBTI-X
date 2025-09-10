package com.kh.mbtix.balgame.model.dto;

import java.util.Map;
import java.util.List;

public class BalGameDtos {

    // ====== 공용 DTO ======
    public record GameDto(Long gameId, String title, String startAt, String isActive) {}

    public record OptionDto(Long optionId, Long gameId, String label, String textContent) {}

    // 오늘의 게임 응답
    public record TodayGameRes(
            Long gameId,
            String title,
            List<OptionBrief> options,
            String myVote
    ) {
        public record OptionBrief(String label, String textContent, long votes) {}
    }

    // 투표 요청 DTO
    public record VoteReq(Long gameId, Long optionId, Long userId) {}
    
    // 투표 응답
    public record VoteRes(Long voteId, Long gameId, Long optionId, Long userId, String snapMbti) {}

    // 지난 게임 목록
    public record PastListRes(
            List<PastCard> content,
            int page,
            int size,
            int totalPages
    ) {
    	public record PastCard(Long gameId, String title, String startAt) {}
    }

    // 통계 
    public record StatsRes(
    		Long gameId,
    	    long totalVotes,
    	    Map<String, OptStat> options,
    	    Map<String, Map<String, MbtiStat>> mbti 
    ) {
    	public record OptStat(long cnt, double ratio) {}
    	 public record MbtiStat(long cnt, double ratio) {}
    }
    
    // 게임 생성
 // 요청 DTO
    public record CreateGameReq(
        String title,
        String optionAText,
        String optionBText
    ) {}

    // 응답 DTO
    public record CreateGameRes(
        Long gameId,
        String title
    ) {}
}
