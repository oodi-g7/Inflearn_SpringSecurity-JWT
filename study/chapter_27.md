# 27강. JWT토큰 서버 구축 완료
- 로그인을 완료한 사용자는 JWT토큰을 발급받는다.
- 이후 사용자는 새로운 요청을 보낼때마다 JWT토큰을 함께 보낸다.
- 서버에선 사용자가 보낸 JWT토큰으로 전자서명이 이뤄지게 되며, 사용자는 추가적인 인증이 필요하지 않다.

## 27-1. BasicAuthenticationFilter 구현하기
```java
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {
    public JwtAuthorizationFilter(AuthenticationManager authenticationManager) {
		super(authenticationManager);
	}

    @Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		System.out.println("인증이나 권한이 필요한 주소 요청이 됨");
		
		String jwtHeader = request.getHeader("Authorization");
		System.out.println(jwtHeader);
    }
}
```
- 시큐리티가 가진 필터 중 BasicAuthenticationFilter는 인증요청이 있을 때 동작하는 필터가 아니라, 사용자가 권한이나 인증이 필요한 특정 주소를 요청했을 때 해당 필터가 동작하도록 되어있다.
- 만약 권한이나 인증이 필요한 주소가 아니라면 해당 필터는 동작하지 않는다.
- 권한/인증이 필요한 요청을 보내어 해당 필터가 동작하는지 테스트해본다.
    1. SecurityConfig 확인
        ```java
        .antMatchers("/api/v1/user/**")
		.access("hasRole('ROLE_USER') or hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
        ```
        - "/api/v1/user/**" 요청은 ROLE_USER와 ROLE_MANAGER, ROLE_ADMIN 권한만 접근가능한 페이지이다. 즉, 권한/인증이 필요한 페이지이다.
    2. Postman 요청

        <img src="./img/chapter27_1.png">

        - JwtAuthorizationFilter에 doFilterInternal()함수에서 Authorization 키값을 가진 헤더를 가져와 출력하고자 하므로, 요청을 보낼때 Headers에다 Authorization 키값을 함께 넣어 전송한다.
    3. 결과확인

        <img src="./img/chapter27_2.png">

        - "인증이나 권한이 필요한 주소 요청이 됨" 문구를 통해 인증/권한이 필요한 요청시, 해당 필터가 동작하는 것을 알 수 있다.
        - 앞서 포스트맨에서 Authorization 키 값에 담아준 hello라는 값이 정상적으로 출력된 것을 확인할 수 있다.
        - 로그인이 완료된 사용자는 헤더에 JWT토큰을 담아올 것이므로, 서버에선 JWT 토큰을 검증하여 해당 사용자가 정상적인 사용자인지를 확인하는 과정이 필요하다.
        - 이제 사용자가 요청헤더에 보낸 JWT토큰을 검증하는 과정을 구현해본다.
    
    ## 27-2. BasicAuthenticationFilter : JWT토큰 검증하기