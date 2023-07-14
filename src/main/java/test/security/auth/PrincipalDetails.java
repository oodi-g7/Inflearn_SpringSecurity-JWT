package test.security.auth;

// 시큐리티는 이제 /login 주소 요청이 오면 이를 낚아채서 로그인을 진행시킴 (SecurityConfig.java에서 loginProcessingUrl("/login") 설정)
// 이때 로그인이 완료되면 시큐리티 session을 만들어 여기에 넣어줌 
// 시큐리티가 갖고 있는 session은 'Security ContextHolder'를 '키'로 가지고, '세션정보'를 '값'으로 가짐
// 시큐리티 세션에 들어갈 수 있는 오브젝트는 "Authentication 타입의 객체" !!
// 즉, 우리가 로그인을 수행하기 위해선 Authentication 안에 User정보가 있어야 함.
// User오브젝트 타입 => UserDetails 타입 객체

// Security Session => Authentication => UserDetails
public class PrincipalDetails {

}
