# 6강. 구글 로그인 준비
## 6-1. 구글 API 콘솔에 접속 (https://console.cloud.google.com/)

## 6-2. 새 프로젝트 추가하기    
<img src="./img/sec1-1.png">

- 새로 추가한 프로젝트를 이용하여 우리가 만든 사이트에 구글 로그인을 추가할 예정!

## 6-3. OAuth 동의화면 생성하기
<img src="./img/sec1-2.png">

1. '외부' 선택 후 '만들기' 클릭
2. 다음 정보입력 후 '저장'
    - (2-1) ✅ 앱 이름
    - (2-2) ✅ 사용자 지원 이메일
    - (2-3) ✅ 개발자 이메일 주소
3. OAuth 동의화면 생성완료

## 6-4. 사용자 인증 정보 만들기
<img src="./img/sec1-3.png">

1. '사용자 인증 정보 만들기' -> 'OAuth 클라이언트 ID' 선택
2. 다음 정보 입력후 '만들기' -> 사용자 인증정보 생성완료
    - (2-1) ✅ 애플리케이션 유형 : 웹 애플리케이션
    - (2-2) ✅ 이름 : springboot-oauth (마음대로 작성가능)
    - (2-3) ✅ 승인된 리디렉션 URI : http://localhost:8080/login/oauth2/code/google
        - **리디렉션 URI란?**
        1. 특정 사용자가 우리 사이트에서 구글 로그인을 하고나면 구글 서버에서 인증이 되었다는 코드를 우리에게 보내줌. 
        2. 우리는 해당 코드를 받아서 해당 코드로 Access Token을 요청함. 
        3. Access Token을 받고 나면, 우리 서버가 사용자 대신 구글 서버에 있는 사용자의 개인정보등에 접근할 수 있는 권한을 갖게 됨.
        4. Access Token을 얻기 위해 필요한 것이 (1)에서 언급한 **인증코드** 이며, 리디렉션 URI에는 해당 코드를 받을 수 있는 주소를 입력해주면 된다.
        5. 만약 구글 로그인을 전부 다 직접 구현할 것이라면 해당 URI를 자유롭게 작성해도 괜찮지만, <U>**우리는 OAuth2-Client라는 라이브러리를 사용할 것이므로 해당 주소로 고정임. 다른 주소로는 사용불가 !**</U>
3. 생성된 클라이언트 ID와 클라이언트 보안 비밀번호 따로 저장해두기. 유출안되도록 조심 !
    <img src="./img/sec1-4.png">
---
#### ※ [참고] OAuth2-Client 라이브러리 ※
- 리디렉션 URI 작성규칙 : 도메인주소 + /login/oauth2/code/[google | facebook | ···]
    - 예시) http://localhost:8080/login/oauth2/code/google
- 리디렉션 URI와 매칭하는 컨트롤러 필요없음
    - 예시) 리디렉션 URI = http://localhost:8080/login/oauth2/code/google 일때,
        ```java
        @GetMapping("/login/oauth2/code/google")
        public String login_googleOauth(){
            return "";
        }
        ```
        와 같은 함수 필요없음. (OAuth Client 라이브러리가 자동으로 알아서 다 처리해주기 때문)

---
## 6-5. OAuth2-Client 라이브러리 설정
1. pom.xml에 dependency추가하기
    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-client</artifactId>
    </dependency>
    ```
2. application.yml에 라이브러리 설정정보 추가
    ```yml
    spring:
      security:
        oauth2:
          client:
            registration:
              google:
                client-id: (6-4)에서 발급받은 클라이언트 ID입력
                client-secret: (6-4)에서 발급받은 클라이언트 보안 비밀번호입력
                scope:
                - email
                - profile
    ```
3. loginForm.html에 버튼 추가하기
    ```html
    <body>
    <h1>로그인 페이지</h1>
    <hr/>
    <form action="/login" method="POST">
        <input type="text" name="username" placeholder="Username" /><br />
        <input type="password" name="password" placeholder="Password" /><br />
        <button>로그인</button>
    </form>
    <a href="/oauth2/authorization/google">구글 로그인</a>
    <a href="/joinForm">회원가입을 아직 하지 않으셨나요?</a>
    </body>
    ```
    - '구글 로그인'이라는 버튼 추가
        - oauth2-client 라이브러리 사용시 "/oauth2/authorization/google" 라는 주소로 입력 (변경불가)






# 7강. 구글 회원 프로필 정보 받아보기
# 8강. Authentication객체가 가질 수 있는 2가지 타입
# 9강. 구글 로그인 및 자동 회원가입 진행 완료
# 10강. 페이스북 로그인 완료
# 11강. 네이버 로그인 완료