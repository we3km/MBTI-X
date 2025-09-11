package com.kh.mbtix.cs.model.service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.kh.mbtix.alarm.model.service.AlarmService;
import com.kh.mbtix.alarm.model.vo.Alarm;
import com.kh.mbtix.common.model.vo.PageInfo;
import com.kh.mbtix.common.model.vo.PageResponse;
import com.kh.mbtix.cs.model.dao.InquiryDao;
import com.kh.mbtix.cs.model.vo.Cs;

@Service
public class InquiryServiceImpl implements InquiryService {
	   
    @Autowired
    private InquiryDao inquiryDao;

    @Autowired
    private AlarmService alarmService;

    @Override
    public PageResponse<Cs> findAllInquiries(String status, int currentPage) {
        Map<String, Object> params = new HashMap<>();
        params.put("status", status);
        int listCount = inquiryDao.selectInquiryListCount(params);
        PageInfo pi = new PageInfo(listCount, currentPage, 10, 10);
        params.put("pi", pi);
        List<Cs> list = inquiryDao.findAllInquiries(params);
        return new PageResponse<>(pi, list);
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
    public PageResponse<Cs> findInquiriesByUserId(Long userId, int currentPage) {
        int listCount = inquiryDao.selectUserInquiryListCount(userId);
        PageInfo pi = new PageInfo(listCount, currentPage, 10, 10);
        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("pi", pi);
        List<Cs> list = inquiryDao.findInquiriesByUserId(params);
        return new PageResponse<>(pi, list);
    }

    @Override
    public Cs findUserInquiryById(Long userId, int inquiryId) {
        Cs cs = new Cs();
        cs.setUserId(userId.intValue());
        cs.setInquiryId(inquiryId);
        return inquiryDao.findUserInquiryById(cs);
    }

    @Override
    @Transactional
    public int createInquiry(Cs cs, MultipartFile file) {
        int result = inquiryDao.createInquiry(cs);
        
        if (result > 0 && file != null && !file.isEmpty()) {
            try {
            	
                String projectRootPath = System.getProperty("user.dir");
                String uploadPath = projectRootPath + "/uploads/cs/";

                String originalFilename = file.getOriginalFilename();
                String savedFilename = UUID.randomUUID().toString() + "_" + originalFilename;

                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                File dest = new File(uploadPath + savedFilename);
                file.transferTo(dest);

                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("refId", cs.getInquiryId());
                fileInfo.put("fileName", savedFilename);
                fileInfo.put("categoryId", 1);
                
                inquiryDao.insertFile(fileInfo);

            } catch (IOException e) {
                throw new RuntimeException("파일 저장에 실패했습니다.", e);
            }
        }
        
        return result;
    }
}