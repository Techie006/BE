package com.sparta.cookbank.service;

import com.sparta.cookbank.ResponseDto;
import com.sparta.cookbank.domain.Ingredient.Ingredient;
import com.sparta.cookbank.domain.Ingredient.dto.IngredientResponseDto;
import com.sparta.cookbank.domain.Ingredient.dto.SearchIngredientDto;
import com.sparta.cookbank.domain.Member.Member;
import com.sparta.cookbank.domain.Storage;
import com.sparta.cookbank.domain.myingredients.MyIngredients;
import com.sparta.cookbank.domain.myingredients.dto.IngredientRequestDto;
import com.sparta.cookbank.domain.myingredients.dto.MyIngredientResponseDto;
import com.sparta.cookbank.repository.IngredientsRepository;
import com.sparta.cookbank.repository.MemberRepository;
import com.sparta.cookbank.repository.MyIngredientsRepository;
import com.sparta.cookbank.security.SecurityUtil;
import com.sparta.cookbank.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientsRepository ingredientsRepository;
    private final MemberRepository memberRepository;
    private final MyIngredientsRepository myIngredientsRepository;
    private final TokenProvider tokenProvider;


    @Transactional(readOnly = true)
    public ResponseDto<?> findAutoIngredient(String food_name, HttpServletRequest request) {

        // Token 유효성 검사 없음

        //해당 검색어 찾기
        List<Ingredient> ingredients = ingredientsRepository.findAllByFoodNameIsContaining(food_name);
        // DTO사용
        List<IngredientResponseDto> dtoList = new ArrayList<>();
        // 5개만 보여주기
        for(int i=0; i<5; i++){
            dtoList.add(IngredientResponseDto.builder()
                    .id(ingredients.get(i).getId())
                    .food_name(ingredients.get(i).getFoodName())
                    .group_name(ingredients.get(i).getFoodCategory())
                    .build());
        }

        return ResponseDto.success(dtoList,"자동완성 리스트 제공에 성공하였습니다.");
    }

    @Transactional(readOnly = true)
    public SearchIngredientDto<?> findIngredient(String food_name, HttpServletRequest request) {

        // Token 유효성 검사 없음

        //해당 검색 찾기
        List<Ingredient> ingredients = ingredientsRepository.findAllByFoodNameIsContaining(food_name);

        // DTO사용
        List<IngredientResponseDto> dtoList = new ArrayList<>();

        for (Ingredient ingredient : ingredients) {
            dtoList.add(IngredientResponseDto.builder()
                    .id(ingredient.getId())
                    .food_name(ingredient.getFoodName())
                    .group_name(ingredient.getFoodCategory())
                    .build());
        }


        return SearchIngredientDto.success(dtoList,ingredients.size(),"식재료 검색에 성공하였습니다.");
    }
    @Transactional
    public ResponseDto<?> saveMyIngredient(IngredientRequestDto requestDto, HttpServletRequest request) {

        //토큰 유효성 검사
        String token = request.getHeader("Authorization");
        token = resolveToken(token);
        tokenProvider.validateToken(token);

        // 멤버 유효성 검사
        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(
                () -> new IllegalArgumentException("해당 유저가 존재하지 않습니다.")
        );
        //재료찾기
        Ingredient ingredient = ingredientsRepository.findById(requestDto.getId()).orElseThrow(
                () -> new IllegalArgumentException("해당 음식 재료가 존재 하지 않습니다.")
        );

        MyIngredients myIngredients = MyIngredients.builder()
                .member(member)
                .ingredient(ingredient)
                .storage(requestDto.getStorage())
                .inDate(requestDto.getIn_date())
                .expDate(requestDto.getExp_date())
                .build();
        myIngredientsRepository.save(myIngredients);

        return ResponseDto.success("","작성완료");
    }


    @Transactional(readOnly = true)
    public ResponseDto<?> getMyIngredient(Storage storage, HttpServletRequest request) {
        //토큰 유효성 검사
        String token = request.getHeader("Authorization");
        token = resolveToken(token);
        tokenProvider.validateToken(token);

        // 멤버 유효성 검사
        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(
                () -> new IllegalArgumentException("해당 유저가 존재하지 않습니다.")
        );

        List<MyIngredients> myIngredients = myIngredientsRepository.findByMemberIdAndStorage(member.getId(), storage);
        List<MyIngredientResponseDto> dtoList = new ArrayList<>();

        for (MyIngredients myIngredient : myIngredients) {
            dtoList.add(MyIngredientResponseDto.builder()
                    .id(myIngredient.getId())
                    .food_name(myIngredient.getIngredient().getFoodName())
                    .group_name(myIngredient.getIngredient().getFoodCategory())
                    .in_date(myIngredient.getInDate())
                    .d_date(myIngredient.getExpDate())
                    .build());
        }

        return ResponseDto.success(dtoList,"리스트 제공에 성공하였습니다.");
    }









    private String resolveToken(String token){
        if(token.startsWith("Bearer "))
            return token.substring(7);
        throw new RuntimeException("not valid token !!");
    }
}