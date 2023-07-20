package test.security.config.auth;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import test.security.model.User;

// 시큐리티는 이제 /login 주소 요청이 오면 이를 낚아채서 로그인을 진행시킴 (SecurityConfig.java에서 loginProcessingUrl("/login") 설정)
// 이때 로그인이 완료되면 시큐리티 session을 만들어 여기에 로그인정보를 넣어줌 
// 시큐리티가 갖고 있는 session은 'Security ContextHolder'를 '키'로 가지고, '세션정보'를 '값'으로 가짐
// 시큐리티 세션에 들어갈 수 있는 오브젝트는 "Authentication 타입의 객체" !!
// 우리가 로그인을 수행하기 위해선 Authentication 안에 User정보가 있어야 함.
// 이때 유저정보, 즉 User오브젝트는 UserDetails 타입 객체로 저장되어 있어야 한다.

// Security Session 영역 
// => 들어갈 수 있는 객체는 Authentication 타입 객체
// => Authentication 객체 안에 유저정보를 저장할때, 유저정보는 UserDetails 타입이어야만 함

// 즉, 유저정보는 UserDetails 타입으로 감싸져서 Authentication타입 객체에 저장되어 Security Session영역에 존재
// : Security Session => Authentication => UserDetails => 유저정보 

// 이를 꺼낼때에는 Security session에 있는 객체를 .get()해서 Authentication객체를 꺼내고, 그 속에 UserDetails객체를 꺼내어 유저정보에 접근
// 그렇다면 UserDetails객체에서 어떻게 유저정보를 꺼낼까?
// => PrincipalDetails가 UserDetails를 상속받게하여 UserDetails타입이 되면, 이를 이용해 유저정보를 꺼낸다.
// : Security Session => Authentication => UserDetails(PrincipalDetails) => 유저정보
// 그럼 이제 PrincipalDetails객체를 Authentication객체에 넣을 수 있게 됨

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
	// User권한은 String타입인데 해당 메소드의 리턴값은 Collection<GrantedAuthority>이므로 Collection에 감싸서 유저권한정보 넘겨야함
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

	// 해당 계정이 활성화되어있는지 여부 (활성화O : true | 활성화X : false)
	@Override
	public boolean isEnabled() {
		// 우리 사이트에서 1년동안 회원이 로그인을 안해서 휴면 계정으로 변환되었다면 그때 false값 반환
		// 즉, 현재시간 - 마지막 로그인 날짜 => 1년을 초과하면 return false;
		return true;
	}
}
