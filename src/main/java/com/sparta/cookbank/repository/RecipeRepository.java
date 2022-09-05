package com.sparta.cookbank.repository;

import com.sparta.cookbank.domain.recipe.Recipe;
import com.sparta.cookbank.repository.search.RecipeRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeRepository extends JpaRepository<Recipe, Long>, RecipeRepositoryCustom {

}
