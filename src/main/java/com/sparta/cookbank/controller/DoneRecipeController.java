package com.sparta.cookbank.controller;

import com.sparta.cookbank.ResponseDto;
import com.sparta.cookbank.domain.donerecipe.dto.*;
import com.sparta.cookbank.domain.donerecipe.dto.DoneRecipeRequestDto;
import com.sparta.cookbank.domain.recipe.dto.RecipeFixRequestDto;
import com.sparta.cookbank.domain.recipe.dto.RecipeFixResponseDto;
import com.sparta.cookbank.service.DoneRecipeService;
import com.sparta.cookbank.service.IngredientService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
public class DoneRecipeController {
    private final DoneRecipeService doneRecipeService;
    private final Bucket bucket;

    @Autowired
    public DoneRecipeController(DoneRecipeService doneRecipeService){
        this.doneRecipeService = doneRecipeService;

        //Refill.intervally token = 1000, 1회충전시 1000개의 토큰을 충전
        //Duration.ofSeconds = 1, 1초마다 토큰을 충전
        //Duration.ofMinutes = 1, 1분마다 토큰을 충전
        //Bandwidth capacity = Bucket의 총 크기는 1000
        Bandwidth limit = Bandwidth.classic(2000, Refill.intervally(2000, Duration.ofMinutes(1)));
        this.bucket = Bucket.builder()
                .addLimit(limit)
                .build();
    }

    @PostMapping("/api/recipe/finish") //레시피 완료
    public ResponseDto<?> RecipeDone(@RequestParam Long id, @RequestBody DoneRecipeRequestDto requestDto){
        if(bucket.tryConsume(1)) {
            doneRecipeService.UsedIngredient(id,requestDto);
            return ResponseDto.success(id,"나의 레시피 저장소에 저장되었습니다.");
        }else{
            return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }

    @PostMapping("/api/statistics/ratio/nutrients") // 탄단지나 통계
    public ResponseDto<?> getNutrientsRatio(@RequestBody NutrientsRatioRequestDto requestDto ) {
        if(bucket.tryConsume(1)) {
            RatioResponseDto nutrientsRatio = doneRecipeService.getNutrientsRatio(requestDto);
            return ResponseDto.success(nutrientsRatio, "통계자료 제공에 성공하였습니다.");
        }else{
            return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }

    @PostMapping("/api/statistics/ratio/calories")  // 열량 통계
    public ResponseDto<?> getCaloriesRatio(@RequestBody CaloriesRatioRequestDto requestDto) {
        if(bucket.tryConsume(1)) {
            RatioResponseDto caloriesRatio = doneRecipeService.getCaloriesRatio(requestDto);
            return ResponseDto.success(caloriesRatio, "통계자료 제공에 성공하였습니다.");
        }else{
            return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }

    @GetMapping("/api/statistics/daily") //어제 대비 오늘 데이터 조회 통계
    public ResponseDto<?> getDailyRatio() {
        if(bucket.tryConsume(1)) {
            RatioResponseDto dailyRatio = doneRecipeService.getDailyRatio();
            return ResponseDto.success(dailyRatio, "통계자료 제공에 성공하였습니다.");
        }else{
            return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }
}
