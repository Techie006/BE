package com.sparta.cookbank.service;

import com.sparta.cookbank.domain.LikeRecipe;
import com.sparta.cookbank.domain.member.Member;
import com.sparta.cookbank.domain.recipe.Recipe;
import com.sparta.cookbank.domain.recipe.dto.*;
import com.sparta.cookbank.redis.recipe.RedisRecipe;
import com.sparta.cookbank.redis.recipe.RedisRecipeRepo;
import com.sparta.cookbank.repository.LikeRecipeRepository;
import com.sparta.cookbank.repository.MemberRepository;
import com.sparta.cookbank.repository.RecipeRepository;
import com.sparta.cookbank.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final LikeRecipeRepository likeRecipeRepository;
    private final MemberRepository memberRepository;
    private final RedisRecipeRepo redisRecipeRepo;

    // 추천 레시피 조회
    @Transactional(readOnly = true)
    public RecipeRecommendResultResponseDto getRecommendRecipe(RecipeRecommendRequestDto requestDto) {
        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(() -> {
            throw new IllegalArgumentException("로그인한 유저를 찾을 수 없습니다.");
        });

        //레디스에서 찾기
        String redisKey = requestDto.getBase()+requestDto.getFoods(); // 고유키
        Optional<RedisRecipe> redisRecipe = redisRecipeRepo.findById(redisKey);

        if(redisRecipe.isEmpty()){ // 레디스 캐시 없을시

            List<Recipe> recipeList = recipeRepository.findByRecommendRecipeOption(requestDto.getBase());
            List<RecipeRecommendResponseDto> recipeRecommendResponseDto = new ArrayList<>();

            HashMap<Recipe, Integer> recipeMap = new LinkedHashMap<>();

            for (Recipe recipe : recipeList) {
                int count = 0;

                // 서브 재료의 개수만큼 반복문을 돌리는데
                for (int i = 0; i < requestDto.getFoods().size(); i++) {
                    // 만약 가져온 recipe 에 검색어 i 번째가 포함되면 count를  1 증가시키고 HashMap에 저장한다. 포함되는게 없으면 Recipe와 함께 0을 저장한다.
                    if (recipe.getRCP_PARTS_DTLS().contains(requestDto.getFoods().get(i))) {
                        count++;
                        recipeMap.put(recipe, count);
                    } else {
                        recipeMap.put(recipe,0);
                    }
                }
            }

            // 저장한 map에서 count를 내림차순으로 정렬하기 위해 list 형태로 map을 가져온다.
            List<Map.Entry<Recipe, Integer>> list = new LinkedList<>(recipeMap.entrySet());
            // 람다식을 통해 내림차순으로 정렬한다.
            list.sort(((o1, o2) -> recipeMap.get(o2.getKey()) - recipeMap.get(o1.getKey())));


        for (Map.Entry<Recipe, Integer> entry : list) {
            boolean liked = false;
            LikeRecipe likeRecipe = likeRecipeRepository.findByMember_IdAndRecipe_Id(member.getId(), entry.getKey().getId());
            if (!(likeRecipe == null)) {
                liked = true;
            }
            // 메인 재료들을  리스트에 담음
            List<String> mainIngredientsList = Arrays.asList(entry.getKey().getMAIN_INGREDIENTS().split(","));
            // 모든 재료들을 리스트에 담음
            List<String> ingredientsList = Arrays.asList(entry.getKey().getRCP_PARTS_DTLS().split(","));
            recipeRecommendResponseDto.add(
                    RecipeRecommendResponseDto.builder()
                            .id(entry.getKey().getId())
                            .recipe_name(entry.getKey().getRCP_NM())
                            .recipe_image(entry.getKey().getATT_FILE_NO_MAIN())
                            .liked(liked)
                            .common_ingredients(mainIngredientsList)
                            .ingredients(ingredientsList)
                            .method(entry.getKey().getRCP_WAY2())
                            .category(entry.getKey().getRCP_PAT2())
                            .calorie(entry.getKey().getINFO_ENG())
                            .build()
            );
        }

            //레디스 캐시 저장
            RedisRecipe saveRedisRecipe = RedisRecipe.builder()
                    .id(redisKey)
                    .recipes(recipeRecommendResponseDto)
                    .build();
            redisRecipeRepo.save(saveRedisRecipe);

            return RecipeRecommendResultResponseDto.builder()
                    .recipes(recipeRecommendResponseDto)
                    .build();

        }else{ // 레디스 캐시 있을시 출력
            RedisRecipe recipes =  redisRecipe.get();
            List<RecipeRecommendResponseDto> recipeRecommendResponseDto = recipes.getRecipes();

            return RecipeRecommendResultResponseDto.builder()
                    .recipes(recipeRecommendResponseDto)
                    .build();

        }

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
        if (!(recipe.getRCP_PARTS_DTLS() == null)) {
            ingredientsList = Arrays.asList(recipe.getRCP_PARTS_DTLS().split(","));
        }


        // 방법들을 리스트에 담음
        List<String> manualDescList = Arrays.asList(
                recipe.getMANUAL01(),
                recipe.getMANUAL02(),
                recipe.getMANUAL03(),
                recipe.getMANUAL04(),
                recipe.getMANUAL05(),
                recipe.getMANUAL06());

        // 방법의 이미지들을 리스트에 담음
        List<String> manualImgList = Arrays.asList(
                recipe.getMANUAL_IMG01(),
                recipe.getMANUAL_IMG02(),
                recipe.getMANUAL_IMG03(),
                recipe.getMANUAL_IMG04(),
                recipe.getMANUAL_IMG05(),
                recipe.getMANUAL_IMG06()
        );

        RecipeDetailResponseDto detailResponseDto = RecipeDetailResponseDto.builder()
                .id(id)
                .recipe_name(recipe.getRCP_NM())
                .ingredients(ingredientsList)
                .method(recipe.getRCP_WAY2())
                .category(recipe.getRCP_PAT2())
                .calorie(recipe.getINFO_ENG())
                .carbohydrates(recipe.getINFO_CAR())
                .proteins(recipe.getINFO_PRO())
                .fats(recipe.getINFO_FAT())
                .sodium(recipe.getINFO_NA())
                .final_img(recipe.getATT_FILE_NO_MK())
                .manual_desc(manualDescList)
                .manual_imgs(manualImgList)
                .build();

        return RecipeDetailResultResponseDto.builder()
                .recipe(detailResponseDto)
                .build();
    }

    // 레시피 전체 조회
    @Transactional(readOnly = true)
    public RecipeResponseDto getAllRecipe(Pageable pageable) {

        Page<Recipe> recipePage = recipeRepository.findAll(pageable);

        List<RecipeAllResponseDto> recipeAllResponseDtoList = converterAllResponseDto(recipePage);

        return RecipeResponseDto.builder()
                .current_page_num(recipePage.getPageable().getPageNumber())
                .total_page_num(recipePage.getTotalPages())
                .recipes(recipeAllResponseDtoList)
                .build();
    }

    // 레시피 검색
    @Transactional(readOnly = true)
    public RecipeResponseDto searchRecipe(RecipeSearchRequestDto searchRequestDto, Pageable pageable) {

        // pageable과 requestdto를 이용해서 조회
        Page<Recipe> recipePage = recipeRepository.findBySearchOption(searchRequestDto,pageable);

        // List형태로 각각 분리
        List<RecipeAllResponseDto> recipeAllResponseDtoList = converterAllResponseDto(recipePage);

        // api 설계형식에 맞게 담아줌

        return RecipeResponseDto.builder()
                .current_page_num(recipePage.getPageable().getPageNumber())
                .total_page_num(recipePage.getTotalPages())
                .recipes(recipeAllResponseDtoList)
                .search_name(searchRequestDto.getRecipe_name())
                .build();
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

    // 북마크 조회
    @Transactional(readOnly = true)
    public RecipeAllBookmarkResponseDto getBookmark(Pageable pageable) {
        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(() -> {
            throw new IllegalArgumentException("로그인한 유저를 찾을 수 없습니다.");
        });

        // pageable과 memberId 를 이용해서 조회
        Page<LikeRecipe> likeRecipeList = likeRecipeRepository.findByMember_Id(member.getId(), pageable);

        // member가 북마크한 레시피가 없으면
        if (likeRecipeList.getContent().isEmpty()) {
            throw new IllegalArgumentException("사용자가 북마크한 레시피가 없습니다.");
        }

        List<RecipeBookmarkResponseDto> recipeBookmarkResponseDtoList = new ArrayList<>();

        for (LikeRecipe likeRecipe : likeRecipeList) {
            List<String> mainIngredientsList = Arrays.asList(likeRecipe.getRecipe().getMAIN_INGREDIENTS().split(","));
            recipeBookmarkResponseDtoList.add(
                    RecipeBookmarkResponseDto.builder()
                            .id(likeRecipe.getRecipe().getId())
                            .recipe_name(likeRecipe.getRecipe().getRCP_NM())
                            .ingredients(mainIngredientsList)
                            .final_img(likeRecipe.getRecipe().getATT_FILE_NO_MK())
                            .method(likeRecipe.getRecipe().getRCP_WAY2())
                            .category(likeRecipe.getRecipe().getRCP_PAT2())
                            .calorie(likeRecipe.getRecipe().getINFO_ENG())
                            .liked(true)
                            .build()
            );
        }

        return RecipeAllBookmarkResponseDto.builder()
                .user_name(member.getUsername())
                .current_page_num(likeRecipeList.getPageable().getPageNumber())
                .total_page_num(likeRecipeList.getTotalPages())
                .recipes(recipeBookmarkResponseDtoList)
                .build();
    }

    // 검색어 자동완성
    @Transactional(readOnly = true)
    public AutoCompleteResultResponseDto getAutoComplete(AutoCompleteRequestDto requestDto) {

        List<Recipe> recipeList = recipeRepository.findAllByRCP_NM(requestDto.getKeyword());
        List<AutoCompleteResponseDto> autoCompleteResponseList = new ArrayList<>();

        boolean empty = false;

        if (recipeList.isEmpty()) {
            empty = true;
        }

        for (Recipe recipe : recipeList) {
            autoCompleteResponseList.add(
                    AutoCompleteResponseDto.builder()
                            .id(recipe.getId())
                            .recipe_name(recipe.getRCP_NM())
                            .build()
            );
        }
        
        return AutoCompleteResultResponseDto.builder()
                .empty(empty)
                .recipes(autoCompleteResponseList)
                .build();
    }

    private List<RecipeAllResponseDto> converterAllResponseDto(Page<Recipe> recipes) {
        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(() -> {
            throw new IllegalArgumentException("로그인한 유저를 찾을 수 없습니다.");
        });
        List<RecipeAllResponseDto> recipeAllResponseDtoList = new ArrayList<>();
        for (Recipe recipe : recipes){
            List<String> mainIngredientsList = new ArrayList<>();
            if (!(recipe.getMAIN_INGREDIENTS() == null)) {
                mainIngredientsList = Arrays.asList(recipe.getMAIN_INGREDIENTS().split(","));
            }

            LikeRecipe likeRecipe = likeRecipeRepository.findByMember_IdAndRecipe_Id(member.getId(),recipe.getId());
            boolean liked = !(likeRecipe == null);

            recipeAllResponseDtoList.add(
                    RecipeAllResponseDto.builder()
                            .id(recipe.getId())
                            .recipe_name(recipe.getRCP_NM())
                            .ingredients(mainIngredientsList)
                            .final_img(recipe.getATT_FILE_NO_MK())
                            .method(recipe.getRCP_WAY2())
                            .category(recipe.getRCP_PAT2())
                            .calorie(recipe.getINFO_ENG())
                            .liked(liked)
                            .build()
            );
        }
        return recipeAllResponseDtoList;
    }

}
