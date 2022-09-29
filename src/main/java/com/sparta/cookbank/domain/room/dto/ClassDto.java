package com.sparta.cookbank.domain.room.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ClassDto {
    private boolean empty;
    private List<RoomResponseDto> Classes;
    public ClassDto(boolean empty, List<RoomResponseDto> c){
        this.empty = empty;
        this.Classes = c;
    }
}
