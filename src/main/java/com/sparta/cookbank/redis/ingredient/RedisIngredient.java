package com.sparta.cookbank.redis.ingredient;


import com.sparta.cookbank.domain.myingredients.dto.StorageResponseDto;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;

import javax.persistence.Id;

@Getter
@Builder
@RedisHash(value = "ingredient", timeToLive = 300) // 지속시간 300s
public class RedisIngredient {
    // @Id   // My ingredient 보여주기
    private String id;
    private StorageResponseDto storageList;

}
