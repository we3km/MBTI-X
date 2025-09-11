package com.kh.mbtix.test.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kh.mbtix.test.model.dao.MbtiTestDao;
import com.kh.mbtix.test.model.dto.MbtiModelDto.Answer;
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
}
