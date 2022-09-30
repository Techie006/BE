package com.sparta.cookbank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.cookbank.config.SecurityConfig;
import com.sparta.cookbank.domain.recipe.Recipe;
import com.sparta.cookbank.domain.recipe.dto.RecipeRecommendRequestDto;
import com.sparta.cookbank.domain.recipe.dto.RecipeRecommendResponseDto;
import com.sparta.cookbank.domain.recipe.dto.RecipeRecommendResultResponseDto;
import com.sparta.cookbank.security.TokenProvider;
import com.sparta.cookbank.service.RecipeService;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// JPA 기능 동작 x, Service, Repository 사용 x, securityconfig scan 에서 제외
@WebMvcTest(controllers = RecipeController.class,
        excludeFilters = { @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)})
@MockBean(JpaMetamodelMappingContext.class)
class RecipeControllerTest {

    // 웹 api 테스츠할 때 사용, 스프링 MVC 테스트의 시작점
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

        for (long i = 0; i < 5; i++) {
            recipeList.add(
                    Recipe.builder()
                            .id(i)
                            .RCP_NM("고등어 무조림" + i)
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
    @DisplayName("추천 레시피")
    void RecommendRecipe() throws Exception {
        // given
        List<Recipe> recipeList = recipeSetUp();
        List<RecipeRecommendResponseDto> recipeRecommendResponseDto = new ArrayList<>();
        for (Recipe recipe : recipeList) {
            List<String> mainIngredientsList = new ArrayList<>();
            mainIngredientsList.add(recipe.getMAIN_INGREDIENTS());
            List<String> ingredientsList = new ArrayList<>();
            ingredientsList.add(recipe.getRCP_PARTS_DTLS());
            recipeRecommendResponseDto.add(
                    RecipeRecommendResponseDto.builder()
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

        String base = "고등어";
        List<String> foods = List.of(new String[]{"양파", "마늘", "간장", "고추", "파"});
        RecipeRecommendRequestDto requestDto = new RecipeRecommendRequestDto(base, foods);

        RecipeRecommendResultResponseDto resultResponseDto = RecipeRecommendResultResponseDto.builder()
                .recipes(recipeRecommendResponseDto)
                .build();

        given(recipeService.getRecommendRecipe(requestDto)).willReturn(resultResponseDto);

        String content = objectMapper.writeValueAsString(requestDto);


        // when
        final ResultActions actions = mockMvc.perform(post("/api/recipes/recommend") // perform 요청을 전송하는 역할
                        .content(content)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andDo(print());


        // then
        actions
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        verify(recipeService, times(1)).getRecommendRecipe(requestDto);


    }
}