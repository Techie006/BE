package com.sparta.cookbank.domain.Ingredient.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor // 모든 필드 값을 파라미터로 받는 생성자를 만듦
@Getter
public class SearchIngredientDto<T> {

    private boolean result;
    private T content;
    private int total_count;
    private Error error;

    public static <T> SearchIngredientDto<T> success(T content, int count) {
        return new SearchIngredientDto<>(true, content, count,null);
    }


}
