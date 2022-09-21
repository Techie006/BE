package com.sparta.cookbank.controller.stomp;

import com.sparta.cookbank.domain.chat.ChatMessage;
import com.sparta.cookbank.repository.ChatRoomRepository;
import com.sparta.cookbank.security.SecurityUtil;
import com.sparta.cookbank.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class ChatController {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatService chatService;

    /**
     * websocket "/pub/chat/message"로 들어오는 메시징을 처리한다.
     */
    @MessageMapping("/chat")
    public void message(ChatMessage message) {
        // Websocket에 발행된 메시지를 redis로 발행(publish)
        chatService.sendChatMessage(message);
    }
}
