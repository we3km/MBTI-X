package com.kh.mbtix.faq.controller;

import com.kh.mbtix.faq.model.service.FaqService;
import com.kh.mbtix.faq.model.vo.Faq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
// @RequestMapping("/admin/faqs") 임시 주석처리
public class FaqController {

    @Autowired
    private FaqService faqService;

    // 1. 전체 목록 조회
    @GetMapping("/faqs")
    public ResponseEntity<List<Faq>> getAllFaqs() {
        List<Faq> list = faqService.findAllFaqs();
        return ResponseEntity.ok(list);
    }

    // 2. 상세 조회
    @GetMapping("/faqs/{faqId}")
    public ResponseEntity<Faq> getFaqById(@PathVariable("faqId") int faqId) {
        Faq faq = faqService.findFaqById(faqId);
        if (faq != null) {
            return ResponseEntity.ok(faq);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 3. 생성
    @PostMapping("/admin/faqs")
    public ResponseEntity<Faq> createFaq(@RequestBody Faq faq) {
        // 관리자 권한 체크 로직 추가 필요
        int result = faqService.saveFaq(faq);
        return ResponseEntity.status(201).body(faq);
    }

    // 4. 수정
    @PutMapping("/admin/faqs/{faqId}")
    public ResponseEntity<Faq> updateFaq(@PathVariable("faqId") int faqId, @RequestBody Faq faq) {
        // 관리자 권한 체크 로직 추가 필요
        faq.setFaqId(faqId);
        int result = faqService.updateFaq(faq);
        return ResponseEntity.ok(faq);
    }

    // 5. 삭제
    @DeleteMapping("/admin/faqs/{faqId}")
    public ResponseEntity<Void> deleteFaq(@PathVariable("faqId") int faqId) {
        // 관리자 권한 체크 로직 추가 필요
        int result = faqService.deleteFaq(faqId);
        return ResponseEntity.noContent().build();
    }
}