package com.sparta.cookbank.service;

import com.sparta.cookbank.domain.chat.Chat;
import com.sparta.cookbank.domain.chat.dto.ChatListResponseDto;
import com.sparta.cookbank.domain.member.Member;
import com.sparta.cookbank.domain.recipe.Recipe;
import com.sparta.cookbank.domain.recipe.dto.RecipeAllResponseDto;
import com.sparta.cookbank.domain.room.Room;
import com.sparta.cookbank.domain.room.dto.RoomRequestDto;
import com.sparta.cookbank.domain.room.dto.RoomResponseDto;
import com.sparta.cookbank.repository.ChatRepository;
import com.sparta.cookbank.repository.MemberRepository;
import com.sparta.cookbank.repository.RecipeRepository;
import com.sparta.cookbank.repository.RoomRepository;
import com.sparta.cookbank.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {
    RoomRepository roomRepository;

    ChatRepository chatRepository;

    MemberRepository memberRepository;

    RecipeRepository recipeRepository;

    public List<RoomResponseDto> ClassLists(){
        List<RoomResponseDto> roomDtoList = new ArrayList<>();

        List<Room> roomList = roomRepository.findAll();
        for(Room room : roomList){
            roomDtoList.add(new RoomResponseDto(room));
        }

        return roomDtoList;
    }


    public List<ChatListResponseDto> ChatLists(Long roomId){
        List<ChatListResponseDto> chatDtoList = new ArrayList<>();

        List<Chat> chatList = chatRepository.findAllByRoom_id(roomId);
        int s = chatList.size();
        for(int i=(s>50)? s-50 : 0; i<s; i++){
            chatDtoList.add(new ChatListResponseDto(chatList.get(i)));
        }
        return chatDtoList;
    }

    public Room CreateRoom(RoomRequestDto requestDto){
        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(() -> {
            throw new IllegalArgumentException("로그인한 유저를 찾을 수 없습니다.");
        });
        Recipe recipe = recipeRepository.findById(requestDto.getRecipe_id()).orElseThrow(() -> {
            throw new IllegalArgumentException("해당 레시피를 찾을 수 없습니다.");
        });

        return roomRepository.save(Room.builder()
                .host(member)
                .name(requestDto.getClass_name())
                .image(requestDto.getClass_img())
                .recipe(recipe)
                .viewers(0L)
                .build());
    }

    public RecipeAllResponseDto ClassRecipeInfo(Long classId){
        Room room = roomRepository.findById(classId).orElseThrow(() -> {
            throw new IllegalArgumentException("해당 클래스를 찾을 수 없습니다.");
        });
        Recipe recipe = room.getRecipe();
        List<String> ingredients = Arrays.asList(recipe.getRCP_PARTS_DTLS().split(","));
        return RecipeAllResponseDto.builder()
                .id(recipe.getId())
                .recipe_name(recipe.getRCP_NM())
                .ingredients(ingredients)
                .final_img(recipe.getATT_FILE_NO_MK())
                .method(recipe.getRCP_WAY2())
                .category(recipe.getRCP_PAT2())
                .calorie(recipe.getINFO_ENG())
                .build();
    }
}
