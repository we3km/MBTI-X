package com.kh.mbtix.security.model.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

import com.kh.mbtix.common.MbtiUtils;
import com.kh.mbtix.security.model.dao.AuthDao;
import com.kh.mbtix.security.model.dto.AuthDto.AuthResult;
import com.kh.mbtix.security.model.dto.AuthDto.FileVO;
import com.kh.mbtix.security.model.dto.AuthDto.SignupRequest;
import com.kh.mbtix.security.model.dto.AuthDto.User;
import com.kh.mbtix.security.model.dto.AuthDto.UserAuthority;
import com.kh.mbtix.security.model.dto.AuthDto.UserCredential;
import com.kh.mbtix.security.model.dto.AuthDto.UserIdentities;
import com.kh.mbtix.security.model.provider.JWTProvider;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
	
	private final PasswordEncoder encoder;
	private final AuthDao authDao;
	private final JWTProvider jwt;
	private final KakaoService service;
	
	
	@Transactional
	public AuthResult signUp(String loginId, String email, String name, String nickname, String password, String mbtiId) {
		

		
		 // 1️ 필수 입력값 체크
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
        if (mbtiId == null || mbtiId.isBlank()) {
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
                .mbtiId(mbtiId)
                .build();
        authDao.insertUser(user);
        System.out.println("생성된 유저 ID = " + user.getUserId());
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
		

		 String fileName = MbtiUtils.getProfileFileName(mbtiId);

		    FileVO file = FileVO.builder()
		            .fileName(fileName)     // "istp.jpg"
		            .refId(user.getUserId())// USER_ID
		            .categoryId(4)          // 프로필
		            .build();
		    authDao.insertProfile(file);
		
		user = authDao.findUserByUserId(user.getUserId());
		
		return AuthResult.builder()
				.user(user)
				.build();
		
	}


	public boolean isLoginIdAvailable(String loginId) {
		log.debug("중복검사 아이디({})",loginId);
		return authDao.findByLoginId(loginId) == null;
	}


	public boolean isNicknameAvailable(String nickname) {
		return authDao.findByNickname(nickname) == null;
	}


	public boolean existsByLoginId(String loginId) {
		User user = authDao.findByLoginId(loginId);
		return user != null;
	}


	public AuthResult login(String loginId, String password) {
		User user = authDao.findByLoginpassword(loginId);
		
		if(!encoder.matches(password, user.getPassword())) {
			throw new BadCredentialsException("비밀번호 오류");
		}
		
		String acessToken = jwt.createAccessToken(user.getUserId(), 30);
		String refreshToken = jwt.createRefreshToken(user.getUserId(), 7);
		
		User userNoPassword = User.builder()
				.userId(user.getUserId())
				.loginId(user.getLoginId())
				.name(user.getName())
				.nickname(user.getNickname())
				.roles(user.getRoles())
				.email(user.getEmail())
				.mbtiId(user.getMbtiId())
				.point(user.getPoint())
				.mbtiName(user.getMbtiName())
				.profileFileName(user.getProfileFileName())
				
				.build();
		return AuthResult.builder()
				.accessToken(acessToken)
				.refreshToken(refreshToken)
				.user(userNoPassword)
				.build();
	}


	public AuthResult refreshByCookie(String refreshCookie) {
		Long userId = jwt.parseRefresh(refreshCookie);
		User user = authDao.findUserByUserId(userId);
		
		String accessToken =jwt.createAccessToken(userId, 30);
		
		return AuthResult.builder()
				.accessToken(accessToken)
				.user(user)
				.build();
	}
	
//	@PostMapping("/logout")
//	public ResponseEntity<Void> logiut(HttpServletRequest request){
//		
//		String accessToken = resolveAccessToken(request);
//		Long userId = jwt.getUserId(accessToken);
//		
//		String authAccessToken = service.getauthAccessToken(userId);
//		
//		if(authAccessToken != null) {
//			
//		}
//			
//	}


	public String resolveAccessToken(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authroiztion");
		if(bearerToken != null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
		
	
	}


	public boolean isEmailAvailable(String email) {
		return authDao.findByEmail(email) == null; 
	}


	public void insertUser(User user) {
		authDao.insertUser(user);
	}


	public void updateUserIdentities(UserIdentities identities) {
		authDao.updateUserIdentities(identities);
	}


	public UserIdentities findUserIdentities(String provider, String providerUserId) {
		
		return authDao.findUserIdentities(provider,providerUserId);
		
	}


	public void insertUserIdentities(UserIdentities identities) {
		authDao.insertIdentities(identities);
	}


	public void insertUserRole(UserAuthority auth) {
		authDao.insertRole(auth);
	}


	public User findUserByUserId(Long userId) {
		return authDao.findUserByUserId(userId);
	}


	public boolean matchName(String name) {
		User user =authDao.matchName(name);
		return user != null;
	}

	public boolean existsByNameAndEmail(String name, String email) {
		User user = authDao.existsByNameAndEmail(name,email);
		return user !=null ;
	}


	public String findId(String name, String email) {
	    User user = authDao.findByNameAndEmail(name, email);
	    if (user == null) {
	        throw new IllegalArgumentException("일치하는 회원 정보가 없습니다.");
	    }

	    // 소셜 가입자인 경우 안내 메시지 반환
	    if (user.getProvider() != null && !user.getProvider().isBlank()) {
	        return user.getProvider() + " 계정으로 가입된 사용자입니다.";
	    }

	    // 일반 회원인 경우 아이디 마스킹 처리
	    String loginId = user.getLoginId();
	    if (loginId.length() <= 3) {
	        // 3글자 이하라면 첫 글자 + 나머지 마스킹
	        return loginId.charAt(0) + "*".repeat(loginId.length() - 1);
	    } else {
	        String start = loginId.substring(0, 3);  // 앞 3자리
	        String end = loginId.substring(loginId.length() - 1); // 맨 끝 1자리
	        int maskLength = loginId.length() - 4;  // 가운데 부분 길이
	        return start + "*".repeat(maskLength) + end;
	    }
	}


	public boolean idmatch(String name, String loginId) {
		User user = authDao.idmatch(name,loginId);
		return user !=null ;
	}
	
	public User findByNameLoginIdEmail(String name, String loginId, String email) {

		return authDao.findByNameLoginIdEmail(name, loginId, email);
	}


	public String updatePw(String name, String loginId, String email, String password) {
		User user = authDao.findProvider(name,email);
		log.info("user={}", user);
		if (user == null) {
			throw new IllegalArgumentException("일치하는 회원 정보가 없습니다.");
		}
		
		String encodedPw = encoder.encode(password);
		authDao.updatePassword(user.getUserId(), encodedPw);
		
		return "비밀번호가 성공적으로 변경되었습니다.";
	}


	public void insertProfile(FileVO file) {
		authDao.insertProfile(file);
		
	}


	


}
	

	
	

