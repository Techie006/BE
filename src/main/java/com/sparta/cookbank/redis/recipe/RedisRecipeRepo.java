package com.sparta.cookbank.redis.recipe;

import com.sparta.cookbank.domain.recipe.Recipe;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface RedisRecipeRepo extends CrudRepository<RedisRecipe, String> {

}
