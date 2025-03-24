package com.personal_baseball.user.repository;

import com.personal_baseball.domain.RefreshToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {

    Optional<RefreshToken> findByUserId(Long userId);

    @Transactional
    void deleteByUserId(Long userId);

}
