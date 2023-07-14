package test.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import test.security.model.User;

// CRUD 함수를 JpaRepository가 들고 있음.
// @Repository라는 어노테이션이 없어도 IoC됨(컨테이너에 빈으로 등록됨).이유는 JpaRepository를 상속했기 때문에
public interface UserRepository extends JpaRepository<User, Integer>{

}
