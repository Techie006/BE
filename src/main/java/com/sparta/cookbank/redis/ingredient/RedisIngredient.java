package com.sparta.cookbank.redis.ingredient;


import com.sparta.cookbank.domain.ingredient.enums.FoodCategory;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter
@RedisHash(value = "ingredient", timeToLive = 3600) // 3600s
public class RedisIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String foodName;
    private FoodCategory foodCategory;
    public enum FoodCategory {
        전분류,
        견과류,
        곡류 ,
        과실류,
        기타,
        난류,
        당류,
        두류,
        버섯류,
        어패류,
        유제품류,
        유지류,
        육류,
        음료류,
        조리가공품류,
        조미료류,
        주류,
        차류,
        채소류,
        해조류,
    }
}
