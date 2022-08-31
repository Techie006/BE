package com.sparta.cookbank.repository;

import com.sparta.cookbank.domain.myingredients.MyIngredients;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MyIngredientsRepository extends JpaRepository<MyIngredients, Long> {
}
