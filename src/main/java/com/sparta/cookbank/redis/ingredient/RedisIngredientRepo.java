package com.sparta.cookbank.redis.ingredient;

import com.sparta.cookbank.domain.ingredient.Ingredient;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RedisIngredientRepo extends CrudRepository<RedisIngredient, String> {

    List<Ingredient> findAllByFoodNameIsContaining(String food_name);
}
