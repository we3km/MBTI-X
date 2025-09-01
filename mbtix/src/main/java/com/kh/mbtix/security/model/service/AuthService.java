package com.kh.mbtix.security.model.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.mbtix.security.model.dao.AuthDao;
import com.kh.mbtix.security.model.dto.AuthDto.AuthResult;
import com.kh.mbtix.security.model.dto.AuthDto.SignupRequest;
import com.kh.mbtix.security.model.dto.AuthDto.User;
import com.kh.mbtix.security.model.dto.AuthDto.UserAuthority;
import com.kh.mbtix.security.model.dto.AuthDto.UserCredential;
import com.kh.mbtix.security.model.provider.JWTProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
	
	private final PasswordEncoder encoder;
	private final AuthDao authDao;
	private final JWTProvider jwt;
	private final KakaoService service;
	
	
	@Transactional
	public AuthResult signUp(String loginId, String email, String name, String nickname, String password, String mbti) {
		
		 // 1️⃣ 필수 입력값 체크
        if (loginId == null || loginId.isBlank()) {
            throw new IllegalArgumentException("아이디는 필수 입력입니다.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일은 필수 입력입니다.");
        }
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("닉네임은 필수 입력입니다.");
        }
        if (password == null || password.isBlank() || password.length() < 8) {
            throw new IllegalArgumentException("비밀번호는 최소 8자리 이상이어야 합니다.");
        }
        if (mbti == null || mbti.isBlank()) {
        	throw new IllegalArgumentException("mbti는 필수 입력입니다.");
        }

        // 2️ 아이디 중복 체크
        if (authDao.findByLoginId(loginId) != null) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        // 3️ 이메일 중복 체크
        if (authDao.findByEmail(email) != null) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 4️ 닉네임 중복 체크
        if (authDao.findByNickname(nickname) != null) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
		
        User user = User.builder()
                .loginId(loginId)
                .email(email)
                .nickname(nickname)
                .name(name)
                .mbtiId(mbti)
                .build();
        authDao.insertUser(user);
        user.setUserId(user.getUserId()); // selectKey 결과 반영
		
		// user_credentials 테이블 
		UserCredential cred = UserCredential.builder()
							.userId(user.getUserId())
							.password(encoder.encode(password))
							.build();
		authDao.insertCred(cred);
		
		//user_authority 테이블
		UserAuthority auth = UserAuthority.builder()
				.userId(user.getUserId())
				.roles(List.of("ROLE_USER"))
				.build();
		authDao.insertUserRole(auth);
		
		// 토큰 발급
		String accessToken = jwt.createAccessToken(user.getUserId(), 30);
		String refreshToken = jwt.createRefreshToken(user.getUserId(), 7);
		
		user = authDao.findUserByUserId(user.getUserId());
		
		return AuthResult.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.user(user)
				.build();
		
	}


	public boolean isLoginIdAvailable(String loginId) {
		return authDao.findByLoginId(loginId) == null;
	}


	public boolean isNicknameAvailable(String nickname) {
		return authDao.findByNickname(nickname) == null;
	}
	
	

}
