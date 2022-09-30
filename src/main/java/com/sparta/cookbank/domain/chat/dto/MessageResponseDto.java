package com.sparta.cookbank.domain.chat.dto;

import com.sparta.cookbank.domain.chat.ChatMessage;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class MessageResponseDto {
    private String session_id;
    private String token;
    private String full_token;
    List<ChatMessage> chats;
}
