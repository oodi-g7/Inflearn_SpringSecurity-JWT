package com.cos.jwt.config.auth;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.cos.jwt.model.User;
import com.cos.jwt.repository.UserRepository;

import lombok.RequiredArgsConstructor;

// http://localhost:8080/login 보통의 시큐리티 설정이었다면 해당 url에서 principaldetailsservice가 동작했겠지만,
// 우리는 jwt를 사용하기위해 시큐리티 설정에서 formlogin을 disable처리 해두었다. 따라서 스프링시큐리티에 기본 로그인 url인 '/login'이 동작하지 않는다!
// 그러므로 PrincipalDetailsService클래스는 http://localhost:8080/login 에서 동작하지 않는다
@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService{
	
	private final UserRepository userRepo;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		System.out.println("PrincipalDetailsService의 loadUserByUsername()");
		User user = userRepo.findByUsername(username);
		
		return new PrincipalDetails(user);
	}

}
