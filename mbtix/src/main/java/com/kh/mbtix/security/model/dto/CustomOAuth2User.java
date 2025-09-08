package com.kh.mbtix.security.model.dto;

import java.util.Collection;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import lombok.Getter;

@Getter
public class CustomOAuth2User extends DefaultOAuth2User {

    private final Long userId;
    private final boolean newUser;
    private final String email;
    private final String name;
    private final String nickname;
    private final String profileImageUrl;
    private final String provider;
    private final String providerUserId;

    public CustomOAuth2User(
            Collection<? extends GrantedAuthority> authorities,
            Map<String, Object> attributes,
            String nameAttributeKey,
            Long userId,
            boolean newUser,
            String email,
            String name,
            String nickname,
            String profileImageUrl,
            String provider,
            String providerUserId
    ) {
        super(authorities, attributes, nameAttributeKey);
        this.userId = userId;
        this.newUser = newUser;
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.provider = provider;
        this.providerUserId = providerUserId;
    }
}
