package com.kh.mbtix.admin;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.mbtix.admin.model.service.AdminService;
import com.kh.mbtix.admin.model.vo.DashboardStatsDTO;
import com.kh.mbtix.admin.model.vo.Report;
import com.kh.mbtix.admin.model.vo.UserDetailDTO;
import com.kh.mbtix.common.model.vo.PageResponse;
import com.kh.mbtix.miniGame.model.dto.CatchMindWord;
import com.kh.mbtix.miniGame.model.dto.Quiz;
import com.kh.mbtix.user.model.vo.UserEntity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private AdminService adminService;

	// 스피드 퀴즈, 캐치마인드 단어 데이터 넣기
	@PostMapping("/insertGameData")
	public ResponseEntity<Void> insertGameData(@RequestBody Map<String, Object> data) {
		adminService.insertGameData(data);

		log.info("받은 데이터 : {}", data);
		return ResponseEntity.ok().build();
	}
	
	// 스피드 퀴즈 다 가져오기
	@GetMapping("/selectAllSpeedQuiz")
	public List<Quiz> selectAllSpeedQuiz() {
		List<Quiz> speedQuizList = adminService.selectAllSpeedQuiz();
		return speedQuizList;
	}
	
	// 캐치마인드 단어 다 가져오기
	@GetMapping("/selectAllCatchMindWords")
	public List<CatchMindWord> selectAllCatchMindWords() {
		List<CatchMindWord> catchMindWordList = adminService.selectAllCatchMindWords();
		return catchMindWordList;
	}
	
	@PatchMapping("/updateSpeedQuiz")
	public ResponseEntity<Void> updateSpeedQuiz(@RequestBody Quiz quiz) {
		adminService.updateSpeedQuiz(quiz);
		return ResponseEntity.ok().build();
	}
	
	@PatchMapping("/updateCatchMindWord")
	public ResponseEntity<Void> updateCatchMindWord(@RequestBody CatchMindWord catchMindWord) {
		adminService.updateCatchMindWord(catchMindWord);
		return ResponseEntity.ok().build();
	}
	
	
	// 대시보드 통계 조회 메소드
	@GetMapping("/dashboard/stats")
	public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
		DashboardStatsDTO stats = adminService.getDashboardStats();
		return ResponseEntity.ok(stats);
	}

	// 회원 목록 조회
	@GetMapping("/users")
	public ResponseEntity<PageResponse<UserEntity>> getAllUsers(
			@RequestParam(value = "cpage", defaultValue = "1") int currentPage,
			@RequestParam(value = "searchType", required = false) String searchType,
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "status", required = false) String status) {

		try {
			PageResponse<UserEntity> response = adminService.selectAllUsers(currentPage, searchType, keyword, status);

			if (response.getList().isEmpty()) {
				return ResponseEntity.noContent().build();
			}

			return ResponseEntity.ok(response);

		} catch (Exception e) {
			// log.error("Error fetching users: ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	// 회원 상세 정보 조회
	@GetMapping("/users/{userId}")
	public ResponseEntity<UserDetailDTO> getUserDetail(@PathVariable("userId") int userId) {
		UserDetailDTO userDetail = adminService.selectUserDetail(userId);

		if (userDetail.getUserInfo() == null) {
			return ResponseEntity.notFound().build(); // 사용자가 없으면 404
		}

		return ResponseEntity.ok(userDetail);
	}

	// 신고 내역 조회
	@GetMapping("/reports")
	public ResponseEntity<PageResponse<Report>> getReportList(
			@RequestParam(value = "cpage", defaultValue = "1") int currentPage,
			@RequestParam(value = "searchType", required = false) String searchType,
			@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "category", required = false) String category) {

		try {
			PageResponse<Report> response = adminService.selectAllReports(currentPage, searchType, keyword, status,
					category);

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
	public ResponseEntity<String> processReport(@PathVariable("reportId") int reportId,
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

	// 관리자가 직접 제재
	@PostMapping("/users/{userId}/ban")
	public ResponseEntity<String> banUserDirectly(@PathVariable("userId") int userId,
			@RequestBody Map<String, Object> payload, @AuthenticationPrincipal Long adminUserId) {
		try {
			if (!payload.containsKey("banDuration") || !payload.containsKey("reason")) {
				return ResponseEntity.badRequest().body("필수 파라미터(제재 기간, 사유)가 누락되었습니다.");
			}

			int banDuration = Integer.parseInt(payload.get("banDuration").toString());
			String reason = payload.get("reason").toString();

			boolean success = adminService.banUserDirectly(userId, banDuration, reason, adminUserId.intValue());

			if (success) {
				return ResponseEntity.ok("사용자 제재 처리가 완료되었습니다.");
			} else {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("제재 처리 중 오류가 발생했습니다.");
			}

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
		}
	}

	// 권한 변경
	@PatchMapping("/users/{userId}/role")
	public ResponseEntity<String> updateUserRole(@PathVariable("userId") int userId,
			@RequestBody Map<String, String> payload) {

		String newRole = payload.get("newRole");
		if (newRole == null || (!newRole.equals("ROLE_USER") && !newRole.equals("ROLE_ADMIN"))) {
			return ResponseEntity.badRequest().body("잘못된 권한 값입니다.");
		}

		try {
			boolean success = adminService.updateUserRole(userId, newRole);
			if (success) {
				return ResponseEntity.ok("회원 권한이 성공적으로 변경되었습니다.");
			} else {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("권한 변경 중 오류가 발생했습니다.");
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
		}
	}

	// 정지 해제
	@DeleteMapping("/users/{userId}/ban")
    public ResponseEntity<String> unbanUser(@PathVariable("userId") int userId) {
    	try {
    		boolean success = adminService.unbanUser(userId);
    		if (success) {
    			return ResponseEntity.ok("사용자 정지 해제가 완료되었습니다.");
    		} else {
    			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해제할 제재 기록을 찾을 수 없습니다.");
    		}
    	} catch (Exception e) {
    		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
    	}
    }
}