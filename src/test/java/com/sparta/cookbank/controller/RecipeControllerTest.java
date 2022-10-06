package com.sparta.cookbank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.cookbank.config.SecurityConfig;
import com.sparta.cookbank.domain.recipe.Recipe;
import com.sparta.cookbank.domain.recipe.dto.*;
import com.sparta.cookbank.security.TokenProvider;
import com.sparta.cookbank.service.RecipeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// JPA 기능 동작 x, Service, Repository 사용 x, securityconfig scan 에서 제외
@WebMvcTest(controllers = RecipeController.class,
        excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)})
@MockBean(JpaMetamodelMappingContext.class)
class RecipeControllerTest {

    // 웹 api 테스트할 때 사용, 스프링 MVC 테스트의 시작점
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    TokenProvider tokenProvider;

    @MockBean
    private RecipeService recipeService;

    @BeforeEach
    public void setUp(@Autowired WebApplicationContext applicationContext) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .alwaysDo(print())
                .build();
    }

    List<Recipe> recipeSetUp() {
        List<Recipe> recipeList = new ArrayList<>();

        List<String> recipeNameList = Arrays.asList("고등어 무 조림", "마늘 장아찌", "간장 계란밥", "닭 칼국수", "마늘 덮밥");

        for (long i = 0; i < 5; i++) {
            recipeList.add(
                    Recipe.builder()
                            .id(i)
                            .RCP_NM(recipeNameList.get((int) i))
                            .RCP_WAY2("끓이기" + i)
                            .RCP_PAT2("반찬" + i)
                            .INFO_ENG(326 * i)
                            .INFO_CAR(43 * i)
                            .INFO_PRO(22 * i)
                            .INFO_FAT(7 * i)
                            .INFO_NA(976 * i)
                            .ATT_FILE_NO_MAIN("음식 이미지 소" + i)
                            .ATT_FILE_NO_MK("음식 이미지 대" + i)
                            .RCP_PARTS_DTLS("고등어, 닭고기, 양파, 고추, 마늘, 무")
                            .MANUAL01("방법1 - " + i).MANUAL_IMG01("이미지1 - " + i)
                            .MANUAL01("방법2 - " + i).MANUAL_IMG01("이미지2 - " + i)
                            .MANUAL01("방법3 - " + i).MANUAL_IMG01("이미지3 - " + i)
                            .MANUAL01("방법4 - " + i).MANUAL_IMG01("이미지4 - " + i)
                            .MANUAL01("방법5 - " + i).MANUAL_IMG01("이미지5 - " + i)
                            .MANUAL01("방법6 - " + i).MANUAL_IMG01("이미지6 - " + i)
                            .MAIN_INGREDIENTS("고등어, 닭고기, 고추")
                            .build()
            );
        }
        return recipeList;
    }

    @Test
    @DisplayName("[API][POST] 추천 레시피")
    void RecommendRecipe() throws Exception {
        // given
        String base = "고등어";
        List<String> foods = List.of(new String[]{"양파", "마늘", "간장", "고추", "파"});
        RecipeRecommendRequestDto requestDto = new RecipeRecommendRequestDto(base, foods);

        List<Recipe> recipeList = recipeSetUp();
        // custom pageable 등록하려면..?
        Pageable pageable = PageRequest.of(0, 20);
        Page<Recipe> pageResult = new PageImpl<Recipe>(recipeList, pageable, recipeList.size());

        List<RecipeRecommendDto> recipeRecommendDto = new ArrayList<>();
        for (Recipe recipe : pageResult) {
            List<String> mainIngredientsList = new ArrayList<>();
            mainIngredientsList.add(recipe.getMAIN_INGREDIENTS());
            List<String> ingredientsList = new ArrayList<>();
            ingredientsList.add(recipe.getRCP_PARTS_DTLS());
            recipeRecommendDto.add(
                    RecipeRecommendDto.builder()
                            .id(recipe.getId())
                            .recipe_name(recipe.getRCP_NM())
                            .recipe_image(recipe.getATT_FILE_NO_MAIN())
                            .liked(false)
                            .common_ingredients(mainIngredientsList)
                            .ingredients(ingredientsList)
                            .method(recipe.getRCP_WAY2())
                            .category(recipe.getRCP_PAT2())
                            .calorie(recipe.getINFO_ENG())
                            .build()
            );
        }

        RecipeRecommendResponseDto resultResponseDto = RecipeRecommendResponseDto.builder()
                .current_page_num(pageResult.getPageable().getPageNumber())
                .total_page_num(pageResult.getTotalPages())
                .recipes(recipeRecommendDto)
                .build();

        given(recipeService.getRecommendRecipe(requestDto, pageable)).willReturn(resultResponseDto);

        String content = objectMapper.writeValueAsString(requestDto);


        // when
        final ResultActions actions = mockMvc.perform(post("/api/recipes/recommend") // perform 요청을 전송하는 역할
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("pageNum", "0")
                        .param("sizeLimit", "20")
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andDo(print());


        // then
        actions
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.['result']").value(true))
                .andExpect(jsonPath("$.['content']['recipes'][0]['ingredients'][0]").value(containsString("고등어")))
                .andExpect(jsonPath("$.['status']['code']").value("200"))
                .andExpect(jsonPath("$.['status']['message']").value("추천레시피 제공에 성공하였습니다."));

        verify(recipeService, times(1)).getRecommendRecipe(requestDto, pageable);
    }

    @Test
    @DisplayName("[API][GET] 레시피 상세 조회")
    void DetailRecipe() throws Exception {

        // given
        Recipe recipe = recipeSetUp().get(0);

        RecipeDetailDto recipeDetailDto = RecipeDetailDto.builder()
                .id(recipe.getId())
                .recipe_name(recipe.getRCP_NM())
                .ingredients(null)
                .method(recipe.getRCP_WAY2())
                .category(recipe.getRCP_PAT2())
                .calorie(recipe.getINFO_ENG())
                .carbohydrates(recipe.getINFO_CAR())
                .proteins(recipe.getINFO_PRO())
                .fats(recipe.getINFO_FAT())
                .sodium(recipe.getINFO_NA())
                .final_img(recipe.getATT_FILE_NO_MK())
                .manual_desc(null)
                .manual_imgs(null)
                .build();

        given(recipeService.getDetailRecipe(anyLong())).willReturn(
                RecipeDetailResponseDto.builder()
                        .recipe(recipeDetailDto)
                        .build()
        );

        // when & then
        mockMvc.perform(get("/api/recipe/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.['result']").value(true))
                .andExpect(jsonPath("$.['content']['recipe']['id']").value(0))
                .andExpect(jsonPath("$.['status']['code']").value("200"))
                .andExpect(jsonPath("$.['status']['message']").value("레시피 제공에 성공하였습니다."));

        verify(recipeService, times(1)).getDetailRecipe(anyLong());
    }

    @Test
    @DisplayName("[API][GET] 레시피 전체 조회")
    void AllRecipe() throws Exception {
        List<Recipe> recipeList = recipeSetUp();
        Pageable pageable = PageRequest.of(0, 20);
        Page<Recipe> recipePage = new PageImpl<>(recipeList, pageable, recipeList.size());
        List<RecipeBasicDto> recipeBasicDtoList = new ArrayList<>();

        for (Recipe recipe : recipePage) {
            recipeBasicDtoList.add(
                    RecipeBasicDto.builder()
                            .id(recipe.getId())
                            .recipe_name(recipe.getRCP_NM())
                            .ingredients(null)
                            .final_img(recipe.getATT_FILE_NO_MK())
                            .method(recipe.getRCP_WAY2())
                            .category(recipe.getRCP_PAT2())
                            .calorie(recipe.getINFO_ENG())
                            .liked(false)
                            .build()
            );
        }

        given(recipeService.getAllRecipe(pageable)).willReturn(
                RecipeResponseDto.builder()
                        .current_page_num(recipePage.getPageable().getPageNumber())
                        .total_page_num(recipePage.getTotalPages())
                        .recipes(recipeBasicDtoList)
                        .build()
        );

        // when
        final ResultActions actions = mockMvc.perform(get("/api/recipes")
                .param("pageNum", "0")
                .param("pageLimit", "20"));

        //then
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['result']").value(true))
                .andExpect(jsonPath("$.['content']['recipes']", hasSize(5)))
                .andExpect(jsonPath("$.['status']['code']").value("200"))
                .andExpect(jsonPath("$.['status']['message']").value("전체레시피 제공에 성공하였습니다."));

        verify(recipeService, times(1)).getAllRecipe(pageable);
    }

    @Test
    @DisplayName("[API][POST] 레시피 검색")
    void SearchRecipe() throws Exception {
        List<Recipe> recipeList = recipeSetUp();

        Pageable pageable = PageRequest.of(0,20);
        Page<Recipe> recipePage = new PageImpl<>(recipeList, pageable, recipeList.size());

        String recipe_name = "마늘"; // 1, 4
        RecipeSearchRequestDto searchRequestDto = new RecipeSearchRequestDto(recipe_name);

        List<RecipeBasicDto> recipeBasicDtoList = new ArrayList<>();

        for (Recipe recipe : recipeList) {
            if (recipe.getRCP_NM().contains("마늘")){
                recipeBasicDtoList.add(
                        RecipeBasicDto.builder()
                                .id(recipe.getId())
                                .recipe_name(recipe.getRCP_NM())
                                .ingredients(null)
                                .final_img(recipe.getATT_FILE_NO_MK())
                                .method(recipe.getRCP_WAY2())
                                .category(recipe.getRCP_PAT2())
                                .calorie(recipe.getINFO_ENG())
                                .liked(false)
                                .build()
                );
            }
        }

        given(recipeService.searchRecipe(searchRequestDto, pageable)).willReturn(
                RecipeSearchResponseDto.builder()
                        .current_page_num(recipePage.getPageable().getPageNumber())
                        .total_page_num(recipePage.getTotalPages())
                        .recipes(recipeBasicDtoList)
                        .search_name(searchRequestDto.getRecipe_name())
                        .build()
        );

        String content = objectMapper.writeValueAsString(searchRequestDto);

        // when
        final ResultActions actions = mockMvc.perform(post("/api/recipes/search")
                .content(content)
                .contentType(MediaType.APPLICATION_JSON)
                .param("pageNum", "0")
                .param("sizeLimit", "20")
                .accept(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8"));

        // then
        actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['result']").value(true))
                .andExpect(jsonPath("$.['content']['recipes']", hasSize(2)))
                .andExpect(jsonPath("$.['content']['recipes'][0]['recipe_name']").value(containsString("마늘")))
                .andExpect(jsonPath("$.['content']['recipes'][1]['recipe_name']").value(containsString("마늘")))
                .andExpect(jsonPath("$.['status']['code']").value("200"))
                .andExpect(jsonPath("$.['status']['message']").value("레시피 검색에 성공하였습니다."));

        verify(recipeService, times(1)).searchRecipe(searchRequestDto, pageable);
    }
}