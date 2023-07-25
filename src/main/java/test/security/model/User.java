package test.security.model;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.CreationTimestamp;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // 자동생성, auto_increment
	private int id;
	private String username;
	private String password;
	private String email;
	private String role; // ROLE_USER, ROLE_MANAGER, ROLE_ADMIN
	
	private String provider;
	private String providerId;

	@CreationTimestamp // 자동생성
	private Timestamp createDate;

	
	// OAuth로그인 사용자 회원가입
	@Builder
	public User(String username, String password, String email, String role, String provider, String providerId, Timestamp createDate) {
		this.username = username;
		this.password = password;
		this.email = email;
		this.role = role;
		this.provider = provider;
		this.providerId = providerId;
		this.createDate = createDate;
	}

}
