package com.sparta.cookbank.service;

import com.sparta.cookbank.domain.donerecipe.DoneRecipe;
import com.sparta.cookbank.domain.donerecipe.dto.*;
import com.sparta.cookbank.domain.member.Member;
import com.sparta.cookbank.domain.myingredients.MyIngredients;
import com.sparta.cookbank.domain.recipe.Recipe;
import com.sparta.cookbank.domain.myingredients.MyIngredients;
import com.sparta.cookbank.domain.recipe.dto.RecipeFixRequestDto;
import com.sparta.cookbank.domain.recipe.dto.RecipeFixResponseDto;
import com.sparta.cookbank.repository.DoneRecipeRepository;
import com.sparta.cookbank.repository.MemberRepository;
import com.sparta.cookbank.repository.MyIngredientsRepository;
import com.sparta.cookbank.repository.RecipeRepository;
import com.sparta.cookbank.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@Service
@RequiredArgsConstructor
public class DoneRecipeService {
    private final MemberRepository memberRepository;
    private final RecipeRepository recipeRepository;
    private final MyIngredientsRepository myIngredientsRepository;
    private final DoneRecipeRepository doneRecipeRepository;


    public void UsedIngredient(Long recipeId, DoneRecipeRequestDto requestDto) {
        for (Long id : requestDto.getIngredients_id()) {
            MyIngredients ingredients = myIngredientsRepository.findById(id).orElseThrow(
                    () -> new IllegalArgumentException("해당 재료가 없습니다.")
            );
            myIngredientsRepository.delete(ingredients);

            Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(
                    () -> new IllegalArgumentException("유저정보가 올바르지 않습니다.")
            );
            Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(
                    () -> new IllegalArgumentException("해당 레시피가 존재하지 않습니다.")
            );
            DoneRecipe doneRecipe = new DoneRecipe(member, recipe);
            doneRecipeRepository.save(doneRecipe);
        }
    }


    // 탄단지나 통계
    public RatioResponseDto getNutrientsRatio(NutrientsRatioRequestDto requestDto) {
        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(
                () -> new IllegalArgumentException("유저정보가 올바르지 않습니다.")
        );

        boolean empty = false;

        List<DoneRecipe> doneRecipeList = doneRecipeRepository.findAllByMember_IdOrderByCreatedAtDesc(member.getId());
        if (doneRecipeList.isEmpty()) {
            empty = true;

            return RatioResponseDto.builder()
                    .empty(empty)
                    .build();
        }
        //시작날짜와 끝날짜 설정
        LocalDate str = doneRecipeList.get(doneRecipeList.size() - 1).getCreatedAt();
        if (requestDto.getFilter().equals("월별")) str = str.minusDays(str.getDayOfMonth() - 1);
        else if (requestDto.getFilter().equals("주별")) str = str.minusDays(str.getDayOfWeek().getValue() - 1);
        LocalDate end = doneRecipeList.get(0).getCreatedAt();
        LocalDate cur = str;

        //내림차순으로 받아와 스택에 저장
        Stack<DoneRecipe> stack = new Stack<>();
        stack.addAll(doneRecipeList);

        //총합을 담을 리스트
        List<LocalDate> days = new ArrayList<>();
        List<Long> carbohydrates = new ArrayList<>();
        List<Long> proteins = new ArrayList<>();
        List<Long> fats = new ArrayList<>();

        //cur>end될때까지 반복
        while (!cur.isAfter(end)) {
            days.add(cur);
            switch (requestDto.getFilter()) {
                case "월별":
                        cur = cur.plusMonths(1);
                    break;
                case "주별":
                    cur = cur.plusWeeks(1);
                    break;
                case "일별":
                    cur = cur.plusDays(1);
                    break;
            }
            long csum = 0;
            long psum = 0;
            long fsum = 0;
            while (!stack.isEmpty()) {
                DoneRecipe d = stack.pop();
                Recipe dr = d.getRecipe();
                if (d.getCreatedAt().isBefore(cur)) {
                    csum += dr.getINFO_CAR();
                    psum += dr.getINFO_PRO();
                    fsum += dr.getINFO_FAT();
                } else {
                    stack.add(d);
                    break;
                }
            }
            carbohydrates.add(csum);
            proteins.add(psum);
            fats.add(fsum);
        }

        //출력 7개로 맞추기
        int size = days.size();
        if(size > 7){
            days = days.subList(size-7, size);
            carbohydrates = carbohydrates.subList(size-7,size);
            proteins = proteins.subList(size-7,size);
            fats = fats.subList(size-7,size);
        }
        else{
            while(size<7){
                switch (requestDto.getFilter()) {
                    case "월별":
                        str = str.minusMonths(1);
                        break;
                    case "주별":
                        str = str.minusWeeks(1);
                        break;
                    case "일별":
                        str = str.minusDays(1);
                        break;
                }
                days.add(0,str);
                carbohydrates.add(0,0L);
                proteins.add(0,0L);
                fats.add(0,0L);

                size++;
            }
        }
        NutrientsRatioResponseDto nutrientsRatioResponseDto = NutrientsRatioResponseDto.builder()
                .days(days)
                .carbohydrates(carbohydrates)
                .proteins(proteins)
                .fats(fats)
                .build();

        return RatioResponseDto.builder()
                .empty(empty)
                .statistics(nutrientsRatioResponseDto)
                .build();
    }

