package com.cos.jwt.config.jwt;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

// 스프링 시큐리티에서 제공하는 UsernamePasswordAuthenticationFilter
// UsernamePasswordAuthenticationFilter 필터는 원래  "/login" 요청과 함께 POST메소드로 username, password가 오면 동작하는 시큐리티 필터임
// 하지만 현재는 SecurityConfig에서 formLogin을 disable해버렸기 때문에 "/login"요청과 함께 인증정보(ID,PW)가 POST로 전송되어도 동작하지 않음 => 404 error

// 우리는 jwt를 이용하므로 formLogin은 필요없지만, UsernamePasswordAuthenticationFilter는 필요함!
// 그래서 SecurityConfig에 해당 필터를 등록해줄 것임

// 등록해주고나면 로그인 default url인 "/login"요청시, UsernamePasswordAuthenticationFilter를 상속받은 JwtAuthenticationFilter필터가 동작할 것이고,
// 자동적으로 attemptAuthentication함수가 실행된다.
// 그러므로 attemptAuthentication함수에서 우리는 로그인 처리를 해주면 됨
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter{
	
	private final AuthenticationManager authenticationManager;

	// /login 요청을 하면 로그인 시도를 위해서 실행되는 함수
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		System.out.println("JwtAuthenticationFilter : 로그인 시도중");
		
		// 1. username, password 받아서
		
		// 2. 정상인지 로그인 시도 해보기. authenticationManager로 로그인 시도를 하면
		// PrincipalDetailsService가 호출되어 loadUserByUsername()함수가 실행됨.
		
		// 3. loadUserByUsername()함수가 실행되어 반환된 PrincipalDetails객체(유저정보가담긴)를 시큐리티 세션에 담고(권환관리를 위해서)
		
		// 4. JWT토큰을 만들어서 응답해주면 됨
		return super.attemptAuthentication(request, response);
	}
}
