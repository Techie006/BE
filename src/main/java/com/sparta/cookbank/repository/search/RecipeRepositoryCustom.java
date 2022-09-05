package com.sparta.cookbank.repository.search;

import com.sparta.cookbank.domain.recipe.Recipe;
import com.sparta.cookbank.domain.recipe.dto.RecipeSearchRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RecipeRepositoryCustom {
    Page<Recipe> findBySearchOption(RecipeSearchRequestDto searchRequestDto, Pageable pageable);
}
