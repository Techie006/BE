package com.sparta.cookbank.controller;

import com.sparta.cookbank.ResponseDto;
import com.sparta.cookbank.domain.recipe.dto.*;
import com.sparta.cookbank.service.RecipeService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
public class RecipeController {
    private final RecipeService recipeService;

    private final Bucket bucket;

    @Autowired
    public RecipeController(RecipeService recipeService){
        this.recipeService = recipeService;

        //Refill.intervally token = 1000, 1회충전시 1000개의 토큰을 충전
        //Duration.ofSeconds = 1, 1초마다 토큰을 충전
        //Duration.ofMinutes = 1, 1분마다 토큰을 충전
        //Bandwidth capacity = Bucket의 총 크기는 1000
        Bandwidth limit = Bandwidth.classic(1000, Refill.intervally(1000, Duration.ofMinutes(1)));
        this.bucket = Bucket.builder()
                .addLimit(limit)
                .build();
    }

    @PostMapping("/api/recipes/recommend") // 추천 레시피 조회
    public ResponseDto<?> getRecommendRecipe(@RequestBody RecipeRecommendRequestDto requestDto) {
        RecipeRecommendResponseDto resultResponseDto = recipeService.getRecommendRecipe(requestDto);
        return ResponseDto.success(resultResponseDto,"추천레시피 제공에 성공하였습니다.");
    }

    @GetMapping("/api/recipe/{id}") // 레시피 상세 조회
    public ResponseDto<?> getDetailRecipe(@PathVariable Long id) {
        RecipeDetailResponseDto detailResponseDto = recipeService.getDetailRecipe(id);
        return ResponseDto.success(detailResponseDto,"레시피 제공에 성공하였습니다.");
    }

    @GetMapping("/api/recipes") // 레시피 전체 조회
    public ResponseDto<?> getAllRecipe(Pageable pageable) {
        if(bucket.tryConsume(1)) {
            RecipeResponseDto recipeResponseDtoPage = recipeService.getAllRecipe(pageable);
            return ResponseDto.success(recipeResponseDtoPage,"전체레시피 제공에 성공하였습니다.");
        }else{
            return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }

    @PostMapping("/api/recipes/search") // 레시피 검색
    public ResponseDto<?> searchRecipe(@RequestBody RecipeSearchRequestDto searchRequestDto, Pageable pageable) {
        RecipeSearchResponseDto ResponseSearchDtoPage = recipeService.searchRecipe(searchRequestDto,pageable);
        return ResponseDto.success(ResponseSearchDtoPage,"레시피 검색에 성공하였습니다.");
    }

    @PostMapping("/api/recipe/like") // 북마크 On
    public ResponseDto<?> likeRecipe(@RequestParam Long id) {
        if(bucket.tryConsume(1)) {
            recipeService.likeRecipe(id);
            return ResponseDto.success(null,"레시피를 성공적으로 북마크했습니다.");
        }else{
            return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }

    @DeleteMapping("/api/recipe/unlike") // 북마크 Off
    public ResponseDto<?> unlikeRecipe(@RequestParam Long id) {
        if(bucket.tryConsume(1)) {
            recipeService.unlikeRecipe(id);
            return ResponseDto.success(null,"레시피 북마크 삭제되었습니다.");
        }else{
        return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }

    @GetMapping("/api/my/bookmark") // 북마크한 레시피 조회
    public ResponseDto<?> getBookmark(Pageable pageable){
        RecipeBookmarkResponseDto recipeResponseDto = recipeService.getBookmark(pageable);

        return ResponseDto.success(recipeResponseDto, "성공적으로 북마크한 레시피를 가져왔습니다.");
    }

    @PostMapping("/api/recipes/autocomplete") // 레시피 자동완성
    public ResponseDto<?> getAutoComplete(@RequestBody AutoCompleteRequestDto requestDto) {
        AutoCompleteResponseDto autoCompleteResponseDto = recipeService.getAutoComplete(requestDto);

        return ResponseDto.success(autoCompleteResponseDto,"레시피 자동완성에 성공하였습니다.");
    }

    @PostMapping("/api/recipes/category") // 레시피 종류별 요리방법별 분류
    public ResponseDto<?> getRecipeByCategory(@RequestBody RecipeByCategoryRequestDto requestDto,
                                              Pageable pageable) {
        RecipeResponseDto responseDto = recipeService.getRecipeByCategory(requestDto, pageable);

        return ResponseDto.success(responseDto, "카테고리별 레시피 제공에 성공하였습니다.");
    }
}
