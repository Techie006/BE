package com.sparta.cookbank.repository;

import com.sparta.cookbank.domain.food_recipe.FoodRecipe;
import com.sparta.cookbank.domain.ingredient.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodRecipeRepository extends JpaRepository<FoodRecipe, Long> {
    List<FoodRecipe> findAllByIngredient_Id(Long ingredient);

    Boolean existsByIngredient_idAndRecipe_id(Long ingredientId, Long recipeId);
}
