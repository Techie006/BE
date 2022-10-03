package com.sparta.cookbank.domain.recipe.dto;


import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@EqualsAndHashCode
public class RecipeRecommendRequestDto {

    // 추천 레시피의 메인 재료
    private String base;
    // 추천 레시피의 서브 재료
    private List<String> foods = new ArrayList<>();

}
