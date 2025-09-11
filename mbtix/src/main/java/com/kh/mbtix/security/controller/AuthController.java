package com.kh.mbtix.security.controller;

import java.time.Duration;
import java.util.List;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import com.kh.mbtix.common.MbtiUtils;
import com.kh.mbtix.config.SecurityConfig;
import com.kh.mbtix.security.model.dto.AuthDto.AuthResult;
import com.kh.mbtix.security.model.dto.AuthDto.FileVO;
import com.kh.mbtix.security.model.dto.AuthDto.LoginRequest;
import com.kh.mbtix.security.model.dto.AuthDto.SignupRequest;
import com.kh.mbtix.security.model.dto.AuthDto.SocialSignupRequest;
import com.kh.mbtix.security.model.dto.AuthDto.User;
import com.kh.mbtix.security.model.dto.AuthDto.UserAuthority;
import com.kh.mbtix.security.model.dto.AuthDto.UserIdentities;
import com.kh.mbtix.security.model.dto.AuthDto.VerifyCodeRequest;
import com.kh.mbtix.security.model.provider.JWTProvider;
import com.kh.mbtix.security.model.service.AuthService;
import com.kh.mbtix.security.model.service.EmailService;
import com.kh.mbtix.security.model.service.KakaoService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final SecurityConfig securityConfig;
	private final KakaoService kakaoService;
	private final AuthService service;
	private final JWTProvider jwt;
	public static final String REFRESH_COOKIE = "REFRESH_TOKEN";
	private final EmailService emailService;

	// 이메일 인증코드 발송
	@PostMapping("/send-code")
	public ResponseEntity<String> sendCode(@RequestParam String email) {
		emailService.sendVerificationCode(email);
		return ResponseEntity.ok("인증 코드 전송 완료");
	}

	// 인증 코드 확인
	@PostMapping("/verify-code")
	public ResponseEntity<String> verifyCode(@RequestBody VerifyCodeRequest request) {
		boolean valid = emailService.verifyCode(request.getEmail(), request.getCode(), false);
		if (valid) {
			return ResponseEntity.ok("인증 완료");
		} else {
			return ResponseEntity.badRequest().body("인증 실패: 코드가 틀리거나 만료됨");
		}
	}

	// 로그인 아이디 중복 확인
	@GetMapping("/checkId")
	public ResponseEntity<Boolean> checkId(@RequestParam String loginId) {
		boolean available = service.isLoginIdAvailable(loginId);
		log.debug("checkId({}) -> available={}", loginId, available);
		return ResponseEntity.ok(available);
	}

	// 닉네임 중복 확인
	@GetMapping("/checkNickname")
	public ResponseEntity<Boolean> checkNickname(@RequestParam String nickname) {
		boolean available = service.isNicknameAvailable(nickname);
		return ResponseEntity.ok(available);
	}

	// 이메일 중복 확인
	@GetMapping("/checkemail")
	public ResponseEntity<Boolean> checkemail(@RequestParam String email) {
		boolean available = service.isEmailAvailable(email);
		return ResponseEntity.ok(available);
	}

	@PostMapping("/signup")
	public ResponseEntity<?> signUp(@RequestBody SignupRequest srq) {
		log.debug("agree1={}, agree2={}, agree3={}", srq.isAgree1(), srq.isAgree2(), srq.isAgree3());

		// 약관 동의 확인
		if (!srq.isAgree1() || !srq.isAgree2() || !srq.isAgree3()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("모든 약관에 동의해야 회원가입이 가능합니다.");
		}

		// 2️⃣ 이메일 인증 코드 확인
		if (!emailService.verifyCode(srq.getEmail(), srq.getVerificationCode().trim(), true)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증 코드가 틀리거나 만료되었습니다.");
		}

		try {
			// 3️⃣ 회원가입 처리
			AuthResult result = service.signUp(srq.getLoginId(), srq.getEmail(), srq.getName(), srq.getNickname(),
					srq.getPassword(), srq.getMbtiId());

			// 4️⃣ Refresh Token 쿠키 설정
			ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_COOKIE, result.getRefreshToken()).httpOnly(true)
					.secure(false) // 배포 시 true로 변경
					.path("/").sameSite("LAX").maxAge(Duration.ofDays(7)).build();

			return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, refreshCookie.toString()).body(result);

		} catch (Exception e) {
			// 5️⃣ 예외 처리
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원가입 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest req) {
		final String loginId = req.getLoginId() == null ? "" : req.getLoginId().trim().toLowerCase();
		final String password = req.getPassword() == null ? "" : req.getPassword();
		final boolean rememberMe = req.isRememberMe();

		try {
			AuthResult result = service.login(loginId, password);

			// ResponseCookie refreshCookie = ResponseCookie
			ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie
					.from(REFRESH_COOKIE, result.getRefreshToken()).httpOnly(true).secure(false) // 배포 시 true
					.path("/").sameSite("Lax");

			if (rememberMe) {
				cookieBuilder.maxAge(Duration.ofDays(7));
			} else {
				cookieBuilder.maxAge(-1);
			}


			return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookieBuilder.build().toString()).body(result);
		} catch (BadCredentialsException e) {
			// 아이디 없거나 비번 틀려도 동일하게 401
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디 또는 비밀번호가 올바르지 않습니다.");
		}
	}

	// accessToken 재발급 url
	@PostMapping("/refresh")
	public ResponseEntity<AuthResult> refresh(
			@CookieValue(name = REFRESH_COOKIE, required = false) String refreshCookie) {
		if (refreshCookie == null || refreshCookie.isBlank()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		AuthResult result = service.refreshByCookie(refreshCookie);
		return ResponseEntity.ok(result);
	}
	
	//로그아웃
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(
			HttpServletRequest req
			){
		String accessToken = resolveAccessToken(req);
		Long userId = jwt.getUserId(accessToken);
		
		ResponseCookie refreshCookie = 
				ResponseCookie
				.from(REFRESH_COOKIE, "")
				.httpOnly(true)
				.secure(false) //https에서만 사용(true)
				.path("/")
				.sameSite("Lax")
				.maxAge(0) // 만료시간
				.build();
		return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, refreshCookie.toString()).build();
		
	}
	
	// 소셜로그인 회원가입 추가 기재사항
	@PostMapping("/social-signup")
	public ResponseEntity<AuthResult> socialSignup(@RequestBody SocialSignupRequest req) {
	    // 1️⃣ User 생성
	    User user = User.builder()
	            .loginId(req.getLoginId())
	            .email(req.getEmail())
	            .nickname(req.getNickname())
	            .name(req.getName())
	            .mbtiId(req.getMbtiId())
	            .build();
	    service.insertUser(user); // userId 생성됨

	    // 2️⃣ UserIdentities 처리 (insert or update)
	    UserIdentities existing = service.findUserIdentities(req.getProvider(), req.getProviderUserId());

	    if (existing == null) {
	        // 신규 소셜 계정 → insert
	        UserIdentities identities = UserIdentities.builder()
	                .provider(req.getProvider())
	                .providerUserId(req.getProviderUserId())
	                .userId(user.getUserId())
	                .accessToken(req.getAccessToken())
	                .build();
	        service.insertUserIdentities(identities);
	    } else {
	        // 기존 소셜 계정 → userId 연결 업데이트
	        existing.setUserId(user.getUserId());
	        existing.setAccessToken(req.getAccessToken());
	        service.updateUserIdentities(existing);
	    }

	    // 3️⃣ 권한 부여
	    UserAuthority auth = UserAuthority.builder()
	            .userId(user.getUserId())
	            .roles(List.of("ROLE_USER"))
	            .build();
	    service.insertUserRole(auth);
	    
	    String fileName = MbtiUtils.getProfileFileName(req.getMbtiId()); // 예: "ENTP.JPG"
	    FileVO file = FileVO.builder()
	            .fileName(fileName)
	            .refId(user.getUserId()) // USER_ID 참조
	            .categoryId(4)           // 4 = 프로필
	            .build();
	    service.insertProfile(file);
	    
	    user.setProfileFileName(fileName);

	    // 4️⃣ JWT 발급
	    String accessToken = jwt.createAccessToken(user.getUserId(), 30);   // 30분
	    String refreshToken = jwt.createRefreshToken(user.getUserId(), 7); // 7일

	    ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, refreshToken)
	            .httpOnly(true)
	            .secure(false) // HTTPS 환경이면 true 권장
	            .sameSite("Lax")
	            .path("/")
	            .maxAge(Duration.ofDays(7))
	            .build();
	    

	    // 5️⃣ 응답 반환
	    AuthResult result = AuthResult.builder()
	            .accessToken(accessToken)
	            .refreshToken(refreshToken)
	            .user(user)
	            .build();

	    return ResponseEntity.ok()
	            .header(HttpHeaders.SET_COOKIE, cookie.toString())
	            .body(result);
	}
	
	
	@GetMapping("/me")
	public ResponseEntity<User> getUserInfo(HttpServletRequest req){
		//1, 요청 헤더에서 jwt토큰 추출
		String jwtToken = resolveAccessToken(req);
		if(jwtToken == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		//2. JWT토큰에서 ID값 추출
		Long userId = jwt.getUserId(jwtToken);
		
		// 사용자정보 조회
		User user = service.findUserByUserId(userId);
		if(user == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(user);
		
	}
	
	public String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
	
	@GetMapping("/namematch")
	public ResponseEntity<Boolean> namemetch(@RequestParam String name){
		boolean available = service.matchName(name);
			log.debug("nameMetch({}) -> available={}",name,available);
			return ResponseEntity.ok(available);
		}
	
	
	@PostMapping("/send-code-if-match")
	public ResponseEntity<String> sendCodeIfMatch(
	        @RequestParam String name,
	        @RequestParam String email) {
	    log.debug("sendCodeIfMatch(name={}, email={})", name, email);

	    // 이름 + 이메일 일치 여부 확인
	    boolean exists = service.existsByNameAndEmail(name, email);

	    if (!exists) {
	        return ResponseEntity.badRequest().body("이름과 이메일이 일치하지 않습니다.");
	    }

	    // ✅ 기존 메서드 재사용
	    emailService.sendVerificationCode(email);

	    return ResponseEntity.ok("인증 코드 전송 완료");
	}
	
	
	@GetMapping("/find-id")
	public ResponseEntity<?> findId(
	        @RequestParam String name,
	        @RequestParam String email
	) {
	    log.debug("findId(name={}, email={})", name, email);

	    try {
	        String maskedId = service.findId(name, email);
	        log.info("✅ findId result={}", maskedId);
	        return ResponseEntity.ok(maskedId); // 마스킹된 아이디 또는 안내 메시지
	    } catch (IllegalArgumentException e) {
	        return ResponseEntity.badRequest().body(e.getMessage());
	    } catch (Exception e) {
	        log.error("findId error", e);
	        return ResponseEntity.status(500).body("서버 오류가 발생했습니다.");
	    }
	}
	
	@GetMapping("/idmatch")
	public ResponseEntity<Boolean> idmatch(@RequestParam String name,
											@RequestParam String loginId){
		boolean available = service.idmatch(name,loginId);
			log.debug("namematch({}) idMatch({}) -> available={}",name,loginId,available);
			return ResponseEntity.ok(available);
		}
	
	
	@PostMapping("/pw-send-code")
	public ResponseEntity<String> pwUpdateIfMatch(
	        @RequestParam String name,
	        @RequestParam String loginId,
	        @RequestParam String email
	) {
	    log.debug("sendCodeIfMatch(name={}, loginId={}, email={})", name, loginId, email);

	    try {
	        // 1️ 회원 조회
	        User user = service.findByNameLoginIdEmail(name, loginId, email);
	        if (user == null) {
	            return ResponseEntity.badRequest().body("일치하는 회원 정보가 없습니다.");
	        }

	        // 2️ 소셜 로그인 계정 여부 체크
	        if (user.getProvider() != null && !user.getProvider().isBlank()) {
	            // provider 값을 사람이 알아보기 쉽게 표시
	            String providerName = switch (user.getProvider().toLowerCase()) {
	                case "kakao" -> "카카오";
	                case "google" -> "구글";
	                case "naver" -> "네이버";
	                default -> user.getProvider();
	            };

	            return ResponseEntity.badRequest().body(
	                providerName + " 계정으로 가입된 사용자입니다. 비밀번호 변경이 불가능합니다."
	            );
	        }

	        // 3️ 이메일 인증 코드 발송
	        emailService.sendVerificationCode(email);
	        return ResponseEntity.ok("인증 코드 전송 완료");

	    } catch (Exception e) {
	        log.error("pw-send-code error", e);
	        return ResponseEntity.status(500).body("서버 오류가 발생했습니다.");
	    }
	}

	
	@PutMapping("/updatePW")
	public ResponseEntity<?> updatePW(
			@RequestParam String name,
			@RequestParam String loginId,
			@RequestParam String email,
			@RequestParam String password
			){
		log.debug("findId(name={}, loginId={} , email={} , )", name, loginId, email);
		
		try {
	        String result = service.updatePw(name, loginId, email, password);
	        return ResponseEntity.ok(result);
	    } catch (IllegalArgumentException e) {
	        // 비즈니스 로직에서 던진 예외 처리 (예: 소셜 로그인 계정은 비번 변경 불가)
	        return ResponseEntity.badRequest().body(e.getMessage());
	    } catch (Exception e) {
	        log.error("❌ updatePW error", e);
	        return ResponseEntity.status(500).body("서버 오류가 발생했습니다.");
	    }
		
	}
	 
}

	
	
