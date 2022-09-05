package com.sparta.cookbank.service;

import com.sparta.cookbank.ResponseDto;
import com.sparta.cookbank.domain.ingredient.Ingredient;
import com.sparta.cookbank.domain.ingredient.dto.*;
import com.sparta.cookbank.domain.member.Member;
import com.sparta.cookbank.domain.Storage;
import com.sparta.cookbank.domain.myingredients.MyIngredients;
import com.sparta.cookbank.domain.myingredients.dto.IngredientRequestDto;
import com.sparta.cookbank.domain.myingredients.dto.MyIngredientResponseDto;
import com.sparta.cookbank.domain.myingredients.dto.StorageResponseDto;
import com.sparta.cookbank.domain.myingredients.dto.WarningResponseDto;
import com.sparta.cookbank.repository.IngredientsRepository;
import com.sparta.cookbank.repository.MemberRepository;
import com.sparta.cookbank.repository.MyIngredientsRepository;
import com.sparta.cookbank.security.SecurityUtil;
import com.sparta.cookbank.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientService {

    private final IngredientsRepository ingredientsRepository;
    private final MemberRepository memberRepository;
    private final MyIngredientsRepository myIngredientsRepository;
    private final TokenProvider tokenProvider;


    @Transactional(readOnly = true)
    public ResponseDto<?> findAutoIngredient(String food_name, HttpServletRequest request) {

        // Token 유효성 검사 없음

        //해당 검색어 찾기
        List<Ingredient> ingredients = ingredientsRepository.findAllByFoodNameIsContaining(food_name);
        // DTO사용
        List<IngredientResponseDto> dtoList = new ArrayList<>();
        // 5개만 보여주기
        for(int i=0; i<5; i++){
            dtoList.add(IngredientResponseDto.builder()
                    .id(ingredients.get(i).getId())
                    .food_name(ingredients.get(i).getFoodName())
                    .group_name(ingredients.get(i).getFoodCategory())
                    .build());
        }

        AutoIngredientResponseDto responseDto = AutoIngredientResponseDto.builder()
                .auto_complete(dtoList)
                .build();


        return ResponseDto.success(responseDto,"자동완성 리스트 제공에 성공하였습니다.");
    }

    @Transactional(readOnly = true)
    public ResponseDto<?> findIngredient(String food_name, HttpServletRequest request) {

        // Token 유효성 검사 없음

        //해당 검색 찾기
        List<Ingredient> ingredients = ingredientsRepository.findAllByFoodNameIsContaining(food_name);

        // DTO사용
        List<IngredientResponseDto> dtoList = new ArrayList<>();

        for (Ingredient ingredient : ingredients) {
            dtoList.add(IngredientResponseDto.builder()
                    .id(ingredient.getId())
                    .food_name(ingredient.getFoodName())
                    .group_name(ingredient.getFoodCategory())
                    .build());
        }

        TotalIngredientResponseDto responseDto = TotalIngredientResponseDto.builder()
                .total_count(dtoList.size())
                .search_list(dtoList)
                .build();
        return ResponseDto.success(responseDto,"식재료 검색에 성공하였습니다.");
    }
    @Transactional
    public ResponseDto<?> saveMyIngredient(IngredientRequestDto requestDto, HttpServletRequest request) {

        //토큰 유효성 검사
        extracted(request);

        // 멤버 유효성 검사
        Member member = getMember();
        //재료찾기
        Ingredient ingredient = ingredientsRepository.findById(requestDto.getId()).orElseThrow(
                () -> new IllegalArgumentException("해당 음식 재료가 존재 하지 않습니다.")
        );

        MyIngredients myIngredients = MyIngredients.builder()
                .member(member)
                .ingredient(ingredient)
                .storage(requestDto.getStorage())
                .inDate(requestDto.getIn_date())
                .expDate(requestDto.getExp_date())
                .build();
        myIngredientsRepository.save(myIngredients);

        return ResponseDto.success("","작성완료");
    }


    @Transactional(readOnly = true)
    public ResponseDto<?> getMyIngredient(String storage, HttpServletRequest request) throws ParseException {
        //토큰 유효성 검사
        extracted(request);

        // 멤버 유효성 검사
        Member member = getMember();


        // 나의 재료 전체조회
        if(storage.equals("")){
            List<MyIngredients> myIngredients = myIngredientsRepository.findAllByMemberId(member.getId());
            List<MyIngredientResponseDto> dtoList = new ArrayList<>();
            StorageResponseDto responseDto = getStorageResponseDto(myIngredients, dtoList);

            return ResponseDto.success(responseDto,"리스트 제공에 성공하였습니다.");
        }else {
            // Storage별 조회
            Storage storage1 = Storage.valueOf(storage);
            List<MyIngredients> myIngredients = myIngredientsRepository.findByMemberIdAndStorage(member.getId(), storage1);
            List<MyIngredientResponseDto> dtoList = new ArrayList<>();
            StorageResponseDto responseDto = getStorageResponseDto(myIngredients, dtoList);

            return ResponseDto.success(responseDto,"리스트 제공에 성공하였습니다.");

        }


    }

    @Transactional(readOnly = true)
    public ResponseDto<?> getMyWarningIngredient(HttpServletRequest request) throws ParseException {
        //토큰 유효성 검사
        extracted(request);

        // 멤버 유효성 검사
        Member member = getMember();

        List<MyIngredients> myIngredients = myIngredientsRepository.findAllByMemberId(member.getId());
        List<MyIngredientResponseDto> outList = new ArrayList<>();
        List<MyIngredientResponseDto> hurryList = new ArrayList<>();

        //현재시각으로 d_day 구하기
        LocalDate now = LocalDate.now();
        String nowString = now.toString();

        for (MyIngredients myIngredient : myIngredients){
            Date outDay = new SimpleDateFormat("yyyy-MM-dd").parse(myIngredient.getExpDate());
            Date nowDay = new SimpleDateFormat("yyyy-MM-dd").parse(nowString);
            Long diffSec= (outDay.getTime()-nowDay.getTime())/1000;
            Long diffDays = diffSec / (24*60*60);
            String d_day;
            if(diffDays < 0){  // 유통기한 넘을시 추가..
                diffDays = -diffDays;
                d_day ="+"+diffDays.toString();
                outList.add(MyIngredientResponseDto.builder()
                        .id(myIngredient.getId())
                        .food_name(myIngredient.getIngredient().getFoodName())
                        .group_name(myIngredient.getIngredient().getFoodCategory())
                        .in_date(myIngredient.getInDate())
                        .d_date("D"+ d_day)
                        .build());

            }else if(diffDays < 7) {     // 7일 미만 HurryList 추가.
                d_day ="-"+diffDays.toString();
                hurryList.add(MyIngredientResponseDto.builder()
                        .id(myIngredient.getId())
                        .food_name(myIngredient.getIngredient().getFoodName())
                        .group_name(myIngredient.getIngredient().getFoodCategory())
                        .in_date(myIngredient.getInDate())
                        .d_date("D"+ d_day)
                        .build());
            }
        }

        WarningResponseDto responseDto = WarningResponseDto.builder()
                .out_dated_num(outList.size())
                .in_hurry_num(hurryList.size())
                .out_dated(outList)
                .in_hurry(hurryList)
                .build();

        return ResponseDto.success(responseDto,"리스트 제공에 성공하였습니다");
    }

    @Transactional
    public ResponseDto<?> deleteMyIngredient(Long myIngredientId, HttpServletRequest request) {
        //토큰 유효성 검사
        extracted(request);

        // 멤버 유효성 검사
        getMember();



        //재료 유효성 검사
        MyIngredients myIngredients = myIngredientsRepository.findById(myIngredientId).orElseThrow(
                () -> new IllegalArgumentException("이미 삭제된 재료입니다.")
        );

        //로그인한 멤버 id와 작성된 재료의 멤버 id와 다를시 예외처리
        if(!getMember().getId().equals(myIngredients.getMember().getId())){
            throw new RuntimeException("타인의 식재료를 삭제할 수 없습니다.");
        }

        myIngredientsRepository.delete(myIngredients);

        return ResponseDto.success("","재료 삭제가 성공하였습니다.");
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

    private StorageResponseDto getStorageResponseDto(List<MyIngredients> myIngredients, List<MyIngredientResponseDto> dtoList) throws ParseException {
        //현재시각으로 d_day 구하기
        LocalDate now = LocalDate.now();
        String nowString = now.toString();

        for (MyIngredients myIngredient : myIngredients) {
            Date outDay = new SimpleDateFormat("yyyy-MM-dd").parse(myIngredient.getExpDate());
            Date nowDay = new SimpleDateFormat("yyyy-MM-dd").parse(nowString);
            Long diffSec= (outDay.getTime()-nowDay.getTime())/1000;  //밀리초로 나와서 1000을 나눠야지 초 차이로됨
            Long diffDays = diffSec / (24*60*60); // 일자수 차이
            String d_day;
            if(diffDays < 0){
                diffDays = -diffDays;
                d_day ="+"+diffDays.toString();
            }else {
                d_day ="-"+diffDays.toString();
            }

            dtoList.add(MyIngredientResponseDto.builder()
                    .id(myIngredient.getId())
                    .food_name(myIngredient.getIngredient().getFoodName())
                    .group_name(myIngredient.getIngredient().getFoodCategory())
                    .in_date(myIngredient.getInDate())
                    .d_date("D"+ d_day)
                    .build());
        }

        StorageResponseDto responseDto = StorageResponseDto.builder()
                .storage(dtoList)
                .build();
        return responseDto;
    }

    // 나만의 냉장고 상태 표시
    @Transactional(readOnly = true)
    public RefrigeratorStateResponseDto MyRefrigeratorState() {
        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(() -> {
            throw new IllegalArgumentException("로그인 한 유저를 찾을 수 없습니다.");
        });
        List<MyIngredients> myIngredientsList = myIngredientsRepository.findAllByMemberId(member.getId());

        List<Double> percentage = new ArrayList<>();
        List<Integer> count = new ArrayList<>();

        int worningCount = 0;
        int in_hurryCount = 0;
        int fineCount = 0;
        for (MyIngredients myIngredients : myIngredientsList) {
            String match = "[^0-9]";
            int exp_date = Integer.parseInt(myIngredients.getExpDate().replaceAll(match,""));
            int in_date = Integer.parseInt(myIngredients.getInDate().replaceAll(match,""));
            if ((exp_date - in_date) <= 3 && (exp_date - in_date) > 0) { // 남은 유통기한이 3일 이내일때
                worningCount++;
            } else if ((exp_date - in_date) <= 5 && (exp_date - in_date) > 3) { // 남은 유통기한이 5일 이하 3일 미만일때
                in_hurryCount++;
            } else if ((exp_date - in_date) > 5) { // 남은 유통기한이 5일 이상일때
                fineCount++;
            } else if ((exp_date - in_date) < 0) { // 유통기한이 지난 재료
                throw new IllegalArgumentException("유통기한이 지난 재료 입니다.");
            }
        }
        // 백분율을 구하고 거기에 Math.round 메소드를 이용해서 소수점 두번째자리까지 구함
        percentage.add (Math.round ((((double) worningCount / (double) myIngredientsList.size() * 100))*100)/100.0);
        percentage.add (Math.round ((((double) in_hurryCount / (double) myIngredientsList.size() * 100))*100)/100.0);
        percentage.add (Math.round ((((double) fineCount / (double) myIngredientsList.size() * 100))*100)/100.0);
        count.add(worningCount);
        count.add(in_hurryCount);
        count.add(fineCount);

        RefrigeratorStateResponseDto refrigeratorStateResponseDto = RefrigeratorStateResponseDto.builder()
                .percentage(percentage)
                .count(count)
                .build();
        return refrigeratorStateResponseDto;
    }

    // 제품류 나눠서 보여주기
    @Transactional(readOnly = true)
    public IngredientsByCategoryResponseDto ingredientsByCategory() {
        Member member = memberRepository.findById(SecurityUtil.getCurrentMemberId()).orElseThrow(() -> {
            throw new IllegalArgumentException("로그인 한 유저를 찾을 수 없습니다.");
        });
        int starch_num = 0;
        int nut_num = 0;
        int cereal_num = 0;
        int fruit_num = 0;
        int etc_num = 0;
        int nan_num = 0;
        int sugar_num = 0;
        int pulses_num = 0;
        int mushroom_num = 0;
        int fish_num = 0;
        int milkProducts_num = 0;
        int fatAndOils_num = 0;
        int meat_num = 0;
        int drink_num = 0;
        int processedFood_num = 0;
        int seasoning_num = 0;
        int alcohol_num = 0;
        int tea_num = 0;
        int vegetable_num = 0;
        int seaweed_num = 0;
        List<MyIngredients> myIngredientsList = myIngredientsRepository.findAllByMemberId(member.getId());
        if (myIngredientsList.isEmpty()) {
            throw new IllegalArgumentException("해당 사용자가 입력한 식재료가 없습니다.");
        }
        for (MyIngredients myIngredients : myIngredientsList) {
            switch (myIngredients.getIngredient().getFoodCategory()){
                case 전분류:
                    starch_num++;
                    break;
                case 견과류:
                    nut_num++;
                    break;
                case 곡류:
                    cereal_num++;
                    break;
                case 과실류:
                    fruit_num++;
                    break;
                case 기타:
                    etc_num++;
                    break;
                case 난류:
                    nan_num++;
                    break;
                case 당류:
                    sugar_num++;
                    break;
                case 두류:
                    pulses_num++;
                    break;
                case 버섯류:
                    mushroom_num++;
                    break;
                case 어패류:
                    fish_num++;
                    break;
                case 유제품류:
                    milkProducts_num++;
                    break;
                case 유지류:
                    fatAndOils_num++;
                    break;
                case 육류:
                    meat_num++;
                    break;
                case 음료류:
                    drink_num++;
                    break;
                case 조리가공품류:
                    processedFood_num++;
                    break;
                case 조미료류:
                    seasoning_num++;
                    break;
                case 주류:
                    alcohol_num++;
                    break;
                case 차류:
                    tea_num++;
                    break;
                case 채소류:
                    vegetable_num++;
                    break;
                case 해조류:
                    seaweed_num++;
                    break;
                default:
            }
        }
        IngredientsByCategoryResponseDto ingredientsByCategoryResponseDto = IngredientsByCategoryResponseDto.builder()
                .starch_num(starch_num)
                .nut_num(nut_num)
                .cereal_num(cereal_num)
                .fruit_num(fruit_num)
                .etc_num(etc_num)
                .nan_num(nan_num)
                .sugar_num(sugar_num)
                .pulses_num(pulses_num)
                .mushroom_num(mushroom_num)
                .fish_num(fish_num)
                .milkProducts_num(milkProducts_num)
                .fatAndOils_num(fatAndOils_num)
                .meat_num(meat_num)
                .drink_num(drink_num)
                .processedFood_num(processedFood_num)
                .seasoning_num(seasoning_num)
                .alcohol_num(alcohol_num)
                .tea_num(tea_num)
                .vegetable_num(vegetable_num)
                .seaweed_num(seaweed_num)
                .build();
        return ingredientsByCategoryResponseDto;
    }
}
