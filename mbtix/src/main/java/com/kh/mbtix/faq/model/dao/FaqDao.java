package com.kh.mbtix.faq.model.dao;

import com.kh.mbtix.faq.model.vo.Faq;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FaqDao {
    List<Faq> findAllFaqs();
    Faq findFaqById(int faqId);
    int saveFaq(Faq faq);
    int updateFaq(Faq faq);
    int deleteFaq(int faqId);
}