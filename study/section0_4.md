# 4강. 시큐리티 로그인
## 4-1. SecurityConfig 설정추가
```java
// SecurityConfig
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
	http.authorizeRequests()
		···
		.loginProcessingUrl("/login")
		···

	return http.build();
}
```
- **.loginProcessingUrl("/login")**
	- "/login" 주소가 호출이 되면 시큐리티가 낚아채서 대신 로그인을 진행해준다.(method = POST)
	- 따라서 우리는 /login주소와 매핑할 컨트롤러를 만들 필요가 없다.

## 4-2. loginForm.html 수정
```html
<body>
<h1>로그인 페이지</h1>
<hr/>
<form action="/login" method="POST">
	<input type="text" name="username" placeholder="Username" /><br />
	<input type="password" name="password" placeholder="Password" /><br />
	<button>로그인</button>
</form>
<a href="/joinForm">회원가입을 아직 하지 않으셨나요?</a>
</body>
```
- **action="/login" method="POST"** : 로그인 정보를 입력후 로그인버튼을 누르면 "/login" 요청을 보내게 되는데, 앞서 SpringSecurity에 설정해준대로 해당 로그인 요청은 스프링 시큐리티가 낚아채어 로그인을 진행시켜준다.
- 여기서 끝이 아니다. 시큐리티 로그인을 성공적으로 수행하기 위해선 추가적인 설정이 필요하다.

## 4-3. UserDetails, UserDetailsService 구현
### 4-3-1. 잠깐 서론,
<img src="./img/sec0-1.png">

1. 앞서 SecurityConfig에 loginProcessingUrl("/login")설정을 통해, 이제 시큐리티는 "/login" 요청이 오면 이를 낚아채서 로그인을 진행시킴
2. 이때 로그인이 완료되면 Security Session을 만들어 여기에 로그인 정보를 넣어준다.
3. 시큐리티가 갖고 있는 세션은 Security ContextHolder를 Key로 갖고, 세션정보를 Value로 가짐
4. 이때, <U>**시큐리티 세션에 들어갈 수 있는 오브젝트는 Authentication타입의 객체뿐**</U>이다.
5. 그러므로 로그인을 수행하기 위해선 Authentication안에 User정보가 있어야 한다.
6. 단, Authentication안에 User정보는 UserDetails타입 객체로 저장되어 있어야 한다. 이러한 관계를 정리해보면 아래 그림과 같다.

<img src="./img/sec0-2.png">

- 정리해보자면,
	- Security Session 영역
	- => 들어갈 수 있는 객체는 Authentication 타입의 객체
	- => Authentication 객체 안에 유저정보 저장시, 유저정보는 UserDetails타입이어야만 함
- 즉, 유저정보는 UserDetails타입으로 감싸져서 Authentication타입 객체에 저장되어 SecuritySession영역에 존재하는 것!

**그렇다면 유저정보를 꺼낼때에는 ?**
- Security Session에 있는 객체를 **.get()** 해서 Authentication객체를 꺼내고, 그 속에 UserDetails객체를 꺼내어 유저정보에 접근하면 된다.
	- 그렇다면, UserDetails객체에서 어떻게 유저정보를 꺼낼까?
	- <U>**→ UserDetails를 상속받는 클래스를 만들어서, UserDetails타입이 된 해당 클래스로 유저정보를 빼내면 된다 !**</U>

