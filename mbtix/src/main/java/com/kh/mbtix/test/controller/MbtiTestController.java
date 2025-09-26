package com.kh.mbtix.test.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.kh.mbtix.test.model.dto.MbtiModelDto;
import com.kh.mbtix.test.model.dto.MbtiModelDto.Answer;
import com.kh.mbtix.test.model.dto.MbtiModelDto.MbtiDetailRes;
import com.kh.mbtix.test.model.dto.MbtiModelDto.MbtiRatioRes;
import com.kh.mbtix.test.model.dto.MbtiModelDto.MbtiTestRequest;
import com.kh.mbtix.test.model.dto.MbtiModelDto.Question;
import com.kh.mbtix.test.service.MbtiTestService;

/**
 * MBTI 검사 컨트롤러
 * - 질문 조회, 검사 실행, 결과 저장 및 조회
 */
@RestController
@RequestMapping("/mbti")
public class MbtiTestController {

    private final MbtiTestService mbtiService;

    public MbtiTestController(MbtiTestService mbtiService) {
        this.mbtiService = mbtiService;
    }

    /**
     * 모든 MBTI 질문 목록 조회
     */
    @GetMapping("/questions")
    public ResponseEntity<List<Question>> getQuestions() {
        return ResponseEntity.ok(mbtiService.getQuestions());
    }

    /**
     * MBTI 검사 수행 및 결과 저장
     */
    @PostMapping("/calculate")
    public ResponseEntity<String> calculate(@RequestBody MbtiTestRequest req) {
        String result = mbtiService.calculateAndSave(req.getUserId(), req.getAnswers());
        return ResponseEntity.ok(result);
    }

    /**
     * 사용자 MBTI 비율 조회
     */
    @GetMapping("/ratio/{userId}")
    public ResponseEntity<MbtiRatioRes> getMbtiRatio(@PathVariable Long userId) {
        return ResponseEntity.ok(mbtiService.getUserMbtiRatio(userId));
    }

    /**
     * MBTI 검사 + 상세 비율 계산
     */
    @PostMapping("/calculate-detail")
    public MbtiDetailRes calculateDetail(@RequestBody MbtiModelDto.MbtiTestRequest req) {
        return mbtiService.calculateWithRatio(req.getUserId(), req.getAnswers());
    }
}
