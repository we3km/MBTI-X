package com.kh.mbtix.faq.model.vo;

import java.sql.Date;
import lombok.Data;

@Data
public class Faq {
	
	private int faqId;
	private String faqCategory;
	private String question;
	private String answer;
	private Date createdAt;

}
