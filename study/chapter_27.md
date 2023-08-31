# 27강. JWT토큰 서버 구축 완료
- 로그인을 완료한 사용자는 JWT토큰을 발급받는다.
- 이후 사용자는 새로운 요청을 보낼때마다 JWT토큰을 함께 보낸다.
- 서버에선 사용자가 보낸 JWT토큰으로 전자서명이 이뤄지게 되며, 사용자는 추가적인 인증이 필요하지 않다.

## 27-1. BasicAuthenticationFilter
```java
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager) {
		super(authenticationManager);
	}

    @Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
		System.out.println("인증이나 권한이 필요한 주소 요청이 됨");
		
        // 헤더값 확인해보기
		String jwtHeader = request.getHeader("Authorization");
		System.out.println(jwtHeader);
    }
}
```
- 시큐리티가 가진 필터 중 BasicAuthenticationFilter는 인증요청이 있을 때 동작하는 필터가 아니라, 사용자가 권한이나 인증이 필요한 특정 주소를 요청했을 때 해당 필터가 동작하도록 되어있다.
- 만약 권한이나 인증이 필요한 주소가 아니라면 해당 필터는 동작하지 않는다.
- 권한/인증이 필요한 요청을 보내서 해당 필터가 동작하는지 테스트해본다.
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
        - **로그인이 완료된 사용자는 헤더에다 로그인 완료후 응답받은 JWT토큰을 담아올 것이므로,   
        서버에선 요청과 함께 전달받은 JWT 토큰을 검증하여 해당 사용자가 정상적인 사용자인지를 확인하는 과정이 필요하다.**
        - **이제 사용자가 요청헤더에 보낸 JWT토큰을 검증하는 과정을 구현해본다.**
    
## 27-2. BasicAuthenticationFilter 구현하기
### 27-2-1. JWT토큰 전송 테스트
- 앞서 헤더에다 Authorization키 값에 hello를 넣어, 서버에서 헤더값이 잘 출력되는지 확인해보았다.
- 이제는 hello가 아닌 실제 JWT토큰을 넣어 테스트해본다.
    1. JwtAuthorizationFilter 코드
        ```java
        public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

            public JwtAuthorizationFilter(AuthenticationManager authenticationManager) {
                super(authenticationManager);
            }

            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
                System.out.println("인증이나 권한이 필요한 주소 요청이 됨");
                
                // 헤더값 확인해보기
                String jwtHeader = request.getHeader("Authorization");
                System.out.println(jwtHeader);
            }
        }
        ```
    2. 회원가입 되어있으면 생략가능
        
        <img src="./img/chapter27_3.png">

    3. 로그인 요청 및 JWT 토큰 응답확인
        
        <img src="./img/chapter27_4.png">

    4. 권한/인증이 필요한 요청
        
        <img src="./img/chapter27_5.png">

    5. 결과 확인
        > 인증이나 권한이 필요한 주소 요청이 됨   
        Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjb3PthqDtgbAiLCJpZCI6MSwiZXhwIjoxNjkzNDk2NzMyLCJ1c2VybmFtZSI6InNzYXIifQ.Jltl0CxpE-7e68ZNZ7dckhgwCu5zjixVI4rRdWreScRQskrZhLAie8GJN88tl1ESpzH_rVsmelOkFJQOFH_x0A   
        Hibernate: select user0_.id as id1_0_, user0_.password as password2_0_, user0_.roles as roles3_0_, user0_.username as username4_0_ from User user0_ where user0_.username=?
    
    - 권한/인증이 필요한 요청이었으므로, JwtAuthorizationFilter 필터가 동작하여 '인증이나 권한이 필요한 주소 요청이 됨을 출력하였다.
    - 요청시 헤더에 보낸 JWT토큰이 정상적으로 출력되었다.

