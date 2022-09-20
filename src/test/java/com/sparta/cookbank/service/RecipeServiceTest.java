package com.sparta.cookbank.service;

import com.sparta.cookbank.domain.recipe.Recipe;
import com.sparta.cookbank.domain.recipe.dto.RecipeDetailResultResponseDto;
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

    @BeforeEach
    void setup() {
        this.recipeService =new RecipeService(recipeRepository,likeRecipeRepository, memberRepository);
    }


    @Nested
    @DisplayName("DetailRecipe")
    class DetailRecipe {
        @Test
        @DisplayName("정상케이스")
        void getDetailRecipe_Normal() {
            // given
            Long recipeId = 1L;

            when(recipeRepository.findById(recipeId)).thenReturn(Optional.of(new Recipe()));
            // when

            RecipeDetailResultResponseDto result = recipeService.getDetailRecipe(recipeId);
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

//    @Test
//    @WithMockCustomUser
//    @DisplayName("레시피 북마크 정상 케이스")
//    void likeRecipe() {
//
//        // given
//
//        Recipe testRecipe = Recipe.builder()
//                .id(1L)
//                .ATT_FILE_NO_MAIN("test")
//                .ATT_FILE_NO_MK("test")
//                .INFO_CAR(25L)
//                .INFO_ENG(220L)
//                .INFO_FAT(17L)
//                .INFO_NA(99L)
//                .INFO_PRO(14L)
//                .MANUAL01("test1")
//                .MANUAL02("test1")
//                .MANUAL03("test1")
//                .MANUAL04("test1")
//                .MANUAL05("test1")
//                .MANUAL06("test1")
//                .MANUAL_IMG01("test1")
//                .MANUAL_IMG02("test1")
//                .MANUAL_IMG03("test1")
//                .MANUAL_IMG04("test1")
//                .MANUAL_IMG05("test1")
//                .MANUAL_IMG06("test1")
//                .RCP_NM("test1메뉴")
//                .RCP_PARTS_DTLS("test1재료1, test1재료2, test1재료3")
//                .RCP_PAT2("반찬")
//                .RCP_WAY2("찌기")
//                .MAIN_INGREDIENTS("test1재료1, test1재료2, test1재료3")
//                .build();
//        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(new Member()));
//        when(recipeRepository.findById(anyLong())).thenReturn(Optional.of(new Recipe()));
//        // when
//        recipeService.likeRecipe(testRecipe.getId());
//        LikeRecipe likeRecipe = likeRecipeRepository.findByMember_IdAndRecipe_Id(1L,testRecipe.getId());
//
//        //then
//        assertThat(likeRecipe.getRecipe().getId()).isEqualTo(testRecipe.getId());
//    }
}