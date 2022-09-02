package com.sparta.cookbank.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GoogleUserInfoDto {
    private String id;
    private String name;
    private String email;
}

