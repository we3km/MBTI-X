package com.kh.mbtix.security.controller;

import java.time.Duration;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
@CrossOrigin(origins = "http://192.168.10.230:5173")
public class AuthController {

	private final SecurityConfig securityConfig;
	private final KakaoService kakaoService;
	private final AuthService service;
	private final JWTProvider jwt;
	public static final String REFRESH_COOKIE = "REFRESH_TOKEN";
	private final EmailService emailService;

	@PostMapping("/send-code")
	public ResponseEntity<String> sendCode(@RequestParam String email) {
		emailService.sendVerificationCode(email);
		return ResponseEntity.ok("인증 코드 전송 완료");
	}

	@PostMapping("/verify-code")
	public ResponseEntity<String> verifyCode(@RequestBody VerifyCodeRequest request) {
		boolean valid = emailService.verifyCode(request.getEmail(), request.getCode(), false);
		if (valid) {
			return ResponseEntity.ok("인증 완료");
		} else {
			return ResponseEntity.badRequest().body("인증 실패: 코드가 틀리거나 만료됨");
		}
	}
	@GetMapping("/checkId")
	public ResponseEntity<Boolean> checkId(@RequestParam String loginId) {
		boolean available = service.isLoginIdAvailable(loginId);
		log.debug("checkId({}) -> available={}", loginId, available);
		return ResponseEntity.ok(available);
	}

	@GetMapping("/checkNickname")
	public ResponseEntity<Boolean> checkNickname(@RequestParam String nickname) {
		boolean available = service.isNicknameAvailable(nickname);
		return ResponseEntity.ok(available);
	}
	
	@GetMapping("/checkemail")
	public ResponseEntity<Boolean> checkemail(@RequestParam String email) {
		boolean available = service.isEmailAvailable(email);
		return ResponseEntity.ok(available);
	}

	@PostMapping("/signup")
	public ResponseEntity<?> signUp(@RequestBody SignupRequest srq) {
		log.debug("agree1={}, agree2={}, agree3={}", srq.isAgree1(), srq.isAgree2(), srq.isAgree3());

		if (!srq.isAgree1() || !srq.isAgree2()) { // isAgree3는 선택이므로 제외
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("필수 약관에 동의해야 회원가입이 가능합니다.");
		}

		if (!emailService.verifyCode(srq.getEmail(), srq.getVerificationCode().trim(), true)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증 코드가 틀리거나 만료되었습니다.");
		}

		try {
			AuthResult result = service.signUp(srq.getLoginId(), srq.getEmail(), srq.getName(), srq.getNickname(),
					srq.getPassword(), srq.getMbtiId());

			ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_COOKIE, result.getRefreshToken()).httpOnly(true)
					.secure(false)
					.path("/").sameSite("LAX").maxAge(Duration.ofDays(7)).build();

			return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, refreshCookie.toString()).body(result);

		} catch (Exception e) {
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

			ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie
					.from(REFRESH_COOKIE, result.getRefreshToken()).httpOnly(true).secure(false)
					.path("/").sameSite("Lax");

			if (rememberMe) {
				cookieBuilder.maxAge(Duration.ofDays(7));
			} else {
				cookieBuilder.maxAge(-1);
			}

			return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookieBuilder.build().toString()).body(result);
		} catch (BadCredentialsException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		}
	}

	@PostMapping("/refresh")
	public ResponseEntity<AuthResult> refresh(
			@CookieValue(name = REFRESH_COOKIE, required = false) String refreshCookie) {
		if (refreshCookie == null || refreshCookie.isBlank()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		AuthResult result = service.refreshByCookie(refreshCookie);
		return ResponseEntity.ok(result);
	}
	
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletRequest req){
		String accessToken = resolveAccessToken(req);
		Long userId = jwt.getUserId(accessToken);
		
		ResponseCookie refreshCookie = 
				ResponseCookie
				.from(REFRESH_COOKIE, "")
				.httpOnly(true)
				.secure(false)
				.path("/")
				.sameSite("Lax")
				.maxAge(0)
				.build();
		return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, refreshCookie.toString()).build();
	}
	
	@PostMapping("/social-signup")
	public ResponseEntity<AuthResult> socialSignup(@RequestBody SocialSignupRequest req) {
	    User user = User.builder()
	            .loginId(req.getLoginId())
	            .email(req.getEmail())
	            .nickname(req.getNickname())
	            .name(req.getName())
	            .mbtiId(req.getMbtiId())
	            .build();
	    service.insertUser(user);

	    UserIdentities existing = service.findUserIdentities(req.getProvider(), req.getProviderUserId());
	    if (existing == null) {
	        UserIdentities identities = UserIdentities.builder()
	                .provider(req.getProvider())
	                .providerUserId(req.getProviderUserId())
	                .userId(user.getUserId())
	                .accessToken(req.getAccessToken())
	                .build();
	        service.insertUserIdentities(identities);
	    } else {
	        existing.setUserId(user.getUserId());
	        existing.setAccessToken(req.getAccessToken());
	        service.updateUserIdentities(existing);
	    }

	    UserAuthority auth = UserAuthority.builder()
	            .userId(user.getUserId())
	            .roles(List.of("ROLE_USER"))
	            .build();
	    service.insertUserRole(auth);
	    
	    String fileName = MbtiUtils.getProfileFileName(req.getMbtiId());
	    FileVO file = FileVO.builder()
	            .fileName(fileName)
	            .refId(user.getUserId())
	            .categoryId(4)
	            .build();
	    service.insertProfile(file);
	    
	    user.setProfileFileName(fileName);
	    user.setRoles(auth.getRoles()); // [중요] user 객체에 roles 설정

	    // [수정] 토큰 생성 시 user 객체에서 roles 정보를 가져옵니다.
	    String accessToken = jwt.createAccessToken(user.getUserId(), user.getRoles(), 30);
	    String refreshToken = jwt.createRefreshToken(user.getUserId(), 7);

	    ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, refreshToken)
	            .httpOnly(true)
	            .secure(false)
	            .sameSite("Lax")
	            .path("/")
	            .maxAge(Duration.ofDays(7))
	            .build();
	    
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
		String jwtToken = resolveAccessToken(req);
		if(jwtToken == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		
		Long userId = jwt.getUserId(jwtToken);
		
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

	    boolean exists = service.existsByNameAndEmail(name, email);
	    if (!exists) {
	        return ResponseEntity.badRequest().body("이름과 이메일이 일치하지 않습니다.");
	    }

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
	        return ResponseEntity.ok(maskedId);
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
	        User user = service.findByNameLoginIdEmail(name, loginId, email);
	        if (user == null) {
	            return ResponseEntity.badRequest().body("일치하는 회원 정보가 없습니다.");
	        }

	        if (user.getProvider() != null && !user.getProvider().isBlank()) {
	            String providerName = switch (user.getProvider().toLowerCase()) {
	                case "kakao" -> "카카오";
	                case "google" -> "구글";
	                case "naver" -> "네이버";
	                default -> user.getProvider();
	            };

	            return ResponseEntity.badRequest().body(
	                providerName + " 계정으로 가입된 사용자입니다."
	            );
	        }

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
	        return ResponseEntity.badRequest().body(e.getMessage());
	    } catch (Exception e) {
	        log.error("❌ updatePW error", e);
	        return ResponseEntity.status(500).body("서버 오류가 발생했습니다.");
	    }
	}
}