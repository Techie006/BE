package com.sparta.cookbank.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.sparta.cookbank.FileUtils;
import com.sparta.cookbank.MiniComparator;
import com.sparta.cookbank.domain.chat.ChatMessage;
import com.sparta.cookbank.domain.chat.dto.MessageResponseDto;
import com.sparta.cookbank.domain.member.Member;
import com.sparta.cookbank.domain.recipe.Recipe;
import com.sparta.cookbank.domain.recipe.dto.RecipeBasicDto;
import com.sparta.cookbank.domain.room.ChatRoom;
import com.sparta.cookbank.domain.room.Room;
import com.sparta.cookbank.domain.room.dto.*;
import com.sparta.cookbank.repository.ChatRoomRepository;
import com.sparta.cookbank.repository.MemberRepository;
import com.sparta.cookbank.repository.RecipeRepository;
import com.sparta.cookbank.repository.RoomRepository;
import com.sparta.cookbank.security.SecurityUtil;
import io.openvidu.java.client.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
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

    @Value("${default.profile.img}")
    private String DEFAULT_PROFILE_IMG;


    private final ChannelTopic channelTopic;
    private final RedisTemplate redisTemplate;
    private final ChatRoomRepository chatRoomRepository;

    // SDK의 진입점인 OpenVidu 개체
    private OpenVidu openVidu;

    // OpenVidu 서버가 수신하는 URL
    @Value("${openvidu.url}")
    private String OPENVIDU_URL;

    // OpenVidu 서버와 공유되는 비밀
    @Value("${openvidu.secret}")
    private String OPENVIDU_SECRET;

    @PostConstruct
    public OpenVidu openVidu() {
        return openVidu = new OpenVidu(OPENVIDU_URL, OPENVIDU_SECRET);
    }


    public ViduRoomResponseDto CreateRoom(RoomRequestDto requestDto) throws IOException, OpenViduJavaClientException, OpenViduHttpException {
        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(() -> {
            throw new IllegalArgumentException("로그인한 유저를 찾을 수 없습니다.");
        });
        Recipe recipe = recipeRepository.findById(requestDto.getRecipe_id()).orElseThrow(() -> {
            throw new IllegalArgumentException("해당 레시피를 찾을 수 없습니다.");
        });
        //새로운 비디오방 생성
        OpenviduResponseDto viduToken = createNewToken(member);

        System.out.println("ViduToken : " + viduToken);


        MultipartFile multipartFile = requestDto.getFile();

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
        Room room = roomRepository.save(Room.builder()
                .host(member)
                .name(requestDto.getClass_name())
                .image(amazonS3Client.getUrl(bucketName, fileName).toString())
                .recipe(recipe)
                .redisClassId(chatRoom.getRedis_class_id())
                .sessionId(viduToken.getSessionId())
                .viewrs(1L)
                .build());
        return ViduRoomResponseDto.builder()
                .class_id(room.getClass_id())
                .redis_class_id(room.getRedisClassId())
                .session_id(room.getSessionId())
                .token(viduToken.getToken().split("&")[1].substring(6))
                .full_token(viduToken.getToken())
                .build();
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

        //로그인 비로그인 구분
        chatMessage.setNickname("UnknownUser");
        chatMessage.setProfile_img(DEFAULT_PROFILE_IMG);
        chatMessage.setNotice(false);
        //로그인되었을 경우
        if(chatMessage.getMember_id() != -1){
            Member member = memberRepository.findById(chatMessage.getMember_id()).orElse(null);
            if(member != null) {
                chatMessage.setNickname(member.getUsername());
                chatMessage.setProfile_img(member.getImage());
            }
        }

        //입장 퇴장일 시
        if (ChatMessage.MessageType.ENTER.equals(chatMessage.getType())) {
            chatMessage.setMessage(chatMessage.getNickname() + "님이 방에 입장했습니다.");
            chatMessage.setNickname("[알림]");
            chatMessage.setNotice(true);
        } else if (ChatMessage.MessageType.LEAVE.equals(chatMessage.getType())) {
            chatMessage.setMessage(chatMessage.getNickname() + "님이 방에서 나갔습니다.");
            chatMessage.setNickname("[알림]");
            chatMessage.setNotice(true);
        }
        chatRoomRepository.saveMessage(chatMessage);
        redisTemplate.convertAndSend(channelTopic.getTopic(), chatMessage);
    }

    public List<RoomResponseDto> findAllRoom() {
        List<RoomResponseDto> responseDtos = new ArrayList<>();
        List<Room> Rooms = roomRepository.findAll();
        for(Room room : Rooms){
            ChatRoom chatRoom = chatRoomRepository.findRoomById(room.getRedisClassId());
            if(chatRoom != null) {
                chatRoom.setUserCount(chatRoomRepository.getUserCount(room.getRedisClassId()));
                responseDtos.add(RoomResponseDto.builder()
                        .class_id(room.getClass_id())
                        .redis_class_id(room.getRedisClassId())
                        .session_id(room.getSessionId())
                        .class_name(room.getName())
                        .viewer_nums(room.getViewrs())
                        .class_img(room.getImage())
                        .ingredients(Arrays.asList(room.getRecipe().getMAIN_INGREDIENTS().split(", ")))
                        .build());
            }
        }
        return responseDtos;
    }

    public MessageResponseDto EnterRoom(Long class_id ) throws OpenViduJavaClientException, OpenViduHttpException {
        Room ClassRoom = roomRepository.findById(class_id).orElseThrow(() -> {
            throw new IllegalArgumentException("해당 클래스를 찾을 수 없습니다");
        });
        if(ClassRoom.getViewrs() >= 50) throw new RuntimeException("방 인원수가 초과되었습니다.");
        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(() -> {
            throw new IllegalArgumentException("해당 사용자를 찾을 수 없습니다.");
        });
        String enterToken = enterNewToken(member,ClassRoom.getSessionId());
        List<ChatMessage> chats = chatRoomRepository.findAllMessageByRoom(ClassRoom.getRedisClassId());
        chats.sort(new MiniComparator());
        return MessageResponseDto.builder()
                .session_id(ClassRoom.getSessionId())
                .token(enterToken.split("&")[1].substring(6))
                .full_token(enterToken)
                .chats(chats)
                .build();
    }

    @Transactional
    public Long PlusMinusViewrs(String roomId, Long num){
        log.info("redis_class_id: "+roomId);
        Room room = roomRepository.findByRedisClassId(roomId).orElseThrow(() -> {
            throw new IllegalArgumentException("해당 클래스를 찾을 수 없습니다");
        });
        return room.FixViewrs(num);
    }

    public RoomInfoResponseDto ClassRecipeInfo(Long classId){
        Room room = roomRepository.findById(classId).orElseThrow(() -> {
            throw new IllegalArgumentException("해당 클래스를 찾을 수 없습니다.");
        });
        Recipe recipe = room.getRecipe();
        List<String> ingredients = Arrays.asList(recipe.getRCP_PARTS_DTLS().split(","));
        return new RoomInfoResponseDto(room.getName(), RecipeBasicDto.builder()
                .id(recipe.getId())
                .recipe_name(recipe.getRCP_NM())
                .ingredients(ingredients)
                .final_img(recipe.getATT_FILE_NO_MK())
                .method(recipe.getRCP_WAY2())
                .category(recipe.getRCP_PAT2())
                .calorie(recipe.getINFO_ENG())
                .build());
    }

    // 채팅방 생성 시 토큰 발급
    private OpenviduResponseDto createNewToken(Member member) throws OpenViduJavaClientException, OpenViduHttpException {

        // 사용자 연결 시 닉네임 전달
        String serverData = member.getUsername();

        // serverData을 사용하여 connectionProperties 객체를 빌드
        ConnectionProperties connectionProperties = new ConnectionProperties.Builder().type(ConnectionType.WEBRTC).data(serverData).build();

        // 새로운 OpenVidu 세션(채팅방) 생성
        Session session = openVidu.createSession();

        String token = session.createConnection(connectionProperties).getToken();

        return OpenviduResponseDto.builder()
                .sessionId(session.getSessionId()) //리턴해주는 해당 세션아이디로 다른 유저 채팅방 입장시 요청해주시면 됩니다.
                .token(token) //이 토큰으로 오픈비두에 해당 유저의 화상 미디어 정보를 받아주세요
                .build();
    }

    //채팅방 입장 시 토큰 발급
    private String enterNewToken(Member member, String sessionId) throws OpenViduJavaClientException, OpenViduHttpException {
        String serverData = member.getUsername();

        //serverData을 사용하여 connectionProperties 객체를 빌드
        ConnectionProperties connectionProperties = new ConnectionProperties.Builder().type(ConnectionType.WEBRTC).data(serverData).build();

        openVidu.fetch();

        //오픈비두에 활성화된 세션을 모두 가져와 리스트에 담음
        List<Session> activeSessionList = openVidu.getActiveSessions();

        // 1. Request : 다른 유저가 타겟 채팅방에 입장하기 위한 타겟 채팅방의 세션 정보 , 입장 요청하는 유저 정보
        Session session = null;

        //활성화된 session의 sessionId들을 registerReqChatRoom에서 리턴한 sessionId(입장할 채팅방의 sessionId)와 비교
        //같을 경우 해당 session으로 새로운 토큰을 생성
        for (Session getSession : activeSessionList) {
            if (getSession.getSessionId().equals(sessionId)) {
                session = getSession;
                break;
            }
        }
        if (session == null){
            throw new NullPointerException("방이 존재하지 않습니다.");
        }

        // 2. Openvidu에 유저 토큰 발급 요청 : 오픈비두 서버에 요청 유저가 타겟 채팅방에 입장할 수 있는 토큰을 발급 요청
        //토큰을 가져옴
        return session.createConnection(connectionProperties).getToken();
    }

    //클래스 삭제 메소드
    public void RemoveClass(Room ClassRoom){
        //채팅 기록 삭제
        List<ChatMessage> chats = chatRoomRepository.findAllMessageByRoom(ClassRoom.getRedisClassId());
        for(ChatMessage chat : chats) chatRoomRepository.removeChat(ClassRoom.getRedisClassId(),chat.getRedis_chat_id());
        log.info("채팅기록 삭제: {}",chats.size());
        //시청자수 기록 삭제
        chatRoomRepository.removeCount(ClassRoom.getRedisClassId());
        log.info("시청자수 기록 삭제");
        //방 삭제
        chatRoomRepository.removeChatRoom(ClassRoom.getRedisClassId());
        log.info("방 삭제");

        //DB 방 삭제
        roomRepository.delete(ClassRoom);
    }

    //클래스를 방장이 나갔을때 삭제
    public void ApiRemoveClass(Long classId){
        Room ClassRoom = roomRepository.findById(classId).orElseThrow(() -> {
            throw new IllegalArgumentException("해당 클래스를 찾을 수 없습니다");
        });
        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(() -> {
            throw new IllegalArgumentException("해당 멤버를 찾을 수 없습니다");
        });
        if(ClassRoom.getHost() != member) throw new RuntimeException("호스트가 아닙니다.");

        RemoveClass(ClassRoom);
    }

    //매일 안쓰는 클래스 삭제
    public void DailyRemoveClass(){
        List<Room> rooms = roomRepository.findAll();

        //오픈비두에 활성화된 세션을 모두 가져와 리스트에 담음
        List<Session> activeSessionList = openVidu.getActiveSessions();
        List<String> sessions = new ArrayList<>();
        for(Session session : activeSessionList) sessions.add(session.getSessionId());

        for(Room room : rooms) {
            if(!sessions.contains(room.getSessionId())) {
                RemoveClass(room);
            }
        }
    }
}

