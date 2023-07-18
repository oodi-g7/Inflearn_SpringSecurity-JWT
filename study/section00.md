# 1장. 환경설정
## 1-1. MySQL Database 및 사용자 생성
```sql
create user 'cos'@'%' identified by 'cos1234';
GRANT ALL PRIVILEGES ON *.* TO 'cos'@'%';
create database security;
use security;
```

## 1-2. 프로젝트 생성
- 추가할 dependencies
    - Spring Boot DevTools
    - Lombok
    - Spring Data JPA
    - MySQL Driver
    - Spring Security
    - Mustache
    - Spring Web

## 1-3. application.yml 설정
```
server:
  port: 8080
  servlet:
    context-path: /
    encoding:
      charset: UTF-8
      enabled: true
      force: true
      
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/security?serverTimezone=Asia/Seoul
    username: cos
    password: cos1234
    
  mvc:
    view:
      prefix: /templates/
      suffix: .mustache

  jpa:
    hibernate:
      ddl-auto: update
      naming:
        physical-strategy: org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
    show-sql: true
```
- mvc/view/prefix와 mvc/view/suffix를 확인해보면 각각 /templates/와 .mustache로 설정되어있다.
	- 기본경로는 src/main/resources이므로 컨트롤러에서 뷰페이지를 반환할때 최종경로는 <U>**src/main/resources/templates/페이지명.mustache**</U> 가 된다.
	- 우린 이미 dependencies에 템플릿 엔진으로 mustache를 추가해두었기 때문에, 사실상 mvc/view/prefix와 mvc/view/suffix 설정은 하지 않아도 무방하다.
	- .mustache파일을 템플릿 엔진으로 추가해뒀지만, 편의상 .html파일을 이용할 것이므로 추가적인 뷰리졸버 설정이 필요하다. <U>**→ (1-4. 뷰리졸버 설정)**</U>

## 1-4. 뷰리졸버 설정
```java
import org.springframework.boot.web.servlet.view.MustacheViewResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer{
	
	@Override
	public void configureViewResolvers(ViewResolverRegistry registry) {
		MustacheViewResolver resolver = new MustacheViewResolver();
		resolver.setCharset("UTF-8");
		resolver.setContentType("text/html; chatset=UTF-8");
		resolver.setPrefix("classpath:/templates/");
		resolver.setSuffix(".html");
		
		registry.viewResolver(resolver);
	}
}
```
- prefix 세팅값인 classpath:/templates/ 에서 classpath는 src/main/resources를 의미한다.
- prefix, suffix 설정을 통해 이제 컨트롤러에서 반환하는 뷰페이지의 최종경로는 <U>**src/main/resources/templates/페이지명.html**</U>이다.



# 2장. 시큐리티 설정
## 2-1. 컨트롤러 매핑 주소 생성
```java
@Controller
public class IndexController {

	@GetMapping({"","/"})
	public String index() {
		return "index";
	}
	
	@GetMapping("/user")
	public @ResponseBody String user() {
		return "user";
	}
	
	@GetMapping("/admin")
	public @ResponseBody String admin() {
		return "admin";
	}
	
	@GetMapping("/manager")
	public @ResponseBody String manager() {
		return "manager";
	}
	
	@GetMapping("/loginForm")
	public String loginForm() {
		return "loginForm";
	}
	
	@GetMapping("/joinForm")
	public String joinForm() {
		return "joinForm";
	}

	@GetMapping("/login")
	public @ResponseBody String login(){
		return "login";
	}

	@GetMapping("/logout")
	public @ResponseBody String logout(){
		return "logout";
	}
}
```
- "/login"과 "/logout" 주소는 특별한 설정이 없으면 스프링 시큐리티가 해당주소를 낚아채간다.

