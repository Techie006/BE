package com.sparta.cookbank.domain.room.dto;

import com.sparta.cookbank.domain.room.ChatRoom;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ClassDto {
    private List<ChatRoom> Classes;
    public ClassDto(List<ChatRoom> c){
        this.Classes = c;
    }
}
