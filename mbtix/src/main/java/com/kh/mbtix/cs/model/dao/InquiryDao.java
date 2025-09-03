package com.kh.mbtix.cs.model.dao;

import com.kh.mbtix.cs.model.vo.Cs;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface InquiryDao {
    List<Cs> findAllInquiries(String status);
    Cs findInquiryById(int inquiryId);
    int submitAnswer(Cs inquiry);
    
    List<Cs> findInquiriesByUserId(Long userId);
    Cs findUserInquiryById(Cs cs);
    int createInquiry(Cs cs);
    
}