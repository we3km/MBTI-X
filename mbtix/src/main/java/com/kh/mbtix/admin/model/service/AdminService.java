package com.kh.mbtix.admin.model.service;

import java.util.List;
import java.util.Map;

import com.kh.mbtix.admin.model.vo.Report;
import com.kh.mbtix.admin.model.vo.UserDetailDTO;
import com.kh.mbtix.common.model.vo.PageResponse;
import com.kh.mbtix.miniGame.model.dto.CatchMindWord;
import com.kh.mbtix.miniGame.model.dto.Quiz;
import com.kh.mbtix.user.model.vo.UserEntity;
import com.kh.mbtix.admin.model.vo.DashboardStatsDTO;

public interface AdminService {
	void insertGameData(Map<String, Object> data);
    
	PageResponse<UserEntity> selectAllUsers(int currentPage, String searchType, String keyword, String status);
    
    PageResponse<Report> selectAllReports(int currentPage, String searchType, String keyword, String status, String category);
    
    Report selectReport(int reportId);

    boolean processReport(int reportId, int banDuration, int adminUserNum);
    
    UserDetailDTO selectUserDetail(int userId);
    
    boolean banUserDirectly(int userId, int banDuration, String reason, int adminUserId);
    
    boolean updateUserRole(int userId, String newRole);
    
    boolean unbanUser(int userId);
    
    DashboardStatsDTO getDashboardStats();

	List<Quiz> selectAllSpeedQuiz();

	List<CatchMindWord> selectAllCatchMindWords();

	void updateSpeedQuiz(Quiz quiz);

	void updateCatchMindWord(CatchMindWord catchMindWord);

	void deleteSpeedQuiz(int id);

	void deleteCatchMindWord(int id);
}