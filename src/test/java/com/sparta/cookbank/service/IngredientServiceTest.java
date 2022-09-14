//package com.sparta.cookbank.service;
//
//import com.sparta.cookbank.domain.ingredient.dto.RefrigeratorStateResponseDto;
//import com.sparta.cookbank.domain.member.Member;
//import com.sparta.cookbank.domain.myingredients.MyIngredients;
//import com.sparta.cookbank.repository.IngredientsRepository;
//import com.sparta.cookbank.repository.MemberRepository;
//import com.sparta.cookbank.repository.MyIngredientsRepository;
//import com.sparta.cookbank.security.TokenProvider;
//import com.sparta.cookbank.security.WithMockCustomUser;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.test.context.support.WithMockUser;
//
//import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
//import static org.junit.jupiter.api.Assertions.*;
//
//@ExtendWith(MockitoExtension.class)
//class IngredientServiceTest {
//
//    @Mock
//    IngredientsRepository ingredientsRepository;;
//
//    @Mock
//    MemberRepository memberRepository;;
//
//    @Mock
//    MyIngredientsRepository myIngredientsRepository;;
//
//    @Mock
//    TokenProvider tokenProvider;
//
//    @InjectMocks
//    IngredientService ingredientService;
//
//    @BeforeEach
//    void setup() {
//        this.ingredientService = new IngredientService(ingredientsRepository,memberRepository,myIngredientsRepository,tokenProvider);
//    }
//
//    @Test
//    @WithMockUser
//    @DisplayName("냉장고 상태표시 정상 케이스")
//    void MyRefrigeratorState_Normal() {
//        // given
//
//        Member member = Member.builder()
//                .id(1L)
//                .build();
//
//        MyIngredients myIngredients = MyIngredients.builder()
//                .member(member)
//                .expDate("2022-09-12")
//                .inDate("2022-09-03")
//                .build();
//
//        RefrigeratorStateResponseDto responseDto = ingredientService.MyRefrigeratorState();
//
//        assertThat(responseDto.getCount()).isEqualTo(1);
//
//    }
//}