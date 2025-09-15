package com.kh.mbtix.admin.model.vo;

import lombok.Data;
import java.sql.Date;

@Data
public class BanInfo {
	
	private int userId;
	private String reson;
	private String isBanned;
	private Date penaltyDate;
	private Integer relesaeDate;
	private String adminUserNum;

}
