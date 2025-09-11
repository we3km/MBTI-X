package com.kh.mbtix.board.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {
	private int reportId;
	private int userId;	
	private int targetUserNum;
	private String reason;
	private int reportCateogry;
}



