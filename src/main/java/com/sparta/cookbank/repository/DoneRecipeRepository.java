package com.sparta.cookbank.repository;

import com.sparta.cookbank.domain.DoneRecipe.DoneRecipe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoneRecipeRepository extends JpaRepository<DoneRecipe, Long > {
}
