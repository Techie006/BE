package com.sparta.cookbank.domain.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    public void changeProfileImage(String profileImg) {
        this.image = profileImg;
    }

    public void setPassword(String password){
        this.password = password;
    }
}
