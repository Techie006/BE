package com.sparta.cookbank.service;

import com.sparta.cookbank.ResponseDto;
import com.sparta.cookbank.domain.LikeRecipe;
import com.sparta.cookbank.domain.calendar.Calendar;
import com.sparta.cookbank.domain.calendar.dto.*;
import com.sparta.cookbank.domain.member.Member;
import com.sparta.cookbank.domain.recipe.Recipe;
import com.sparta.cookbank.redis.calendar.RedisDayCalendar;
import com.sparta.cookbank.redis.calendar.RedisDayCalendarRepo;
import com.sparta.cookbank.redis.recipe.RedisRecipe;
import com.sparta.cookbank.redis.recipe.RedisRecipeRepo;
import com.sparta.cookbank.repository.CalendarRepository;
import com.sparta.cookbank.repository.LikeRecipeRepository;
import com.sparta.cookbank.repository.MemberRepository;
import com.sparta.cookbank.repository.RecipeRepository;
import com.sparta.cookbank.security.SecurityUtil;
import com.sparta.cookbank.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final RecipeRepository recipeRepository;
    private final LikeRecipeRepository likeRecipeRepository;
    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final CalendarRepository calendarRepository;
    private final RedisRecipeRepo redisRecipeRepo;
    private final RedisDayCalendarRepo redisDayCalendarRepo;

    @Transactional(readOnly = true)
    public ResponseDto<?> getSpecificDayDiet(String day, HttpServletRequest request) {

        //토큰 유효성 검사
        extracted(request);

        // 멤버 유효성 검사
        Member member = getMember();
        //레디스에서 찾기
        String redisDay = member.getEmail() + day;
        Optional<RedisDayCalendar> redisDayCalendar = redisDayCalendarRepo.findById(redisDay);
        if (redisDayCalendar.isEmpty()){
            //해당 날짜 캘린더 다 찾기
            List<CalendarResponseDto> dtoList = new ArrayList<>();
            List<CalendarResponseDto> mealList = getCalendar(day, member,dtoList);

            if(mealList.isEmpty()){
                CalendarListResponseDto ListResponseDto = CalendarListResponseDto.builder()
                        .empty(true)
                        .day(day)
                        .meals(null)
                        .build();
                return ResponseDto.success(ListResponseDto,"성공적으로 해당 날짜의 식단을 조회하였습니다.");
            }

            CalendarListResponseDto ListResponseDto = CalendarListResponseDto.builder()
                    .empty(false)
                    .day(day)
                    .meals(mealList)
                    .build();
            // 레디스 저장
            RedisDayCalendar redisCalendar = RedisDayCalendar.builder()
                    .id(redisDay)
                    .empty(true)
                    .meals(ListResponseDto)
                    .build();
            redisDayCalendarRepo.save(redisCalendar);

            return ResponseDto.success(ListResponseDto,"성공적으로 해당 날짜의 식단을 조회하였습니다.");

        }else {
            // 레디스에 있다면 레디스 불러오기
            RedisDayCalendar calendarList = redisDayCalendar.get();
            CalendarListResponseDto ListResponseDto = calendarList.getMeals();

            return ResponseDto.success(ListResponseDto,"성공적으로 해당 날짜의 식단을 조회하였습니다.");
        }



    }


    @Transactional
    public ResponseDto<?> createSpecificDayDiet(CalendarRequestDto requestDto, HttpServletRequest request) {
        //토큰 유효성 검사
        extracted(request);

        // 멤버 유효성 검사
        Member member = getMember();


        //레시피
        Optional<Recipe> recipeOptional = recipeRepository.findById(requestDto.getRecipe_id());
        if (recipeOptional.isEmpty()){
            throw new NullPointerException("해당 레시피를 잘못 입력 하셨습니다.");
        }
        Recipe recipe = recipeOptional.get();

        Calendar calendar = Calendar.builder()
                .member(member)
                .recipe(recipe)
                .mealDay(requestDto.getDay())
                .mealDivision(requestDto.getCategory())
                .build();
        calendarRepository.save(calendar);

        //북마크 확인하기
        boolean liked = false;
        LikeRecipe likedRecipe = likeRecipeRepository.findByMember_IdAndRecipe_Id(member.getId(), recipe.getId());
        if (!(likedRecipe == null)) {
            liked = true;
        }


        CalendarResponseDto calendarResponseDto = CalendarResponseDto.builder()
                .id(calendar.getId())
                .recipe_id(calendar.getRecipe().getId())
                .recipe_name(calendar.getRecipe().getRCP_NM())
                .time(calendar.getMealDivision().toString())
                .day(calendar.getMealDay())
                .liked(liked)
                .category(calendar.getRecipe().getRCP_PAT2())
                .calorie(calendar.getRecipe().getINFO_ENG())
                .method(calendar.getRecipe().getRCP_WAY2())
                .build();

        CalendarMealsDto calendarMealsDto = CalendarMealsDto.builder()
                .meals(calendarResponseDto)
                .day(calendar.getMealDay())
                .build();

        //레디스 캐싱 초기화
        String redisDay = member.getEmail() + requestDto.getDay();
        redisDayCalendarRepo.deleteById(redisDay);
        return ResponseDto.success(calendarMealsDto, "성공적으로 해당 날짜에 식단을 생성하였습니다");
    }

    @Transactional
    public ResponseDto<?> updateSpecificDayDiet(Long id, CalendarRequestDto requestDto, HttpServletRequest request) {
        //토큰 유효성 검사
        extracted(request);

        // 멤버 유효성 검사
        Member member = getMember();

        Calendar calendar = calendarRepository.findById(id).orElseThrow(
                () -> new NullPointerException("해당 날짜에 작성된 식캘린더가 없습니다.")
        );

        //타인 캘린더일시 차단
        if(!getMember().getId().equals(calendar.getMember().getId())){
            throw new RuntimeException("타인의 캘린더를 변경할 수 없습니다.");
        }



        // Request 에서 레시피에서 찾아야됨
        Optional<Recipe> recipeOptional = recipeRepository.findById(requestDto.getRecipe_id());
        if (recipeOptional.isEmpty()){
            throw new NullPointerException("해당 레시피를 잘못 입력 하셨습니다.");
        }
        Recipe recipe = recipeOptional.get();

        String beforeDay = calendar.getMealDay();


        //레디스 캐싱 초기화redisAfterDay
        String redisAfterDay = member.getEmail() + requestDto.getDay();
        String redisBeforeDay = member.getEmail() + beforeDay;
        redisDayCalendarRepo.deleteById(redisAfterDay);
        redisDayCalendarRepo.deleteById(redisBeforeDay);

        // db 업데이트
        calendar.update(requestDto, recipe);



        //북마크 확인하기
        boolean liked = false;
        LikeRecipe likedRecipe = likeRecipeRepository.findByMember_IdAndRecipe_Id(member.getId(),recipe.getId());
        if(!(likedRecipe==null)){
            liked = true;
        }


        CalendarResponseDto calendarResponseDto = CalendarResponseDto.builder()
                .id(calendar.getId())
                .recipe_id(calendar.getRecipe().getId())
                .recipe_name(calendar.getRecipe().getRCP_NM())
                .time(calendar.getMealDivision().toString())
                .day(calendar.getMealDay())
                .liked(liked)
                .category(calendar.getRecipe().getRCP_PAT2())
                .calorie(calendar.getRecipe().getINFO_ENG())
                .method(calendar.getRecipe().getRCP_WAY2())
                .build();

        CalendarMealsDto calendarMealsDto = CalendarMealsDto.builder()
                .meals(calendarResponseDto)
                .day(calendar.getMealDay())
                .build();


        return ResponseDto.success(calendarMealsDto,"성공적으로 해당 날짜의 식단을 변경하였습니다.");
    }


    public ResponseDto<?> deleteSpecificDayDiet(Long id, HttpServletRequest request) {
        //토큰 유효성 검사
        extracted(request);

        // 멤버 유효성 검사
        Member member = getMember();

        Calendar calendar = calendarRepository.findById(id).orElseThrow(
                () -> new NullPointerException("해당 날짜에 작성된 식캘린더가 없습니다.")
        );

        //타인 캘린더일시 차단
        if(!getMember().getId().equals(calendar.getMember().getId())){
            throw new RuntimeException("타인의 캘린더를 삭제할 수 없습니다.");
        }

        Optional<Recipe> recipeOptional = recipeRepository.findById(calendar.getRecipe().getId());
        if (recipeOptional.isEmpty()){
            throw new NullPointerException("해당 레시피를 잘못 입력 하셨습니다.");
        }
        Recipe recipe = recipeOptional.get();


        //북마크 확인하기
        boolean liked = false;
        LikeRecipe likedRecipe = likeRecipeRepository.findByMember_IdAndRecipe_Id(member.getId(),recipe.getId());
        if(!(likedRecipe==null)){
            liked = true;
        }

        CalendarResponseDto calendarResponseDto = CalendarResponseDto.builder()
                .id(calendar.getId())
                .recipe_id(calendar.getRecipe().getId())
                .recipe_name(calendar.getRecipe().getRCP_NM())
                .time(calendar.getMealDivision().toString())
                .day(calendar.getMealDay())
                .liked(liked)
                .category(calendar.getRecipe().getRCP_PAT2())
                .calorie(calendar.getRecipe().getINFO_ENG())
                .method(calendar.getRecipe().getRCP_WAY2())
                .build();
        CalendarMealsDto calendarMealsDto = CalendarMealsDto.builder()
                .meals(calendarResponseDto)
                .day(calendar.getMealDay())
                .build();

        calendarRepository.delete(calendar);
        //캘린더 업데이트후 레디스 캐싱 초기화
        String redisDay = member.getEmail() + calendar.getMealDay();
        redisDayCalendarRepo.deleteById(redisDay);


        return ResponseDto.success(calendarMealsDto,"성공적으로 해당 날짜에 식단을 삭제하였습니다.");

    }

    @Transactional(readOnly = true)
    public ResponseDto<?> getSpecificWeekDiet(String day, HttpServletRequest request) throws ParseException {
        //토큰 유효성 검사
        extracted(request);

        // 멤버 유효성 검사
        Member member = getMember();

        // 요일 구하기, dayOfWeekNumber = 월요일 1 ~ 일요일 7 // 2022-09-01
        int year = Integer.parseInt(day.substring(0, 4));
        int month = Integer.parseInt(day.substring(5, 7));
        int dayOfMonth = Integer.parseInt(day.substring(8));

        LocalDate date = LocalDate.of(year, month, dayOfMonth);
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        int dayOfWeekNumber = dayOfWeek.getValue();

        // 리스트 넣기..


        List<String> daysList = new ArrayList<>();
        List<CalendarResponseDto> dtoList = new ArrayList<>();

        // 현재 날짜 Calendar사용
        Date inPutDay = new SimpleDateFormat("yyyy-MM-dd").parse(day);
        java.util.Calendar cal = java.util.Calendar.getInstance();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");  // 날짜형식
        cal.setTime(inPutDay); // 입력한 날짜로 계산 함!


        switch (dayOfWeekNumber) {
            case 1:  //월요일 , 앞 1
                cal.add(java.util.Calendar.DATE, -1);
                inputWeekDiet(member, dtoList, daysList, cal, df);
                break;
            case 2:  //화요일, 앞 2
                cal.add(java.util.Calendar.DATE, -2);
                inputWeekDiet(member, dtoList, daysList, cal, df);
                break;
            case 3:  //수요일 앞 3
                cal.add(java.util.Calendar.DATE, -3);
                inputWeekDiet(member, dtoList, daysList, cal, df);
                break;
            case 4:  //목요일 앞 4
                cal.add(java.util.Calendar.DATE, -4);
                inputWeekDiet(member, dtoList, daysList, cal, df);
                break;
            case 5:  //금요일 앞 5
                cal.add(java.util.Calendar.DATE, -5);
                inputWeekDiet(member, dtoList, daysList, cal, df);
                break;
            case 6:  //토요일 앞 6
                cal.add(java.util.Calendar.DATE, -6);
                inputWeekDiet(member, dtoList, daysList, cal, df);

                break;
            case 7:  //일요일
                inputWeekDiet(member, dtoList, daysList, cal, df);
                break;
        }

        CalendarWeekResponseDto weekList =  CalendarWeekResponseDto.builder()
                .days(daysList)
                .meals(dtoList)
                .build();

        return ResponseDto.success(weekList,"성공적으로 해당 날짜에 식단을 생성하였습니다");
    }

    @Transactional(readOnly = true)
    public ResponseDto<?> getSpecificMonthDiet(HttpServletRequest request) throws ParseException {
        //토큰 유효성 검사
        extracted(request);

        // 멤버 유효성 검사
        Member member = getMember();

//        // date 구하기      2022-09
//        int year = Integer.parseInt(day.substring(0, 4));
//        int month = Integer.parseInt(day.substring(5, 7));
//        int dayOfMonth = 1;
//        // 해당월의 일수 구하기
//        YearMonth yearMonth = YearMonth.of(year, month);
//        int daysInMonth = yearMonth.lengthOfMonth();
//
//
//        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");  // 날짜형식
//        List<List> list = new ArrayList<>();
//
//        for(int i =1 ; i<daysInMonth+1 ; i++){
//            String stringDay = String.valueOf(i);
//            Date inPutDay = new SimpleDateFormat("yyyy-MM-dd").parse(day+"-"+stringDay);
//            String oneDay = df.format(inPutDay);
//            // 해당월 캘린더 들고오기
//            List<CalendarResponseDto> dtoList = getCalendar(oneDay, member);
//
//            if(!dtoList.isEmpty()){
//                list.add(dtoList);
//            }
//
//        }

        List<Calendar> calendarList = calendarRepository.findAllByMember_Id(member.getId());
        List<CalendarResponseDto> dtoList = new ArrayList<>();
        for(int i = 0 ; i < calendarList.size() ; i++){

            //나의 레시피 찾기
            Optional<Recipe> recipeOptional = recipeRepository.findById(calendarList.get(i).getRecipe().getId());
            if (recipeOptional.isEmpty()){
                throw new NullPointerException("해당 레시피를 잘못 입력 하셨습니다.");
            }
            Recipe recipe = recipeOptional.get();

            //북마크 확인하기
            boolean liked = false;
            LikeRecipe likedRecipe = likeRecipeRepository.findByMember_IdAndRecipe_Id(member.getId(),recipe.getId());
            if(!(likedRecipe==null)){
                liked = true;
            }

            dtoList.add(CalendarResponseDto.builder()
                    .id(calendarList.get(i).getId())
                    .recipe_id(calendarList.get(i).getRecipe().getId())
                    .recipe_name(calendarList.get(i).getRecipe().getRCP_NM())
                    .time(calendarList.get(i).getMealDivision().toString())
                    .day(calendarList.get(i).getMealDay())
                    .liked(liked)
                    .category(calendarList.get(i).getRecipe().getRCP_PAT2())
                    .calorie(calendarList.get(i).getRecipe().getINFO_ENG())
                    .method(calendarList.get(i).getRecipe().getRCP_WAY2())
                    .build());
        }

        CalendarMonthResponseDto monthList = CalendarMonthResponseDto.builder()
                .meals(dtoList)
                .build();
        return ResponseDto.success(monthList,"성공적으로 해당 날짜에 식단을 생성하였습니다");
    }

    private void inputWeekDiet(Member member,  List<CalendarResponseDto> dtoList, List<String> daysList, java.util.Calendar cal, DateFormat df) {
        for(int i = 0 ; i < 7  ; i++){
            String oneDay = df.format(cal.getTime());  // oneDay "Mon Sep 05 00:00:00 KST 2022",
            //해당 하루 캘린더 식단 리스트 만들기
            getCalendar(oneDay, member, dtoList);
            daysList.add(oneDay);

            cal.add(java.util.Calendar.DATE, +1);
        }
    }

    private void extracted(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        token = resolveToken(token);
        tokenProvider.validateToken(token);
    }

    private Member getMember() {
        return memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(
                () -> new IllegalArgumentException("해당 유저가 존재하지 않습니다.")
        );
    }

    private String resolveToken(String token){
        if(token.startsWith("Bearer "))
            return token.substring(7);
        throw new RuntimeException("not valid token !!");
    }

    private List<CalendarResponseDto> getCalendar(String day, Member member, List<CalendarResponseDto> dtoList) {
        List<Calendar> calendarList = calendarRepository.findAllByMealDayAndMember_Id(day, member.getId());
//        List<CalendarResponseDto> dtoList = new ArrayList<>();


        for(int i = 0 ; i < calendarList.size() ; i++){

            //나의 레시피 찾기
            //나의 레시피 찾기
            Optional<Recipe> recipeOptional = recipeRepository.findById(calendarList.get(i).getRecipe().getId());
            if (recipeOptional.isEmpty()){
                throw new NullPointerException("해당 레시피를 잘못 입력 하셨습니다.");
            }
            Recipe recipe = recipeOptional.get();


            //북마크 확인하기
            boolean liked = false;
            LikeRecipe likedRecipe = likeRecipeRepository.findByMember_IdAndRecipe_Id(member.getId(),recipe.getId());
            if(!(likedRecipe==null)){
                liked = true;
            }

            dtoList.add(CalendarResponseDto.builder()
                    .id(calendarList.get(i).getId())
                    .recipe_id(calendarList.get(i).getRecipe().getId())
                    .recipe_name(calendarList.get(i).getRecipe().getRCP_NM())
                    .time(calendarList.get(i).getMealDivision().toString())
                    .day(calendarList.get(i).getMealDay())
                    .liked(liked)
                    .category(calendarList.get(i).getRecipe().getRCP_PAT2())
                    .calorie(calendarList.get(i).getRecipe().getINFO_ENG())
                    .method(calendarList.get(i).getRecipe().getRCP_WAY2())
                    .build());
        }
        return dtoList;
    }

}
