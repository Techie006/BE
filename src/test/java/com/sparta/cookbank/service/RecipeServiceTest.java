package com.sparta.cookbank.service;

import com.sparta.cookbank.domain.LikeRecipe;
import com.sparta.cookbank.domain.member.Member;
import com.sparta.cookbank.domain.recipe.Recipe;
import com.sparta.cookbank.domain.recipe.dto.RecipeDetailResultResponseDto;
import com.sparta.cookbank.repository.LikeRecipeRepository;
import com.sparta.cookbank.repository.MemberRepository;
import com.sparta.cookbank.repository.RecipeRepository;
import com.sparta.cookbank.security.WithMockCustomUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssumptions.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    RecipeRepository recipeRepository;

    @Mock
    LikeRecipeRepository likeRecipeRepository;

    @Mock
    MemberRepository memberRepository;

    @Mock
    MemberService memberService;

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
//        // given
//        Member member = Member.builder()
//                .id(1L)
//                .email("admin1234@naver.com")
//                .username("admin1234")
//                .password("admin1234")
//                .image("sadkmk111")
//                .build();
//
//        UserDetails userDetails = new User(member);
//
//        Long recipeId = 1L;
//        Authentication a = SecurityContextHolder.getContext().getAuthentication();
//        UserDetails principal = (UserDetails) a.getPrincipal();
//        Member member = memberRepository.findByEmail(principal.getUsername()).orElseThrow(() -> {
//            throw new IllegalArgumentException("로그인한 유저를 찾을 수 없습니다.");
//        });
//
//        when(likeRecipeRepository.findByMember_IdAndRecipe_Id(member.getId(),anyLong())).thenReturn(new LikeRecipe());
//        // when
//        recipeService.likeRecipe(recipeId);
//        LikeRecipe likeRecipe = likeRecipeRepository.findByMember_IdAndRecipe_Id(member.getId(),recipeId);
//
//        //then
//        assertThat(likeRecipe.getRecipe().getId()).isEqualTo(recipeId);
//    }
}