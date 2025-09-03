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
		String path = request.getRequestURI();
		// =========================== 테스트용: 요청 인증 생략 ===========================
//		if (request.getRequestURI().startsWith("/speedquiz")) {
//			filterChain.doFilter(request, response);
//			return;
//		}
//		if (request.getRequestURI().startsWith("/point")) {
//			filterChain.doFilter(request, response);
//			return;
//		}
//		if (request.getRequestURI().startsWith("/rank")) {
//			filterChain.doFilter(request, response);
//			return;
//		}
//		if (request.getRequestURI().startsWith("/getUserMBTI")) {
//			filterChain.doFilter(request, response);
//			return;
//		}
//		
		
		// 기존 JWT 인증임
		// 1) 요청 header에서 Authorization 추출
		String header = request.getHeader("Authorization");
		if (header != null && header.startsWith("Bearer ")) {

			try {
				// 2) 토큰에서 userId추출
				String token = header.substring(7).trim();
				Long userId = jwt.getUserId(token);

				log.debug("userId : {}", userId);
				UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userId, null,
						List.of(new SimpleGrantedAuthority("ROLE_USER")));

				// 인증처리 끝
				SecurityContextHolder.getContext().setAuthentication(authToken);
			} catch (ExpiredJwtException e) {
				SecurityContextHolder.clearContext();
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED);// 401상태
				return;
			}
		}

		filterChain.doFilter(request, response);
	}
}
