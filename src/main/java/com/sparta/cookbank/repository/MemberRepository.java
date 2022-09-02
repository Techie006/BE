package com.sparta.cookbank.repository;

import com.sparta.cookbank.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByKakaoId(String kakaoId);
    Boolean existsByEmail(String email);

    Optional<Member> findByGoogleId(String googleId);
}