# 3강. 시큐리티 회원가입
## 3-1. model생성 : User
```java
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.CreationTimestamp;

import lombok.Data;

@Entity
@Data
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // 자동생성, auto_increment
	private int id;
	private String username;
	private String password;
	private String email;
	private String role; // ROLE_USER, ROLE_ADMIN
	@CreationTimestamp // 자동생성
	private Timestamp createDate;
}
```
- @Entity를 붙여주면, 프로젝트와 연결된 DB에 User라는 이름의 위 필드들을 컬럼으로 가진 테이블이 생성됨.

## 3-2. 로그인 및 회원가입 페이지 생성
- IndexController의 "/loginForm"과 "/joinForm" 리턴값인 로그인, 회원가입 페이지를 생성한다.
```html
</head>
<body>
<h1>로그인 페이지</h1>
<hr/>
<form>
	<input type="text" name="username" placeholder="Username" /><br />
	<input type="password" name="password" placeholder="Password" /><br />
	<button>로그인</button>
</form>
<a href="/joinForm">회원가입을 아직 하지 않으셨나요?</a>
</body>
```
```html
<body>
<h1>회원가입 페이지</h1>
<hr/>
<form action="/join" method="POST">
	<input type="text" name="username" placeholder="Username" /><br />
	<input type="password" name="password" placeholder="Password" /><br />
	<input type="email" name="email" placeholder="Email" /><br />
	<button>회원가입</button>
</form>
</body>
```
- **로그인 및 회원가입 시, input박스의 name속성들은 아이디의 경우 username, 비밀번호의 경우 password를 입력해주어야 한다.**
- 회원가입 경로는 아래와 같다.
	- "/loginForm" 로그인페이지로 이동 
	- → 회원가입을 아직 하지 않으셨나요? 링크 클릭 
	- → "/joinForm" 회원가입페이지로 이동
	- → 회원가입정보입력(이름, 비밀번호, 이메일) 
	- → "/join" 회원가입진행

## 3-3. 시큐리티 회원가입 및 비밀번호 암호화
#### 3-3-1. 회원가입
- 이제 사용자가 joinForm에서 입력한 회원가입정보를 이용하여 회원가입을 진행한다.
```java
// UserRepository

public interface UserRepository extends JpaRepository<User, Integer>{

}
```
- JpaRepository : 기본적인 CRUD함수를 JpaRepository가 갖고 있다.
	- JpaRepository에다 엔티티클래스와 해당 엔티티의 @Id(PrimaryKey) 타입을 넘겨준다. → <User, Integer>
	- JpaRepository를 상속하면 @Repository라는 어노테이션이 없어도 스프링 컨테이너에 빈으로 등록된다.(IoC)
```java
// IndexController

@Controller
public class IndexController {
	
	@Autowired
	private UserRepository userRepository;

	@PostMapping("/join")
	public String join(User user) {
		user.setRole("ROLE_USER");
		userRepository.save(user);
		
		return "join";
	}
}
```
- **user.setRole("ROLE_USER");**
	- 회원가입정보로 넣어준 이름, 비밀번호, 이메일을 제외하면 회원id, 회원role, createDate 필드가 null값이다.
	- 하지만, id같은 경우 @GeneratedValue 어노테이션을 이용하여 auto_increment 설정을 해주었고,
	- createDate는 @CreationTimestamp 어노테이션을 통해 자동생성되게끔 설정해주었으므로
	- 회원role값만 추가적으로 설정해준다. 이를 통해 우선 회원가입하는 모든 사용자는 ROLE_USER권한을 갖게된다.
- **userRepository.save(user);**
	- 간단하게 회원가입 로직이 완성되었다. 하지만 해당 정보를 가지고 시큐리티 로그인은 불가능하다.
	- 그 이유는 회원가입시 비밀번호를 1234로 등록했을때, 이를 <U>**암호화하지않고 DB에 그대로 저장(1234)**</U>했기 때문.
	- 패스워드 암호화를 해주어야 시큐리티 로그인이 가능하다.

#### 3-3-2. 비밀번호 암호화
- SecurityConfig에서 비밀번호 암호화 로직을 빈으로 등록해준다.
```java
// SecurityConfig

@Configuration
@EnableWebSecurity
public class SecurityConfig{
	
	@Bean 
	public BCryptPasswordEncoder encodePwd() {
		return new BCryptPasswordEncoder();
	}
}
```
- 컨트롤러에 암호화 로직을 추가해준다.
```java
// IndexController

@Controller
public class IndexController {
	
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BCryptPasswordEncoder bcryptPasswordEncoder;

	@PostMapping("/join")
	public String join(User user) {
		user.setRole("ROLE_USER");

		// 비밀번호 암호화
		String rawPassword = user.getPassword();
		String encPassword = bcryptPasswordEncoder.encode(rawPassword);
		user.setPassword(encPassword);

		userRepository.save(user);
		
		return "redirect:/loginForm"; // redirect를 붙여주면 /loginForm URL에 해당하는 함수를 호출. loginForm()
	}
}
```
- **user.setPassword(encPassword);** : 유저에 인코딩된 패스워드를 넣어준 후, .save()하여 회원가입을 진행한다.
- **return "redirect:/loginForm";** : 회원가입을 완료한 사용자는 redirect를 이용하여 /loginForm URL로 보내준다.