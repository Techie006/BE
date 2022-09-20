package com.sparta.cookbank.redis.ingredient;

import com.sparta.cookbank.domain.myingredients.dto.MyIngredientResponseDto;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface RedisIngredientRepo extends CrudRepository<RedisIngredient, String> {

    Optional<RedisIngredient> findById(String storage);

}
