// security/filter/JWTAutenticationFilter.java
package com.kh.mbtix.security.filter;

import java.io.IOException;
import java.util.Collections; // [추가]
import java.util.List;
import java.util.stream.Collectors;

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
		String header  = request.getHeader("Authorization");
		if(header != null && header.startsWith("Bearer ")) {
			
			try {
				String token = header.substring(7).trim();
				Long userId = jwt.getUserId(token);
				List<String> roles = jwt.getRoles(token);
				
				// [수정] roles가 null일 경우를 대비한 방어 코드
				List<SimpleGrantedAuthority> authorities;
				if (roles == null) {
					authorities = Collections.emptyList();
				} else {
					authorities = roles.stream()
							.map(SimpleGrantedAuthority::new)
							.collect(Collectors.toList());
				}
									
				UsernamePasswordAuthenticationToken authToken
				= new UsernamePasswordAuthenticationToken(userId, null , authorities);
				
				SecurityContextHolder.getContext().setAuthentication(authToken);
				log.debug("userId: {} -> roles: {} 권한 처리 완료", userId, roles);
				
			} catch(ExpiredJwtException e) {
				SecurityContextHolder.clearContext();
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
				return;
			}
		}
		
		filterChain.doFilter(request, response);
	}
}