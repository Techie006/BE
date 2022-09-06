package com.sparta.cookbank.domain.donerecipe.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DoneRecipeRequestDto {
    List<Long> ingredients_id;
}
