package com.sparta.cookbank.repository;

import com.sparta.cookbank.domain.Member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.stream.DoubleStream;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByKakaoId(String kakaoId);
    Boolean existsByEmail(String email);
}