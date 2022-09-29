package com.sparta.cookbank.domain.myingredients.dto;

import com.sparta.cookbank.domain.myingredients.MyIngredients;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryIngredientDto {
    private List<TotalMyIngredientDto> storage;
}
