package com.sparta.cookbank.domain.myingredients.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarningResponseDto {

    private int out_dated_num;
    private int in_hurry_num;
    private List<MyIngredientResponseDto> out_dated;
    private List<MyIngredientResponseDto> in_hurry;

}
