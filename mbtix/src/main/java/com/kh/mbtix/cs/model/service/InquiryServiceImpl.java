package com.kh.mbtix.cs.model.service;

import com.kh.mbtix.cs.model.dao.InquiryDao;
import com.kh.mbtix.cs.model.vo.Cs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InquiryServiceImpl implements InquiryService {

    @Autowired
    private InquiryDao inquiryDao;

    @Override
    public List<Cs> findAllInquiries(String status) {
        return inquiryDao.findAllInquiries(status);
    }

    @Override
    public Cs findInquiryById(int inquiryId) {
        return inquiryDao.findInquiryById(inquiryId);
    }

    @Override
    public int submitAnswer(int inquiryId, String answer) {
        Cs inquiry = new Cs();
        inquiry.setInquiryId(inquiryId);
        inquiry.setAnswer(answer);
        return inquiryDao.submitAnswer(inquiry);
    }
    
    @Override
    public List<Cs> findInquiriesByUserId(Long userId) {
        return inquiryDao.findInquiriesByUserId(userId);
    }

    @Override
    public Cs findUserInquiryById(Long userId, int inquiryId) {
        Cs cs = new Cs();
        cs.setUserId(userId.intValue());
        cs.setInquiryId(inquiryId);
        return inquiryDao.findUserInquiryById(cs);
    }

    @Override
    public int createInquiry(Cs cs) {
        return inquiryDao.createInquiry(cs);
    }
}