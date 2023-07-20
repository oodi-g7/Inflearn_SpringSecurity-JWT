package test.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // 스프링 시큐리티 필터(SecurityConfig.class)가 스프링 필터체인에 등록됨. 현재 클래스에 등록될 필터들이 기본 필터 체인(스프링 필터체인)에 등록될 것이다! 
@EnableGlobalMethodSecurity(securedEnabled = true, // secured 어노테이션 활성화 : IndexController - info() 메소드의  @Secured ! @Secured는 개별 매핑url에 간단하게 권한처리가능
							prePostEnabled = true) // preAuthorize 어노테이션, postAuthorize 어노테이션 활성화 : IndexController - data() 메소드의 @PreAuthorize, @PostAuthorize
public class SecurityConfig{
	
	// 해당 메서드의 리턴되는 오브젝트를 IoC로 등록해준다.
	@Bean 
	public BCryptPasswordEncoder encodePwd() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
		//csrf = cross site request forgery (사이트 간 위조요청) : 정상적인 사용자가 의도치않은 위조요청을 보내는것을 막음
		//REST API의 앤드포인트에 의존하는 구조(JSON방식으로 통신)는 서버쪽에 세션이나 브라우저 쿠키에 의존하지 않음. 따라서 더이상 CSRF에 대한 관련이 없으므로 이러한 API는 CSRF공격을 받을 가능성이 존재하지 않음
		http.csrf().disable();
		http.authorizeRequests()
			// 요청url마다 권한설정해주기
			.antMatchers("/user/**").authenticated() // "/user/**" 식의 요청은 인증이 필요하다. =로그인한 사람만 접근가능(로그인한 사용자는 모두 접근가능)
			.antMatchers("/manager/**").access("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')") // "/manager/**" 식의 요청은 로그인 한 사용자 중 admin과 manager만 접근가능
			.antMatchers("/admin/**").access("hasRole('ROLE_ADMIN')") // "/admin/**" 식의 요청은 로그인 한 사용자 중 admin만 접근가능
			.anyRequest().permitAll() // "/user/**", "/manager/**", "/admin/**" 를 제외한 다른 요청들은 권한 상관없이 접근가능
			.and()
			
			// 권한이 없는 페이지에 접근하려고 할때, 404페이지가 아니라 로그인 페이지를 보여주기(로그인을 하지 않았을경우에! 로그인 한 경우인데 권한없는 페이지로 가려고 하면 404페이지로 보냄)
			.formLogin() 
			.loginPage("/loginForm")
			
			// "/login" 주소가 호출이 되면 시큐리티가 낚아채서 대신 로그인을 진행해줌.(method=POST)
			// 따라서 우리는 /login주소와 매핑할 컨트롤러를 만들필요없음.
			.loginProcessingUrl("/login")
			
			// default페이지는 메인페이지
			// "/loginForm"을 요청해서 로그인 실행을 했을 경우에 default페이지가 메인페이지("/")이고,
			// 만약 "/user"를 요청해서 로그인 실행을 했을 경우는 스프링시큐리티가 default페이지인 메인페이지가 아닌 사용자가 가려고 했던 "/user" 페이지로 이동시켜줌
			.defaultSuccessUrl("/");
		
		return http.build();
	}
}
