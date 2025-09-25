package com.kh.mbtix.balComment.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.mbtix.balComment.model.dao.BalCommentDao;
import com.kh.mbtix.balComment.model.dto.BalCommentDto.BalComment;

/**
 * 댓글 서비스
 * - DAO와 연결, 트랜잭션 관리
 */
@Service
public class BalCommentService {

    private final BalCommentDao dao;

    public BalCommentService(BalCommentDao dao) {
        this.dao = dao;
    }

    /**
     * 특정 게임의 댓글 목록 조회
     */
    public List<BalComment> findByBalId(long balId) {
        return dao.selectCommentsByBal(balId);
    }

    /**
     * 댓글 작성
     */
    @Transactional
    public void write(BalComment dto) {
        dao.insertComment(
            Map.of(
                "balId", dto.getBalId(),
                "userId", dto.getUserId(),
                "content", dto.getContent()
            )
        );
    }

    /**
     * 댓글 삭제 (작성자 본인만)
     */
    @Transactional
    public boolean delete(long commentId, long userId) {
        int result = dao.deleteComment(commentId, userId);
        return result > 0;
    }
}
