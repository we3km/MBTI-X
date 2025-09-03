package com.kh.mbtix.admin.model.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.mbtix.admin.model.dao.AdminDao;
import com.kh.mbtix.admin.model.vo.BanInfo;
import com.kh.mbtix.admin.model.vo.Report;
import com.kh.mbtix.common.model.vo.PageInfo;
import com.kh.mbtix.common.model.vo.PageResponse;
import com.kh.mbtix.user.model.vo.UserEntity;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminDao adminDao;

    @Override
    public PageResponse<UserEntity> selectAllUsers(int currentPage) {
        int listCount = adminDao.selectListCount();
        PageInfo pi = new PageInfo(listCount, currentPage, 10, 10);
        List<UserEntity> list = adminDao.selectAllUsers(pi);
        return new PageResponse<>(pi, list);
    }
    
    @Override
    public PageResponse<Report> selectAllReports(int currentPage) {
    	int listCount = adminDao.selectReportListCount();
    	PageInfo pi = new PageInfo(listCount, currentPage, 10, 10);
    	List<Report> list = adminDao.selectAllReports(pi);
    	return new PageResponse<>(pi, list);
    }
    
    @Override
    public Report selectReport(int reportId) {
    	return adminDao.selectReport(reportId);
    }
    
    @Transactional
    @Override
    public boolean processReport(int reportId, int banDuration, int adminUserNum) {
        Report report = adminDao.selectReport(reportId);

        if (report != null && "N".equals(report.getStatus())) {
            BanInfo banInfo = new BanInfo();
            banInfo.setUserId(report.getTargetUserNum());
            banInfo.setReson(report.getReportCategoryName());
            banInfo.setAdminUserNum(String.valueOf(adminUserNum));
            
            if (banDuration == -1) {
                banInfo.setReleasaeDate(9999);
            } else {
                banInfo.setReleasaeDate(banDuration);
            }

            int banResult = adminDao.banUser(banInfo);
            int updateResult = adminDao.updateReportStatus(reportId);

            return (banResult > 0 && updateResult > 0);
        }
        
        return false;
    }
}