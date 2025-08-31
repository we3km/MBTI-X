package com.kh.mbtix.security.model.service;

public class OAuth2Service {
	
	@Override	
	@Transactional
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);
		Map<String, Object> attributes = oAuth2User.getAttributes();
		
		String provider = userRequest.getClientRegistration().getRegistrationId();
		String providerUserId = String.valueOf(attributes.get("id"));
		String accessToken = userRequest.getAccessToken().getTokenValue();
		
		if(provider.equals("kakao")) {
			Map<String,Object> kakaoAccount = (Map<String,Object>) attributes.get("kakao_account");
			String email = (String) kakaoAccount.get("email");
			Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
			
			// 데이터베이스에서 회원정보 조회. 
			User user = authDao.findUserByEmail(email);
			if(user == null) {
				// 새로운 사용자인 경우 자동회원가입. 
				user = User.builder()
						.email(email)
						.name((String)profile.get("nickname"))
						.profile((String) profile.get("profile_image_url"))
						.build();
				authDao.insertUser(user);
				
				// 유저 소셜정보
				UserIdentities userIdentities = UserIdentities.builder()
						.provider(provider)
						.providerUserId(providerUserId)
						.accessToken(accessToken)
						.userId(user.getId())
						.build();
				authDao.insertUserIdentities(userIdentities);
				
				UserAuthority auth = UserAuthority.builder()
						.userId(user.getId())
						.roles(List.of("ROLE_USER"))
						.build();
				authDao.insertUserRole(auth);
				// 자동회원가입 끝.
			}
			
			// 이미 회원가입은 했찌만 다시 로그인한 경우.
			// accessToken 업데이트
			UserIdentities userIdentities = UserIdentities.builder()
					.provider(provider)
					.providerUserId(providerUserId)
					.accessToken(accessToken)
					.build();
			authDao.updateUserIdentities(userIdentities);
			
			
			return new CustomOAuth2User(
					oAuth2User.getAuthorities(), 
					attributes, 
					"id", 
					user.getId());
		}
	
	
		
		return 
		new DefaultOAuth2User(oAuth2User.getAuthorities(), attributes, "id");
	
	}


}
