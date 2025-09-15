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
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        CustomOAuth2User oauthUser = (CustomOAuth2User) authentication.getPrincipal();

        // ✅ 신규 회원 → 소셜 회원가입 페이지로
        if (oauthUser.isNewUser()) {
            String redirect = UriComponentsBuilder
                    .fromUriString("http://localhost:5173/social-signup")
                    .queryParam("provider", oauthUser.getProvider())
                    .queryParam("providerUserId", oauthUser.getProviderUserId())
                    .queryParam("email", oauthUser.getEmail())
                    .queryParam("name", oauthUser.getName())
                    .queryParam("nickname", oauthUser.getNickname())
                    .queryParam("profileImageUrl", oauthUser.getProfileImageUrl())
                    .encode()
                    .build()
                    .toUriString();

            response.sendRedirect(redirect);
            return;
        }

        // ✅ 기존 회원 → JWT 발급 + RefreshToken 쿠키 저장 후 메인 페이지로
        Long id = oauthUser.getUserId();
        String accessToken = jwt.createAccessToken(id, 30);   // 30분
        String refreshToken = jwt.createRefreshToken(id, 7);  // 7일

        ResponseCookie cookie = ResponseCookie.from(AuthController.REFRESH_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(false) // 배포 시 true 권장
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        String redirect = "http://localhost:5173/";
        response.sendRedirect(redirect);
        return;
    }
}



