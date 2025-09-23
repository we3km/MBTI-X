package com.kh.mbtix.admin.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    private int reportId;
    private int userId;
    private int targetUserNum;
    private String reson;
    private String status;
    private Date createdAt;
    private Date processedAt;
    private int reportCategory;
    
    private String reporterId; // 신고한 회원 아이디
    private String reportedId; // 신고된 회원 아이디
    private String reportCategoryName; // 신고 유형
    
    private String reporterNickname; // 신고한 회원 닉네임
    private String reportedNickname; // 신고된 회원 닉네임
    
    
}