package com.sparta.cookbank.repository;

import com.sparta.cookbank.domain.donerecipe.DoneRecipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface DoneRecipeRepository extends JpaRepository<DoneRecipe, Long > {
    List<DoneRecipe> findAllByMember_Id(Long id);
    List<DoneRecipe> findAllByMember_IdAndCreatedAt(Long id, LocalDate today);

    List<DoneRecipe> findByCreatedAtBetween(LocalDateTime startDay, LocalDateTime endDay);

    List<DoneRecipe> findAllByMember_IdOrderByCreatedAtDesc(Long id);
}
