package com.sparta.cookbank.domain.myingredients.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IngredientRequestDto {
    private Long id;
    private String food_name;
    private String group_name;
    private String storage;
    private String in_date;
    private String exp_date;

}
