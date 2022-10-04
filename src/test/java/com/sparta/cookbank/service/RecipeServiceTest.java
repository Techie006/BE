package com.sparta.cookbank.service;

import com.sparta.cookbank.domain.recipe.Recipe;
import com.sparta.cookbank.domain.recipe.dto.AutoCompleteRequestDto;
import com.sparta.cookbank.domain.recipe.dto.AutoCompleteResponseDto;
import com.sparta.cookbank.domain.recipe.dto.RecipeDetailResponseDto;
import com.sparta.cookbank.redis.recipe.RedisRecipeRepo;
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
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @Mock
    RedisRecipeRepo redisRecipeRepo;

    @BeforeEach
    void setup() {
        this.recipeService =new RecipeService(recipeRepository,likeRecipeRepository, memberRepository,redisRecipeRepo);
    }

    @Nested
    @DisplayName("DetailRecipe")
    class DetailRecipe {
        @Test
        @DisplayName("정상 케이스")
        void getDetailRecipe_Normal() {
            // given
            Long recipeId = 1L;

            when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(new Recipe()));
            // when

            RecipeDetailResponseDto result = recipeService.getDetailRecipe(recipeId);
            //then
            verify(recipeRepository, times(1)).findById(recipeId);
            assertThat(result.getRecipe().getId()).isEqualTo(recipeId);
        }

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

            AutoCompleteResponseDto result = recipeService.getAutoComplete(requestDto);

            verify(recipeRepository, times(1)).findAllByRCP_NM(any());
            assertThat(result.getRecipes().get(0).getRecipe_name()).contains(requestDto.getKeyword());
            assertThat(result.isEmpty()).isFalse();
        }
    }
}