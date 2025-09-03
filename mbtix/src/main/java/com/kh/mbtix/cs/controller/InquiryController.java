package com.kh.mbtix.cs.controller;

import com.kh.mbtix.cs.model.service.InquiryService;
import com.kh.mbtix.cs.model.vo.Cs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/inquiries")
public class InquiryController {

    @Autowired
    private InquiryService inquiryService;

    // 목록 조회
    @GetMapping
    public ResponseEntity<List<Cs>> getInquiryList(@RequestParam(value = "status", required = false) String status) {
        List<Cs> list = inquiryService.findAllInquiries(status);
        return ResponseEntity.ok(list);
    }

    // 상세 조회
    @GetMapping("/{inquiryId}")
    public ResponseEntity<Cs> getInquiryDetail(@PathVariable("inquiryId") int inquiryId) {
        Cs inquiry = inquiryService.findInquiryById(inquiryId);
        return inquiry != null ? ResponseEntity.ok(inquiry) : ResponseEntity.notFound().build();
    }

    // 답변 등록
    @PostMapping("/{inquiryId}/answer")
    public ResponseEntity<String> submitAnswer(
            @PathVariable("inquiryId") int inquiryId,
            @RequestBody Map<String, String> payload) {
        String answer = payload.get("answer");
        int result = inquiryService.submitAnswer(inquiryId, answer);

        if (result > 0) {
            return ResponseEntity.ok("답변이 성공적으로 등록되었습니다.");
        } else {
            return ResponseEntity.status(500).body("답변 등록에 실패했습니다.");
        }
    }
}