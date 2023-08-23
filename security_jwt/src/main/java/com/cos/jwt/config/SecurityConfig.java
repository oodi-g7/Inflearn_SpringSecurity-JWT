package com.cos.jwt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.CorsFilter;

import com.cos.jwt.config.jwt.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration // IoC
@EnableWebSecurity // 시큐리티 설정 활성화
@RequiredArgsConstructor
public class SecurityConfig {
	
	private final CorsFilter corsFilter;
	
	AuthenticationManager authenticationManager;
	
	@Bean
	public AuthenticationManager authenticationManager
		(AuthenticationConfiguration authenticationConfiguration)throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
//		http.addFilterBefore(new MyFilter3(), SecurityContextPersistenceFilter.class);
		http.csrf().disable(); // JWT 로그인시 필수 설정
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // JWT 로그인시 필수 설정, 세션을 사용하지 않겠다. (stateless 무상태성 서버로 만들겠다.)
		.and()
		.addFilter(corsFilter) // JWT 로그인시 필수 설정, 만들어둔 CorsConfig를 필터에 등록시킴.
							   // 이제 더 이상 내 서버는 CrossOrigin정책을 사용하지 않을 것! 모든 요청을 허용하겠다!
							   // 다른 방법으로는 @CrossOrigin를 사용하는 방법도 있는데 이건 인증이 필요없을 경우에만 사용이 가능하다. 인증이 필요한 경우라면 이처럼 시큐리티 필터에 직접 등록해주어야 한다.
		.formLogin().disable() // JWT 로그인시 필수 설정, JWT서버니까 formLogin을 사용하지 않음. formLogin => Spring Security에서 제공하는 인증방식
		.httpBasic().disable() // 기본적인  http 로그인 방식은 사용하지 않는다.
		.addFilter(new JwtAuthenticationFilter(authenticationManager)) // UsernamePasswordAuthenticationFilter 필터를 등록
		.authorizeRequests()
			.antMatchers("/api/v1/user/**")
			.access("hasRole('ROLE_USER') or hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
			.antMatchers("/api/v1/manager/**")
			.access("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')")
			.antMatchers("/api/v1/admin/**")
			.access("hasRole('ROLE_ADMIN')")
			.anyRequest().permitAll();
		
		return http.build();
	}
}
