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
public class ListTotalMyIngredientsDto {
    private int ingredients_num;
    private List<TotalMyIngredientDto> storage;
}