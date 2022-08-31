package com.sparta.cookbank.controller;

import com.sparta.cookbank.domain.myingredients.dto.IngredientRequestDto;
import com.sparta.cookbank.domain.Ingredient.dto.SearchIngredientDto;
import com.sparta.cookbank.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseDto<?> enterIngredient(@RequestBody IngredientRequestDto requestDto, HttpServletRequest request){

        return ingredientService.enterIngredient(requestDto,request);
    }

    // 저장소별 식재료 조회



    // 임박 식재료 조회(시간데이터..)

    //나의 냉장고 식재료 삭제

}
