package com.kh.mbtix.test.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.mbtix.test.model.dto.MbtiModelDto;
import com.kh.mbtix.test.model.dto.MbtiModelDto.Answer;
import com.kh.mbtix.test.model.dto.MbtiModelDto.MbtiDetailRes;
import com.kh.mbtix.test.model.dto.MbtiModelDto.MbtiRatioRes;
import com.kh.mbtix.test.model.dto.MbtiModelDto.MbtiTestRequest;
import com.kh.mbtix.test.model.dto.MbtiModelDto.Question;
import com.kh.mbtix.test.service.MbtiTestService;

@RestController

@RequestMapping("/mbti")
public class MbtiTestController {

    private final MbtiTestService mbtiService;

    public MbtiTestController(MbtiTestService mbtiService) {
        this.mbtiService = mbtiService;
    }
    

    

    @GetMapping("/questions")
    public ResponseEntity<List<Question>> getQuestions() {
        return ResponseEntity.ok(mbtiService.getQuestions());
    }

    @PostMapping("/calculate")
    public ResponseEntity<String> calculate(@RequestBody MbtiTestRequest req) {
        String result = mbtiService.calculateAndSave(req.getUserId(), req.getAnswers());
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/ratio/{userId}")
    public ResponseEntity<MbtiRatioRes> getMbtiRatio(@PathVariable Long userId) {
    	System.out.println("넘어온 userId = " + userId);
        return ResponseEntity.ok(mbtiService.getUserMbtiRatio(userId));
    }
    
    @PostMapping("/calculate-detail")
    public MbtiDetailRes calculateDetail(@RequestBody MbtiModelDto.MbtiTestRequest req) {
        return mbtiService.calculateWithRatio(req.getUserId(), req.getAnswers());
    }



}
