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
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoneRecipeService {
    private final MemberRepository memberRepository;
    private final RecipeRepository recipeRepository;
    private final MyIngredientsRepository myIngredientsRepository;
    private final DoneRecipeRepository doneRecipeRepository;


    public void UsedIngredient(Long recipeId, DoneRecipeRequestDto requestDto) {
        for(Long id : requestDto.getIngredients_id()){
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
            DoneRecipe doneRecipe = new DoneRecipe(member,recipe);
            doneRecipeRepository.save(doneRecipe);
        }
    }


    // 탄단지나 통계
    public NutrientsRatioResponseDto getNutrientsRatio(NutrientsRatioRequestDto requestDto) {
        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(
                () -> new IllegalArgumentException("유저정보가 올바르지 않습니다.")
        );


        List<String> days = List.of("2022.01", "2022.02", "2022.03", "2022.04", "2022.05", "2022.06", "2022.07", "2022.08", "2022.09", "2022.10", "2022.11", "2022.12");
        long[] carbohydrates = new long[]{0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L};
        long[] proteins = new long[]{0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L};
        long[] fats = new long[]{0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L};
        long[] sodium = new long[]{0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L};

        List<DoneRecipe> doneRecipeList = doneRecipeRepository.findAllByMember_Id(member.getId());
        for (DoneRecipe doneRecipe : doneRecipeList) {
            if (requestDto.getFilter().equals("month")) {
                for (int i = 0; i < 12; i++) {
                    if (doneRecipe.getCreatedAt().getMonth().getValue() == i+1) {
                        carbohydrates[i] += doneRecipe.getRecipe().getINFO_CAR();
                        proteins[i] += doneRecipe.getRecipe().getINFO_PRO();
                        fats[i] += doneRecipe.getRecipe().getINFO_FAT();
                        sodium[i] += doneRecipe.getRecipe().getINFO_NA();
                    } else {
                        carbohydrates[i] += 0;
                        proteins[i] += 0;
                        fats[i] += 0;
                        sodium[i] += 0;
                    }
                }
            }

        }
        NutrientsRatioResponseDto nutrientsRatioResponseDto = NutrientsRatioResponseDto.builder()
                .days(days)
                .carbohydrates(carbohydrates)
                .proteins(proteins)
                .fats(fats)
                .sodium(sodium)
                .build();
        return nutrientsRatioResponseDto;
    }

    public CaloriesRatioResponseDto getCaloriesRatio(CaloriesRatioRequestDto requestDto) {

        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(
                () -> new IllegalArgumentException("유저정보가 올바르지 않습니다.")
        );

        List<String> days = List.of("2022.01", "2022.02", "2022.03", "2022.04", "2022.05", "2022.06", "2022.07", "2022.08", "2022.09", "2022.10", "2022.11", "2022.12");
        long[] calories = new long[]{0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L};
        List<DoneRecipe> doneRecipeList = doneRecipeRepository.findAllByMember_Id(member.getId());
        for (DoneRecipe doneRecipe : doneRecipeList) {
            if (requestDto.getFilter().equals("month")) {
                for (int i = 0; i < 12; i++) {
                    if (doneRecipe.getCreatedAt().getMonth().getValue() == i+1) {
                        calories[i] += doneRecipe.getRecipe().getINFO_ENG();
                    } else {
                        calories[i] += 0;
                    }
                }
            }
        }
        CaloriesRatioResponseDto caloriesRatioResponseDto = CaloriesRatioResponseDto.builder()
                .days(days)
                .calories(calories)
                .build();
        return caloriesRatioResponseDto;
    }

    @Transactional
    public RecipeFixResponseDto FixRecipe(Long id, RecipeFixRequestDto requestDto) {
        Recipe recipe = recipeRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("해당 레시피가 존재하지 않습니다.")
        );
        recipe.SetMainRecipe(requestDto);
        Recipe nextRecipe = recipeRepository.findById(id+1).orElseThrow(
                () -> new IllegalArgumentException("해당 레시피가 존재하지 않습니다.")
        );
        return new RecipeFixResponseDto(nextRecipe);
    }


    //어제 대비 오늘 데이터 조회 통계
    public DailyRatioResponseDto getDailyRatio() {
        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(
                () -> new IllegalArgumentException("유저정보가 올바르지 않습니다.")
        );

        List<List<Long>> today = new ArrayList<>();
        Long todayCalorie = 0L;
        Long todayCarbohydrates = 0L;
        Long todayProteins = 0L;
        Long todayFats = 0L;
        Long todaySodium = 0L;
        List<Long> todayNutrients = new ArrayList<>();
        List<Long> todayCalories = new ArrayList<>();
        List<List<Long>> yesterday = new ArrayList<>();
        Long yesterdayCalorie = 0L;
        Long yesterdayCarbohydrates = 0L;
        Long yesterdayProteins = 0L;
        Long yesterdayFats = 0L;
        Long yesterdaySodium = 0L;
        List<Long> yesterdayNutrients = new ArrayList<>();
        List<Long> yesterdayCalories = new ArrayList<>();
        LocalDate getToday = LocalDate.now();
        LocalDate getYesterday = LocalDate.now().minusDays(1);
        List<DoneRecipe> todayRecipeList = doneRecipeRepository.findByMember_IdAndCreatedAt(member.getId(), getToday);
        for (DoneRecipe doneRecipe : todayRecipeList) {
            todayCalorie += doneRecipe.getRecipe().getINFO_ENG();
            todayCarbohydrates += doneRecipe.getRecipe().getINFO_CAR();
            todayProteins += doneRecipe.getRecipe().getINFO_PRO();
            todayFats += doneRecipe.getRecipe().getINFO_FAT();
            todaySodium += doneRecipe.getRecipe().getINFO_NA();
        }
        todayCalories.add(todayCalorie);
        todayNutrients.add(todayCarbohydrates);
        todayNutrients.add(todayProteins);
        todayNutrients.add(todayFats);
        todayNutrients.add(todaySodium);
        today.add(todayCalories);
        today.add(todayNutrients);
        List<DoneRecipe> yesterdayRecipeList = doneRecipeRepository.findByMember_IdAndCreatedAt(member.getId(), getYesterday);
        for (DoneRecipe doneRecipe : yesterdayRecipeList) {
            yesterdayCalorie += doneRecipe.getRecipe().getINFO_ENG();
            yesterdayCarbohydrates += doneRecipe.getRecipe().getINFO_CAR();
            yesterdayProteins += doneRecipe.getRecipe().getINFO_PRO();
            yesterdayFats += doneRecipe.getRecipe().getINFO_FAT();
            yesterdaySodium += doneRecipe.getRecipe().getINFO_NA();
        }
        yesterdayCalories.add(yesterdayCalorie);
        yesterdayNutrients.add(yesterdayCarbohydrates);
        yesterdayNutrients.add(yesterdayProteins);
        yesterdayNutrients.add(yesterdayFats);
        yesterdayNutrients.add(yesterdaySodium);
        yesterday.add(yesterdayCalories);
        yesterday.add(yesterdayNutrients);

        DailyRatioResponseDto dailyRatioResponseDto = DailyRatioResponseDto.builder()
                .today(today)
                .yesterday(yesterday)
                .build();
        return dailyRatioResponseDto;
    }

}
