package com.kh.mbtix.cs.controller;

import com.kh.mbtix.cs.model.service.InquiryService;
import com.kh.mbtix.cs.model.vo.Cs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cs/inquiries")
public class Cscontroller {

    @Autowired
    private InquiryService inquiryService;

    // 1. 내 문의 목록 조회
    @GetMapping
    public ResponseEntity<List<Cs>> getMyInquiryList(@AuthenticationPrincipal Long userId) {
        List<Cs> list = inquiryService.findInquiriesByUserId(userId);
        return ResponseEntity.ok(list);
    }

    // 2. 내 문의 상세 조회
    @GetMapping("/{inquiryId}")
    public ResponseEntity<Cs> getMyInquiryDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable("inquiryId") int inquiryId) {
        Cs inquiry = inquiryService.findUserInquiryById(userId, inquiryId);
        // 본인 문의가 아니거나 존재하지 않으면 404
        return inquiry != null ? ResponseEntity.ok(inquiry) : ResponseEntity.notFound().build();
    }

    // 3. 새 문의 작성
    @PostMapping
    public ResponseEntity<Cs> createInquiry(
            @AuthenticationPrincipal Long userId,
            @RequestBody Cs inquiry) {
        inquiry.setUserId(userId.intValue()); // JWT 토큰에서 가져온 userId를 설정
        int result = inquiryService.createInquiry(inquiry);
        return ResponseEntity.status(201).body(inquiry);
    }
}