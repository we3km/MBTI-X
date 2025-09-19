package com.kh.mbtix.faq.model.service;

import com.kh.mbtix.common.model.vo.PageInfo;
import com.kh.mbtix.common.model.vo.PageResponse;
import com.kh.mbtix.faq.model.dao.FaqDao;
import com.kh.mbtix.faq.model.vo.Faq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FaqServiceImpl implements FaqService {

    @Autowired
    private FaqDao faqDao;

    @Override
    public PageResponse<Faq> findAllFaqs(int currentPage) {
    	int listCount = faqDao.selectFaqListCount();
    	PageInfo pi = new PageInfo(listCount, currentPage, 10, 10);
    	List<Faq> list = faqDao.findAllFaqs(pi);
    	return new PageResponse<>(pi, list);
    }

    @Override
    public Faq findFaqById(int faqId) {
        return faqDao.findFaqById(faqId);
    }

    @Override
    public int saveFaq(Faq faq) {
        return faqDao.saveFaq(faq);
    }

    @Override
    public int updateFaq(Faq faq) {
        return faqDao.updateFaq(faq);
    }

    @Override
    public int deleteFaq(int faqId) {
        return faqDao.deleteFaq(faqId);
    }
    
    
}