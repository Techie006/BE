package com.sparta.cookbank.controller;

import com.sparta.cookbank.ResponseDto;
import com.sparta.cookbank.domain.doneRecipe.dto.DoneRecipeRequestDto;
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
}
