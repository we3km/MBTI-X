package com.kh.mbtix.balComment.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.mbtix.balComment.model.dao.BalCommentDao;
import com.kh.mbtix.balComment.model.dto.BalCommentDto.BalComment;

@Service
public class BalCommentService {

	private final BalCommentDao dao;

    public BalCommentService(BalCommentDao dao) {
        this.dao = dao;
    }

    public List<BalComment> findByBalId(long balId){
        return dao.selectCommentsByBal(balId);  // ✅ 변경
    }

    @Transactional
    public void write(BalComment dto){
        dao.insertComment(
            Map.of(
                "balId", dto.getBalId(),
                "userId", dto.getUserId(),
                "content", dto.getContent()
            )
        );
    }
    
    @Transactional
    public boolean delete(long commentId, long userId) {
        int result = dao.deleteComment(commentId, userId);
        return result > 0;  // 삭제 성공 여부 반환
    }
}