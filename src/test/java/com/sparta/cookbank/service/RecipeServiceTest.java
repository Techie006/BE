package com.sparta.cookbank.service;

import com.sparta.cookbank.domain.recipe.Recipe;
import com.sparta.cookbank.domain.recipe.dto.*;
import com.sparta.cookbank.repository.LikeRecipeRepository;
import com.sparta.cookbank.repository.MemberRepository;
import com.sparta.cookbank.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.predicate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    RecipeRepository recipeRepository;

    @Mock
    LikeRecipeRepository likeRecipeRepository;

    @Mock
    MemberRepository memberRepository;

    @InjectMocks
    RecipeService recipeService;

    @BeforeEach
    void setup() {
        this.recipeService =new RecipeService(recipeRepository,likeRecipeRepository, memberRepository);
    }

    @Nested
    @DisplayName("DetailRecipe")
    class DetailRecipe {
//        @Test
//        @DisplayName("정상 케이스")
//        void getDetailRecipe_Normal() {
//            // given
//            Long recipeId = 1L;
//
//            when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(new Recipe()));
//            // when
//
//            RecipeDetailResultResponseDto result = recipeService.getDetailRecipe(recipeId);
//            //then
//            verify(recipeRepository, times(1)).findById(recipeId);
//            assertThat(result.getRecipe().getId()).isEqualTo(recipeId);
//        }

        @Test
        @DisplayName("Id가 null이 들어왔을 때")
        void getDetailRecipe_NullFail(){
            // given
            Long recipeId = null;

            //when

            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                recipeService.getDetailRecipe(recipeId);
            });

            //then
            assertEquals("id를 입력해주세요!", exception.getMessage());
        }

        @Test
        @DisplayName("입력한 recipe의 id가 존재하지 않을 때")
        void getDetailRecipe_nonExistentFail() {
            // given
            Long recipeId = 10321L;

            // when
            Exception exception = assertThrows(IllegalArgumentException.class, () -> {
                recipeService.getDetailRecipe(recipeId);
            });

            //then
            assertEquals("해당 레시피가 존재하지 않습니다.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("AllRecipe")
    class AllRecipe {
        @Test
        @DisplayName("Pageable 모든 레시피 조회")
        void getAllRecipe() {

            // given
            List<Recipe> recipeList = new ArrayList<>();
            for (long i =0; i < 15; i++) {
                recipeList.add(
                        Recipe.builder()
                                .id(i)
                                .build()
                );
            }

            Pageable pageable = Pageable.ofSize(5);
            Page<Recipe> recipePage = new PageImpl<>(recipeList,pageable, recipeList.size());

            when(recipeRepository.findAll(pageable)).thenReturn(recipePage);

            // when
            RecipeResponseDto result = recipeService.getAllRecipe(pageable);

            // then
            verify(recipeRepository,times(1)).findAll(pageable);
            assertThat(result.getTotal_page_num()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("SearchRecipe")
    class SearchRecipe {

        @Test
        @DisplayName("정상 케이스")
        void getSearchRecipe_Normal() {

            // given
            String recipeName = "마늘";
            Pageable pageable = Pageable.ofSize(5);

            RecipeSearchRequestDto requestDto = new RecipeSearchRequestDto(recipeName);

            List<Recipe> recipeList = new ArrayList<>();
            List<String> recipeResultName = List.of(new String[]{"마늘무조림", " 마늘탕", " 마늘구이", "마늘밥", "마늘제육볶음"});
            for (int i =0; i < 5; i++) {
                recipeList.add(
                        Recipe.builder()
                                .RCP_NM(recipeResultName.get(i))
                                .build()
                );
            }

            Page<Recipe> recipePage = new PageImpl<>(recipeList, pageable, recipeList.size());

            when(recipeRepository.findBySearchOption(any(),any())).thenReturn(recipePage);

            // when
            RecipeResponseDto result = recipeService.searchRecipe(requestDto,pageable);

            // then
            verify(recipeRepository, times(1)).findBySearchOption(requestDto,pageable);
            assertThat(result.getRecipes().get(0).getRecipe_name()).contains(requestDto.getRecipe_name());
            assertThat(result.getTotal_page_num()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("AutoComplete")
    class AutoComplete {

        @Test
        @DisplayName("정상 케이스")
        void getAutoComplete_Normal() {
            String keyword = "마늘";

            AutoCompleteRequestDto requestDto = new AutoCompleteRequestDto(keyword);
            List<Recipe> recipeList = new ArrayList<>();
            for (long i = 0; i < 5; i++) {
                recipeList.add(
                        Recipe.builder()
                                .id(i)
                                .RCP_NM("마늘된장찌개" + i)
                                .build()
                );
            }


            when(recipeRepository.findAllByRCP_NM(requestDto.getKeyword())).thenReturn(recipeList);

            AutoCompleteResultResponseDto result = recipeService.getAutoComplete(requestDto);

            verify(recipeRepository, times(1)).findAllByRCP_NM(any());
            assertThat(result.getRecipes().get(0).getRecipe_name()).contains(requestDto.getKeyword());
            assertThat(result.isEmpty()).isFalse();
        }
    }
}