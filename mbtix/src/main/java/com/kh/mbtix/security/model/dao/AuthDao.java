package com.kh.mbtix.security.model.dao;

import org.apache.ibatis.annotations.Param;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.mbtix.security.model.dto.AuthDto;
import com.kh.mbtix.security.model.dto.AuthDto.User;
import com.kh.mbtix.security.model.dto.AuthDto.UserAuthority;
import com.kh.mbtix.security.model.dto.AuthDto.UserCredential;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AuthDao {
	
	private final SqlSessionTemplate session;
	
	public AuthDto.User findByLoginId(String loginId) {
		log.debug(">>> DAO 호출 loginId={}", loginId);
	    User u = session.selectOne("auth.findByLoginId", loginId);
	    log.debug(">>> 조회 결과 = {}", u);
	    return u;
	}

	public Object findByEmail(String email) {
		return session.selectOne("auth.findByEmail",email);
	}

	public Object findByNickname(String nickname) {
		return session.selectOne("auth.findByNickname",nickname);
	}

	public void insertUser(User user) {
		session.insert("auth.insertUser",user);
	}

	public void insertCred(UserCredential cred) {
		session.insert("auth.insertCred",cred);
	}

	public void insertUserRole(UserAuthority auth) {
		session.insert("auth.insertUserRole",auth);
	}

	public User findUserByUserId(Long userId) {
		return session.selectOne("auth.findUserByUserId",userId);
	}

	public User findByLoginpassword(String loginId) {
		return session.selectOne("auth.findByLoginpassword",loginId);
	}

}
