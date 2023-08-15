# 21강. JWT Bearer 인증 방식
## 21-1. SecurityConfig 클래스 추가설명
```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	
	private final CorsFilter corsFilter;
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
		http.csrf().disable();
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // (1)
		.and()
		.addFilter(corsFilter)
		.formLogin().disable()
		.httpBasic().disable() // (2)
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
```
1. 
    ```java
    http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    ```
    - STATELESS : 세션을 사용하지 않겠다는 의미.   
    - 기본적으로 web은 stateless방식이지만, 이를 stateful 처럼 사용하기 위해 세션과 쿠키를 이용해왔음. JWT에선 기존 stateless방식을 이용하기 위해 세션과 쿠키를 사용하지 않겠다고 설정.
2. 
    ```java
    .httpBasic().disable()
    ```
	- httpBasic방식을 사용하지 않겠다는 의미. 우리는 JWT Bearer방식을 사용할 것.
	---
	### JWT Bearer방식을 사용하는 이유
    #### http 세션, 쿠키방식의 단점
	1. [chapter12-5. 세션방식의 문제 : 로드밸런싱 환경](./chapter_12.md) → 확장성이 떨어짐
	2. 최근 들어 서버는 http only라는 설정을 통해 http에서 생성한 쿠키만을 다루며, 사용자가 자바스크립트 등을 이용해 임의로 쿠키를 설정할 수 없게끔 막아둠.
		- 2-1. http 쿠키방식을 이용하기 위해선 사용자가 임의로 설정한 쿠키를 서버에서 거절하지 못하도록 http only설정을 false로 설정해야 함.
		- 2-2. **"http only : false"** 설정은 외부에서 쉽게 위/변조가 가능하므로 보안적인 부분에서 취약함.
    #### http 세션, 쿠키방식을 보완한 httpBasic 방식
	1. header에다가 Authorization 키 값에 인증정보(ID, PW)를 담아서 요청하는 방식.
	2. 요청할때마다 header에다가 인증정보를 담아가기 때문에 요청할 때마다 인증을 하는 꼴이므로 쿠키, 세션 방식을 이용하지 않아도 됨. → 확장성이 좋음
	3. 그러나 ID, PW와 같은 인증정보는 암호화가 되지 않으므로 중간에 노출이 될 수 있음. 그러므로 https(http secure)서버를 사용하여 인증정보를 암호화함.
		- 3-1. 아무리 암호화를 하더라도 인증정보가 노출되는 것은 완벽히 막을 수 없음.
	#### httpBasic방식을 보완한 JWT Bearer 방식
	1. 우리가 사용하려는 JWT Bearer 방식은 httpBasic방식에서 사용한 Authorization키에다 인증정보가 아닌 **토큰**을 넣어 사용함.
	2. 토큰 또한 노출될 가능성이 없지는 않지만, 그 자체가 사용자의 ID, PW와 같은 인증정보가 아니므로 httpBasic방식보다는 안전함.
		- 2-1. 토큰은 로그인할때마다 서버에서 다시 만들어주므로, httpBasic방식과 같이 사용자의 ID, PW가 노출되는 것보다 비교적 안전함.
		- 2-2. 또한 토큰은 유효시간이 있으므로, 특정 시간이 지나면 더 이상 유효하지 않음.