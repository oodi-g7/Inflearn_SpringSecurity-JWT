package com.cos.jwt.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Data
@Entity
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // => Auto Increment
	private long id;
	private String username;
	private String password;
	private String roles; // USER, ADMIN
	
	// Role이 2개이상일 경우 이런식으로 처리하는 것도 하나의 방법
	public List<String> getRoleList(){
		if(this.roles.length() > 0) {
			// Arrays 클래스 => 배열을 다루기 위한 다양한 메소드가 포함
			// asList() 메소드 => 전달받은 배열을 고정 크기의 리스트로 변환하여 반환.
			// http://www.tcpschool.com/java/java_api_arrays
			return Arrays.asList(this.roles.split(","));
			
		}
		return new ArrayList<>();
	}
}