### 27-2-2. JWT토큰 검증하기
- 로그인 한 사용자는 이후 요청에서 요청헤더에 JWT토큰을 담아 전송하므로, 서버에서는 해당 토큰이 유효한 토큰인지를 검증할 필요가 있다.
- 서버는 해당 JWT토큰을 검증해서 정상적인 사용자인지를 확인한다.


```java
@Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
    
    System.out.println("인증이나 권한이 필요한 주소 요청이 됨");
    
    // 헤더값 확인해보기
    String jwtHeader = request.getHeader("Authorization");
    System.out.println("jwtHeader : " + jwtHeader);
    
    // JWT 토큰 검증
    // 1. header가 있는지 확인
    if(jwtHeader == null || !jwtHeader.startsWith("Bearer")) {
        chain.doFilter(request, response);
        return;
    }
    
    // 2. 사용자 검증 - 정상적인 사용자인지 검증
    String jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
    String username = JWT.require(Algorithm.HMAC512("cos")).build().verify(jwtToken).getClaim("username").asString(); 
}
```
1. 헤더가 있는지, 그리고 Bearer로 시작하는지 확인
    - 헤더가 존재하지 않는다면 계속해서 필터를 타도록 보내주고 return; 
2. 받아온 jwtHeader에서 알고리즘부분인 Bearer를 없애주면 jwtToken부분만 남게됨. 이를 jwtToken변수에 저장 및 이를 이용하여 전자서명을 진행한다.
    ```java
    // HMAC512암호화를 사용하고, secret이 "cos"인 토큰
    JWT.require(Algorithm.HMAC512("cos")) 
        // jwt토큰을 서명
        .build().verify(jwtToken)
        // 서명이 정상적으로 이뤄지면 토큰 내 정보인 'username' (유저아이디)를 반환
        .getClaim("username")
        //반환된 'username'을 String으로 캐스팅
        .asString();
    ```
    <details>
    <summary>
    [참고] 앞서 JWT토큰을 만들때 알고리즘은 HMAC512를 사용했고, SecretKey는 cos를 사용
    </summary>

    <img src="./img/chapter27_6.png">
    </details>
3. 서명이 정상적으로 이루어졌는지 확인
    - SecurityConfig 코드
        ```java
        .addFilter(new JwtAuthorizationFilter(authenticationManager, userRepository));
        ```
        - 필터 호출시, userRepository도 함께 넘기기
    - JwtAuthorizationFilter 코드
        ```java
        private final UserRepository userRepository; // userRepository 선언

        public JwtAuthorizationFilter(AuthenticationManager authenticationManager) { // 생성자 이용 변수 초기화
            super(authenticationManager);
            this.userRepository = userRepository;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
            
            System.out.println("인증이나 권한이 필요한 주소 요청이 됨");
            
            // 헤더값 확인해보기
            String jwtHeader = request.getHeader("Authorization");
            System.out.println("jwtHeader : " + jwtHeader);
            
            // JWT 토큰 검증
            // 1. header가 있는지 확인
            if(jwtHeader == null || !jwtHeader.startsWith("Bearer")) {
                chain.doFilter(request, response);
                return;
            }
            
            // 2. 사용자 검증 - 정상적인 사용자인지 검증
            String jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
            String username = JWT.require(Algorithm.HMAC512("cos")).build().verify(jwtToken).getClaim("username").asString(); 

            // 3. 서명 검증 - null이 아니라면 서명이 완료된 것
            if(username != null) {
                // 3-1. 요청한 사용자 객체
                // 받아온 username으로 DB에서 조회가 가능하다면 서명한 사용자는 정상적인 사용자이다.
                User userEntity = userRepository.findByUsername(username);
                
                // 3-2. 요청한 사용자의 인증정보를 담기 위해 Authentication 객체 만들기
                // 원래 Authentication객체는 로그인이 성공했을때 만들어지는 객체 (JwtAuthenticationFilter참고)
                // 하지만 여기선 JWT토큰 서명을 통해, 서명이 정상일 경우 authentication객체가 생성된다.
                PrincipalDetails principalDetails = new PrincipalDetails(userEntity);
                Authentication authentication = 
                        new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities()); // null = 비밀번호, 필수 파라미터이지만 현재는 알 수 없는 값이기도 하고 요청한 사용자가 정상임이 확실하므로 그냥 null로 보내주기
                
                // 3-3. 시큐리티 세션에 인증정보 넣어주기
                // 강제로 시큐리티 세션에 접근하여 Authentication객체를 저장한다.
                // SecurityContextHolder.getContext() = 시큐리티 세션
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                chain.doFilter(request, response);
            }
        }
        ```
        - 포스트맨 확인
            - 이제 인증이 완료되었으니 403(Forbidden)에러가 아니라 500에러가 발생한 것을 확인할 수 있음
        
        <img src="./img/chapter27_7.png">

