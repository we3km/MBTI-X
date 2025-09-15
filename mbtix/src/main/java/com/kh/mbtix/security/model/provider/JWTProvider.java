package com.kh.mbtix.security.model.provider;

import java.security.Key;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;


@Component
public class JWTProvider {
	
	private final Key key;
	private final Key refreshKey;
	
	public JWTProvider(
			@Value("${jwt.secret}")
			String secretBase64,
			@Value("${jwt.refresh-secret}")
			String refreshSecretBase64
			) {
		byte[] keyBytes = Decoders.BASE64.decode(secretBase64);
		this.key = Keys.hmacShaKeyFor(keyBytes);
		this.refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecretBase64));
	}
	
	// [수정] 메소드에 List<String> roles 파라미터를 추가합니다.
	public String createAccessToken(Long id, List<String> roles, int minutes) {
		Date now = new Date();
		return Jwts.builder()
				.setSubject(String.valueOf(id))
				.claim("roles", roles) // [수정] 토큰에 roles 정보를 추가합니다.
				.setIssuedAt(now)
				.setExpiration(new Date(now.getTime()+ (1000L * 60 * minutes)))
				.signWith(key, SignatureAlgorithm.HS256)
				.compact();
	}
    
	public String createRefreshToken(Long id, int i) {
		Date now = new Date();
		return Jwts.builder()
				.setSubject(String.valueOf(id))
				.setIssuedAt(now)
				.setExpiration(new Date(System.currentTimeMillis() +(1000 * 60 * 60 * 24 * i) ))
				.signWith(refreshKey, SignatureAlgorithm.HS256)
				.compact();
	}
    
	public Long getUserId(String token) {
		return Long.valueOf(
				Jwts.parserBuilder()
					.setSigningKey(key)
					.build()
					.parseClaimsJws(token)
					.getBody()
					.getSubject()
				);
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getRoles(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(token)
				.getBody()
				.get("roles", List.class);
	}
	
	public Long parseRefresh(String token) {
		return Long.valueOf(
				Jwts.parserBuilder()
					.setSigningKey(refreshKey)
					.build()
					.parseClaimsJws(token)
					.getBody()
					.getSubject()
				);
	}
}