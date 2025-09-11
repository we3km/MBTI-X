package com.kh.mbtix.cs.model.vo;

import java.sql.Date;
import lombok.Data;

@Data
public class Cs {
	
	private int inquiryId;
	private int userId;
	private String inquiryTitle;
	private String inquiryContent;
	private Date createdAt;
	private String answer;
	private Date answerAt;
	private String status;
	private int csCategory;
	
	private String userLoginId;
	private String userNickname;
	private String fileName;

}
