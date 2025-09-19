package com.kh.mbtix.alarm.model.vo;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alarm {
	
	private int alarmId;
	private int receiverId; // 알림을 받는 사용자 ID
	private String content; // 알림 내용
	private int refId;      // 게시글 번호 같은 관련 컨텐츠 ID
	private String type;    // 알림 종류
	private Date createdAt;
	private String isRead;

}
