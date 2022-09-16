package com.sparta.cookbank.controller;

import com.sparta.cookbank.ResponseDto;
import com.sparta.cookbank.domain.chat.dto.ChatListResponseDto;
import com.sparta.cookbank.domain.recipe.dto.RecipeAllResponseDto;
import com.sparta.cookbank.domain.room.Room;
import com.sparta.cookbank.domain.room.dto.RoomRequestDto;
import com.sparta.cookbank.domain.room.dto.RoomResponseDto;
import com.sparta.cookbank.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoomController {
    RoomService roomService;

    @GetMapping("/api/class")
    public ResponseDto<?> ClassList(){
        List<RoomResponseDto> Classes = roomService.ClassLists();
        return ResponseDto.success(Classes, "성공적으로 클래스을 가져왔습니다.");
    }

    @GetMapping("/api/chat/{class_id}")
    public ResponseDto<?> EnterClass(@PathVariable Long class_id){
        List<ChatListResponseDto> chats = roomService.ChatLists(class_id);
        return ResponseDto.success(chats,"성공적으로 이전 채팅을 가져왔습니다.");
    }

    @PostMapping("/api/class")
    public ResponseDto<?> CreateClass(@RequestBody RoomRequestDto requestDto){
        Room room = roomService.CreateRoom(requestDto);
        return ResponseDto.success(room.getId(),"성공적으로 방을 만들었습니다.");
    }

    @GetMapping("api/class/{class_id}")
    public ResponseDto<?> RecipeInfo(@PathVariable Long class_id){
        RecipeAllResponseDto recipe = roomService.ClassRecipeInfo(class_id);
        return ResponseDto.success(recipe,"성공적으로 레시피를 가져왔습니다.");
    }
}
