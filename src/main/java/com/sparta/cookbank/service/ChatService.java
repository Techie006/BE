package com.sparta.cookbank.service;

import com.sparta.cookbank.domain.chat.Chat;
import com.sparta.cookbank.domain.chat.dto.ChatDto;
import com.sparta.cookbank.domain.chat.dto.ChatResponseDto;
import com.sparta.cookbank.domain.member.Member;
import com.sparta.cookbank.domain.room.Room;
import com.sparta.cookbank.repository.ChatRepository;
import com.sparta.cookbank.repository.MemberRepository;
import com.sparta.cookbank.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {
    private final RoomRepository roomRepository;

    private final ChatRepository chatRepository;

    private final MemberRepository memberRepository;


    @Transactional
    public ChatResponseDto saveChat(Long roomId, ChatDto message) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new NullPointerException("해당하는 클래스가 없습니다."));
        Member member = memberRepository.findById(message.getMember_id())
                .orElseThrow(() -> new NullPointerException("해당하는 유저가 없습니다."));

        Chat chat = Chat.builder()
                .room(room)
                .member(member)
                .content(message.getMessage())
                .build();

        chatRepository.save(chat);
        return new ChatResponseDto(message,room.getViewers());
    }


}
