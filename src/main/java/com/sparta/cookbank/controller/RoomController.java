package com.sparta.cookbank.controller;

import com.sparta.cookbank.ResponseDto;
import com.sparta.cookbank.domain.chat.dto.ChatListResponseDto;
import com.sparta.cookbank.domain.recipe.dto.RecipeAllResponseDto;
import com.sparta.cookbank.domain.room.Room;
import com.sparta.cookbank.domain.room.dto.ClassDto;
import com.sparta.cookbank.domain.room.dto.RoomRequestDto;
import com.sparta.cookbank.domain.room.dto.RoomResponseDto;
import com.sparta.cookbank.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    @GetMapping("/api/class")
    public ResponseDto<?> ClassList(){
        return ResponseDto.success(new ClassDto(roomService.ClassLists()), "성공적으로 클래스을 가져왔습니다.");
    }

    @GetMapping("/api/chat/{class_id}")
    public ResponseDto<?> EnterClass(@PathVariable Long class_id){
        List<ChatListResponseDto> chats = roomService.ChatLists(class_id);
        return ResponseDto.success(chats,"성공적으로 이전 채팅을 가져왔습니다.");
    }

    @PostMapping("/api/class")
    public ResponseDto<?> CreateClass(@RequestPart(value = "RoomRequestDto") RoomRequestDto requestDto,
                                      @RequestPart(value = "Multipart") MultipartFile multipartFile) throws IOException {
        System.out.println(requestDto.getRecipe_id()+ " "+ requestDto.getClass_name());
        System.out.println(multipartFile.getOriginalFilename());
        Room room = roomService.CreateRoom(requestDto, multipartFile);
        return ResponseDto.success(room,"성공적으로 방을 만들었습니다.");
    }

    @GetMapping("api/class/{class_id}")
    public ResponseDto<?> RecipeInfo(@PathVariable Long class_id){
        RecipeAllResponseDto recipe = roomService.ClassRecipeInfo(class_id);
        return ResponseDto.success(recipe,"성공적으로 레시피를 가져왔습니다.");
    }
}