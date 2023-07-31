# 12강. JWT를 이해하기전 세션에 대해 알아보자
## 12-1. 세션이란

<img src="./img/chapter12_1.png">

1. 웹 브라우저가 서버에게 <U>**최초 요청**</U>을 수행한다. (www.naver.com)
2. 요청을 받은 서버는 해당 주소에 맞는 컨트롤러의 메서드를 찾아서 www.naver.com에 해당하는 .html파일을 리턴한다.
3. 이때 서버는 응답으로 보내는 .html파일에다가 Http Header를 함께 달아서 전송한다.
    - 3-1. Http Header에는 Cookie라는 게 담겨있는데, Cookie에는 (Java일 경우)세션ID가 포함되어 있다. 
4. 웹 브라우저는 Http Header에서 받아온 세션ID정보를 웹 브라우저의 Cookie영역에다 저장해둔다.