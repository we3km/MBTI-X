package com.kh.mbtix.cs.controller;

import com.kh.mbtix.common.model.vo.PageResponse;
import com.kh.mbtix.cs.model.service.InquiryService;
import com.kh.mbtix.cs.model.vo.Cs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/cs/inquiries")
public class CsController {

    @Autowired
    private InquiryService inquiryService;

    // 내 문의 목록 조회
    @GetMapping
    public ResponseEntity<PageResponse<Cs>> getMyInquiryList(
            @AuthenticationPrincipal Long userId,
            @RequestParam(value = "cpage", defaultValue = "1") int currentPage) {
        PageResponse<Cs> response = inquiryService.findInquiriesByUserId(userId, currentPage);
        return ResponseEntity.ok(response);
    }

    // 내 문의 상세 조회
    @GetMapping("/{inquiryId}")
    public ResponseEntity<Cs> getMyInquiryDetail(
            @AuthenticationPrincipal Long userId,
            @PathVariable("inquiryId") int inquiryId) {
        Cs inquiry = inquiryService.findUserInquiryById(userId, inquiryId);
        return inquiry != null ? ResponseEntity.ok(inquiry) : ResponseEntity.notFound().build();
    }

    // 새 문의 작성
    @PostMapping
    public ResponseEntity<Cs> createInquiry(
            @AuthenticationPrincipal Long userId,
            @RequestPart("inquiry") Cs inquiry,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        
        inquiry.setUserId(userId.intValue());
        inquiryService.createInquiry(inquiry, file);
        
        return ResponseEntity.status(201).body(inquiry);
    }
    
    // 내 문의 삭제
    @DeleteMapping("/{inquiryId}")
    public ResponseEntity<Void> deleteMyInquiry(
    		@AuthenticationPrincipal Long userId,
    		@PathVariable("inquiryId") int inquiryId) {
    	
    	int result = inquiryService.deleteInquiry(userId, inquiryId);
    	
    	if (result > 0) {
    		return ResponseEntity.ok().build(); // 성공시 200
    	} else {
    		return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403
    	}
    }
    
}