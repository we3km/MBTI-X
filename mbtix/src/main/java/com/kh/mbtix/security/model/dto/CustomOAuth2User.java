package com.kh.mbtix.security.model.dto;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import lombok.Getter;

@Getter
public class CustomOAuth2User extends DefaultOAuth2User {

    private final Long userId;        // 우리 서비스 Users 테이블 PK
    private final boolean newUser;    // 신규 회원 여부

    public CustomOAuth2User(
            Collection<? extends GrantedAuthority> authorities,
            Map<String, Object> attributes,
            String nameAttributeKey,
            Long userId,
            boolean newUser
    ) {
        super(authorities, attributes, nameAttributeKey);
        this.userId = userId;
        this.newUser = newUser;
    }
}
