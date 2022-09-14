package com.sparta.cookbank.service;

import com.sparta.cookbank.domain.LikeRecipe;
import com.sparta.cookbank.domain.member.Member;
import com.sparta.cookbank.domain.recipe.Recipe;
import com.sparta.cookbank.domain.recipe.dto.*;
import com.sparta.cookbank.repository.LikeRecipeRepository;
import com.sparta.cookbank.repository.MemberRepository;
import com.sparta.cookbank.repository.RecipeRepository;
import com.sparta.cookbank.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final LikeRecipeRepository likeRecipeRepository;
    private final MemberRepository memberRepository;

    // 추천 레시피 조회
    @Transactional(readOnly = true)
    public RecipeRecommendResultResponseDto getRecommendRecipe(RecipeRecommendRequestDto requestDto) {
        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(() -> {
            throw new IllegalArgumentException("로그인한 유저를 찾을 수 없습니다.");
        });

        // base 재료가 포함된 모든 레시피를 가져옴
        List<Recipe> recipeList = recipeRepository.findByRecommendRecipeOption(requestDto);
        List<RecipeRecommendResponseDto> recipeRecommendResponseDto = new ArrayList<>();
        for (Recipe recipe : recipeList) {
            // 메인 재료들을  리스트에 담음
            List<String> mainIngredientsList = new ArrayList<>();
            mainIngredientsList.add(recipe.getMAIN_INGREDIENTS());
            // 모든 재료들을 리스트에 담음
            List<String> ingredientsList = new ArrayList<>();
            ingredientsList.add(recipe.getRCP_PARTS_DTLS());
            for (int i = 0; i < requestDto.getFoods().size(); i++) {
                if (recipe.getRCP_PARTS_DTLS().matches(requestDto.getFoods().get(i))) {
                    recipeRecommendResponseDto.add(
                            RecipeRecommendResponseDto.builder()
                                    .id(recipe.getId())
                                    .recipe_name(recipe.getRCP_NM())
                                    .common_ingredients(mainIngredientsList)
                                    .ingredients(ingredientsList)
                                    .method(recipe.getRCP_WAY2())
                                    .category(recipe.getRCP_PAT2())
                                    .calorie(recipe.getINFO_ENG())
                                    .build()
                    );
                }
            }
            recipeRecommendResponseDto.add(
                    RecipeRecommendResponseDto.builder()
                            .id(recipe.getId())
                            .recipe_name(recipe.getRCP_NM())
                            .common_ingredients(mainIngredientsList)
                            .ingredients(ingredientsList)
                            .method(recipe.getRCP_WAY2())
                            .category(recipe.getRCP_PAT2())
                            .calorie(recipe.getINFO_ENG())
                            .build()
            );
        }
        RecipeRecommendResultResponseDto responseDto = RecipeRecommendResultResponseDto.builder()
                .recipes(recipeRecommendResponseDto)
                .build();

        return responseDto;
    }

    // 레시피 상세 조회
    @Transactional(readOnly = true)
    public RecipeDetailResultResponseDto getDetailRecipe(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id를 입력해주세요!");
        }

        Recipe recipe = recipeRepository.findById(id).orElseThrow(() -> {
            throw new IllegalArgumentException("해당 레시피가 존재하지 않습니다.");
        });

        // 해당 내용을 Redis에 저장


        // 재료들을 리스트에 담음
        List<String> ingredientsList = new ArrayList<>();
        ingredientsList.add(recipe.getRCP_PARTS_DTLS());

        // 방법들을 리스트에 담음
        List<String> manualDescList = new ArrayList<>();
        manualDescList.add(recipe.getMANUAL01());
        manualDescList.add(recipe.getMANUAL02());
        manualDescList.add(recipe.getMANUAL03());
        manualDescList.add(recipe.getMANUAL04());
        manualDescList.add(recipe.getMANUAL05());
        manualDescList.add(recipe.getMANUAL06());

        // 방법의 이미지들을 리스트에 담음
        List<String> manualImgList = new ArrayList<>();
        manualImgList.add(recipe.getMANUAL_IMG01());
        manualImgList.add(recipe.getMANUAL_IMG02());
        manualImgList.add(recipe.getMANUAL_IMG03());
        manualImgList.add(recipe.getMANUAL_IMG04());
        manualImgList.add(recipe.getMANUAL_IMG05());
        manualImgList.add(recipe.getMANUAL_IMG06());

        RecipeDetailResponseDto detailResponseDto = RecipeDetailResponseDto.builder()
                .id(id)
                .recipe_name(recipe.getRCP_NM())
                .ingredients(ingredientsList)
                .method(recipe.getRCP_WAY2())
                .category(recipe.getRCP_PAT2())
                .calorie(recipe.getINFO_ENG())
                .calbohydrates(recipe.getINFO_CAR())
                .proteins(recipe.getINFO_PRO())
                .fats(recipe.getINFO_FAT())
                .sodium(recipe.getINFO_NA())
                .final_img(recipe.getATT_FILE_NO_MK())
                .manual_desc(manualDescList)
                .manual_imgs(manualImgList)
                .build();

        RecipeDetailResultResponseDto resultResponseDto = RecipeDetailResultResponseDto.builder()
                .recipe(detailResponseDto)
                .build();

        return resultResponseDto;
    }

    // 레시피 전체 조회
    @Transactional(readOnly = true)
    public RecipeResponseDto getAllRecipe(Pageable pageable) {

        Page<Recipe> recipePage = recipeRepository.findAll(pageable);

        List<RecipeAllResponseDto> recipeAllResponseDtoList = ConverterAllResponseDto(recipePage);

        RecipeResponseDto recipeResponseDto = RecipeResponseDto.builder()
                .current_page_num(recipePage.getPageable().getPageNumber())
                .total_page_num(recipePage.getTotalPages())
                .recipes(recipeAllResponseDtoList)
                .build();

        return recipeResponseDto;
    }

    // 레시피 검색
    @Transactional(readOnly = true)
    public RecipeResponseDto searchRecipe(RecipeSearchRequestDto searchRequestDto, Pageable pageable) {

        // pageable과 requestdto를 이용해서 조회
        Page<Recipe> recipePage = recipeRepository.findBySearchOption(searchRequestDto,pageable);

        // List형태로 각각 분리
        List<RecipeAllResponseDto> recipeAllResponseDtoList = ConverterAllResponseDto(recipePage);

        // api 설계형식에 맞게 담아줌
        RecipeResponseDto recipeResponseDto = RecipeResponseDto.builder()
                .current_page_num(recipePage.getPageable().getPageNumber())
                .total_page_num(recipePage.getTotalPages())
                .recipes(recipeAllResponseDtoList)
                .build();

        return recipeResponseDto;
    }

    // 북마크 On
    @Transactional
    public void likeRecipe(Long id) {
        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(() -> {
            throw new IllegalArgumentException("로그인한 유저를 찾을 수 없습니다.");
        });
        Recipe recipe = recipeRepository.findById(id).orElseThrow(() -> {
            throw  new IllegalArgumentException("해당 레시피를 찾을 수 없습니다.");
        });
        LikeRecipe findLikeRecipe = likeRecipeRepository.findByMember_IdAndRecipe_Id(member.getId(), recipe.getId());
        if (!(findLikeRecipe == null)) {
            throw new IllegalArgumentException("이미 북마크된 레시피 입니다.");
        }
        LikeRecipe likeRecipe = LikeRecipe.builder()
                .member(member)
                .recipe(recipe)
                .build();
        likeRecipeRepository.save(likeRecipe);
    }

    // 북마크 Off
    @Transactional
    public void unlikeRecipe(Long id) {
        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(() -> {
            throw new IllegalArgumentException("로그인한 유저를 찾을 수 없습니다.");
        });
        Recipe recipe = recipeRepository.findById(id).orElseThrow(() -> {
            throw  new IllegalArgumentException("해당 레시피를 찾을 수 없습니다.");
        });
        LikeRecipe likeRecipe = likeRecipeRepository.findByMember_IdAndRecipe_Id(member.getId(), recipe.getId());
        if (likeRecipe == null) {
            throw new IllegalArgumentException("이미 삭제한 레시피입니다.");
        }
        likeRecipeRepository.delete(likeRecipe);
    }

    private List<RecipeAllResponseDto> ConverterAllResponseDto(Page<Recipe> recipes) {
        List<RecipeAllResponseDto> recipeAllResponseDtoList = new ArrayList<>();
        List<String> ingredientsList = new ArrayList<>();
        for (Recipe recipe : recipes){
            ingredientsList.add(recipe.getRCP_PARTS_DTLS());
            recipeAllResponseDtoList.add(
                    RecipeAllResponseDto.builder()
                            .id(recipe.getId())
                            .recipe_name(recipe.getRCP_NM())
                            .ingredients(ingredientsList)
                            .final_img(recipe.getATT_FILE_NO_MK())
                            .method(recipe.getRCP_WAY2())
                            .category(recipe.getRCP_PAT2())
                            .calorie(recipe.getINFO_ENG())
                            .build()
            );
        }
        return recipeAllResponseDtoList;
    }
}
