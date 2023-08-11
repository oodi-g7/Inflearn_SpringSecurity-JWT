# 19강. JWT를 위한 yml파일 세팅  
## 19-1. application.yml 설정
    ```
    server:
    port: 8080
    servlet:
        context-path: /
        encoding:
        charset: UTF-8
        enabled: true
        force: true
        
    spring:
    datasource:
        url: jdbc:mysql://localhost:3306/security?serverTimezone=Asia/Seoul
        driver-class-name: com.mysql.cj.jdbc.Driver
        username: cos
        password: cos1234
    h2:
        console:
        enabled: true


    jpa:
        hibernate:
        ddl-auto: create #create update none
        naming:
            physical-strategy: org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
        show-sql: true
    ```