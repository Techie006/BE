package com.sparta.cookbank.controller;

import com.sparta.cookbank.ResponseDto;
import com.sparta.cookbank.domain.chat.dto.MessageResponseDto;
import com.sparta.cookbank.domain.room.dto.*;
import com.sparta.cookbank.service.ChatService;
import com.sparta.cookbank.service.DoneRecipeService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import io.openvidu.java.client.OpenViduHttpException;
import io.openvidu.java.client.OpenViduJavaClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class ChatRoomController {

    private final ChatService chatService;
    private final Bucket bucket;

    @Autowired
    public ChatRoomController(ChatService chatService){
        this.chatService = chatService;

        //Refill.intervally token = 1000, 1회충전시 1000개의 토큰을 충전
        //Duration.ofSeconds = 1, 1초마다 토큰을 충전
        //Duration.ofMinutes = 1, 1분마다 토큰을 충전
        //Bandwidth capacity = Bucket의 총 크기는 1000
        Bandwidth limit = Bandwidth.classic(1000, Refill.intervally(1000, Duration.ofMinutes(1)));
        this.bucket = Bucket.builder()
                .addLimit(limit)
                .build();
    }

    @GetMapping("api/class") // 쿠킹클래스 전체 조회
    @ResponseBody
    public ResponseDto<?> room() {
        if(bucket.tryConsume(1)) {
            List<RoomResponseDto> Rooms = chatService.findAllRoom();
            boolean empty = false;
            if(Rooms.isEmpty())  empty = true;
            return ResponseDto.success(new ClassDto(empty, Rooms),"성공적으로 클래스를 가져왔습니다.");
        }else{
            return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }

    @PostMapping("/api/class") //쿠킹클래스 생성
    @ResponseBody
    public ResponseDto<?> CreateClass(@ModelAttribute RoomRequestDto requestDto) throws IOException, OpenViduJavaClientException, OpenViduHttpException {
        if(bucket.tryConsume(1)) {
            ViduRoomResponseDto room = chatService.CreateRoom(requestDto);
            return ResponseDto.success(room,"성공적으로 방을 만들었습니다");
        }else{
            return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }


    @GetMapping("api/class/enter/{class_id}") //쿠킹클래스 입장(이전채팅 불러오기)
    @ResponseBody
    public ResponseDto<?> EnterClass(@PathVariable Long class_id) throws OpenViduJavaClientException, OpenViduHttpException {
        MessageResponseDto responseDto = chatService.EnterRoom(class_id);
        return ResponseDto.success(responseDto,"성공적으로 이전 채팅을 가져왔습니다.");
    }

    @GetMapping("api/class/{class_id}") //쿠킹클래스 레시피 조회
    @ResponseBody
    public ResponseDto<?> RecipeInfo(@PathVariable Long class_id){
        RoomInfoResponseDto recipe = chatService.ClassRecipeInfo(class_id);
        return ResponseDto.success(recipe,"성공적으로 레시피를 가져왔습니다.");
    }

    @DeleteMapping("api/class/{class_id}") //쿠킹클래스 종료
    @ResponseBody
    public ResponseDto<?> RemoveClass(@PathVariable Long class_id){
        chatService.ApiRemoveClass(class_id);
        return ResponseDto.success(null,"클래스가 종료되었습니다.");
    }
}