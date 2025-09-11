package com.kh.mbtix.security.controller;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kh.mbtix.config.SecurityConfig;
import com.kh.mbtix.security.model.dto.AuthDto.AuthResult;
import com.kh.mbtix.security.model.dto.AuthDto.LoginRequest;
import com.kh.mbtix.security.model.dto.AuthDto.SignupRequest;
import com.kh.mbtix.security.model.dto.AuthDto.VerifyCodeRequest;
import com.kh.mbtix.security.model.provider.JWTProvider;
import com.kh.mbtix.security.model.service.AuthService;
import com.kh.mbtix.security.model.service.EmailService;
import com.kh.mbtix.security.model.service.KakaoService;

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
	
	//이메일 인증코드 발송
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

	    //이메일 중복 확인
	    @GetMapping("/checkemail")
	    public ResponseEntity<Boolean> checkemail(@RequestParam String email){
	    	boolean available = service.isEmailAvailable(email);
	    	return ResponseEntity.ok(available);
	    }

	
	
	    @PostMapping("/signup")
	    public ResponseEntity<?> signUp(@RequestBody SignupRequest srq) {
	    	log.debug("agree1={}, agree2={}, agree3={}", srq.isAgree1(), srq.isAgree2(), srq.isAgree3());

	        // 약관 동의 확인
	    	if (!srq.isAgree1() || !srq.isAgree2() || !srq.isAgree3()) {
	    	    return ResponseEntity
	    	            .status(HttpStatus.BAD_REQUEST)
	    	            .body("모든 약관에 동의해야 회원가입이 가능합니다.");
	    	}
	    	
	        // 2️⃣ 이메일 인증 코드 확인
	    	if (!emailService.verifyCode(srq.getEmail(), srq.getVerificationCode().trim(), true)) {
	    	    return ResponseEntity
	    	            .status(HttpStatus.BAD_REQUEST)
	    	            .body("인증 코드가 틀리거나 만료되었습니다.");
	    	}
	    	
	        try {
	            // 3️⃣ 회원가입 처리
	            AuthResult result = service.signUp(
	                    srq.getLoginId(),
	                    srq.getEmail(),
	                    srq.getName(),
	                    srq.getNickname(),
	                    srq.getPassword(),
	                    srq.getMbtiId()
	            );

	            // 4️⃣ Refresh Token 쿠키 설정
	            ResponseCookie refreshCookie = ResponseCookie.from(REFRESH_COOKIE, result.getRefreshToken())
	                    .httpOnly(true)
	                    .secure(false) // 배포 시 true로 변경
	                    .path("/")
	                    .sameSite("LAX")
	                    .maxAge(Duration.ofDays(7))
	                    .build();

	            return ResponseEntity.ok()
	                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
	                    .body(result);

	        } catch (Exception e) {
	            // 5️⃣ 예외 처리
	            return ResponseEntity
	                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body("회원가입 중 오류가 발생했습니다: " + e.getMessage());
	        }
	    }
	    
	    @PostMapping("/login")
	    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
	        final String loginId = req.getLoginId() == null ? "" : req.getLoginId().trim().toLowerCase();
	        final String password = req.getPassword() == null ? "" : req.getPassword();
	        final boolean rememberMe = req.isRememberMe();

	        try {
	            AuthResult result = service.login(loginId, password);
	            
	            //ResponseCookie refreshCookie = ResponseCookie
	            ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie
	                    .from(REFRESH_COOKIE, result.getRefreshToken())
	                    .httpOnly(true)
	                    .secure(false)            // 배포 시 true
	                    .path("/")
	                    .sameSite("Lax");
//	                    .maxAge(Duration.ofDays(7))
//	                    .build();
	            
	            if(rememberMe) {
	            	cookieBuilder.maxAge(Duration.ofDays(7));
	            }else {
	            	cookieBuilder.maxAge(-1);
	            }
	            
//	            return ResponseEntity.ok()
//	                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
//	                    .body(result); // 로그인은 user 포함 전체 반환 (프론트에서 userId 저장)
	            return ResponseEntity.ok()
	            		.header(HttpHeaders.SET_COOKIE,cookieBuilder.build().toString())
	            		.body(result);
	        } catch (BadCredentialsException e) {
	            // 아이디 없거나 비번 틀려도 동일하게 401
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
	                    .body("아이디 또는 비밀번호가 올바르지 않습니다.");
	        }
	    }

	 //accessToken 재발급 url
		@PostMapping("/refresh")
		public ResponseEntity<AuthResult> refresh(
				@CookieValue(name = REFRESH_COOKIE,required = false)
				String refreshCookie
				){
			if(refreshCookie == null || refreshCookie.isBlank()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			}
			AuthResult result = service.refreshByCookie(refreshCookie);
			return ResponseEntity.ok(result);
		}
		
}
