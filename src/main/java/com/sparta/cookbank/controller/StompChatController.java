package com.sparta.cookbank.controller;

import com.sparta.cookbank.domain.chat.dto.ChatDto;
import com.sparta.cookbank.domain.chat.dto.ChatResponseDto;
import com.sparta.cookbank.domain.chat.dto.ChatTest;
import com.sparta.cookbank.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class StompChatController {

    private final SimpMessagingTemplate template; //특정 Broker로 메세지를 전달
    private final ChatService chatService;

    //"/pub/chat/enter"
    //메세지 받았을 때
    @MessageMapping(value = "/chat/room/{roomId}")
    public void message(@DestinationVariable String class_id, ChatTest message) {
        //채팅 저장
        //ChatResponseDto responseDto = chatService.saveChat(Long.parseLong(class_id), message);
        log.info("pub success " + message.getMessage());
        //채팅방에 정보 전달
        template.convertAndSend("/sub/chat/room/" + class_id, message);
    }

}
