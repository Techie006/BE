package com.sparta.cookbank.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.cookbank.config.SecurityConfig;
import com.sparta.cookbank.domain.recipe.Recipe;
import com.sparta.cookbank.domain.recipe.dto.RecipeRecommendDto;
import com.sparta.cookbank.domain.recipe.dto.RecipeRecommendRequestDto;
import com.sparta.cookbank.domain.recipe.dto.RecipeRecommendResponseDto;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// JPA 기능 동작 x, Service, Repository 사용 x, securityconfig scan 에서 제외
@WebMvcTest(controllers = RecipeController.class,
        excludeFilters = { @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)})
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
    @DisplayName("[API][POST] 추천 레시피")
    void RecommendRecipe() throws Exception {
        // given
        String base = "고등어";
        List<String> foods = List.of(new String[]{"양파", "마늘", "간장", "고추", "파"});
        RecipeRecommendRequestDto requestDto = new RecipeRecommendRequestDto(base, foods);

        List<Recipe> recipeList = recipeSetUp();
        // custom pageable 등록하려면..?
        Pageable pageable = PageRequest.of(0,20);
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
                .andExpect(MockMvcResultMatchers.jsonPath("$.['result']").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.['content']['recipes'][0]['ingredients'][0]").value(containsString("고등어")))
                .andExpect(MockMvcResultMatchers.jsonPath("$.['status']['code']").value("200"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.['status']['message']").value("추천레시피 제공에 성공하였습니다."))
                .andDo(print());

        verify(recipeService, times(1)).getRecommendRecipe(requestDto,pageable);
    }
}