package com.sparta.cookbank.domain.room.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OpenviduResponseDto {
    private String sessionId;
    private String token;
}
