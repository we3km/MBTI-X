package com.kh.mbtix.admin.model.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.kh.mbtix.admin.model.vo.BanInfo;
import com.kh.mbtix.admin.model.vo.Report;
import com.kh.mbtix.common.model.vo.PageInfo;
import com.kh.mbtix.user.model.vo.UserEntity;

@Mapper
public interface AdminDao {

    List<UserEntity> selectAllUsers(PageInfo pi);
    
    int selectListCount();
    int selectReportListCount();
    List<Report> selectAllReports(PageInfo pi);
    Report selectReport(int reportId);
    int banUser(BanInfo banInfo);
    int updateReportStatus(int reportId);
}