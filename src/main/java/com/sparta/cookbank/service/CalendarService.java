package com.sparta.cookbank.service;

import com.sparta.cookbank.ResponseDto;
import com.sparta.cookbank.domain.LikeRecipe;
import com.sparta.cookbank.domain.calendar.Calendar;
import com.sparta.cookbank.domain.calendar.dto.CalendarRequestDto;
import com.sparta.cookbank.domain.calendar.dto.CalendarResponseDto;
import com.sparta.cookbank.domain.donerecipe.DoneRecipe;
import com.sparta.cookbank.domain.member.Member;
import com.sparta.cookbank.domain.recipe.Recipe;
import com.sparta.cookbank.repository.*;
import com.sparta.cookbank.security.SecurityUtil;
import com.sparta.cookbank.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final RecipeRepository recipeRepository;
    private final LikeRecipeRepository likeRecipeRepository;
    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final CalendarRepository calendarRepository;


    public ResponseDto<?> getSpecificDayDiet(String day, HttpServletRequest request) {

        //토큰 유효성 검사
        extracted(request);

        // 멤버 유효성 검사
        Member member = getMember();





        return ResponseDto.success("엄","준식");
    }


    @Transactional
    public ResponseDto<?> createSpecificDayDiet(CalendarRequestDto requestDto, HttpServletRequest request) {
        //토큰 유효성 검사
        extracted(request);

        // 멤버 유효성 검사
        Member member = getMember();

        //레시피 찾기
        Recipe recipe = recipeRepository.findByRCP_NM(requestDto.getRecipe_name());

        Calendar calendar = Calendar.builder()
                .member(member)
                .recipe(recipe)
                .mealDay(requestDto.getDay())
                .mealDivision(requestDto.getCategory())
                .build();
        calendarRepository.save(calendar);

        boolean liked = false;
        LikeRecipe likedRecipe = likeRecipeRepository.findByMember_IdAndRecipe_Id(member.getId(),recipe.getId());
        if(!(likedRecipe==null)){
           liked = true;
        }


        CalendarResponseDto calendarResponseDto = CalendarResponseDto.builder()
                .id(calendar.getId())
                .recipe_name(calendar.getRecipe().getRCP_NM())
                .time(calendar.getMealDivision().toString())
                .day(calendar.getMealDay())
                .liked(liked)
                .category(calendar.getRecipe().getRCP_PAT2())
                .calorie(calendar.getRecipe().getINFO_ENG())
                .method(calendar.getRecipe().getRCP_WAY2())
                .build();

        return ResponseDto.success(calendarResponseDto,"성공적으로 해당 날짜에 식단을 생성하였습니다");
    }

    private void extracted(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        token = resolveToken(token);
        tokenProvider.validateToken(token);
    }

    private Member getMember() {
        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(
                () -> new IllegalArgumentException("해당 유저가 존재하지 않습니다.")
        );
        return member;
    }

    private String resolveToken(String token){
        if(token.startsWith("Bearer "))
            return token.substring(7);
        throw new RuntimeException("not valid token !!");
    }
}
