package com.sparta.cookbank.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.cookbank.domain.chat.ChatMessage;
import com.sparta.cookbank.domain.member.Member;
import com.sparta.cookbank.repository.ChatRoomRepository;
import com.sparta.cookbank.repository.MemberRepository;
import com.sparta.cookbank.security.SecurityUtil;
import com.sparta.cookbank.security.TokenProvider;
import com.sparta.cookbank.service.ChatService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor {

    private final TokenProvider tokenProvider;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatService chatService;

    // websocket을 통해 들어온 요청이 처리 되기전 실행된다.
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.SUBSCRIBE == accessor.getCommand()) { // 채팅룸 구독요청
            //토큰 까기
            Long memberId = -1L;
            if(accessor.getNativeHeader("Authorization")!=null){
                String token = accessor.getNativeHeader("Authorization").get(0);
                log.info("TOKEN {}",token);
                if(token != null) token = token.substring(7);
                memberId = tokenProvider.getMemberId(token);
            }
            // header정보에서 구독 destination정보를 얻고, roomId를 추출한다.
            String roomId = chatService.getRoomId(Optional.ofNullable((String) message.getHeaders().get("simpDestination")).orElse("InvalidRoomId"));
            // 채팅방에 들어온 클라이언트 sessionId를 roomId와 맵핑해 놓는다.(나중에 특정 세션이 어떤 채팅방에 들어가 있는지 알기 위함)
            String sessionId = (String) message.getHeaders().get("simpSessionId");
            chatRoomRepository.setUserEnterInfo(sessionId, roomId);
            // 채팅방의 인원수를 +1한다.
            chatRoomRepository.plusUserCount(roomId);
            chatService.PlusMinusViewrs(roomId,1L);
            // 클라이언트 입장 메시지를 채팅방에 발송한다.(redis publish)
            chatService.sendChatMessage(ChatMessage.builder().type(ChatMessage.MessageType.ENTER).redis_class_id(roomId).member_id(memberId).build());
            log.info("SUBSCRIBED {}, {}, {}", sessionId, memberId, roomId);
        } else if (StompCommand.DISCONNECT == accessor.getCommand()) { // Websocket 연결 종료
            //토큰 까기
            Long memberId = -1L;
            if(accessor.getNativeHeader("Authorization")!=null){
                String token = accessor.getNativeHeader("Authorization").get(0);
                log.info("TOKEN {}",token);
                if(token != null) token = token.substring(7);
                memberId = tokenProvider.getMemberId(token);
            }
            // 연결이 종료된 클라이언트 sesssionId로 채팅방 id를 얻는다.
            String sessionId = (String) message.getHeaders().get("simpSessionId");
            String roomId = chatRoomRepository.getUserEnterRoomId(sessionId);
            // 채팅방의 인원수를 -1한다.
            chatRoomRepository.minusUserCount(roomId);
            chatService.PlusMinusViewrs(roomId,-1L);
            // 클라이언트 퇴장 메시지를 채팅방에 발송한다.(redis publish)
            chatService.sendChatMessage(ChatMessage.builder().type(ChatMessage.MessageType.LEAVE).redis_class_id(roomId).member_id(memberId).build());
            // 퇴장한 클라이언트의 roomId 맵핑 정보를 삭제한다.
            chatRoomRepository.removeUserEnterInfo(sessionId);
            log.info("SUBSCRIBED {}, {}, {}", sessionId, memberId, roomId);
        }
        return message;
    }
}