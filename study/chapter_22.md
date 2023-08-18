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
    - 에러를 살펴보면, 앞서 등록한 MyFilter1는 SecurityFilterChain에 등록할 수 없다. 그 이유는 MyFilter1의 타입이 'SecurityFilter'가 아니라 'Filter'이기 때문.
        - 시큐리티 필터에 해당 필터를 등록하고 싶으면 'addFilter'가 아닌,   
        '**addFilterBefore**' 또는 '**addFilterAfter**'를 이용해서 시큐리티 필터가 시작되기 전 또는 이후에 해당 필터를 걸어주어야 한다.
        - addFilterBefore와 addFilterAfter   
        : SecurityFilterChain에 등록된 필터 중 특정 필터를 선택하여, 선택한 필터가 시작되기 전/후에 커스텀한 필터를 실행시킬 수 있게 해줌. (가장 먼저 실행가능, SecurityFilterChain이 모두 실행된 후에 실행하는 것도 가능!)

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

- 하지만 이처럼 커스텀한 필터(MyFilter1)를 굳이 시큐리티 필터에다 걸 필요는 없음
    - 이제 시큐리티 필터 체인에 거는 것이 아닌, 개별적으로 필터를 설정하는 클래스 파일을 생성해보자.

## 22-3. FilterConfig 클래스 생성
### 22-3-1. SecurityConfig 내 addFilterBefore 설정 삭제
```java
http.addFilterBefore(new MyFilter1(), BasicAuthenticationFilter.class);
```
- 앞서 SecurityConfig 파일에 추가해둔 설정정보는 삭제해준다.

### 22-3-2. FilterConfig 생성
```java
@Configuration
public class FilterConfig {

	@Bean
	public FilterRegistrationBean<MyFilter1> filter1(){
		FilterRegistrationBean<MyFilter1> bean = new FilterRegistrationBean<>(new MyFilter1());
		bean.addUrlPatterns("/*"); // 모든 url에 필터 적용
		bean.setOrder(0); // 순서 지정. 낮은 번호일수록 필터중에서 먼저 실행됨
		return bean;
	}
}
```
- 결과는 MyFilter1 이 호출되기 때문에 필터에 등록해둔 출력문이 조회된다.
    ```
    필터1
    ```

### 22-3-3. 새로운 필터 추가
```java
public class MyFilter2 implements Filter{

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		System.out.println("필터2");
		chain.doFilter(request, response);
	}
}
```
```java
@Configuration
public class FilterConfig {

	@Bean
	public FilterRegistrationBean<MyFilter1> filter1(){
		FilterRegistrationBean<MyFilter1> bean = new FilterRegistrationBean<>(new MyFilter1());
		bean.addUrlPatterns("/*"); 
		bean.setOrder(0); 
		return bean;
	}
	
	@Bean
	public FilterRegistrationBean<MyFilter2> filter2(){
		FilterRegistrationBean<MyFilter2> bean = new FilterRegistrationBean<>(new MyFilter2());
		bean.addUrlPatterns("/*"); 
		bean.setOrder(1); 
		return bean;
	}
}
```
- 순서로 지정한 숫자가 낮을수록 우선순위가 높기때문에, 결과는 다음과 같다.
    ```
    필터1
    필터2
    ```

### 22-3-4. 정리
1. SecurityFilterChain에 설정할 필요없이 이처럼 따로 필터를 설정해주어도 된다.
2. 커스텀한 필터는 SecurityFilterChain에 등록된 필터가 전부 다 실행된 이후에 동작한다.
    ```java
    // 2-1. MyFilter3 생성
    public class MyFilter3 implements Filter{

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {
            System.out.println("필터3");
            chain.doFilter(request, response);
        }
    }
    ```
    ```java
    // 2-2. SecurityConfig에 MyFilter3 등록
    @Configuration
    @EnableWebSecurity
    @RequiredArgsConstructor
    public class SecurityConfig {
        
        private final CorsFilter corsFilter;
        
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
            http.addFilterBefore(new MyFilter3(), BasicAuthenticationFilter.class); // 필터추가
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
    // 2-3. 실행결과
    필터3
    필터1
    필터2
    ```
    - SecurityFilterChain에 등록한 필터3이 가장 먼저 실행되는 것을 통해, SecurityFilterChain이 모두 실행된 이후 커스텀한 필터가 실행됨을 알 수 있다.
3. 이와 같은 필터를 이용하여, JWT토큰처리를 할 예정.
