package com.sparta.cookbank.repository;

import com.sparta.cookbank.domain.ingredient.Ingredient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IngredientsRepository extends JpaRepository<Ingredient, Long> {

    Page<Ingredient> findAllByFoodNameIsContaining(String food_name, Pageable pageable);  // 포함된 글자가 맨처음으로 오는게 맨위로 할려면?
    List<Ingredient> findAllByFoodNameIsContainingOrderByMarkName(String food_name);
}
