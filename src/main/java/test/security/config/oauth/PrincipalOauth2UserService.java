package test.security.config.oauth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import test.security.config.auth.PrincipalDetails;
import test.security.model.User;
import test.security.repository.UserRepository;

@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService{
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepsoitory;
	
	// 구글로부터 받은 userRequest 데이터에 대한 후처리가 되는 함수
	// 함수 종료시 @AuthenticationPrincipal 어노테이션이 만들어진다.
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		System.out.println("getClientRegistration : " + userRequest.getClientRegistration()); //registrationId로 어떤 OAuth로 로그인 했는지 확인가능
		System.out.println("getAccessTokenValue : " + userRequest.getAccessToken().getTokenValue());
		
		OAuth2User oauth2User = super.loadUser(userRequest);
		// 구글로그인 버튼 클릭 -> 구글로그인창 -> 여기서 로그인을 완료 -> code를 리턴(OAuth-client라이브러리) -> AccessToken요청 -> AccessToken받음 : 여기까지가 userRequest정보에 해당
		// userRequest 정보 -> loadUser()함수를 호출 : loadUser(userRequest) -> 구글로부터 회원프로필을 받아옴(loadUser함수의 역할)
		System.out.println("getAttributes : " + oauth2User.getAttributes());
		
		// 회원가입을 강제로 진행해볼 예정
		String provider = userRequest.getClientRegistration().getRegistrationId(); // google
		String providerId = oauth2User.getAttribute("sub"); // 109696850338476008763
		// oauth로그인시 username과 password 모두 사용자가 넣어주지 않았기때문에 우리가 임의로 값을 생성, 조합하여 DB에 저장
		String username = provider+"_"+providerId; // google_109696850338476008763
		String password = bCryptPasswordEncoder.encode("겟인데어");
		String email = oauth2User.getAttribute("email");
		String role = "ROLE_USER";
		
		User userEntity = userRepsoitory.findByUsername(username);
		if(userEntity == null) {
			System.out.println("구글 로그인이 최초입니다.");
			userEntity = User.builder()
								.username(username)
								.password(password)
								.email(email)
								.role(role)
								.provider(provider)
								.providerId(providerId)
								.build();
			userRepsoitory.save(userEntity);
		}else {
			System.out.println("구글 로그인을 이미 한 적이 있습니다. 당신은 자동회원가입이 되어 있습니다.");
		}
		
		return new PrincipalDetails(userEntity, oauth2User.getAttributes());
	}
}
