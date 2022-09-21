package com.sparta.cookbank.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.sparta.cookbank.FileUtils;
import com.sparta.cookbank.MiniComparator;
import com.sparta.cookbank.domain.chat.ChatMessage;
import com.sparta.cookbank.domain.member.Member;
import com.sparta.cookbank.domain.recipe.Recipe;
import com.sparta.cookbank.domain.recipe.dto.RecipeAllResponseDto;
import com.sparta.cookbank.domain.room.ChatRoom;
import com.sparta.cookbank.domain.room.Room;
import com.sparta.cookbank.domain.room.dto.RoomRequestDto;
import com.sparta.cookbank.domain.room.dto.RoomResponseDto;
import com.sparta.cookbank.repository.*;
import com.sparta.cookbank.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {
    private final RoomRepository roomRepository;

    private final MemberRepository memberRepository;

    private final RecipeRepository recipeRepository;

    private final AmazonS3Client amazonS3Client;
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;



    private final ChannelTopic channelTopic;
    private final RedisTemplate redisTemplate;
    private final ChatRoomRepository chatRoomRepository;

    public Room CreateRoom(RoomRequestDto requestDto, MultipartFile multipartFile) throws IOException{
        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(() -> {
            throw new IllegalArgumentException("로그인한 유저를 찾을 수 없습니다.");
        });
        Recipe recipe = recipeRepository.findById(requestDto.getRecipe_id()).orElseThrow(() -> {
            throw new IllegalArgumentException("해당 레시피를 찾을 수 없습니다.");
        });

        //파일 비었는지 검증
        if (multipartFile.isEmpty()) {
            throw new IOException("파일이 비어있습니다.");
        }

        String fileName = FileUtils.buildFileName(multipartFile.getOriginalFilename());

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(multipartFile.getContentType());

        try (InputStream inputStream = multipartFile.getInputStream()) {
            amazonS3Client.putObject(new PutObjectRequest(bucketName, fileName, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e) {
            throw new IOException("변환에 실패했습니다.");
        }
        ChatRoom chatRoom = chatRoomRepository.createChatRoom(requestDto.getClass_name());
        return roomRepository.save(Room.builder()
                .host(member)
                .name(requestDto.getClass_name())
                .image(amazonS3Client.getUrl(bucketName, fileName).toString())
                .recipe(recipe)
                .redis_class_id(chatRoom.getRedis_class_id())
                .build());
    }


     //destination정보에서 roomId 추출
    public String getRoomId(String destination) {
        int lastIndex = destination.lastIndexOf('/');
        if (lastIndex != -1)
            return destination.substring(lastIndex + 1);
        else
            return "";
    }

     //채팅방에 메시지 발송
    public void sendChatMessage(ChatMessage chatMessage) {
        chatMessage.setViewer_num(chatRoomRepository.getUserCount(chatMessage.getRedis_class_id()));
        if (ChatMessage.MessageType.ENTER.equals(chatMessage.getType())) {
            chatMessage.setMessage(chatMessage.getNickname() + "님이 방에 입장했습니다.");
            chatMessage.setNickname("[알림]");
        } else if (ChatMessage.MessageType.LEAVE.equals(chatMessage.getType())) {
            chatMessage.setMessage(chatMessage.getNickname() + "님이 방에서 나갔습니다.");
            chatMessage.setNickname("[알림]");
        }
        chatRoomRepository.saveMessage(chatMessage);
        redisTemplate.convertAndSend(channelTopic.getTopic(), chatMessage);
    }

    public List<RoomResponseDto> findAllRoom() {
        List<RoomResponseDto> responseDtos = new ArrayList<>();
        List<Room> Rooms = roomRepository.findAll();
        for(Room room : Rooms){
            ChatRoom chatRoom = chatRoomRepository.findRoomById(room.getRedis_class_id());
            if(chatRoom != null) {
                chatRoom.setUserCount(chatRoomRepository.getUserCount(room.getRedis_class_id()));

                responseDtos.add(RoomResponseDto.builder()
                        .class_id(room.getClass_id())
                        .redis_class_id(room.getRedis_class_id())
                        .class_name(room.getName())
                        .viewer_nums(chatRoom.getUserCount())
                        .class_img(room.getImage())
                        .build());
            }
        }
        return responseDtos;
    }

    public List<ChatMessage> findAllChatByRoom(Long class_id ){
        Room ClassRoom = roomRepository.findById(class_id).orElseThrow(() -> {
            throw new IllegalArgumentException("해당 클래스를");
        });
        List<ChatMessage> chats = chatRoomRepository.findAllMessageByRoom(ClassRoom.getRedis_class_id());
        chats.sort(new MiniComparator());
        return chats;
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

