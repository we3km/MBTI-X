package com.kh.mbtix.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
    public SecurityFilterChain filterChain(HttpSecurity http,
            JWTAutenticationFilter jwtFilter,
            OAuth2SuccessHandler oauth2SuccessHandler
            ) throws Exception {
        http
                // CORS 관련 빈객체 등록
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // CSRF는 SPA어플리케이션에서 사용하지 않음
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> {
                            // 인증 실패시 401처리
                            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED");
                        }).accessDeniedHandler((req, res, ex) -> {
                            // 권한 없음시 403처리
                            res.sendError(HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN");
                        }))
                
                // 서버에서 인증상태를 관리하지 않게 하는 설정 (JWT 사용)
                .sessionManagement(
                        management -> 
                        management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
//                .oauth2Login( oauth -> oauth
//                        // 인증정보를 바탕으로 자동 회원가입
//                        // 요청처리 완료후, accessToken과 refreshToken을 사용자에게 전달
//                        .userInfoEndpoint(u -> u.userService(oauth2Service))
//                        .successHandler(oauth2SuccessHandler)
//                )
                .authorizeHttpRequests(auth -> 
                    auth
                    // 인증 관련 경로 허용
                    .requestMatchers("/auth/login", "/auth/signup", "/auth/logout", "/auth/refresh",
                             "/auth/checkId", "/auth/checkNickname", "/auth/send-code", "/auth/verify-code"
                            ).permitAll()
                    // OAuth2 관련 경로 허용
                    .requestMatchers("/oauth2/**", "/login**", "/error").permitAll()
                    // 관리자 페이지 허용
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    // FAQ는 GET 요청만 허용
                    .requestMatchers(HttpMethod.GET, "/faqs/**").permitAll()
                    // 알림 허용
                    .requestMatchers("/alarms/**").authenticated()
                    // 그 외 모든 경로는 인증 필요
                    .anyRequest().authenticated()
                );
        
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    // CORS 설정정보를 가진 빈객체
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 허용 Origin설정 - React 개발 서버
        config.setAllowedOrigins(List.of("http://localhost:5173"));

        // 허용 메서드
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Location", "Authorization"));
        config.setAllowCredentials(true); // 세션, 쿠키 허용
        config.setMaxAge(3600L); // 요청정보 캐싱시간

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}