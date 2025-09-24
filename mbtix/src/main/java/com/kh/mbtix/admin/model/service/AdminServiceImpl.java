package com.kh.mbtix.admin.model.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.mbtix.admin.model.dao.AdminDao;
import com.kh.mbtix.admin.model.vo.BanInfo;
import com.kh.mbtix.admin.model.vo.DashboardStatsDTO;
import com.kh.mbtix.admin.model.vo.Report;
import com.kh.mbtix.admin.model.vo.UserDetailDTO;
import com.kh.mbtix.board.model.vo.Board;
import com.kh.mbtix.board.model.vo.BoardComment;
import com.kh.mbtix.common.model.vo.PageInfo;
import com.kh.mbtix.common.model.vo.PageResponse;
import com.kh.mbtix.security.model.dto.AuthDto.UserAuthority;
import com.kh.mbtix.user.model.vo.UserEntity;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminDao adminDao;

    @Override
    public PageResponse<UserEntity> selectAllUsers(
    	int currentPage, String searchType, String keyword, String status) {
    	
    	Map<String, Object> param = new HashMap<>();
        param.put("searchType", searchType);
        param.put("keyword", keyword);
        param.put("status", status);
    	
    	int listCount = adminDao.selectListCount(param);
        
        PageInfo pi = new PageInfo(listCount, currentPage, 10, 10);
        param.put("pi", pi);
        
        List<UserEntity> list = adminDao.selectAllUsers(param);
        
        return new PageResponse<>(pi, list);
    }
    
    @Override
    public PageResponse<Report> selectAllReports
    (int currentPage, String searchType, String keyword, String status, String category) {
        Map<String, Object> param = new HashMap<>();
        param.put("searchType", searchType);
        param.put("keyword", keyword);
        param.put("status", status);
        param.put("category", category);

        int listCount = adminDao.selectReportListCount(param);
        
        PageInfo pi = new PageInfo(listCount, currentPage, 10, 10);
        param.put("pi", pi);
        
        List<Report> list = adminDao.selectAllReports(param);
        
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
            
            if (banDuration == -1) { // 영정처리
                banInfo.setRelesaeDate(9999);
            } else {
                banInfo.setRelesaeDate(banDuration);
            }

            int banResult = adminDao.banUser(banInfo);
            int updateResult = adminDao.updateReportStatus(reportId);

            return (banResult > 0 && updateResult > 0);
        }
        
        return false;
    }
    
    @Override
    public UserDetailDTO selectUserDetail(int userId) {
        UserDetailDTO userDetail = new UserDetailDTO();
        
        // 각 DAO 메소드 호출 및 DTO에 설정
        userDetail.setUserInfo(adminDao.selectUserInfo(userId));
        userDetail.setBanHistory(adminDao.selectBanHistory(userId));
        userDetail.setReportsMade(adminDao.selectReportsMade(userId));
        userDetail.setReportsReceived(adminDao.selectReportsReceived(userId));
        
        return userDetail;
    }
    
    // 관리자가 직접 제재
    @Override
    @Transactional
    public boolean banUserDirectly(int userId, int banDuration, String reason, int adminUserId) {
    	BanInfo banInfo = new BanInfo();
    	banInfo.setUserId(userId);
    	banInfo.setReson(reason);
    	banInfo.setAdminUserNum(String.valueOf(adminUserId));
    	
    	if(banDuration == -1) { // 영정 처리
    		banInfo.setRelesaeDate(9999);
    	} else {
    		banInfo.setRelesaeDate(banDuration);
    	}
    	
    	int result = adminDao.banUser(banInfo);
    	return result > 0;
    }
    
    // 특정 회원이 작성한 게시글 목록 조회
    @Override
    public PageResponse<Board> findPostsByUserId(int userId, int currentPage) {
        Map<String, Object> param = new HashMap<>();
        param.put("userId", userId);

        int listCount = adminDao.selectPostCountByUserId(param);
        PageInfo pi = new PageInfo(listCount, currentPage, 5, 10); // 한 페이지에 5개씩 표시
        param.put("pi", pi);

        List<Board> list = adminDao.selectPostsByUserId(param);
        return new PageResponse<>(pi, list);
    }
    
    // 특정 회원이 작성한 댓글 목록 조회
    @Override
    public PageResponse<BoardComment> findCommentsByUserId(int userId, int currentPage) {
        Map<String, Object> param = new HashMap<>();
        param.put("userId", userId);

        int listCount = adminDao.selectCommentCountByUserId(param);
        PageInfo pi = new PageInfo(listCount, currentPage, 5, 10); // 한 페이지에 5개씩 표시
        param.put("pi", pi);

        List<BoardComment> list = adminDao.selectCommentsByUserId(param);
        return new PageResponse<>(pi, list);
    }
    
    
    
    @Override
    @Transactional
    public boolean updateUserRole(int userId, String newRole) {
    	UserAuthority userAuthority = new UserAuthority();
    	userAuthority.setUserId((long) userId);
    	userAuthority.setRoles(List.of(newRole));
    	
    	int result = adminDao.updateUserRole(userAuthority);
    	return result > 0;
    }
    
    @Override
    @Transactional
    public boolean unbanUser(int userId) {
    	int result = adminDao.unbanUser(userId);
    	return result > 0;
    }
    
    @Override
    @Transactional
    public boolean rejectReport(int reportId) {
    	int result = adminDao.updateReportStatus(reportId);
    	return result > 0;
    }
    
    @Override
    public DashboardStatsDTO getDashboardStats() {
    	return adminDao.selectDashboardStats();
    }
    
    @Override
    public int createReport(Report report) {
    	return adminDao.insertReport(report);
    }
    
}