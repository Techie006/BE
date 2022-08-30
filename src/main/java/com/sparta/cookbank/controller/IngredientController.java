package com.sparta.cookbank.controller;

import com.sparta.cookbank.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class IngredientController {

    private final IngredientService ingredientService;

    @GetMapping("/api/ingredients/search")  // 식재료 검색
    public ResponseEntity<?> findIngredient(){

        return ingredientService.findIngredient();
    }




    @PostMapping("/api/ingredient")  // 식재료 작성
    public ResponseEntity<?> enterIngredient(){

        return ingredientService.enterIngredient();
    }

}
