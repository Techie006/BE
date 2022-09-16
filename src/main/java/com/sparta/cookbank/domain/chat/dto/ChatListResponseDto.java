package com.sparta.cookbank.domain.chat.dto;

import com.sparta.cookbank.domain.chat.Chat;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatListResponseDto {
    private Long chat_id;
    private String username;
    private String profile_img;
    private String message;

    public ChatListResponseDto(Chat chat){
        this.chat_id = chat.getId();
        this.username = chat.getMember().getUsername();
        this.profile_img = chat.getMember().getImage();
        this.message = chat.getContent();
    }
}
