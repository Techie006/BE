package com.sparta.cookbank.redis.recipe;

import com.sparta.cookbank.domain.recipe.dto.RecipeRecommendDto;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;

import javax.persistence.Id;
import java.util.List;

@Getter
@Builder
@RedisHash(value = "recipe", timeToLive = 3600) // 3600s
public class RedisRecipe {

    @Id
    private String id;
    private List<RecipeRecommendDto> recipes;

}
