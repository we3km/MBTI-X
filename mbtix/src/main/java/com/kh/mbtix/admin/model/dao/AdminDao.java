package com.kh.mbtix.admin.model.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.kh.mbtix.admin.model.vo.BanInfo;
import com.kh.mbtix.admin.model.vo.DashboardStatsDTO;
import com.kh.mbtix.admin.model.vo.Report;
import com.kh.mbtix.admin.model.vo.UserDetailDTO;
import com.kh.mbtix.common.model.vo.PageInfo;
import com.kh.mbtix.security.model.dto.AuthDto.UserAuthority;
import com.kh.mbtix.user.model.vo.UserEntity;

@Mapper
public interface AdminDao {

    List<UserEntity> selectAllUsers(Map<String, Object> param);
    int selectListCount(Map<String, Object> param);
    int selectReportListCount();
    
    List<Report> selectAllReports(PageInfo pi);
    Report selectReport(int reportId);
    int banUser(BanInfo banInfo);
    int updateReportStatus(int reportId);
    
    UserDetailDTO.UserInfo selectUserInfo(int userId);
    List<UserDetailDTO.BanHistory> selectBanHistory(int userId);
    List<UserDetailDTO.ReportHistory> selectReportsMade(int userId);
    List<UserDetailDTO.ReportHistory> selectReportsReceived(int userId);
    
    int updateUserRole(UserAuthority userAuthority);
    
    int unbanUser(int usreId);
    
    // 관리자 페이지 통계
    DashboardStatsDTO selectDashboardStats();
    
}