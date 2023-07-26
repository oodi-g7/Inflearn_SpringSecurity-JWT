# 5강. 시큐리티 권한처리
## 5-1. ROLE_MANAGER, ROLE_ADMIN 유저 생성
- 회원가입 페이지에서 manager와 admin 계정을 생성한다.

	<img src="./img/chapter5_1.png">

- 워크벤치를 확인해보면 방금 생성한 manager와 admin계정을 확인할 수 있다.
	<img src="./img/chapter5_2.png">

- IndexController에서 회원가입하는 모든 유저의 권한을 "ROLE_USER"로 설정하게끔 했으니 manager와 admin계정 또한 "ROLE_USER"권한으로 생성되어 있다. 그러므로 권한을 변경해준다. 
	```sql
	update user set role = 'ROLE_MANAGER' where id = 2;
	update user set role = 'ROLE_ADMIN' where id = 3;
	commit;
	```

## 5-2. @EnableGlobalMethodSecurity
### 5-2-1. securedEnabled 옵션
- SecurityConfig에 @EnableGlobalMethodSecurity 어노테이션을 추가한다.
	```java
	@Configuration
	@EnableWebSecurity
	@EnableGlobalMethodSecurity(securedEnabled = true) 
	public class SecurityConfig{ 
		···
	}
	```
	- **@EnableGlobalMethodSecurity** 어노테이션은 옵션설정을 통해서 컨트롤러 메소드별 권한설정을 도와준다.
	- 현재 securedEnabled옵션을 true로 설정해두면, @Secured 어노테이션이 활성화된다.

- IndexController에 새로운 메서드를 추가한다.
	```java
	@Secured("ROLE_ADMIN")
	@GetMapping("/info")
	public @ResponseBody String info() {
		return "개인정보";
	}
	```
	- **@Secured** 어노테이션은 개별 매핑 url에 간단하게 권한처리가 가능하도록 도와준다.
	- 그러므로 "/info"요청은 오직 "ROLE_ADMIN"권한을 가진 계정만 접근이 가능하다.
	- 만약 ADMIN계정이 아닌 계정으로 접근시 아래와 같은 페이지가 뜬다.
	<img src="./img/chapter5_3.png">

### 5-2-2. prePostEnabled 옵션
- SecurityConfig 내 @EnableGlobalMethodSecurity 어노테이션에 prePostEnabled 옵션을 추가한다.
	```java
	@Configuration
	@EnableWebSecurity
	@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
	public class SecurityConfig{ 
		···
	}
	```
	- prePostEnabled 옵션을 true로 설정해두면, @PreAuthorize 어노테이션과 @PostAuthorize 어노테이션이 활성화된다.

- IndexController에 새로운 메서드를 추가한다.
	```java
	@PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')") 
	@GetMapping("/data")
	public @ResponseBody String data() {
		return "데이터정보";
	}
	```
	- **@PreAuthorize** 어노테이션은 현재 자신이 위치해있는 data()함수가 실행되기 직전에 먼저 실행된다.
	- 일반적으로 자신이 위치해있는 해당 메소드에 접근권한을 2개이상 설정시 사용한다.
		- 문법 : hasRole('권한명') or hasRole('권한')
	- 보통 접근권한을 1개만 설정시 @Secured어노테이션을 사용하고, 2개이상 설정시 @PreAuthorize어노테이션을 사용한다.

- 방금 생성한 data()함수에 새로운 어노테이션 @PostAuthorize을 추가한다.
	```java
	//@PreAuthorize("hasRole('ROLE_MANAGER') or hasRole('ROLE_ADMIN')") 
	@PostAuthorize
	@GetMapping("/data")
	public @ResponseBody String data() {
		return "데이터정보";
	}
	```
	- **@PostAuthorize** 어노테이션은 현재 자신이 위치해있는 data()함수가 실행된 이후에 실행된다.
	- 일반적으로 잘 사용되지는 않는다.

### 5-2-3. 정리
- SecurityConfig에서 글로벌로 권한처리를 하는 것이 아니라, 개별 메소드에 대한 특정한 권한처리를 하고 싶을때는 @EnableGlobalMethodSecurity 어노테이션을 이용할 수 있다.