package com.kh.mbtix.cs.model.dao;

import com.kh.mbtix.cs.model.vo.Cs;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface InquiryDao {
    
	// 관리자
	List<Cs> findAllInquiries(Map<String, Object> params);
    int selectInquiryListCount(Map<String, Object> params);
    
    Cs findInquiryById(int inquiryId);
    int submitAnswer(Cs inquiry);
    
    // 사용자
    List<Cs> findInquiriesByUserId(Map<String, Object> params);
    int selectUserInquiryListCount(Long userId);
    
    Cs findUserInquiryById(Cs cs);
    int createInquiry(Cs cs);
    int insertFile(Map<String, Object> fileInfo);
}