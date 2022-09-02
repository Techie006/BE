package com.sparta.cookbank.domain.member;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column
    private String kakaoId;

    @Column
    private String googleId;

    @Column(nullable = false)
    private boolean mail_auth;

    @Column
    private String mail_key;

    public void EmailCheck(){
        this.mail_auth = true;
    }

}
