package com.kh.mbtix.test.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kh.mbtix.common.MbtiUtils;
import com.kh.mbtix.security.model.dao.AuthDao;
import com.kh.mbtix.security.model.dto.AuthDto.FileVO;
import com.kh.mbtix.security.model.dto.AuthDto.User;
import com.kh.mbtix.test.model.dao.MbtiTestDao;
import com.kh.mbtix.test.model.dto.MbtiModelDto.Answer;
import com.kh.mbtix.test.model.dto.MbtiModelDto.MbtiRatioRes;
import com.kh.mbtix.test.model.dto.MbtiModelDto.Question;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MbtiTestService {

    private final MbtiTestDao mbtiDao;
    private final AuthDao  authDao;

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
        
        User user = authDao.findUserByUserId(userId);
        if ("DEFAULT".equals(user.getProfileType())) {
            String fileName = MbtiUtils.getProfileFileName(String.valueOf(mbtiId));

            FileVO file = FileVO.builder()
                    .fileName(fileName)
                    .refId(userId)
                    .categoryId(4) // 프로필
                    .build();

            authDao.updateProfile(file);
        }

        return mbti;
    }
    
    public MbtiRatioRes getUserMbtiRatio(Long userId) {
    	System.out.println("Service 받은 userId = " + userId);
        return mbtiDao.selectUserMbtiRatio(userId);
    }
}
