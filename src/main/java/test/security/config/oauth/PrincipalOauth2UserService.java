package test.security.config.oauth;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService{
	
	// 구글로부터 받은 userRequest 데이터에 대한 후처리가 되는 함수
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		System.out.println("getClientRegistration : " + userRequest.getClientRegistration()); //registrationId로 어떤 OAuth로 로그인 했는지 확인가능
		System.out.println("getAccessTokenValue : " + userRequest.getAccessToken().getTokenValue());
		// 구글로그인 버튼 클릭 -> 구글로그인창 -> 여기서 로그인을 완료 -> code를 리턴(OAuth-client라이브러리) -> AccessToken요청 -> AccessToken받음 : 여기까지가 userRequest정보에 해당
		// userRequest 정보 -> loadUser()함수를 호출 : loadUser(userRequest) -> 구글로부터 회원프로필을 받아옴(loadUser함수의 역할)
		System.out.println("getAttributes : " + super.loadUser(userRequest).getAttributes());
		
		OAuth2User oauth2User = super.loadUser(userRequest);
		
		// 회원가입을 강제로 진행해볼 예정
		
		return super.loadUser(userRequest);
	}
}
