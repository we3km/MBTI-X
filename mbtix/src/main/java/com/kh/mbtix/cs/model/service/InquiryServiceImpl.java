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

import lombok.extern.slf4j.Slf4j;

@Slf4j
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
    
    // 문의 삭제 기능
    @Override
    public int deleteInquiry(Long userId, int inquiryId) {
    	Cs cs = new Cs();
    	cs.setUserId(userId.intValue());
    	cs.setInquiryId(inquiryId);
    	return inquiryDao.deleteInquiry(cs);
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
    
    // 사용자 문의 삭제(숨김)
    @Override
    public int hideInquiry(int inquiryId) {
    	return inquiryDao.hideInquiryByAdmin(inquiryId);
    }
    
    // 사용자 문의 삭제
    @Override
    @Transactional
    public void permanentlyDeleteOldInquiries() {
        // 1. 영구 삭제 대상 문의 목록 조회 (첨부 파일 이름 포함)
        List<Cs> targets = inquiryDao.findOldInquiriesForDeletion();
        if (targets.isEmpty()) {
            log.info("삭제할 오래된 문의 데이터가 없습니다.");
            return;
        }

        String projectRootPath = System.getProperty("user.dir");
        String uploadPath = projectRootPath + "/uploads/cs/";

        // 2. 각 문의에 대해 파일 삭제 후 DB 기록 삭제
        for (Cs inquiry : targets) {
            if (inquiry.getFileName() != null && !inquiry.getFileName().isEmpty()) {
                File fileToDelete = new File(uploadPath + inquiry.getFileName());
                if (fileToDelete.exists()) {
                    if (fileToDelete.delete()) {
                        log.info("파일 삭제 성공: " + inquiry.getFileName());
                    } else {
                        log.warn("파일 삭제 실패: " + inquiry.getFileName());
                    }
                }
            }
            inquiryDao.permanentlyDeleteInquiryById(inquiry.getInquiryId());
        }
        log.info("총 " + targets.size() + "개의 오래된 문의 데이터가 영구 삭제되었습니다.");
    }
    
}