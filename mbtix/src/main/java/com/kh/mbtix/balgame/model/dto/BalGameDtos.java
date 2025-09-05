package com.kh.mbtix.balgame.model.dto;

import java.util.List;
import java.util.Map;

public final class BalGameDtos {
    private BalGameDtos() {} // 인스턴스화 방지

public record GameDto(Long gameId, String title, String startAt, String isActive) {}

public record OptionDto(
    Long optionId,
    Long gameId,
    String label,
    String textContent,
    String imageUrl
) {}

public record TodayGameRes(
    Long gameId,
    String title,
    List<OptionBrief> options,
    String myVote
) {
    // ⬇️ 내부 record (중첩)
    public record OptionBrief(String label, String text) {}
}

public record PastListRes(
    List<PastCard> content,
    int page,
    int size,
    int totalPages
) {
    public record PastCard(Long gameId, String title, String startAt) {}
}

public record StatsRes(
    Long gameId,
    String title,
    long totalVotes,
    Map<String,OptStat> options
) {
    public record OptStat(long total, double ratio, Map<String,Long> mbti) {}
}
}