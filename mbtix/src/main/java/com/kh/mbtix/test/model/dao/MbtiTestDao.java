package com.kh.mbtix.test.model.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Repository;

import com.kh.mbtix.test.model.dto.MbtiModelDto.MbtiRatioRes;
import com.kh.mbtix.test.model.dto.MbtiModelDto.Question;

@Repository
public class MbtiTestDao {

    private final SqlSession sqlSession;

    public MbtiTestDao(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    // 질문 전체 조회
    public List<Question> findAllQuestions() {
        return sqlSession.selectList("MbtiMapper.findAllQuestions");
    }

    // MBTI_ID 찾기
    public Long findMbtiIdByName(String mbtiName) {
        return sqlSession.selectOne("MbtiMapper.findMbtiIdByName", mbtiName);
    }

    // USERS 테이블 업데이트
    public void updateUserMbti(Long userId, Long mbtiId) {
        Map<String,Object> param = new HashMap<>();
        param.put("userId", userId);
        param.put("mbtiId", mbtiId);
        sqlSession.update("MbtiMapper.updateUserMbti", param);
    }
    
 // ✅ 사용자 MBTI 비율 조회
    public MbtiRatioRes selectUserMbtiRatio(Long userId) {
        Map<String,Object> map = sqlSession.selectOne("MbtiMapper.selectUserMbtiRatio", userId);
        if (map == null) return null;
        return new MbtiRatioRes(
            (String) map.get("MBTI_NAME"),
            ((Number) map.get("RATIO")).doubleValue()
        );
    }

}
