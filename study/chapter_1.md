# 1강. 환경설정
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