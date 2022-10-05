package com.sparta.cookbank.redis.recipe;

import org.springframework.data.repository.CrudRepository;

public interface RedisRecipeRepo extends CrudRepository<RedisRecipe, String> {
}
