package com.cos.jwt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

	// 이렇게 설정만 해서 끝나는 것이 아니라, filter에 등록을 해주어야 함  => SecurityConfig에 꼭 등록해주기
	@Bean
	public CorsFilter corsFilter() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		
		// 내 서버가 응답을 할 때 json을 자바스크립트에서 처리할 수 있게 할지 설정. 
		// ajax등으로 서버에 요청이 오면, 이에 대한 응답을 자바스크립트에서 다루게 할 지 설정. 만약 false로 설정하면 ajax등으로 요청을 보내면 응답이 오지 않음
		config.setAllowCredentials(true);  
		
		// 모든 ip에 응답을 허용하겠다.
		config.addAllowedOrigin("*");
		
		// 모든 header에 응답을 허용하겠다.
		config.addAllowedHeader("*");
		
		// 모든 post, get, put, delete, patch 요청을 허용하겠다.
		config.addAllowedMethod("*");
		
		source.registerCorsConfiguration("/api/**", config); 
		
		return new CorsFilter(source);
	}
}