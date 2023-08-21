package com.cos.jwt.config.jwt;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// 스프링 시큐리티에서 제공하는 UsernamePasswordAuthenticationFilter
// UsernamePasswordAuthenticationFilter 필터는 원래  "/login" 요청과 함께 POST메소드로 username, password가 오면 동작하는 시큐리티 필터임
// 하지만 현재는 SecurityConfig에서 formLogin을 disable해버렸기 때문에 "/login"요청과 함께 인증정보(ID,PW)가 POST로 전송되어도 동작하지 않음 => 404 error

// 우리는 jwt를 이용하므로 formLogin은 필요없지만, UsernamePasswordAuthenticationFilter는 필요함!
// 그래서 SecurityConfig에 해당 필터를 등록해줄 것임
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter{

}
