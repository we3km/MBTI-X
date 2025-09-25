package com.kh.mbtix.balgame.model.dto;

import java.util.Map;
import java.util.List;

public class BalGameDtos {
    // ===== 공용 DTO =====
    public record GameDto(Long gameId, String title, String startAt, String isActive) {}
    public record OptionDto(Long optionId, Long gameId, String label, String textContent) {}

    // 오늘의 게임 응답
    public record TodayGameRes(Long gameId, String title, List<OptionBrief> options, String myVote) {
        public record OptionBrief(String label, String textContent, long votes) {}
    }
    public record TodayListRes(List<TodayGameRes> content, int page, int size, int totalPages) {}

    // 지난 게임 목록
    public record PastListRes(List<PastCard> content, int page, int size, int totalPages) {
        public record PastCard(Long gameId, String title, String startAt, List<Map<String,String>> options) {}
    }

    // 투표 DTO
    public record VoteReq(Long gameId, Long optionId, Long userId) {}
    public record VoteRes(Long voteId, Long gameId, Long optionId, Long userId, String snapMbti) {}

    // 통계 DTO
    public record StatsRes(Long gameId, long totalVotes, Map<String, OptStat> options, Map<String, Map<String, MbtiStat>> mbti) {
        public record OptStat(long cnt, double ratio) {}
        public record MbtiStat(long cnt, double ratio) {}
    }

    // 게임 생성 DTO
    public record CreateGameReq(String title, String optionAText, String optionBText) {}
    public record CreateGameRes(Long gameId, String title) {}
}
