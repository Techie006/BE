package com.sparta.cookbank.domain.room.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomResponseDto {
    private Long class_id;
    private String redis_class_id;
    private String session_id;
    private String class_name;
    private Long viewer_nums;
    private String class_img;
    private List<String> ingredients;

}
