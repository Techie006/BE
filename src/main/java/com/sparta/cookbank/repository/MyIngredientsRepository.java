package com.sparta.cookbank.repository;

import com.sparta.cookbank.domain.Storage;
import com.sparta.cookbank.domain.myingredients.MyIngredients;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MyIngredientsRepository extends JpaRepository<MyIngredients, Long> {
    List<MyIngredients> findByMemberIdAndStorageOrderByExpDate(Long id, Storage storage);

    List<MyIngredients> findAllByMemberId(Long id);
    List<MyIngredients> findAllByMemberIdOrderByExpDate(Long id);
}
