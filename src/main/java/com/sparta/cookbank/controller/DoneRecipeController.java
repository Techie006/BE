package com.sparta.cookbank.controller;

import com.sparta.cookbank.ResponseDto;
import com.sparta.cookbank.domain.donerecipe.dto.*;
import com.sparta.cookbank.domain.donerecipe.dto.DoneRecipeRequestDto;
import com.sparta.cookbank.domain.recipe.dto.RecipeFixRequestDto;
import com.sparta.cookbank.domain.recipe.dto.RecipeFixResponseDto;
import com.sparta.cookbank.service.DoneRecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    // 탄단지나 통계
    @PostMapping("/api/statistics/ratio/nutrients")
    public ResponseDto<?> getNutrientsRatio(@RequestBody NutrientsRatioRequestDto requestDto ) {
        RatioResponseDto nutrientsRatio = doneRecipeService.getNutrientsRatio(requestDto);
        return ResponseDto.success(nutrientsRatio, "통계자료 제공에 성공하였습니다.");
    }

    // 열량 통계
    @PostMapping("/api/statistics/ratio/calories")
    public ResponseDto<?> getCaloriesRatio(@RequestBody CaloriesRatioRequestDto requestDto) {
        RatioResponseDto caloriesRatio = doneRecipeService.getCaloriesRatio(requestDto);
        return ResponseDto.success(caloriesRatio, "통계자료 제공에 성공하였습니다.");
    }

    //어제 대비 오늘 데이터 조회 통계
    @GetMapping("/api/statistics/daily")
    public ResponseDto<?> getDailyRatio() {
        RatioResponseDto dailyRatio = doneRecipeService.getDailyRatio();
        return ResponseDto.success(dailyRatio, "통계자료 제공에 성공하였습니다.");
    }
}
