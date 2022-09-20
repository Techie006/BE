package com.sparta.cookbank.domain.member;

import com.sparta.cookbank.domain.member.dto.ChangePasswordRequestDto;
import lombok.*;
import org.springframework.security.core.userdetails.UserDetails;

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

    @Column(nullable = false)
    private String image;

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

    public void changePassword(String chagePassword) {
        this.password = chagePassword;
    }
}