### **4-3-2. UserDetails구현 : PrincipalDetails implements UserDetails**
- Spring Security에서 사용자의 정보를 담는 인터페이스
- Spring Security에서 사용자의 정보를 불러오기 위해서 구현해야하는 인터페이스
- 유저정보를 담기 위한 UserDetails객체를 만들어보자 !
```java
@Data
public class PrincipalDetails implements UserDetails{
	
	// 콤포지션(has-a)
	// 기존클래스가 새로운 클래스의 구성요소로 사용되는것
	// 기존 클래스를 확장하는 대신, 새로운 클래스에 private필드로 구체 클래스의 인스턴스를 참조
	// 상이한 클래스 관계에서, 한 클래스가 다른 클래스의 기능을 사용하여 구현해야 할때, composition(합성)을 사용
	private User user; 
	
	// PrincipalDetails객체 생성과 동시에 필드값도 채움
	// 따라서 생성된 PrincipalDetails객체는 생성자 파라미터에 넘어온 유저에 한해서만 기능을 동작함. 
	public PrincipalDetails(User user) {
		this.user = user;
	}
	
	// User의 권한을 리턴
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> collect = new ArrayList<>(); 
		collect.add(new GrantedAuthority() {
			@Override
			public String getAuthority() {
				return user.getRole();
			}
		});
		return collect;
	}

	// User의 패스워드를 리턴
	@Override
	public String getPassword() {
		return user.getPassword();
	}

	// User의 이름를 리턴
	@Override
	public String getUsername() {
		return user.getUsername();
	}

	// 해당 계정이 만료되지 않았는지 여부 (만료X : true | 만료O : false)
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	// 해당 계정이 잠겼는지 여부 (잠김X : true | 잠김O : false)
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	// 해당 계정의 비밀번호가 유효기간이 지났는지 여부(오래된 비밀번호 사용중인지 여부) (지남X : true | 지남O : false)
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	// 해당 계정이 활성화되어있는지 여부 (활성화O : true | 활성화x : false)
	@Override
	public boolean isEnabled() {
		// 우리 사이트에서 1년동안 회원이 로그인을 안해서 휴면 계정으로 변환되었다면 그때 false값 반환
		// 즉, 현재시간 - 마지막 로그인 날짜 => 1년을 초과하면 return false;
		return true;
	}
}
```
1. 
	```java
	// User의 권한을 리턴
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> collect = new ArrayList<>(); // ArrayList는 Collection의 자식임 
		collect.add(new GrantedAuthority() {
			@Override
			public String getAuthority() { // String을 리턴할 수 있음
				return user.getRole();
			}
		});
		return collect;
	}
	```
	- model패키지의 User를 확인해보면, User권한은 String타입인데 해당 메소드의 리턴값은 Collection\<GrantedAuthority\>이므로 Collection에 감싸서 유저권한정보를 넘겨야한다.
	- 그러므로 우선 리턴값으로 사용될 Collection\<GrantedAuthority\> 타입 변수 collect를 선언해주고, Collection의 자식인 ArrayList로 초기화 시켜준다. 그리고 collect를 반환한다.
		```java
		@Override
		public Collection<? extends GrantedAuthority> getAuthorities() {
			Collection<GrantedAuthority> collect = new ArrayList<>(); // ArrayList는 Collection의 자식임 
			return collect;
		}
		```
	- 이제 반환할 collect안에 GrantedAuthority타입을 넣어줘야 하므로(해당 메소드의 리턴타입과 동일하게) new GrantedAuthority() 를 입력해준다.
		```java
		@Override
		public Collection<? extends GrantedAuthority> getAuthorities() {
			Collection<GrantedAuthority> collect = new ArrayList<>(); // ArrayList는 Collection의 자식임 
			collect.add(new GrantedAuthority() {
				@Override
				public String getAuthority() { // String을 리턴할 수 있음
					return user.getRole();
				}
			});
			return collect;
		}
		```
	- 그럼 GrantedAuthority 인터페이스의 getAuthority() 메소드가 오버라이드 되는데, 해당 메소드는 String을 반환할 수 있으므로 여기에다 유저의 권한정보를 반환해주면 완성 !
2. 이 외에도 UserDetails 인터페이스의 기본 오버라이드 메서드들은 아래와 같다.

	<table>
		<thead>
			<tr>
				<th>메소드</th>
				<th>리턴타입</th>
				<th>설명</th>
				<th>기본값</th>
			</tr>
		</thead>
		<tbody>
			<tr>
				<td>getAuthorities()</td>
				<td>Collection<? extends GrantedAuthority></td>
				<td>계정의 권한 목록</td>
				<td></td>
			</tr>
			<tr>
				<td>getPassword()</td>
				<td>String</td>
				<td>계정의 비밀번호</td>
				<td></td>
			</tr>
			<tr>
				<td>getUsername()</td>
				<td>String</td>
				<td>계정의 고유한 값(ex. DB의 PK값, 중복이 없는 회원id값 등)</td>
				<td></td>
			</tr>
			<tr>
				<td>isAccountNonExpired()</td>
				<td>boolean</td>
				<td>계정의 만료 여부</td>
				<td>true(만료안됨)</td>
			</tr>
			<tr>
				<td>isAccountNonLocked()</td>
				<td>boolean</td>
				<td>계정의 잠김 여부</td>
				<td>true(안잠김)</td>
			</tr>
			<tr>
				<td>isCredentialsNonExpired()</td>
				<td>boolean</td>
				<td>계정의 비밀번호 만료 여부</td>
				<td>true(만료안됨)</td>
			</tr>
			<tr>
				<td>isEnabled()</td>
				<td>boolean</td>
				<td>계정의 활성화 여부</td>
				<td>true(활성화됨)</td>
			</tr>
		</tbody>
	</table>
