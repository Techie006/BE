package com.sparta.cookbank.domain.chat.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatResponseDto {
    private Long viewer_num;
    private Long member_id;
    private String nickname;
    private String profile_img;
    private String message;

    public ChatResponseDto(ChatDto chatDto, Long viewers){
        this.member_id = chatDto.getMember_id();
        this.nickname = chatDto.getNickname();
        this.profile_img = chatDto.getProfile_img();
        this.message = chatDto.getMessage();;
        this.viewer_num = viewers;
    }
}
