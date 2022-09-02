package com.sparta.cookbank.repository;

import com.sparta.cookbank.domain.recipe.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository<Recipe, Long > {
}
