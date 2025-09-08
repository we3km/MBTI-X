package com.kh.mbtix.cs.model.service;

import com.kh.mbtix.alarm.model.service.AlarmService;
import com.kh.mbtix.alarm.model.vo.Alarm;
import com.kh.mbtix.cs.model.dao.InquiryDao;
import com.kh.mbtix.cs.model.vo.Cs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class InquiryServiceImpl implements InquiryService {

    @Autowired
    private InquiryDao inquiryDao;

    @Autowired
    private AlarmService alarmService;

    @Override
    public List<Cs> findAllInquiries(String status) {
        return inquiryDao.findAllInquiries(status);
    }

    @Override
    public Cs findInquiryById(int inquiryId) {
        return inquiryDao.findInquiryById(inquiryId);
    }

    @Transactional
    @Override
    public int submitAnswer(int inquiryId, String answer) {
        Cs inquiry = inquiryDao.findInquiryById(inquiryId);
        if (inquiry == null) {
            return 0;
        }

        inquiry.setAnswer(answer);
        int result = inquiryDao.submitAnswer(inquiry);

        if (result > 0) {
            Alarm alarm = Alarm.builder()
                    .receiverId(inquiry.getUserId())
                    .content("회원님의 1:1 문의에 답변이 등록되었습니다.")
                    .refId(inquiryId)
                    .type("INQUIRY_ANSWER")
                    .build();
            alarmService.createAlarm(alarm);
        }
        return result;
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