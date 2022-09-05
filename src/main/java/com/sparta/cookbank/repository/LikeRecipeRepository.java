package com.sparta.cookbank.repository;

import com.sparta.cookbank.domain.LikeRecipe;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRecipeRepository extends JpaRepository<LikeRecipe, Long> {
    LikeRecipe findByMember_IdAndRecipe_Id(Long memberId, Long recipeId);
}
