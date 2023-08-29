# 26강. JWT토큰 만들어서 응답하기
## 26-1. successfulAuthentication() 함수 구현하기
- successfulAuthentication 함수
    1. JwtAuthenticationFilter에서 "/login"요청이 오면 가장 먼저 실행되는 함수 → attemptAuthentication
    2. attemptAuthentication함수에서 로그인 요청에 대한 인증이 완료되면 successfulAuthentication함수가 실행된다.
    3. 인증이 완료된 로그인 요청에 대해 JWT토큰을 응답해주어야 하므로, successfulAuthentication함수에서 JWT토큰을 만들어본다.

### 26-1-1. JWT 토큰 만들기
```java
@Override
protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
        Authentication authResult) throws IOException, ServletException {
    PrincipalDetails principalDetails = (PrincipalDetails)authResult.getPrincipal();
    
    String jwtToken = JWT.create() // builder패턴
            .withSubject("cos토큰") // 토큰 이름
            .withExpiresAt(new Date(System.currentTimeMillis()+(60000*10))) // 만료시간
            .withClaim("id", principalDetails.getUser().getId()) 
            .withClaim("username", principalDetails.getUser().getUsername())
            .sign(Algorithm.HMAC512("cos")); 
    
    response.addHeader("Authorization", "Bearer " + jwtToken);
}
```
1. pom.xml내 dependency되어 있는 java-jwt 라이브러리를 이용하여 JWT토큰을 생성한다.(Builder패턴)
2. withSubject : 토큰 이름
3. withExpiresAt : 토큰 만료시간 (토큰이 언제까지 유효할지 지정)
    - System.currentTimeMillis() = 현재시간
    - 60000 * 10 = 10분
        - 1000 = 1초
        - 10000 = 10초
        - 60000 = 60초(1분)
        - 60000 * 10 = 10분
    - ※ 토큰 만료시간은 최대한 짧게 지정하는 것을 추천
4. withClaim : 비공개 claim. 토큰에 넣고 싶은 key ↔ value 값을 자유롭게 작성할 수 있다. (정해진 규칙 없음)
5. sign : 내 서버만 아는 고유한 값(SecretKey)
    - HMAC512 : RSA방식 아니고 Hash암호방식. 특징은 내 서버만 아는 Secret값이 필수
6. 응답헤더에 Key = Authorization, Value = Bearer + jwtToken 담아주기
    - value값에 Bearer방식임을 명시하고, **"한 칸 띄운 후"** jwtToken을 담아 주기

### 26-1-2. 포스트맨으로 응답 확인해보기
<img src="./img/chapter26_1.png">

- 받아온 JWT 토큰
    ```
    Bearer 
    eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJjb3PthqDtgbAiLCJpZCI6MSwiZXhwIjoxNjkzMTI4MDk0LCJ1c2VybmFtZSI6InNzYXIifQ.geTrK4jivRJmEGq1DL5LKoRqMdUXSTh4BknyGhzV8BPVvJbrTZYkp2IGq9KrFTKiTy2C0TSTmYvKrI8ATkDkPA
    ```
    1. 로그인에 성공한 사용자는 응답헤더에 담긴 JWT토큰을 이용해서 인증정보가 필요한 요청을 할 수 있다. ( → request에다 jwt를 함께 전송)
    2. 우리는 사용자가 보내는 JWT토큰을 처리하는 필터를 구현해주어야 한다.

### 26-1-3. To do
1. 사용자의 로그인 요청
2. username, password 정상 (인증완료)
3. 서버는 JWT토큰을 생성 및 사용자에게 응답
4. 이후 사용자는 요청할때마다 JWT토큰을 가지고 요청
5. **서버는 JWT토큰이 유효한지를 판단 → 이제 우리는 JWT토큰이 유효한지를 판단할 수 있는 필터를 추가로 만들어주어야 함.**