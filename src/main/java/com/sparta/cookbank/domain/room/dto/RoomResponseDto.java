package com.sparta.cookbank.domain.room.dto;

import com.sparta.cookbank.domain.room.Room;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomResponseDto {
    private Long class_id;
    private String class_name;
    private Long viewer_nums;
    private String class_img;

    public RoomResponseDto(Room room){
        this.class_id = room.getId();
        this.class_name = room.getName();
        this.viewer_nums = room.getViewers();
        this.class_img = room.getImage();
    }
}
