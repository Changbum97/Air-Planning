package com.example.airplanning.repository;

import com.example.airplanning.domain.entity.User;
import com.example.airplanning.domain.enum_class.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUserName(String userName);
    Optional<User> findByNickname(String nickName);
    Optional<User> findByEmail(String email);
    boolean existsByUserNameAndEmail(String userName, String email);
    boolean existsByUserName(String userName);
    boolean existsByNickname(String nickname);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    List<User> findAllByRole(UserRole role);
}
