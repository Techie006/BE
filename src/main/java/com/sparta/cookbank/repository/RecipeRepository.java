package com.sparta.cookbank.repository;

import com.sparta.cookbank.domain.Recipe.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository<Recipe, Long > {
}
