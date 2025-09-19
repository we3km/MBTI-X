package com.kh.mbtix.member.model.vo;

import java.sql.Date;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Member {
	
	private int userId;
	private int mbtiId;
	private String loginId;
	private String email;
	private String name;
	private String nickname;
	private Date createdAt;
	private String isQuit;
	private int point;
	

}