- 이제 PrincipalDetails가 UserDetails를 상속하여 UserDetails타입이 되었으므로, 이를 Authentication객체에 넣을 수 있게 되었다.
- 그렇다면 이제 SecuritySession에 접근하기 위한 Authentication객체를 생성해보자.

### **4-3-3. UserDetailsService구현 : PrincipalDetailsService implements UserDetailsService**
- Spring Security에서 유저의 정보를 가져오는 인터페이스
- Spring Security에서 유저의 정보를 불러오기 위해서 구현해야하는 인터페이스
- 유저정보가 담긴 UserDetails객체를 SecuritySesssion에 넣기 위해 Authentication객체로 감싸주자 !
```java
@Service
public class PrincipalDetailsService implements UserDetailsService{

	@Autowired
	private UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		System.out.println("username : "+username);
		User userEntity = userRepository.findByUsername(username);
		if(userEntity != null) { 
			return new PrincipalDetails(userEntity); 
		}											 
		return null;
	}
}
```
1. 
	```java
	@Service
	public class PrincipalDetailsService implements UserDetailsService{ }
	```
	- SecurityConfig에서 loginProcessingUrl("/login") 설정은,
		- "/login" 요청이 왔을때 자동으로 UserDetailsService타입으로 IoC되어 있는 loadUserByUsername()함수를 실행시킨다.
		- 자동 IoC를 위해 해당 클래스에는 @Service 어노테이션을 붙여주자.
2. 사용자가 "/login"요청을 하자마자 (1)에서 말한 것처럼 스프링 컨테이너는 UserDetailsService 타입으로 등록된 빈을 찾는다. (그것이 바로 해당 클래스 파일인 PrincipalDetailsService.class !)
3. 
	```java
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException { }
	```
	- UserDetailsService타입의 빈인 PrincipalDetailsService.class를 찾은 후 loadUserByUsername()함수를 실행시켜, 로그인시 사용자가 입력한 username파라미터 값을 가져온다. (이때, loginForm.html에 유저이름을 넣는 input태그의 name속성은 무조건 "username"으로 오탈자 없이 적어줘야한다.)
4. 
	```java
	// UserRepository.class
	public interface UserRepository extends JpaRepository<User, Integer>{
		// Jpa Query methods
		// select * from user where username = ?
		public User findByUsername(String username); 
	}
	


	// PrincipalDetailsService.class
	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User userEntity = userRepository.findByUsername(username);
		return null;
	}
	```
	- loadUserByUsername()에서는 가져온 username파라미터를 이용하여, 로그인을 시도한 사용자가 DB에 저장되어 있는 회원인지를 검증한다.
5. 
	```java
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User userEntity = userRepository.findByUsername(username);
		if(userEntity != null) { 
			return new PrincipalDetails(userEntity); 
		}											 
		return null;
	}
	```
	- 가입되어 있는 유저라면 (userEntity != null), 해당 유저정보를 앞서 구현해둔 UserDetails타입의 PrincipalDetails객체에 담아 UserDetails타입의 객체를 생성하여 반환한다.(```return new PrincipalDetails(userEntity)```)
		- **Q. 이때, 반환된 new PrincipalDetails(userEntity) 는 어디로 반환되는건가 ?**
			- **A.** Authentication객체 내부로 자동 반환된다. 그리고 로그인한 유저정보를 품게 된 Authentication 객체는 SecuritySession안으로 들어가게 된다.
			- 이러한 일련의 활동들은 전부 loadUserByUsername() 메소드가 자동으로 알아서 다 해줌.
			- 이로써 로그인 완료 - !

---
### *ref*
- [SpringSecurity UserDetails, UserDetailsService란?](https://programmer93.tistory.com/68)
- [SpringSecurity 주요 아키텍처 이해](https://catsbi.oopy.io/f9b0d83c-4775-47da-9c81-2261851fe0d0)
---