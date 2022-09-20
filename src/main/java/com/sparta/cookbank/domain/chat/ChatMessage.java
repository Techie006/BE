package com.sparta.cookbank.domain.chat;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


@Getter
@Setter
public class ChatMessage implements Serializable {

    public ChatMessage() {
    }

    @Builder
    public ChatMessage(MessageType type, String redis_class_id, String nickname, String message, long viewer_num) {
        this.type = type;
        this.redis_class_id = redis_class_id;
        this.nickname = nickname;
        this.message = message;
        this.viewer_num = viewer_num;
    }

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