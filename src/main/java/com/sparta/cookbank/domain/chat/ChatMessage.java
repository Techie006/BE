package com.sparta.cookbank.domain.chat;

import lombok.*;

import java.io.Serializable;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage implements Serializable {
    // 메시지 타입 : 입장, 퇴장, 채팅
    public enum MessageType {
        MESSAGE, ENTER, LEAVE
    }

    private String redis_chat_id;
    private MessageType type; // 메시지 타입
    private String redis_class_id; // 방번호

    private Long member_id;
    private String nickname; // 메시지 보낸사람

    private String profile_img;
    private String message; // 메시지
    private long viewer_num; // 채팅방 인원수, 채팅방 내에서 메시지가 전달될때 인원수 갱신시 사용

    private String createAt;
}