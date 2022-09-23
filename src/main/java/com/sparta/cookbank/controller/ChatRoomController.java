package com.sparta.cookbank.controller;

import com.sparta.cookbank.ResponseDto;
import com.sparta.cookbank.domain.chat.ChatMessage;
import com.sparta.cookbank.domain.chat.dto.MessageResponseDto;
import com.sparta.cookbank.domain.recipe.dto.RecipeAllResponseDto;
import com.sparta.cookbank.domain.room.ChatRoom;
import com.sparta.cookbank.domain.room.Room;
import com.sparta.cookbank.domain.room.dto.ClassDto;
import com.sparta.cookbank.domain.room.dto.RoomRequestDto;
import com.sparta.cookbank.domain.room.dto.RoomResponseDto;
import com.sparta.cookbank.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class ChatRoomController {

    private final ChatService chatService;


    @GetMapping("api/class")
    @ResponseBody
    public ResponseDto<?> room() {
        List<RoomResponseDto> Rooms = chatService.findAllRoom();
        return ResponseDto.success(new ClassDto(Rooms),"성공적으로 클래스를 가져왔습니다.");
    }

    @PostMapping("/api/class")
    @ResponseBody
    public ResponseDto<?> CreateClass(@ModelAttribute RoomRequestDto requestDto) throws IOException {
        Room room = chatService.CreateRoom(requestDto);
        return ResponseDto.success(room,"성공적으로 방을 만들었습니다");
    }


    @GetMapping("api/class/enter/{class_id}")
    @ResponseBody
    public ResponseDto<?> EnterClass(@PathVariable Long class_id){
        List<ChatMessage> chats = chatService.findAllChatByRoom(class_id);
        return ResponseDto.success(new MessageResponseDto(chats),"성공적으로 이전 채팅을 가져왔습니다.");
    }

    @GetMapping("api/class/{class_id}")
    @ResponseBody
    public ResponseDto<?> RecipeInfo(@PathVariable Long class_id){
        RecipeAllResponseDto recipe = chatService.ClassRecipeInfo(class_id);
        return ResponseDto.success(recipe,"성공적으로 레시피를 가져왔습니다.");
    }
}