package test.security.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import test.security.config.auth.PrincipalDetails;
import test.security.model.User;
import test.security.repository.UserRepository;

@Controller
public class IndexController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bcryptPasswordEncoder;
	
	@GetMapping("/test/login")
	public @ResponseBody String testLogin(
			Authentication authentication, // DI (의존성주입)
			@AuthenticationPrincipal PrincipalDetails userDetails) { 
		System.out.println("============ /test/login ============");
		PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal(); // 다운캐스팅
		System.out.println("authentication : " + principalDetails.getUser());
		
		System.out.println("userDetails : " + userDetails.getUser());
		
		return "세션 정보 확인하기";
	}
	
	@GetMapping("/test/oauth/login")
	public @ResponseBody String testOAuthLogin(
			Authentication authentication, // DI (의존성주입)
			@AuthenticationPrincipal OAuth2User oauth) {
		System.out.println("============ /test/oauth/login ============");
		OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal(); // 다운캐스팅
		System.out.println("authentication : " + oauth2User.getAttributes());
		
		System.out.println("oauth2User : " + oauth.getAttributes());
		
		return "OAuth 세션 정보 확인하기";
	}
	
	// localhost:8080/
	// localhost:8080
	@GetMapping({"","/"})
	public String index() {
		// 머스테치 기본폴더 src/main/resources/
		// 뷰리졸버 설정 : templates (prefix), .mustache (suffix) 생략가능!!
		return "index"; // src/main/resources/templates/index.mustache
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
	
	// 해당주소는 스프링 시큐리티가 낚아채감 - SecurityConfig 파일 생성 후 더이상 작동하지 않음. why?
	//								그렇다보니 로그인을 하지 않은 상태에서 인증 또는 권한이 필요한 페이지 접근시, 로그인 페이지로 이동하는 게 아니라 404 화면이 뜸.
	@GetMapping("/loginForm")
	public String loginForm() {
		return "loginForm";
	}
	
	@GetMapping("/joinForm")
	public String joinForm() {
		return "joinForm";
	}
	
	@PostMapping("/join")
	public String join(User user) {
		System.out.println(user.toString());
		user.setRole("ROLE_USER");
		// 비밀번호 암호화
		String rawPassword = user.getPassword();
		String encPassword = bcryptPasswordEncoder.encode(rawPassword);
		user.setPassword(encPassword);
		userRepository.save(user); // 회원가입 잘됨. 비밀번호 : 1234 => 시큐리티로 로그인할 수 없음. 이유는 패스워드가 암호화 안되었기 때문!!
		
		return "redirect:/loginForm"; // redirect를 붙여주면 /loginForm URL에 해당하는 함수를 호출. loginForm()
	}
	
	@Secured("ROLE_ADMIN") // admin계정만 "/info" 매핑주소에 접근가능
	@GetMapping("/info")
	public @ResponseBody String info() {
		return "개인정보";
	}
	
//	@PostAuthorize // data() 메소드가 실행된 후 실행됨. 잘 사용하지는 않음
	@PreAuthorize("hasRole('ROLE_MANAGER') " // data() 메소드가 실행되기 직전에 실행됨. 해당 메소드에 접근권한을 2개이상 설정시 사용. 
			+ "or hasRole('ROLE_ADMIN')") // 1개만 설정할때는 @secured 사용하고, 2개이상이면 hasrole() or hasrole() ... 사용이 가능한 @preAuthorize() 사용
	@GetMapping("/data")
	public @ResponseBody String data() {
		return "데이터정보";
	}
}
