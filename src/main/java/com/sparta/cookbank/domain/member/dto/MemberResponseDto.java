package com.sparta.cookbank.domain.member.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberResponseDto {
    private Long member_id;
    private String username;
    private String profile_img;

}
