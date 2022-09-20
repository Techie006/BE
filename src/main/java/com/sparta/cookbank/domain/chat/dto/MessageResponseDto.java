package com.sparta.cookbank.domain.chat.dto;

import com.sparta.cookbank.domain.chat.ChatMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MessageResponseDto {
    List<ChatMessage> chats;
    public MessageResponseDto(List<ChatMessage> list){
        this.chats = list;
    }
}
