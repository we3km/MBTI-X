package com.kh.mbtix.cs.model.service;

import com.kh.mbtix.common.model.vo.PageResponse;
import com.kh.mbtix.cs.model.vo.Cs;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface InquiryService {
	
	// 관리자
    PageResponse<Cs> findAllInquiries(String status, int currentPage);
    Cs findInquiryById(int inquiryId);
    int submitAnswer(int inquiryId, String answer);
        
    // 사용자
    PageResponse<Cs> findInquiriesByUserId(Long userId, int currentPage);
    Cs findUserInquiryById(Long userId, int inquiryId);
    int createInquiry(Cs cs, MultipartFile file);
}