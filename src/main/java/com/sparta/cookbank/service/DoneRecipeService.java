package com.sparta.cookbank.service;

import com.sparta.cookbank.domain.DoneRecipe.DoneRecipe;
import com.sparta.cookbank.domain.DoneRecipe.dto.DoneRecipeRequestDto;
import com.sparta.cookbank.domain.member.Member;
import com.sparta.cookbank.domain.recipe.Recipe;
import com.sparta.cookbank.domain.myingredients.MyIngredients;
import com.sparta.cookbank.domain.recipe.dto.RecipeFixRequestDto;
import com.sparta.cookbank.domain.recipe.dto.RecipeFixResponseDto;
import com.sparta.cookbank.repository.DoneRecipeRepository;
import com.sparta.cookbank.repository.MemberRepository;
import com.sparta.cookbank.repository.MyIngredientsRepository;
import com.sparta.cookbank.repository.RecipeRepository;
import com.sparta.cookbank.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DoneRecipeService {
    private final MemberRepository memberRepository;
    private final RecipeRepository recipeRepository;
    private final MyIngredientsRepository myIngredientsRepository;
    private final DoneRecipeRepository doneRecipeRepository;


    public void UsedIngredient(Long recipeId, DoneRecipeRequestDto requestDto) {
        for(Long id : requestDto.getIngredients_id()){
            MyIngredients ingredients = myIngredientsRepository.findById(id).orElseThrow(
                    () -> new IllegalArgumentException("해당 재료가 없습니다.")
            );
            myIngredientsRepository.delete(ingredients);

            Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(
                    () -> new IllegalArgumentException("유저정보가 올바르지 않습니다.")
            );
            Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(
                    () -> new IllegalArgumentException("해당 레시피가 존재하지 않습니다.")
            );
            DoneRecipe doneRecipe = new DoneRecipe(member,recipe);
            doneRecipeRepository.save(doneRecipe);

        }
    }

    @Transactional
    public RecipeFixResponseDto FixRecipe(Long id, RecipeFixRequestDto requestDto) {
        Recipe recipe = recipeRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("해당 레시피가 존재하지 않습니다.")
        );
        recipe.SetMainRecipe(requestDto);
        Recipe nextRecipe = recipeRepository.findById(id+1).orElseThrow(
                () -> new IllegalArgumentException("해당 레시피가 존재하지 않습니다.")
        );
        return new RecipeFixResponseDto(nextRecipe);
    }
}
