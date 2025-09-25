package com.kh.mbtix.balComment.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 댓글 DTO
 */
public class BalCommentDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BalComment {
        Long commentId;   // 댓글 ID
        Long balId;       // 게임 ID
        Long userId;      // 작성자 ID
        String userName;  // 작성자 닉네임
        String mbti;      // 작성자 MBTI (투표 시점 스냅샷)
        String content;   // 댓글 내용
        String createAt;  // 작성 시각
    }
}
