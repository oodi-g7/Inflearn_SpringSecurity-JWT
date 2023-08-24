# 25강. JWT를 위한 강제 로그인 진행
## 25-1. JwtAuthenticationFilter - attemptAuthentication() 함수 구현하기
### 25-1-1. 로그인 요청을 통해 username과 password 가져오기
- 코드
```java
@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		System.out.println("JwtAuthenticationFilter : 로그인 시도중");
		
		// username, password 받아오기
		try {
			BufferedReader br = request.getReader();
			
			String input = null;
			while((input = br.readLine()) != null) {
				System.out.println(input);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("=================================");
		return super.attemptAuthentication(request, response);
	}
```
- 로그인 요청 테스트
    1. form-unlencoded방식(웹)으로 로그인 정보 담아 요청하기
        <img src="./img/chapter25_1.png">

        <img src="./img/chapter25_2.png">

    2. JSON방식으로 로그인 정보 담아 요청하기
        <img src="./img/chapter25_3.png">

        <img src="./img/chapter25_4.png">

1. 포스트맨으로 로그인 요청을 해보면 콘솔에 로그인 정보가 정상적으로 출력되는 것을 확인할 수 있다.
2. 그런데 로그인 방식은 매우 다양하므로(웹, 안드로이드, 리액트, ...) request에서 받아온 로그인 데이터 형태가 제각각일 수 있다. (위 사진 참조)
3. 따라서 우리는 각기 다른 형태의 로그인 데이터를 가공할 수 있도록 파싱하는 과정이 필요하다.


