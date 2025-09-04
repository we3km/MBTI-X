package com.kh.mbtix.security.model.service;

import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.mbtix.security.model.dao.AuthDao;
import com.kh.mbtix.security.model.dto.AuthDto.User;
import com.kh.mbtix.security.model.dto.CustomOAuth2User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2Service implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final AuthDao authDao;

    @Override
    @Transactional(readOnly = true)
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 어떤 Provider(kakao/naver)인지 구분
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String accessToken = userRequest.getAccessToken().getTokenValue();

        String providerUserId = null;
        String email = null;
        String nickname = null;
        String profileImageUrl = null;

        // ===== Kakao 응답 파싱 =====
        if ("kakao".equals(provider)) {
            providerUserId = String.valueOf(attributes.get("id"));
            Map<String, Object> kakaoAccount = (Map<String,Object>) attributes.get("kakao_account");
            if (kakaoAccount != null) {
                email = (String) kakaoAccount.get("email");
                Map<String, Object> profile = (Map<String,Object>) kakaoAccount.get("profile");
                if (profile != null) {
                    nickname = (String) profile.get("nickname");
                    profileImageUrl = (String) profile.get("profile_image_url");
                }
            }
        }

        // ===== Naver 응답 파싱 =====
        if ("naver".equals(provider)) {
            Map<String, Object> response = (Map<String,Object>) attributes.get("response");
            if (response != null) {
                providerUserId = (String) response.get("id");
                email = (String) response.get("email");
                nickname = (String) response.get("nickname");
                profileImageUrl = (String) response.get("profile_image");
            }
        }

        // DB에서 기존 유저 조회 (이메일 기준 → provider 기준 순으로)
        User user = null;
        if (email != null) {
            user = authDao.findUserByEmail(email);
        }
        if (user == null) {
            // 신규 유저 → DB에 아직 User 없음
            return new CustomOAuth2User(
                    oAuth2User.getAuthorities(),
                    attributes,
                    "id",
                    null,      // userId 없음
                    true       // 신규 유저
            );
        } else {
            // 기존 회원
            return new CustomOAuth2User(
                    oAuth2User.getAuthorities(),
                    attributes,
                    "id",
                    user.getUserId(),
                    false
            );
        }
    }
}
