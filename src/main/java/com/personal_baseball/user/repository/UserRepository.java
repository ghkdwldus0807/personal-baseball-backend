package com.personal_baseball.user.repository;


import com.personal_baseball.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByUserId(Long userId);

    Optional<User> findByEmailAndPlatformType(String email, String platformType);

}

