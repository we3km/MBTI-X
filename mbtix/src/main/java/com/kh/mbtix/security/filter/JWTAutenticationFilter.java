package com.kh.mbtix.security.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.kh.mbtix.security.model.provider.JWTProvider;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JWTAutenticationFilter extends OncePerRequestFilter {
	private final JWTProvider jwt;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		//1) 요청 header에서 Authorization 추출
				String header  = request.getHeader("Authorization");
				if(header != null && header.startsWith("Bearer ")) {
					
					try {
					//2) 토큰에서 userId추출
					String token = header.substring(7).trim();
					Long userId = jwt.getUserId(token);
					
					List<SimpleGrantedAuthority> authorities;
	                if (userId == 44L) {
	                    authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
	                    log.debug("userId: {} -> ROLE_ADMIN 권한 부여", userId);
	                } else {
	                    authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
	                    log.debug("userId: {} -> ROLE_USER 권한 부여", userId);
	                }

	                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
	                    userId, 
	                    null, 
	                    authorities // 수정된 권한 리스트 적용
	                );
					
					//인증처리 끝
					SecurityContextHolder.getContext().setAuthentication(authToken);
					}catch(ExpiredJwtException e) {
						SecurityContextHolder.clearContext();
						response.sendError(HttpServletResponse.SC_UNAUTHORIZED);//401상태
						return;
					}
				}
				
				filterChain.doFilter(request, response);
	}
}
