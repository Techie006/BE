package com.sparta.cookbank.domain.room.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
public class RoomRequestDto {
    private Long recipe_id;
    private String class_name;
    private MultipartFile file;
}
