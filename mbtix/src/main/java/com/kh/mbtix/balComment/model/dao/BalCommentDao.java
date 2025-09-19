package com.kh.mbtix.balComment.model.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.kh.mbtix.balComment.model.dto.BalCommentDto.BalComment;

@Repository
public class BalCommentDao {

    private final SqlSession session;

    public BalCommentDao(SqlSession session) {
        this.session = session;
    }

    

    public List<BalComment> selectCommentsByBal(long balId) {
        return session.selectList("BalCommentMapper.selectCommentsByBal", balId);
    }

    public int insertComment(Map<String,Object> map) {
        return session.insert("BalCommentMapper.insertComment", map);
    }
    
    public int deleteComment(long commentId, long userId) {
        Map<String,Object> map = Map.of(
            "commentId", commentId,
            "userId", userId
        );
        return session.delete("BalCommentMapper.deleteComment", map);
    }
}
