package com.kh.mbtix.balComment.model.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.kh.mbtix.balComment.model.dto.BalCommentDto.BalComment;

/**
 * 댓글 DAO
 * - MyBatis SqlSession으로 Mapper 호출
 */
@Repository
public class BalCommentDao {

    private final SqlSession session;

    public BalCommentDao(SqlSession session) {
        this.session = session;
    }

    /**
     * 특정 게임의 댓글 목록 조회
     */
    public List<BalComment> selectCommentsByBal(long balId) {
        return session.selectList("BalCommentMapper.selectCommentsByBal", balId);
    }

    /**
     * 댓글 추가
     */
    public int insertComment(Map<String,Object> map) {
        return session.insert("BalCommentMapper.insertComment", map);
    }

    /**
     * 댓글 삭제 (작성자 본인만 가능)
     */
    public int deleteComment(long commentId, long userId) {
        Map<String,Object> map = Map.of(
            "commentId", commentId,
            "userId", userId
        );
        return session.delete("BalCommentMapper.deleteComment", map);
    }
}
