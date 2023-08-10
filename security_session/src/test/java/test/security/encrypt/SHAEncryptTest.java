package test.security.encrypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SHAEncryptTest {

	@Test
	@DisplayName("SHA-256을 이용한 비밀번호 암호화 테스트")
	void sha_encrypt() {
		SHAEncryptTest en = new SHAEncryptTest();
		
		String pwd = "tistory";
		System.out.println("pwd : "+ pwd);
		
		// Salt 생성
	    // 현재 랜덤으로 Salt값을 생성하였지만, 실제 구현시 고정시키거나 Salt값을 저장해 두어야함
		// => 사용자별 salt값 다르게, 사용자마다 db에 salt값 저장해주기
		String salt = en.getSalt();
		System.out.println("salt : "+salt);
		
		//최종 비밀번호 생성
		String res = en.getEncrypt(pwd, salt);
	}
	
	public String getSalt() {
		
		//1. Random, byte 객체 생성
		SecureRandom  r = new SecureRandom ();
		byte[] salt = new byte[20];
		
		//2. 난수 생성
		r.nextBytes(salt);
		
		//3. byte To String (10진수의 문자열로 변경)
		StringBuffer sb = new StringBuffer();
		for(byte b : salt) {
			sb.append(String.format("%02x", b));
		};
		
		return sb.toString();
	}

	public String getEncrypt(String pwd, String salt) {
		
		String result = "";
		try {
			//1. SHA256 알고리즘 객체 생성
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			
			//2. 비밀번호와 salt 합친 문자열에 SHA 256 적용
			System.out.println("비밀번호 + salt 적용 전 : " + pwd+salt);
			md.update((pwd+salt).getBytes());
			byte[] pwdsalt = md.digest();
			
			//3. byte To String (10진수의 문자열로 변경)
			StringBuffer sb = new StringBuffer();
			for (byte b : pwdsalt) {
				sb.append(String.format("%02x", b));
			}
			
			result=sb.toString();
			System.out.println("비밀번호 + salt 적용 후 : " + result);
			
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return result;
	}
}
