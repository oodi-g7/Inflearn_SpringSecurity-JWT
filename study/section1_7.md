# 7강. 구글 회원 프로필 정보 받아보기
## 7-1. 소셜로그인 작동순서
1. 코드받기(인증) : Redirect_URI
2. 엑세스토큰받기(접근권한)
3. 사용자프로필 정보 갖고오기
4. (4-1) 그 정보를 토대로 회원가입을 자동으로 진행
5. (4-2) 또는 갖고 온 사용자프로필 정보 외에 추가적인 정보가 필요할 경우에는 자동 회원가입이 아니라 추가적인 회원가입 창을 만들어 필요한 정보를 수집해야 함

## 7-2. 구글 로그인 후처리
1. **SecurityConfig 설정 추가**
    ```java
    .userInfoEndpoint()
    .userService(null);
    ```
- userService() 내에 들어갈 수 있는 타입은 OAuth2UserServic\<OAuth2UserRequest, OAuth2User\> 타입이다. 아직 만들어둔게 없으니 우선 null로 채워두자.

2. **메인패키지-config 밑에 oauth패키지 생성**
3. **PrincipalOauth2UserService 클래스 생성**
    ```java
    import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
    import org.springframework.stereotype.Service;

    @Service
    public class PrincipalOauth2UserService extends DefaultOAuth2UserService{
        // 구글로그인이 완료된 후, 구글로부터 받은 userRequest 데이터에 대한 후처리가 되는 함수
        // userRequest 데이터에는 로그인한 사용자에 대한 AccessToken과 사용자프로필정보가 함께 들어있음.
        @Override
        public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
            // 일단 받아오는 userRequest가 무엇인지 다 찍어보자
            System.out.println("getClientRegistration : " + userRequest.getClientRegistration());
            System.out.println("getAccessTokenValue : " + userRequest.getAccessToken().getTokenValue());
            System.out.println("getAttributes : " + super.loadUser(userRequest).getAttributes());
            
            return super.loadUser(userRequest);
        }
    }
    ```

4. **SecurityConfig 설정 수정**
    ```java
    @Authwired
    private PrincipalOauth2UserService principalOauth2UserService;

    @Bean
    public SecurityFilterChain filterChain(HttpService http) throws Exception{
        http.authorizeRequest()
            ···
            .userInfoEndPoint()
            .userService(principalOauth2UserService);
    }
    ```

5. **localhost:8080/loginForm 접속 후 구글로그인 진행**
    ```bash
    getClientRegistration : ClientRegistration{registrationId='google', clientId='330067917305-rmbfjubajdiqelvoehsp1ripqoa8k1fn.apps.googleusercontent.com', clientSecret='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', clientAuthenticationMethod=org.springframework.security.oauth2.core.ClientAuthenticationMethod@4fcef9d3, authorizationGrantType=org.springframework.security.oauth2.core.AuthorizationGrantType@5da5e9f3, redirectUri='{baseUrl}/{action}/oauth2/code/{registrationId}', scopes=[email, profile], providerDetails=org.springframework.security.oauth2.client.registration.ClientRegistration$ProviderDetails@21acbace, clientName='Google'}
    getAccessTokenValue : ya29.a0AbVbY6NNwkW8ANtgT33Ieu6aOv3b4sXh5PSziOuRoCbs8Vqz8ewMI78wpK7-Mg4fgDhBpAnMkg_CSdPOtgbNGAcIBCpZTLNpMYjLLNrUCLlsshEthCSWl7SBIXreK5dKlwT9jmU1SDjsIBmuGKlOle5qxTecaCgYKAa0SARESFQFWKvPlgSOTrjBE01eyJg4RNiL7RA0163
    getAttributes : {sub=109696850338476008763, name=Eun Ji Kim, given_name=Eun Ji, family_name=Kim, picture=https://lh3.googleusercontent.com/a/AAcHTtfbjzXXvWBeUAKeNktxrPmH4OO1ySfz4obudeY3Y2YI=s96-c, email=rladmswl1707@gmail.com, email_verified=true, locale=ko}
    ```
- getClientRegistration : 우리 서버의 기본적인 정보(regustrationId, clientId, clientSecret, ···)
    - registrationId로 어떤 OAuth로 로그인 했는지 확인가능
- getAccessTokenValue : AccessToken 값. 이미 사용자 프로필 정보를 받아왔으므로 사실 더 이상 필요한 정보는 아님
- getAttributes : 사용자프로필정보
    - sub : 구글회원가입시 내 id (primary key와 비슷한 개념)
    - name / given_name / family_name : 이름
    - picture : 사용자 프로필 사진
    - email : 사용자 이메일
    - email_verified : 이메일 만료여부
    - locale : 언어

**6. 회원가입 구상**
- 받아온 사용자 프로필정보를 이용하여 회원가입을 어떻게 시킬지 구상하기
    ```bash
    // 구글 서버로부터 받아온 사용자프로필정보
    getAttributes : {
        sub=109696850338476008763, 
        name=Eun Ji Kim, 
        given_name=Eun Ji, 
        family_name=Kim, 
        picture=https://lh3.googleusercontent.com/a/AAcHTtfbjzXXvWBeUAKeNktxrPmH4OO1ySfz4obudeY3Y2YI=s96-c, 
        email=rladmswl1707@gmail.com, 
        email_verified=true, 
        locale=ko}
    ```
    ```java
    // User Entity
    public class User {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int id;
        private String username;
        private String password;
        private String email;
        private String role;
        @CreationTimestamp
        private Timestamp createDate;
    }
    ```

- 회원가입정보는 아래와 같이 구상한다.
    - username = "google_109696850338476008763"
    - password = "암호화(겟인데어)"
    - email = "rladmswl1707@gmail.com"
    - role = "ROLE_USER"
    - <U>**그런데, 이렇게 저장했을경우 해당 회원이 OAuth로그인을 통해 로그인한 것인지 알아보기 힘드므로 User엔티티에 필드를 추가해준다.**</U>

    ```java
    public class User {
        ···
        private String provider;
        private String providerId;
        ···
    }
    ```

- 그리고 회원가입정보 내용을 추가해준다.
    - username = "google_109696850338476008763" ("google_" + sub)
    - password = "암호화(겟인데어)"
    - email = "rladmswl1707@gmail.com" (email)
    - role = "ROLE_USER"
    - <U>**provider = "google"**</U>
    - <U>**providerId = "109696850338476008763"**</U> (sub)

- 이제 사용자가 구글 로그인을 완료하면 사용자의 프로필정보인 getAttributes를 이용하여 강제 회원가입을 진행시킬 것이다.
    ```java
    import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
    import org.springframework.stereotype.Service;

    @Service
    public class PrincipalOauth2UserService extends DefaultOAuth2UserService{
        @Override
        public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
            ···
            // 로그인 후처리 진행
            // 받아온 getAttributes정보를 이용하여 회원가입을 강제로 진행해볼 예정
            
            return super.loadUser(userRequest);
        }
    }
    ```