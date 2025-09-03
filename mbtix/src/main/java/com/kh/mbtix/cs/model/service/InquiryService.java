package com.kh.mbtix.cs.model.service;

import com.kh.mbtix.cs.model.vo.Cs;
import java.util.List;

public interface InquiryService {
	
	// 관리자
    List<Cs> findAllInquiries(String status);
    Cs findInquiryById(int inquiryId);
    int submitAnswer(int inquiryId, String answer);
        
    // 사용자
    List<Cs> findInquiriesByUserId(Long userId);
    Cs findUserInquiryById(Long userId, int inquiryId);
    int createInquiry(Cs cs);
    
    
    
    
    
}