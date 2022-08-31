package com.sparta.cookbank.service;

import com.sparta.cookbank.controller.ResponseDto;
import com.sparta.cookbank.domain.Ingredient.Ingredient;
import com.sparta.cookbank.domain.Ingredient.dto.IngredientResponseDto;
import com.sparta.cookbank.domain.Ingredient.dto.SearchIngredientDto;
import com.sparta.cookbank.domain.myingredients.MyIngredients;
import com.sparta.cookbank.domain.myingredients.dto.IngredientRequestDto;
import com.sparta.cookbank.repository.IngredientsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientsRepository ingredientsRepository;


    public ResponseDto<?> findAutoIngredient(String food_name, HttpServletRequest request) {

        // Token 유효성 검사 없음

        //해당 검색어 찾기
        List<Ingredient> ingredients = ingredientsRepository.findAllByFoodNameIsContaining(food_name);
        // DTO사용
        List<IngredientResponseDto> dtoList = new ArrayList<>();
        // 5개만 보여주기
        for(int i=0; i<4; i++){
            dtoList.add(IngredientResponseDto.builder()
                    .id(ingredients.get(i).getId())
                    .food_name(ingredients.get(i).getFoodName())
                    .group_name(ingredients.get(i).getFoodCategory())
                    .build());
        }

        return ResponseDto.success(dtoList);
    }



    public SearchIngredientDto<?> findIngredient(String food_name, HttpServletRequest request) {

        // Token 유효성 검사 없음

        //해당 검색 찾기
        List<Ingredient> ingredients = ingredientsRepository.findAllByFoodNameIsContaining(food_name);

        // DTO사용
        List<IngredientResponseDto> dtoList = new ArrayList<>();

        for(int i=0; i<ingredients.size(); i++){
            dtoList.add(IngredientResponseDto.builder()
                            .id(ingredients.get(i).getId())
                            .food_name(ingredients.get(i).getFoodName())
                            .group_name(ingredients.get(i).getFoodCategory())
                    .build());
        }


        return SearchIngredientDto.success(dtoList,ingredients.size());
    }

    public ResponseDto<?> enterIngredient(IngredientRequestDto requestDto, HttpServletRequest request) {

        // Token 유효성 검사



        
        //
        MyIngredients myIngredients = MyIngredients.builder()



                .build();



        return ResponseDto.success("작성완료");
    }



}
