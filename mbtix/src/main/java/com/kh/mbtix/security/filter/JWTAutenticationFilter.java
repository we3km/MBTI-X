package com.kh.mbtix.security.filter;

import java.io.IOException;
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
		String path = request.getServletPath();

		if (path.startsWith("/api/ws/")) {
			filterChain.doFilter(request, response);
			return;
		}

		String header = request.getHeader("Authorization");

		if (header != null && header.startsWith("Bearer ")) {

			try {
				String token = header.substring(7).trim();
				
				Long userId = jwt.getUserId(token);
				
				List<String> roles = jwt.getRoles(token);
				
				List<SimpleGrantedAuthority> authorities = roles.stream()
																.map(SimpleGrantedAuthority::new)
																.collect(Collectors.toList());
				
				UsernamePasswordAuthenticationToken authToken = 
						new UsernamePasswordAuthenticationToken(userId, null, authorities);
				
				// 인증처리 끝
				SecurityContextHolder.getContext().setAuthentication(authToken);

			} catch (ExpiredJwtException e) {
				SecurityContextHolder.clearContext();
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED); // 401상태
				return;
			}
		}

		filterChain.doFilter(request, response);
	}
}