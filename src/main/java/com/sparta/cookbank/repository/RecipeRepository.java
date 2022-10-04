package com.sparta.cookbank.repository;

import com.sparta.cookbank.domain.recipe.Recipe;
import com.sparta.cookbank.repository.search.RecipeRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecipeRepository extends JpaRepository<Recipe, Long>, RecipeRepositoryCustom {

    @Query("select r from Recipe r where r.RCP_NM LIKE %:keyword%")
    List<Recipe> findAllByRCP_NM(@Param("keyword") String keyword);

}