    public RatioResponseDto getCaloriesRatio(CaloriesRatioRequestDto requestDto) {
        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(
                () -> new IllegalArgumentException("유저정보가 올바르지 않습니다.")
        );

        boolean empty = false;

        //내림차순으로 받아와 스택에 저장
        List<DoneRecipe> doneRecipeList = doneRecipeRepository.findAllByMember_IdOrderByCreatedAtDesc(member.getId());
        if (doneRecipeList.isEmpty()) {
            empty = true;

            return RatioResponseDto.builder()
                    .empty(empty)
                    .build();
        }
        Stack<DoneRecipe> stack = new Stack<>();
        stack.addAll(doneRecipeList);

        //시작날짜와 끝날짜 설정
        LocalDate str = doneRecipeList.get(doneRecipeList.size() - 1).getCreatedAt();
        if (requestDto.getFilter().equals("월별")) str = str.minusDays(str.getDayOfMonth() - 1);
        else if (requestDto.getFilter().equals("주별")) str = str.minusDays(str.getDayOfWeek().getValue() - 1);
        LocalDate end = doneRecipeList.get(0).getCreatedAt();
        LocalDate cur = str;

        //총합을 담을 리스트
        List<LocalDate> days = new ArrayList<>();
        List<Long> calories = new ArrayList<>();

        while (!cur.isAfter(end)) {
            days.add(cur);
            switch (requestDto.getFilter()) {
                case "월별":
                    cur = cur.plusMonths(1);
                    break;
                case "주별":
                    cur = cur.plusWeeks(1);
                    break;
                case "일별":
                    cur = cur.plusDays(1);
                    break;
            }
            long csum = 0;
            while (!stack.isEmpty()) {
                DoneRecipe d = stack.pop();
                Recipe dr = d.getRecipe();
                if (d.getCreatedAt().isBefore(cur)) {
                    csum += dr.getINFO_ENG();
                } else {
                    stack.add(d);
                    break;
                }
            }
            calories.add(csum);
        }

        //출력 7개로 맞추기
        int size = days.size();
        if(size > 7){
            days = days.subList(size-7, size);
            calories = calories.subList(size-7,size);
        }
        else{
            while(size<7){
                switch (requestDto.getFilter()) {
                    case "월별":
                        str = str.minusMonths(1);
                        break;
                    case "주별":
                        str = str.minusWeeks(1);
                        break;
                    case "일별":
                        str = str.minusDays(1);
                        break;
                }
                days.add(0,str);
                calories.add(0,0L);

                size++;
            }
        }

        CaloriesRatioResponseDto caloriesRatioResponseDto = CaloriesRatioResponseDto.builder()
                .days(days)
                .calories(calories)
                .build();

        return RatioResponseDto.builder()
                .empty(empty)
                .statistics(caloriesRatioResponseDto)
                .build();
    }

    @Transactional
    public RecipeFixResponseDto FixRecipe(Long id, RecipeFixRequestDto requestDto) {
        Recipe recipe = recipeRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("해당 레시피가 존재하지 않습니다.")
        );
        recipe.SetMainRecipe(requestDto);
        Recipe nextRecipe = recipeRepository.findById(id + 1).orElseThrow(
                () -> new IllegalArgumentException("해당 레시피가 존재하지 않습니다.")
        );
        return new RecipeFixResponseDto(nextRecipe);
    }


    //어제 대비 오늘 데이터 조회 통계
    public RatioResponseDto getDailyRatio() {
        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(
                () -> new IllegalArgumentException("유저정보가 올바르지 않습니다.")
        );

        boolean empty = false;

        // 현재 날짜 구하기
        LocalDate today = LocalDate.now();
        List<DoneRecipe> todayList = doneRecipeRepository.findAllByMember_IdAndCreatedAt(member.getId(), today);
        List<DoneRecipe> yesterdayList = doneRecipeRepository.findAllByMember_IdAndCreatedAt(member.getId(), today.minusDays(1));
        if (todayList.isEmpty() || yesterdayList.isEmpty()) {
            empty =true;
        }


        DailyRatioResponseDto dailyRatioResponseDto = DailyRatioResponseDto.builder()
                .today(getDayNutrientData(todayList))
                .yesterday(getDayNutrientData(yesterdayList))
                .build();

        return RatioResponseDto.builder()
                .empty(empty)
                .statistics(dailyRatioResponseDto)
                .build();
    }

    public DayRatioDto getDayNutrientData(List<DoneRecipe> list){
        long[] info = {0,0,0,0};
        for (DoneRecipe d : list) {
            Recipe r = d.getRecipe();
            info[0] += r.getINFO_ENG();
            info[1] += r.getINFO_CAR();
            info[2] += r.getINFO_PRO();
            info[3] += r.getINFO_FAT();
        }
        List<Long> nutrients = new ArrayList<>();
        for(int i=1; i<4; i++) nutrients.add(info[i]);

        return DayRatioDto.builder()
                .calories(info[0])
                .nutrients(nutrients)
                .build();
    }
}

