package com.kh.mbtix.security.model.handler;

import java.io.IOException;
import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.kh.mbtix.security.controller.AuthController;
import com.kh.mbtix.security.model.dto.CustomOAuth2User;
import com.kh.mbtix.security.model.provider.JWTProvider;
import com.nimbusds.jose.Payload;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JWTProvider jwt;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        CustomOAuth2User oauthUser = (CustomOAuth2User) authentication.getPrincipal();

        // 신규 회원 여부 확인
        if (oauthUser.isNewUser()) {
            log.info("신규 소셜 로그인 사용자 → 추가정보 입력 필요");
            // 신규 가입자는 access/refresh 토큰 발급하지 않고 추가정보 입력 페이지로 리다이렉트
            String redirect = UriComponentsBuilder
                    .fromUriString("http://localhost:5173/social-signup")
                    .queryParam("provider", oauthUser.getAttributes().get("provider"))
                    .queryParam("providerUserId", oauthUser.getAttributes().get("id"))
                    .queryParam("email", oauthUser.getAttributes().get("email"))
                    .queryParam("nickname", oauthUser.getAttributes().get("nickname"))
                    .queryParam("profileImageUrl", oauthUser.getAttributes().get("profile_image_url"))
                    .build()
                    .toUriString();

            response.sendRedirect(redirect);
            return;
        }

        // 기존 회원 → JWT 발급 + RefreshToken 쿠키 설정
        Long id = oauthUser.getUserId();

        String accessToken = jwt.createAccessToken(id, 30);   // 30분
        String refreshToken = jwt.createRefreshToken(id, 7);  // 7일

        ResponseCookie cookie =
                ResponseCookie.from(AuthController.REFRESH_COOKIE, refreshToken)
                        .httpOnly(true)
                        .secure(false) // HTTPS 환경에서 true 권장
                        .sameSite("Lax")
                        .path("/")
                        .maxAge(Duration.ofDays(7))
                        .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        String redirect = UriComponentsBuilder
                .fromUriString("http://localhost:5173/oauth2/success")
                .queryParam("accessToken", accessToken)
                .queryParam("provider", oauthUser.getAttributes().get("provider"))
                .queryParam("providerUserId", oauthUser.getAttributes().get("id"))
                .queryParam("email", oauthUser.getAttributes().get("email"))
                .queryParam("name", oauthUser.getAttributes().get("nickname"))
                .queryParam("profileImageUrl", oauthUser.getAttributes().get("profile_image_url"))
                .build()
                .toUriString();

        response.sendRedirect(redirect);
    }
}
