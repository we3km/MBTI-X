package com.kh.mbtix.test.model.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MBTI 검사 관련 DTO 모음
 */
public class MbtiModelDto {

    /** 질문 DTO */
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Question {
        private Long id;
        private String question;
        private String aType;
        private String bType;
    }

    /** 답변 DTO */
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Answer {
        private Long questionId;
        private String choice; // "A" or "B"
    }

    /** 검사 요청 DTO */
    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class MbtiTestRequest {
        private Long userId;
        private List<Answer> answers;
    }

    /** MBTI 비율 응답 DTO */
    public record MbtiRatioRes(String mbtiName, double ratio) {}

    /** MBTI 상세 비율 응답 DTO */
    public record MbtiDetailRes(String type, Map<String, Map<String,Integer>> ratios) {}
}
