package com.cos.jwt.config.jwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.authentication.UserServiceBeanDefinitionParser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.cos.jwt.config.auth.PrincipalDetails;
import com.cos.jwt.model.User;
import com.cos.jwt.repository.UserRepository;

// 인증요청이 있을때 동작하는 필터가 아님
// 시큐리티가 filter를 가지고 있는데 그 필터중에 BasicAuthenticationFilter라는 것이 있음
// 권한이나 인증이 필요한 특정 주소를 요청했을 때 위 필터를 무조건 타게 되어있음
// 만약에 권한이나 인증이 필요한 주소가 아니라면 해당 필터를 타지 않음
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {
	
	private UserRepository userRepository;

	public JwtAuthorizationFilter(AuthenticationManager authenticationManager, UserRepository userRepository) {
		super(authenticationManager);
		this.userRepository = userRepository;
		System.out.println("인증이나 권한이 필요한 주소 요청이 됨");
	}

	// 인증이나 권한이 필요한 주소요청이 있을 때 해당 필터를 타게 됨
	// 여기서 헤더값을 확인해보자
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		System.out.println("인증이나 권한이 필요한 주소 요청이 됨");
		
		String jwtHeader = request.getHeader("Authorization");
		System.out.println(jwtHeader);
		
		// JWT 토큰을 검증해서 정상적인 사용자인지 확인
		// 1. header가 있는지 확인
		if(jwtHeader == null || !jwtHeader.startsWith("Bearer")) {
			chain.doFilter(request, response);
			return;
		}
		
		// 2. 사용자 검증
		String jwtToken = request.getHeader("Authorization").replace("Bearer ", "");
		
		String username = JWT.require(Algorithm.HMAC512("cos")) // HMAC512암호화를 사용하고, secret이 "cos"인 토큰
							.build()
							.verify(jwtToken) // jwt토큰을 서명하기
							.getClaim("username") // 서명이 정상적으로 이뤄지면 토큰 내 정보인 "username"을 가져올 것
							.asString(); // "username"을 가져와서 String으로 캐스팅해줌
		
		// 3. 서명되었는지 확인 - null이 아니라면 서명이 완료된 것
		if(username != null) {
			// select 조회가 이뤄지면 서명한 사용자는 정상적인 사용자 
			User userEntity = userRepository.findByUsername(username);
			
			// jwt토큰 서명을 통해 서명이 정상이면 Authentication 객체를 만들어준다. 
			PrincipalDetails principalDetails = new PrincipalDetails(userEntity);
			Authentication authentication = // authentication객체는 사용자의 로그인 요청시 만들어지는 객체로서(JwtAuthenticationFilter - attemptAuthentication() 참고)
											// principalDetails객체(로그인한 사용자 객체)와 사용자의 비밀번호가 필요하다. 그 정보들을 가지고  로그인을 시도하여 loadByUsername()을 실행시킴 
											// 현재는 이미 로그인 된 사용자(username != null, 정상적인 사용자)가 가지고 온 jwt토큰을 검증하는 과정이므로 
											// 정석대로 authentication을 만드는 것이 아니라 강제로 authentication 객체를 생성해준다.
											// 넘기는 파라미터로는 principalDetails객체, 사용자의 비밀번호 값은 null로 두고, 권한정보(authorities)를 추가해준다.
					new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities()); 
			
			// 강제로 시큐리티 세션에 접근하여 Authentication객체를 저장
			SecurityContextHolder.getContext().setAuthentication(authentication);
			
			chain.doFilter(request, response);
			
			// 인증은 토큰 검증시 끝. 인증을 하기 위해서가 아닌 스프링 시큐리티가 수행해주는 권한 처리를 위해!
			// 아래와 같이 토큰을 만들어서 Authentication 객체를 강제로 만들고 그걸 세션에 저장!
			// 패스워드는 모르니까 null 처리, 어차피 지금 인증하는게 아니니까!!
			// https://velog.io/@blacklandbird/JWT%EB%A1%9C-TOKEN%EB%B0%9C%EA%B8%89%ED%95%98%EA%B8%B0
			
		}
		
		/*
		String header = request.getHeader(JwtProperties.HEADER_STRING);
		if (header == null || !header.startsWith(JwtProperties.TOKEN_PREFIX)) {
			chain.doFilter(request, response);
			return;
		}
		System.out.println("header : " + header);
		String token = request.getHeader(JwtProperties.HEADER_STRING)
				.replace(JwtProperties.TOKEN_PREFIX, "");

		// 토큰 검증 (이게 인증이기 때문에 AuthenticationManager도 필요 없음)
		// 내가 SecurityContext에 집적접근해서 세션을 만들때 자동으로 UserDetailsService에 있는
		// loadByUsername이 호출됨.
		String username = JWT.require(Algorithm.HMAC512(JwtProperties.SECRET)).build().verify(token)
				.getClaim("username").asString();

		if (username != null) {
			User user = userRepository.findByUsername(username);

			// 인증은 토큰 검증시 끝. 인증을 하기 위해서가 아닌 스프링 시큐리티가 수행해주는 권한 처리를 위해
			// 아래와 같이 토큰을 만들어서 Authentication 객체를 강제로 만들고 그걸 세션에 저장!
			PrincipalDetails principalDetails = new PrincipalDetails(user);
			Authentication authentication = new UsernamePasswordAuthenticationToken(
					principalDetails, // 나중에 컨트롤러에서 DI해서 쓸 때 사용하기 편함.
					null, // 패스워드는 모르니까 null 처리, 어차피 지금 인증하는게 아니니까!!
					principalDetails.getAuthorities());

			// 강제로 시큐리티의 세션에 접근하여 값 저장
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}

		chain.doFilter(request, response);
		*/
	}
}
