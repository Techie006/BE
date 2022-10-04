package com.sparta.cookbank.domain.room.dto;

import com.sparta.cookbank.domain.recipe.dto.RecipeAllResponseDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
public class RoomInfoResponseDto {
    private String class_name;
    private RecipeAllResponseDto recipe;
}
