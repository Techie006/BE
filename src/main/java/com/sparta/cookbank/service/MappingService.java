package com.sparta.cookbank.service;

import com.sparta.cookbank.domain.food_recipe.FoodRecipe;
import com.sparta.cookbank.domain.food_recipe.MappingRequestDto;
import com.sparta.cookbank.domain.ingredient.Ingredient;
import com.sparta.cookbank.domain.member.Member;
import com.sparta.cookbank.domain.recipe.Recipe;
import com.sparta.cookbank.domain.recipe.dto.RecipeRecommendResponseDto;
import com.sparta.cookbank.domain.recipe.dto.RecipeRecommendResultResponseDto;
import com.sparta.cookbank.repository.*;
import com.sparta.cookbank.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MappingService {
    private final RecipeRepository recipeRepository;
    private final IngredientsRepository ingredientsRepository;

    private final FoodRecipeRepository foodRecipeRepository;

    private final MemberRepository memberRepository;

    private final LikeRecipeRepository likeRecipeRepository;


    public Long MakeRecipeIngredient(){
        List<Recipe> recipes = recipeRepository.findAll();

        List<Ingredient> ingredients = ingredientsRepository.findAll();

        long cnt = 0;
        for(Ingredient ingredient : ingredients){
            for(Recipe recipe : recipes){
                if(recipe.getRCP_PARTS_DTLS().contains(ingredient.getMarkName())){
                    foodRecipeRepository.save(FoodRecipe.builder()
                            .recipe(recipe)
                            .ingredient(ingredient)
                            .build()
                    );
                    cnt++;
                }
            }
        }
        return cnt;
    }

    public RecipeRecommendResultResponseDto FindRelationRecipe(MappingRequestDto requestDto){
        //만약 레디스가 없으면
        List<Map.Entry<Long, Integer>> entryList = SortRecipe(requestDto);
        //레디스가 있으면 그냥불러오기
        ////////////////////////////////////////////////////////////////////////////////////

        List<RecipeRecommendResponseDto> recipeRecommendResponseDto = new ArrayList<>();
        for(Map.Entry<Long, Integer> entry : entryList){
            if(foodRecipeRepository.existsByIngredient_idAndRecipe_id(requestDto.getBaseId(),entry.getKey())){
                Recipe r = recipeRepository.findById(entry.getKey()).orElseThrow(() -> {
                    throw new IllegalArgumentException("레시피를 찾을 수 없습니다.");
                });
                boolean liked = likeRecipeRepository.existsByMember_IdAndRecipe_Id(SecurityUtil.getCurrentMemberId(), entry.getKey());
                recipeRecommendResponseDto.add(
                        RecipeRecommendResponseDto.builder()
                                .id(r.getId())
                                .recipe_name(r.getRCP_NM())
                                .recipe_image(r.getATT_FILE_NO_MAIN())
                                .liked(liked)
                                .common_ingredients(Arrays.asList(r.getMAIN_INGREDIENTS().split(", ")))
                                .ingredients(Arrays.asList(r.getRCP_PARTS_DTLS().split(", ")))
                                .method(r.getRCP_WAY2())
                                .category(r.getRCP_PAT2())
                                .calorie(r.getINFO_ENG())
                                .build()
                );
            }
        }
        return RecipeRecommendResultResponseDto.builder()
                .recipes(recipeRecommendResponseDto)
                .build();
    }

    public List<Map.Entry<Long, Integer>> SortRecipe(MappingRequestDto requestDto){
        List<FoodRecipe> foodRecipes = foodRecipeRepository.findAllByIngredient_Id(requestDto.getBaseId());
        for(Long subId : requestDto.getSubId()){
            List<FoodRecipe> sub = foodRecipeRepository.findAllByIngredient_Id(subId);
            foodRecipes.addAll(sub);
        }

        Map<Long, Integer> map = new HashMap<>();
        for(FoodRecipe r : foodRecipes){
            Long key = r.getRecipe().getId();
            if(map.containsKey(key)) map.put(key,map.get(key)+1);
            else map.put(key,0);
        }

        List<Map.Entry<Long, Integer>> entryList = new LinkedList<>(map.entrySet());
        entryList.sort((o1, o2) -> o2.getValue() - o1.getValue());
        return entryList;
    }
}