## 2-2. SecurityConfig 설정
- 권한별 페이지 접근권한을 상세하게 해주기 위해 SecurityConfig 파일을 작성해준다.
- 참고. [Deprecated된 WebSecurityConfigurerAdapter 대처방법](https://velog.io/@pjh612/Deprecated%EB%90%9C-WebSecurityConfigurerAdapter-%EC%96%B4%EB%96%BB%EA%B2%8C-%EB%8C%80%EC%B2%98%ED%95%98%EC%A7%80)
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig{

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http.csrf().disable();
		http.authorizeRequests()
			.antMatchers("/user/**").authenticated()
			.antMatchers("/manager/**").access("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
			.antMatchers("/admin/**").access("hasRole('ROLE_ADMIN')")
			.anyRequest().permitAll()
			.and()
			.formLogin()
			.loginPage("/loginForm")
			.loginProcessingUrl("/login")
			.defaultSuccessUrl("/");
		
		return http.build();
    }
}
```
1. **@Configuration** : 스프링 컨테이너가 동작할때, 해당 클래스파일 설정값을 컨테이너에 등록. 의존성 주입(DI)
2. **@EnableWebSecurity** : 스프링 시큐리티 필터(SecurityConfig.class, 해당 클래스파일)를 스프링 필터 체인에 등록. 즉, 현재 클래스에 등록할 필터들을 기본 필터 체인(Spring FilterChain)에 등록시키기 위해 설정.
3. 
	```java
	http.csrf().disable();
	```
	- csrf 란? cross site request forgery, 사이트 간 위조요청. <U>정상적인 사용자가 의도치않은 위조요청을 보내는것을 막음</U>
	- REST API의 앤드포인트에 의존하는 구조(JSON방식으로 통신)는 서버쪽에 세션이나 브라우저 쿠키에 의존하지 않음. 따라서 더이상 CSRF에 대한 관련이 없으며 이러한 API는 CSRF공격을 받을 가능성이 존재하지 않으므로 disable() 해준다.
4. 
	```java
	http.authorizeRequests() // (1)
		.antMatchers("/user/**").authenticated() // (2)
		.antMatchers("/manager/**").access("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')") // (3)
		.antMatchers("/admin/**").access("hasRole('ROLE_ADMIN')") // (4)
		.anyRequest().permitAll() // (5)
	```
	- (1) 요청 url마다 접근권한 설정해주기
	- (2) "/user/**" 요청은 인증이 필요하다. 즉, 로그인한 사람만 접근가능.(로그인만 하면 모두 접근 가능)
	- (3) "/manager/**" 식의 요청은 로그인 한 사용자 중 admin과 manager만 접근가능
	- (4) "/admin/**" 식의 요청은 로그인 한 사용자 중 admin만 접근가능
	- (5) "/user/**", "/manager/**", "/admin/**" 를 제외한 다른 요청들은 권한 상관없이 접근가능
5. 
	```java
	.formLogin() 
	.loginPage("/loginForm")
	```
	- 권한이 없는 페이지에 접근하려고 할때, 404페이지가 아니라 로그인 페이지를 보여주기
	- 이는 사용자가 아직 로그인을 하지 않았을 경우에 해당하는 설정임. 만약 로그인 한 경우인데 권한없는 페이지로 가려고 한다면 로그인페이지가 아닌 404페이지로 보냄(접근권한이 없는 페이지로 이동하려고 했으니 없는페이지 404표시)
6. 
	```java
	.loginProcessingUrl("/login")
	```
	- "/login" 주소가 호출이 되면 시큐리티가 낚아채서 대신 로그인을 진행해줌.(method = POST)
	- 따라서 우리는 /login주소와 매핑할 컨트롤러를 만들필요없음.
7. 
	```java
	.defaultSuccessUrl("/");
	```
	- default페이지는 메인페이지로 설정
	- "/loginForm"을 요청해서 로그인을 완료했을 경우에는 default페이지가 메인페이지("/")이고, 만약 "/user"를 요청해서 로그인 실행을 했을 경우는 스프링시큐리티가 default페이지인 메인페이지가 아닌 사용자가 가려고 했던 "/user" 페이지로 이동시켜줌. (시큐리티가 제공해주는 기능)


# 3장. 시큐리티 회원가입
# 4장. 시큐리티 로그인
# 5장. 시큐리티 권한처리