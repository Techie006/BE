package com.sparta.cookbank.domain.RefreshToken;

import com.sparta.cookbank.domain.Member.Member;
import lombok.*;

import javax.persistence.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String tokenValue;

    public RefreshToken(Member member) {
        this.member = member;
    }

    public void updateTokenValue(String refreshToken) {
        this.tokenValue = refreshToken;
    }
}