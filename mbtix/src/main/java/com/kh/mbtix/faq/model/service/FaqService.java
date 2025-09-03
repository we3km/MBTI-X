package com.kh.mbtix.faq.model.service;

import com.kh.mbtix.faq.model.vo.Faq;
import java.util.List;

public interface FaqService {

    List<Faq> findAllFaqs();

    Faq findFaqById(int faqId);

    int saveFaq(Faq faq);

    int updateFaq(Faq faq);

    int deleteFaq(int faqId);
}