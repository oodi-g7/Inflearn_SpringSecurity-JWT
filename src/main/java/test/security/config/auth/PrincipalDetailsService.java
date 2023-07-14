package test.security.config.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import test.security.model.User;
import test.security.repository.UserRepository;
import org.springframework.stereotype.Service;

// 시큐리티 설정에서 loginProcessingUrl("/login");
// "/login" 요청이 오면 자동으로 UserDetailsService 타입으로 IoC되어 있는 loadUserByUsername 함수가 실행
@Service
public class PrincipalDetailsService implements UserDetailsService{
	// loginForm.html에서 사용자가 이름과 패스워드를 입력 후 로그인 버튼을 누르면, Form태그 내 action에서 "/login" url이 요청됨.
	// 앞서 시큐리티 설정에서 loginProcessingUrl("/login") 해줬으므로, 스프링 컨테이너는 "/login"요청이 들어오자마자 UserDetailsService 타입으로 등록된 빈을 찾음
	// 그럼 그게 바로 해당 클래스 파일인 PrincipalDetailsService !!
	// PrincipalDetailsService를 찾으면 바로 오버라이드 되어 있는 loadUserByUsername 함수를 호출함. 이때, 로그인시 사용자가 입력한 username파라미터 값을 가져옴.
	// 그러므로 loginForm.html에 유저이름을 넣는 input태그의 name속성을 오탈자없이 잘 적어야함.(username2, userName, ... 모두 X. 무조건ㅇ username으로!!!)
	
	@Autowired
	private UserRepository userRepository;
	
	// 시큐리티session > Authentication > UserDetails
	// 해당메소드에서 반환하는 UserDetails객체는 Authentication객체 안으로 들어갈 예정임!
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// 여기선 우선 넘어온 유저이름을 가지고 해당 유저가 현재 저장되어 있는 유저인지 확인!
		System.out.println("username : "+username);
		User userEntity = userRepository.findByUsername(username);
		if(userEntity != null) { // 가입되어 있는 유저라면,
			return new PrincipalDetails(userEntity);
		}
		return null;
	}

}
