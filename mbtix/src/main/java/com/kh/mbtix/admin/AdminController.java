package com.kh.mbtix.admin;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.mbtix.admin.model.service.AdminService;
import com.kh.mbtix.admin.model.vo.Report;
import com.kh.mbtix.common.model.vo.PageResponse;
import com.kh.mbtix.user.model.vo.UserEntity;

@RestController
@RequestMapping("/admin")
public class AdminController {
	
    @Autowired
    private AdminService adminService;

    // 회원 목록 조회
    @GetMapping("/users")
    public ResponseEntity<PageResponse<UserEntity>> getAllUsers(
            @RequestParam(value="cpage", defaultValue="1") int currentPage) {
        
        try {
            PageResponse<UserEntity> response = adminService.selectAllUsers(currentPage);
            
            if (response.getList().isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            // log.error("Error fetching users: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // 신고 내역 조회
    @GetMapping("/reports")
    public ResponseEntity<PageResponse<Report>> getReportList(
            @RequestParam(value="cpage", defaultValue="1") int currentPage) {
    	
        try {
            PageResponse<Report> response = adminService.selectAllReports(currentPage);
            
            if (response.getList().isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // 신고 내역 상세 조회
    @GetMapping("/reports/{reportId}")
    public ResponseEntity<Report> getReportDetail(@PathVariable("reportId") int reportId) {
    	
        try {
            Report report = adminService.selectReport(reportId);
            
            if (report == null) {
                return ResponseEntity.notFound().build(); // 해당 신고가 없을 때 404
            }
            
            return ResponseEntity.ok(report);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // 신고 처리
    @PostMapping("/reports/{reportId}/process")
    public ResponseEntity<String> processReport(
            @PathVariable("reportId") int reportId, 
            @RequestBody Map<String, Object> payload) {
        
        try {
            if (!payload.containsKey("banDuration") || !payload.containsKey("adminUserNum")) {
                return ResponseEntity.badRequest().body("필수 파라미터가 누락되었습니다."); // 잘못된 요청 400
            }
            
            int banDuration = Integer.parseInt(payload.get("banDuration").toString());
            int adminUserNum = Integer.parseInt(payload.get("adminUserNum").toString());

            boolean success = adminService.processReport(reportId, banDuration, adminUserNum);
            
            if (success) {
                return ResponseEntity.ok("신고 처리가 완료되었습니다.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("처리할 신고를 찾을 수 없습니다."); // 대상을 못찾았을 때 404
            }
            
        } catch (NumberFormatException e) {
             return ResponseEntity.badRequest().body("파라미터 타입이 올바르지 않습니다."); // 잘못된 요청 400
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다."); // 서버 에러 500
        }
    }
}