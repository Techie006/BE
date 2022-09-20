package com.sparta.cookbank.repository;

import com.sparta.cookbank.domain.LikeRecipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LikeRecipeRepository extends JpaRepository<LikeRecipe, Long>{
    LikeRecipe findByMember_IdAndRecipe_Id(Long memberId, Long recipeId);
    Page<LikeRecipe> findByMember_Id(Long memberId, Pageable pageable);

}
