package com.kh.mbtix.faq.model.service;

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
    public List<Faq> findAllFaqs() {
        return faqDao.findAllFaqs();
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