package com.sparta.cookbank.controller;

import com.sparta.cookbank.ResponseDto;
import com.sparta.cookbank.domain.Storage;
import com.sparta.cookbank.domain.ingredient.dto.IngredientsByCategoryResponseDto;
import com.sparta.cookbank.domain.ingredient.dto.RefrigeratorStateResponseDto;
import com.sparta.cookbank.domain.myingredients.dto.IngredientRequestDto;
import com.sparta.cookbank.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;

@RequiredArgsConstructor
@RestController
public class IngredientController {

    private final IngredientService ingredientService;


    @GetMapping("/api/ingredients/autocomplete")  // 식재료 자동완성(5개만 보여줌)
    public ResponseDto<?> findAutoIngredient(@RequestBody IngredientRequestDto requestDto, HttpServletRequest request){
        return ingredientService.findAutoIngredient(requestDto.getFood_name(),request);
    }

    @GetMapping("/api/ingredients/search")  // 식재료 검색 HTTPSERVLET 추가해줘야됨..
    public ResponseDto<?> findIngredient(@RequestBody IngredientRequestDto requestDto, HttpServletRequest request){
        return ingredientService.findIngredient(requestDto.getFood_name(),request);
    }

    @PostMapping("/api/ingredient")  // 식재료 작성
    public ResponseDto<?> saveMyIngredient(@RequestBody IngredientRequestDto requestDto, HttpServletRequest request){
        return ingredientService.saveMyIngredient(requestDto,request);
    }

    // 저장소별 식재료 조회
    @GetMapping("/api/ingredients")
    public ResponseDto<?> getMyIngredient(@RequestParam("storage") String storage, HttpServletRequest request) throws ParseException {
        return ingredientService.getMyIngredient(storage,request);
    }

    // 임박 식재료 조회(시간데이터..)
    @GetMapping("/api/ingredients/warning")
    public ResponseDto<?> getMyWarningIngredient(HttpServletRequest request) throws ParseException {
        return ingredientService.getMyWarningIngredient(request);
    }

    //나의 냉장고 식재료 삭제
    @DeleteMapping("/api/ingredient")
    public ResponseDto<?> deleteMyIngredient(@RequestParam("id") Long myIngredientId,HttpServletRequest request){
        return ingredientService.deleteMyIngredient(myIngredientId,request);
    }


    // 우리집 냉장고 상태표시
    @GetMapping("/api/statistics/state")
    public ResponseDto<?> MyRefrigeratorState(){
        RefrigeratorStateResponseDto stateResponseDto =ingredientService.MyRefrigeratorState();

        return ResponseDto.success(stateResponseDto, "냉장고 상태 제공에 성공하였습니다.");
    }

    // (통계)제품류 나눠서 보여주기
    @GetMapping("/api/statistics/category")
    public ResponseDto<?> ingredientsByCategory() {
        IngredientsByCategoryResponseDto categoryResponseDto = ingredientService.ingredientsByCategory();
        return ResponseDto.success(categoryResponseDto,"성공");
    }
}
