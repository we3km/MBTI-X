package com.kh.mbtix.admin.model.service;

import com.kh.mbtix.admin.model.vo.Report;
import com.kh.mbtix.common.model.vo.PageResponse;
import com.kh.mbtix.user.model.vo.UserEntity;

public interface AdminService {
    
    PageResponse<UserEntity> selectAllUsers(int currentPage);
    
    PageResponse<Report> selectAllReports(int currentPage);
    
    Report selectReport(int reportId);

    boolean processReport(int reportId, int banDuration, int adminUserNum);
}