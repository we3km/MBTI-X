package com.kh.mbtix.security.model.dao;

import java.util.HashMap;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;
import com.kh.mbtix.security.model.dto.AuthDto;
import com.kh.mbtix.security.model.dto.AuthDto.FileVO;
import com.kh.mbtix.security.model.dto.AuthDto.User;
import com.kh.mbtix.security.model.dto.AuthDto.UserAuthority;
import com.kh.mbtix.security.model.dto.AuthDto.UserCredential;
import com.kh.mbtix.security.model.dto.AuthDto.UserIdentities;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Repository
@RequiredArgsConstructor
public class AuthDao {
	
	private final SqlSessionTemplate session;
	
	public AuthDto.User findByLoginId(String loginId) {
		log.debug(">>> findByLoginId DAO 호출 loginId={}", loginId);
	    User u = session.selectOne("auth.findByLoginId", loginId);
	    log.debug(">>> 조회 결과 = {}", u);
	    return u;
	}
	public Object findByEmail(String email) {
		log.debug(">>> findByEmail DAO 호출 email={}", email);
	    User u = session.selectOne("auth.findByEmail",email);
	    log.debug(">>> 조회 결과 = {}", u);
	    return u;
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
	public User findUserByEmail(String email) {
		return session.selectOne("auth.findUserByEmail",email);
	}

	public void insertUserIdentities(UserIdentities userIdentities) {
		session.insert("auth.insertUserIdentities",userIdentities);
	}

	public void updateUserIdentities(UserIdentities userIdentities) {
		session.update("auth.updateUserIdentities",userIdentities);
	}

	public UserIdentities findUserIdentities(String provider, String providerUserId) {
	        Map<String, Object> params = new HashMap<>();
	        params.put("provider", provider);
	        params.put("providerUserId", providerUserId);

	       return session.selectOne("auth.findUserIdentities", params);
	}

	public void insertIdentities(UserIdentities identities) {
		session.insert("auth.insertIdentities",identities);
	}

	public void insertRole(UserAuthority auth) {
		session.insert("auth.insertRole",auth);
	}

	public String getKakaoAccessToken(Long userId) {
		return session.selectOne("auth.getKakaoAccessToken",userId);
	}

	public User matchName(String name) {
		return session.selectOne("auth.matchName",name);
	}

	public User existsByNameAndEmail(String name, String email) {
		Map<String,Object> params = new HashMap<>();
		params.put("name", name);
		params.put("email", email);
		return session.selectOne("auth.existsByNameAndEmail",params);
	}

	public User findByNameAndEmail(String name, String email) {
		Map<String,Object> params = new HashMap<>();
		params.put("name", name);
		params.put("email", email);
		return session.selectOne("auth.findByNameAndEmail",params);
	}

	public User idmatch(String name, String loginId) {
		Map<String,Object> params = new HashMap<>();
		params.put("name", name);
		params.put("loginId", loginId);
		return session.selectOne("auth.idmatch",params);
	}

	public User existsByNameAndloginIdAnmdEmail(String name, String loginId, String email) {
		Map<String,Object> params = new HashMap<>();
		params.put("name", name);
		params.put("loginId", loginId);
		params.put("email", email);		
		return session.selectOne("auth.existsByNameAndloginIdAnmdEmail",params);
	}

	public User findProvider(String name, String email) {
		Map<String,Object> params = new HashMap<>();
		params.put("name", name);
		params.put("email", email);		
		return session.selectOne("auth.findProvider",params);
	}

	public void updatePassword(Long userId, String encodedPw) {
		Map<String,Object> params = new HashMap<>();
		params.put("userId", userId);
		params.put("encodedPw", encodedPw);
		session.update("auth.updatePassword",params);
	}

	public User findByNameLoginIdEmail(String name, String loginId, String email) {
		Map<String,Object> params = new HashMap<>();
		params.put("name", name);
		params.put("loginId", loginId);
		params.put("email", email);		
		return session.selectOne("auth.findByNameLoginIdEmail",params);
	}

	public void insertProfile(FileVO file) {
		log.debug(">>> insertProfile DAO 호출 file={}", file);
		session.insert("auth.insertProfile",file);
	}

	public void updateProfile(FileVO file) {
		session.update("auth.updateProfile",file);
	}


}
