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
import com.kh.mbtix.test.model.dto.MbtiModelDto.MbtiDetailRes;
import com.kh.mbtix.test.model.dto.MbtiModelDto.MbtiRatioRes;
import com.kh.mbtix.test.model.dto.MbtiModelDto.Question;

import lombok.RequiredArgsConstructor;

/**
 * MBTI 검사 서비스
 * - 질문 로드, 검사 계산, 결과 저장
 */
@Service
@RequiredArgsConstructor
public class MbtiTestService {

    private final MbtiTestDao mbtiDao;
    private final AuthDao authDao;

    /**
     * 질문 전체 조회
     */
    public List<Question> getQuestions() {
        return mbtiDao.findAllQuestions();
    }

    /**
     * MBTI 검사 수행 후 USERS 테이블에 결과 저장
     */
    public String calculateAndSave(Long userId, List<Answer> answers) {
        List<Question> questions = mbtiDao.findAllQuestions();

        // 점수 맵 초기화
        Map<String, Integer> scores = initScores();

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
        String mbti = buildMbti(scores);

        // USERS 업데이트
        Long mbtiId = mbtiDao.findMbtiIdByName(mbti);
        mbtiDao.updateUserMbti(userId, mbtiId);

        // 프로필 이미지 업데이트 (기본값일 경우만)
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

    /**
     * 사용자 MBTI 비율 조회
     */
    public MbtiRatioRes getUserMbtiRatio(Long userId) {
        return mbtiDao.selectUserMbtiRatio(userId);
    }

    /**
     * MBTI 검사 + 상세 비율 계산
     */
    public MbtiDetailRes calculateWithRatio(Long userId, List<Answer> answers) {
        List<Question> questions = mbtiDao.findAllQuestions();
        Map<String, Integer> scores = initScores();

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

        // MBTI 결과 문자열
        String mbti = buildMbti(scores);

        // USERS 업데이트
        Long mbtiId = mbtiDao.findMbtiIdByName(mbti);
        mbtiDao.updateUserMbti(userId, mbtiId);

        // 각 지표별 비율 계산
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

    /** 점수 초기화 */
    private Map<String, Integer> initScores() {
        Map<String, Integer> scores = new HashMap<>();
        for (String key : List.of("E","I","S","N","T","F","J","P")) {
            scores.put(key, 0);
        }
        return scores;
    }

    /** MBTI 결과 문자열 생성 */
    private String buildMbti(Map<String, Integer> scores) {
        return (scores.get("E") >= scores.get("I") ? "E" : "I") +
               (scores.get("S") >= scores.get("N") ? "S" : "N") +
               (scores.get("T") >= scores.get("F") ? "T" : "F") +
               (scores.get("J") >= scores.get("P") ? "J" : "P");
    }

    /** 퍼센트 계산 */
    private int percent(int num, int total) {
        return total == 0 ? 0 : (int)Math.round((num * 100.0) / total);
    }
}
