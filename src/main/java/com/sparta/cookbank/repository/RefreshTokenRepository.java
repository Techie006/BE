package com.sparta.cookbank.repository;

import com.sparta.cookbank.domain.Member.Member;
import com.sparta.cookbank.domain.RefreshToken.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByMember(Member member);

    void deleteByMember(Member member);

    Optional<RefreshToken> findByTokenValue(String refreshToken);
}
