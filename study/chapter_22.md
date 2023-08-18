# 22강. JWT Filter 등록테스트
## 22-1. 필터 생성하기
```java
package com.cos.jwt.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class MyFilter1 implements Filter{ // (1)

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {		
		System.out.println("필터1");
		chain.doFilter(request, response); // (2)
	}
}
```
1. 필터를 생성하기 위해선 'javax.servlet.Filter'를 implement 받아주어야 한다.
2. 
    ```java
    chain.doFilter(request, response);
    ```
    - 필터를 생성해준 후, 위와 같이 필터체인에다가 등록해주어야 한다.

## 22-2. 시큐리티 설정에 필터추가
### 22-2-1. http.addFilter 사용
```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	
	private final CorsFilter corsFilter;
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
		http.addFilter(new MyFilter1()); // 필터추가
		http.csrf().disable(); 
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) 
		.and()
		.addFilter(corsFilter) 
		.formLogin().disable() 
		.httpBasic().disable() 
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

```
org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration': Unsatisfied dependency expressed through method 'setFilterChains' parameter 0; nested exception is org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'filterChain' defined in class path resource [com/cos/jwt/config/SecurityConfig.class]: Bean instantiation via factory method failed; nested exception is org.springframework.beans.BeanInstantiationException: Failed to instantiate [org.springframework.security.web.SecurityFilterChain]: Factory method 'filterChain' threw exception; nested exception is java.lang.IllegalArgumentException: The Filter class com.cos.jwt.filter.MyFilter1 does not have a registered order and cannot be added without a specified order. Consider using addFilterBefore or addFilterAfter instead.
```
- 필터를 추가해준 후 실행을 해보면 위와 같은 에러가 발생한다.
    ```
    The Filter class com.cos.jwt.filter.MyFilter1 does not have a registered order and cannot be added without a specified order. Consider using addFilterBefore or addFilterAfter instead.
    ```
    - 에러를 살펴보면, 앞서 등록한 MyFilter1는 SecurityFilterChain에 등록할 수 없다. 그 이유는 MyFilter1의 타입은 'SecurityFilter'가 아니라 'Filter'이기 때문.
        - 시큐리티 필터에 해당 필터를 등록하고 싶으면 'addFilter'가 아닌,   
        '**addFilterBefore**' 또는 '**addFilterAfter**'를 이용해서 시큐리티 필터가 시작되기 전 또는 이후에 해당 필터를 걸어주어야 한다.

### 22-2-2. http.addFilterBefore 사용
```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	
	private final CorsFilter corsFilter;
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
		http.addFilterBefore(new MyFilter1(), BasicAuthenticationFilter.class); // 필터추가
		http.csrf().disable(); 
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) 
		.and()
		.addFilter(corsFilter) 
		.formLogin().disable() 
		.httpBasic().disable() 
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
<img src="./img/chapter22_1.jpg">

- addFilterBefore() 이용
    - 위 사진에서 SecurityFilterChain에 등록되어있는 필터 중 특정 한 필터를 선택하여,   
    해당 필터가 시작되기 전에 'MyFilter1'을 실행시키게끔 설정할 수 있다.
    - 현재는 BasicAuthenticationFilter가 시작되기 전에 'MyFilter1'가 실행되도록 설정해둔 것
    - 이 방법을 사용하려면 SecurityFilterChain에 등록된 필터들을 알아야 한다는 불편함이 있음

- 사실 이처럼 커스텀한 필터를 굳이 시큐리티 필터에다 걸 필요는 없음
    - 이제 시큐리티 필터 체인에 거는 것이 아닌, 개별적으로 필터를 설정하는 설정파일을 따로 생성해보자.

## 22-3. 
- 커스텀한 필터를 굳이 시큐리티 필터에다 걸 필요는 없음.
- 
