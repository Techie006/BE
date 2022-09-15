package com.sparta.cookbank.redis.recipe;

import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Getter
@RedisHash(value = "recipe", timeToLive = 3600) // 3600s
public class RedisRecipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;


}
