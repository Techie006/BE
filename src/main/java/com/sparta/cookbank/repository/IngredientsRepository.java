package com.sparta.cookbank.repository;

import com.sparta.cookbank.domain.Ingredient.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IngredientsRepository extends JpaRepository<Ingredient, Long> {

    List<Ingredient> findAllByFoodNameIsContaining(String food_name);  // 포함된 글자가 맨처음으로 오는게 맨위로 할려면?
}