## 27-3. 정상작동 테스트
### 27-3-1. RestApiController 추가
```java
// user, manager, admin 권한만 접근 가능
@GetMapping("/api/v1/user")
public String user() {
    
    return "user";
}
// manager, admin 권한만 접근 가능
@GetMapping("/api/v1/manager")
public String manager() {
    
    return "manager";
}
// admin 권한만 접근 가능
@GetMapping("/api/v1/admin")
public String admin() {
    
    return "admin";
}
```

### 27-3-2. 로그인 요청

<img src="./img/chapter27_8.png">

- 응답헤더에 담긴 JWT 토큰을 이용하여 새로운 요청을 해본다.

### 27-3-3. 요청 테스트
- "/user" 요청
    <img src="./img/chapter27_9.png">

- "/manager" 요청
    <img src="./img/chapter27_10.png">

- "/admin" 요청
    <img src="./img/chapter27_11.png">

- 로그인 한 사용자 (ssar/1234) 는 ROLE_USER 권한이므로 "/user"요청만 접근이 가능하고, 그 외 ("/manager", "/admin") 요청은 권한없음에러(403)가 발생하는 것을 확인할 수 있다.


## 27-4. [참고] JwtProperties 사용하기
- 앞서 JWT토큰을 생성할때나, 사용자 요청헤더에 담겨온 JWT토큰을 검증할때나 코드에다 직접 secret키나 prefix정보를 작성해주었다. 
- 하지만 이는 토큰관련 정보를 확인하기에 직관적이지 않을뿐더러, 오탈자등의 실수를 알아채기도 힘드므로 JwtProperties 하나를 생성하여 여기서 토큰정보를 관리하도록 한다.

### 27-4-1. JwtProperties 만들기
```java
package com.cos.jwt.config.jwt;

public interface JwtProperties {
	String SECRET = "cos";
	int EXPIRATION_TIME = 60000*10;
	String TOKEN_PREFIX = "Bearer "; // 한칸 띄워주기
	String HEADER_STRING = "Authorization";
}
```

### 27-4-2. 코드 적용
#### Before
```java
// JWT 토큰 생성
String jwtToken = JWT.create()
				.withSubject("cos토큰") 
				.withExpiresAt(new Date(System.currentTimeMillis()+(60000*10))) 
				.withClaim("id", principalDetails.getUser().getId()) 
				.withClaim("username", principalDetails.getUser().getUsername())
				.sign(Algorithm.HMAC512("cos"));

response.addHeader("Authorization", "Bearer " + jwtToken);
```

#### After
```java
// JWT 토큰 생성
String token = JWT.create()
				.withSubject("cos토큰")
				.withExpiresAt(new Date(System.currentTimeMillis()+JwtProperties.EXPIRATION_TIME))
				.withClaim("id", principalDetails.getUser().getId())
				.withClaim("username", principalDetails.getUser().getUsername())
				.sign(Algorithm.HMAC512(JwtProperties.SECRET));

response.addHeader(JwtProperties.HEADER_STRING, JwtProperties.TOKEN_PREFIX + jwtToken);
```