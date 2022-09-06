package com.sparta.cookbank.controller;

import com.sparta.cookbank.ResponseDto;
import com.sparta.cookbank.domain.recipe.dto.RecipeFixRequestDto;
import com.sparta.cookbank.domain.recipe.dto.RecipeFixResponseDto;
import com.sparta.cookbank.domain.donerecipe.dto.DoneRecipeRequestDto;
import com.sparta.cookbank.service.DoneRecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DoneRecipeController {
    private final DoneRecipeService doneRecipeService;


    @PostMapping("/api/recipe/finish")
    public ResponseDto<?> RecipeDone(@RequestParam Long id, @RequestBody DoneRecipeRequestDto requestDto){
        doneRecipeService.UsedIngredient(id,requestDto);
        return ResponseDto.success(id,"나의 레시피 저장소에 저장되었습니다.");
    }

    @PostMapping("/api/recipe/fix")
    public ResponseDto<?> RecipeFix(@RequestParam Long id, @RequestBody RecipeFixRequestDto requestDto){
        RecipeFixResponseDto responseDto = doneRecipeService.FixRecipe(id,requestDto);
        return ResponseDto.success(responseDto,"다음 레시피 정보");
    }
}
