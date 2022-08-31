package com.sparta.cookbank.controller;

import com.sparta.cookbank.ResponseDto;
import com.sparta.cookbank.domain.Storage;
import com.sparta.cookbank.domain.myingredients.dto.IngredientRequestDto;
import com.sparta.cookbank.domain.Ingredient.dto.SearchIngredientDto;
import com.sparta.cookbank.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RequiredArgsConstructor
@RestController
public class IngredientController {

    private final IngredientService ingredientService;


    @GetMapping("/api/ingredients/autocomplete")  // 식재료 자동완성(5개만 보여줌)
    public ResponseDto<?> findAutoIngredient(@RequestBody IngredientRequestDto requestDto, HttpServletRequest request){
        return ingredientService.findAutoIngredient(requestDto.getFood_name(),request);
    }

    @GetMapping("/api/ingredients/search")  // 식재료 검색 HTTPSERVLET 추가해줘야됨..
    public SearchIngredientDto<?> findIngredient(@RequestBody IngredientRequestDto requestDto, HttpServletRequest request){
        return ingredientService.findIngredient(requestDto.getFood_name(),request);
    }

    @PostMapping("/api/ingredient")  // 식재료 작성
    public ResponseDto<?> saveMyIngredient(@RequestBody IngredientRequestDto requestDto, HttpServletRequest request){
        return ingredientService.saveMyIngredient(requestDto,request);
    }

    // 저장소별 식재료 조회
    @GetMapping("/api/ingredients")
    public ResponseDto<?> getMyIngredient(@RequestParam("storage") Storage storage, HttpServletRequest request){
        return ingredientService.getMyIngredient(storage,request);
    }

    // 임박 식재료 조회(시간데이터..)

    //나의 냉장고 식재료 삭제

}