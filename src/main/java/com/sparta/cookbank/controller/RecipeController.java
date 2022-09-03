package com.sparta.cookbank.controller;

import com.sparta.cookbank.ResponseDto;
import com.sparta.cookbank.domain.recipe.dto.*;
import com.sparta.cookbank.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RecipeController {
    private final RecipeService recipeService;

    // 추천 레시피 조회
    @GetMapping("/api/recipes/recommend")
    public ResponseDto<?> getRecommendRecipe(@RequestBody RecipeRecommendRequestDto requestDto) {
        List<RecipeRecommendResponseDto> recommendResponseDto = recipeService.getRecommendRecipe(requestDto);

        return ResponseDto.success(recommendResponseDto,"추천레시피 제공에 성공하였습니다.");
    }

    // 레시피 상세 조회
    @GetMapping("/api/recipe/{id}")
    public ResponseDto<?> getDetailRecipe(@PathVariable Long id) {
        RecipeDetailResultResponseDto detailResponseDto = recipeService.getDetailRecipe(id);
        return ResponseDto.success(detailResponseDto,"레시피 제공에 성공하였습니다.");
    }

    // 레시피 전체 조회
    @GetMapping("/api/recipes")
    public ResponseDto<?> getAllRecipe(@PageableDefault(size = 5)Pageable pageable) {
        RecipeResponseDto recipeResponseDtoPage = recipeService.getAllRecipe(pageable);
        return ResponseDto.success(recipeResponseDtoPage,"전체레시피 제공에 성공하였습니다.");
    }

    // 레시피 검색
    @GetMapping("/api/recipes/search")
    public ResponseDto<?> searchRecipe(@RequestBody RecipeSearchRequestDto searchRequestDto,
                                       @PageableDefault(size = 5) Pageable pageable) {
        RecipeResponseDto ResponseDtoPage = recipeService.searchRecipe(searchRequestDto,pageable);
        return ResponseDto.success(ResponseDtoPage,"레시피 검색에 성공하였습니다.");
    }

    // 북마크 On
    @PostMapping("/api/recipe/like")
    public ResponseDto<?> likeRecipe(@RequestParam Long id) {
        recipeService.likeRecipe(id);
        return ResponseDto.success(null,"레시피 검색에 성공하였습니다.");
    }

    // 북마크 Off
    @DeleteMapping("/api/recipe/unlike")
    public ResponseDto<?> unlikeRecipe(@RequestParam Long id) {
        recipeService.unlikeRecipe(id);

        return ResponseDto.success(null,"레시피 북마크 삭제되었습니다.");
    }
}
