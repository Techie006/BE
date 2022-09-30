package com.sparta.cookbank.domain.room.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ViduRoomResponseDto {
    private Long class_id;
    private String redis_class_id;
    private String session_id;
    private String token;
    private String full_token;
}
