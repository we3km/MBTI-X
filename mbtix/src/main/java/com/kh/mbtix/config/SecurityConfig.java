package com.kh.mbtix.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.kh.mbtix.security.filter.JWTAutenticationFilter;
import com.kh.mbtix.security.model.handler.OAuth2SuccessHandler;
//import com.kh.mbtix.security.model.service.OAuth2Service;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, JWTAutenticationFilter jwtFilter,
//			OAuth2Service oauth2Service,
			OAuth2SuccessHandler oauth2SuccessHandler) throws Exception {
		http
				// Cors관련 빈객체 등록
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				// CSRF는 SPA어플리케이션에서 사용하지 않음.
				.csrf(csrf -> csrf.disable()).exceptionHandling(e -> e.authenticationEntryPoint((req, res, ex) -> {
					// 인증 실패시 401처리
					res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED");
				}).accessDeniedHandler((req, res, ex) -> {
					// 인증 실패시 403처리
					res.sendError(HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN");
				}))

				// 서버에서 인증상태를 관리하지 않게 하는 설정.
				.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth.requestMatchers("/auth/login", "/auth/signup", "/auth/logout",
						"/auth/refresh", "/auth/checkId", "/auth/checkNickname", "/auth/send-code", "/auth/verify-code",
						"/auth/checkemail","/speedquiz", "/point" , "/rank", "/getUserMBTI"						
				).permitAll()
						.requestMatchers("/oauth2/**", "/login**", "/error").permitAll()
						.requestMatchers("/ws/**", "/api/ws/**", "/topic/**", "/app/**").permitAll()
						.requestMatchers("/**").authenticated());
		http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
		
		return http.build();
	}

	// CORS 설정정보를 가진 빈객체
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();

		// 허용 Origin설정
		config.setAllowedOrigins(List.of(
			    "http://localhost:5173",
			    "http://192.168.10.230:5173" // LAN IP 허용
			));

		// 허용 메서드
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
		config.setAllowedHeaders(List.of("*"));

		config.setExposedHeaders(List.of("Location", "Authorization", "Set-Cookie"));

		config.setAllowCredentials(true); // 세션,쿠키 허용
		config.setMaxAge(3600L); // 요청정보 캐싱시간

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);

		return source;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		PasswordEncoder encoder = new BCryptPasswordEncoder();
		return encoder;
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();

	}
}
