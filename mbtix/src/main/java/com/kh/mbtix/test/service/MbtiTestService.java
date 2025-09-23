package com.kh.mbtix.test.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kh.mbtix.test.model.dao.MbtiTestDao;
import com.kh.mbtix.test.model.dto.MbtiModelDto.Answer;
import com.kh.mbtix.test.model.dto.MbtiModelDto.MbtiDetailRes;
import com.kh.mbtix.test.model.dto.MbtiModelDto.MbtiRatioRes;
import com.kh.mbtix.test.model.dto.MbtiModelDto.Question;

@Service
public class MbtiTestService {

    private final MbtiTestDao mbtiDao;

    public MbtiTestService(MbtiTestDao mbtiDao) {
        this.mbtiDao = mbtiDao;
    }

    public List<Question> getQuestions() {
        return mbtiDao.findAllQuestions();
    }

    public String calculateAndSave(Long userId, List<Answer> answers) {
        List<Question> questions = mbtiDao.findAllQuestions();

        // 점수 맵 초기화
        Map<String, Integer> scores = new HashMap<>();
        for (String key : List.of("E","I","S","N","T","F","J","P")) {
            scores.put(key, 0);
        }

        // 답변 채점
        for (Answer ans : answers) {
            Question q = questions.stream()
                    .filter(qq -> qq.getId().equals(ans.getQuestionId()))
                    .findFirst()
                    .orElseThrow();

            if ("A".equals(ans.getChoice())) {
                scores.put(q.getAType(), scores.get(q.getAType()) + 1);
            } else if ("B".equals(ans.getChoice())) {
                scores.put(q.getBType(), scores.get(q.getBType()) + 1);
            }
        }

        // MBTI 결과 산출
        String mbti =
                (scores.get("E") >= scores.get("I") ? "E" : "I") +
                (scores.get("S") >= scores.get("N") ? "S" : "N") +
                (scores.get("T") >= scores.get("F") ? "T" : "F") +
                (scores.get("J") >= scores.get("P") ? "J" : "P");

        // USERS 업데이트
        Long mbtiId = mbtiDao.findMbtiIdByName(mbti);
        mbtiDao.updateUserMbti(userId, mbtiId);

        return mbti;
    }
    
    public MbtiRatioRes getUserMbtiRatio(Long userId) {
    	System.out.println("Service 받은 userId = " + userId);
        return mbtiDao.selectUserMbtiRatio(userId);
    }
    
    public MbtiDetailRes calculateWithRatio(Long userId, List<Answer> answers) {
        List<Question> questions = mbtiDao.findAllQuestions();

        // 점수 초기화
        Map<String, Integer> scores = new HashMap<>();
        for (String key : List.of("E","I","S","N","T","F","J","P")) {
            scores.put(key, 0);
        }

        // 채점
        for (Answer ans : answers) {
            Question q = questions.stream()
                    .filter(qq -> qq.getId().equals(ans.getQuestionId()))
                    .findFirst()
                    .orElseThrow();

            if ("A".equals(ans.getChoice())) {
                scores.put(q.getAType(), scores.get(q.getAType()) + 1);
            } else if ("B".equals(ans.getChoice())) {
                scores.put(q.getBType(), scores.get(q.getBType()) + 1);
            }
        }

        // MBTI 문자열 산출
        String mbti =
                (scores.get("E") >= scores.get("I") ? "E" : "I") +
                (scores.get("S") >= scores.get("N") ? "S" : "N") +
                (scores.get("T") >= scores.get("F") ? "T" : "F") +
                (scores.get("J") >= scores.get("P") ? "J" : "P");

        // USERS 테이블 업데이트 (기존 로직 재사용)
        Long mbtiId = mbtiDao.findMbtiIdByName(mbti);
        mbtiDao.updateUserMbti(userId, mbtiId);

        // ✅ 비율 계산
        Map<String, Map<String,Integer>> ratios = new HashMap<>();
        ratios.put("EI", Map.of(
            "E", percent(scores.get("E"), scores.get("E")+scores.get("I")),
            "I", percent(scores.get("I"), scores.get("E")+scores.get("I"))
        ));
        ratios.put("SN", Map.of(
            "S", percent(scores.get("S"), scores.get("S")+scores.get("N")),
            "N", percent(scores.get("N"), scores.get("S")+scores.get("N"))
        ));
        ratios.put("TF", Map.of(
            "T", percent(scores.get("T"), scores.get("T")+scores.get("F")),
            "F", percent(scores.get("F"), scores.get("T")+scores.get("F"))
        ));
        ratios.put("JP", Map.of(
            "J", percent(scores.get("J"), scores.get("J")+scores.get("P")),
            "P", percent(scores.get("P"), scores.get("J")+scores.get("P"))
        ));

        return new MbtiDetailRes(mbti, ratios);
    }

    private int percent(int num, int total) {
        return total == 0 ? 0 : (int)Math.round((num * 100.0) / total);
    }

}
