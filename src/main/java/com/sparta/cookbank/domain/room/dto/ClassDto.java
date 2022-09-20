package com.sparta.cookbank.domain.room.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ClassDto {
    List<RoomResponseDto> Classes;

    public ClassDto(List<RoomResponseDto> c){
        this.Classes = c;
    }
}
