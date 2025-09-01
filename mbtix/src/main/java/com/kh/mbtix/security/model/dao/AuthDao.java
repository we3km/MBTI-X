package com.kh.mbtix.security.model.dao;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import com.kh.mbtix.security.model.dto.AuthDto.User;
import com.kh.mbtix.security.model.dto.AuthDto.UserAuthority;
import com.kh.mbtix.security.model.dto.AuthDto.UserCredential;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class AuthDao {
	
	private final SqlSessionTemplate session;
	
	public Object findByLoginId(String loginId) {
		return session.selectOne("auth.findByLoginId",loginId);
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

}
