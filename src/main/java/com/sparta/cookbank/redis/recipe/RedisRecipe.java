package com.sparta.cookbank.redis.recipe;

import com.sparta.cookbank.domain.recipe.dto.RecipeRecommendResponseDto;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.List;

@Getter
@Builder
@RedisHash(value = "recipe", timeToLive = 3600) // 3600s
public class RedisRecipe {

    @Id
    private String id;
    private List<RecipeRecommendResponseDto> recipes;

}
