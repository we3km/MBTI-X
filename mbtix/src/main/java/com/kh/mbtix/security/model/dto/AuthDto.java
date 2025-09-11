package com.kh.mbtix.security.model.dto;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
public class AuthDto {
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class LoginRequest{
		private String loginId;
		private String password;
		private boolean rememberMe;
		
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class AuthResult{
		private String accessToken;
		private String refreshToken;
		private User user;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class UserCredential {
		private Long userId;
		private String password;
	}
	
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class SignupRequest {
		private Long userId;
	    private String loginId;   // 사용자 ID (로컬 회원가입용)
	    private String password;  // 비밀번호
	    private String email;     // 이메일
	    private String name;      // 이름
	    private String nickname;  // 닉네임
	    private String profile;   // 프로필 이미지 URL
	    private String mbtiId;
	    private String verificationCode;
	   
	    @JsonProperty("agree1")
	    private boolean agree1; //약관동의
	    @JsonProperty("agree2")
	    private boolean agree2;
	    @JsonProperty("agree3")
	    private boolean agree3;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class SignupResponse {
	    private Long userId;       // 새로 생성된 사용자 PK
	    private String email;      // 이메일
	    private String name;       // 이름
	    private String nickname;   // 닉네임
	    private String profile;    // 프로필 이미지
	    private List<String> roles; // 권한 정보 (기본 ROLE_USER)
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class UserAuthority{
		private Long userId;
		private List<String> roles;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class UserIdentities{
		private Long userIdentitiesId;
		private Long userId;
		private String accessToken;
		private String provider;
		private String providerUserId;
		
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class User{
	private Long userId;
	private String password;
    private String loginId;   // 사용자 ID (로컬 회원가입용)
    private String email;     // 이메일
    private String name;      // 이름
    private String nickname;  // 닉네임
    private String mbtiId;
    private List<String> roles;
	}
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class VerifyCodeRequest {
	    private String email;
	    private String code;
	}
}





