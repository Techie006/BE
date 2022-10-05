package com.sparta.cookbank.controller;

import com.sparta.cookbank.ResponseDto;
import com.sparta.cookbank.domain.ingredient.dto.IngredientsRatioResponseDto;
import com.sparta.cookbank.domain.myingredients.dto.IngredientRequestDto;
import com.sparta.cookbank.service.IngredientService;
import com.sparta.cookbank.service.MemberService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.time.Duration;

@RequiredArgsConstructor
@RestController
public class IngredientController {

    private final IngredientService ingredientService;
    private final Bucket bucket;

    @Autowired
    public IngredientController(IngredientService ingredientService){
        this.ingredientService = ingredientService;

        //Refill.intervally token = 1000, 1회충전시 1000개의 토큰을 충전
        //Duration.ofSeconds = 1, 1초마다 토큰을 충전
        //Duration.ofMinutes = 1, 1분마다 토큰을 충전
        //Bandwidth capacity = Bucket의 총 크기는 1000
        Bandwidth limit = Bandwidth.classic(1000, Refill.intervally(1000, Duration.ofMinutes(1)));
        this.bucket = Bucket.builder()
                .addLimit(limit)
                .build();
    }


    @GetMapping("/api/ingredients/autocomplete")  // 식재료 자동완성
    public ResponseDto<?> findAutoIngredient(@RequestParam("foodname") String requestDto, HttpServletRequest request
                                             ){
        if(bucket.tryConsume(1)) {
            return ingredientService.findAutoIngredient(requestDto,request);
        }else{
            return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }

    @PostMapping("/api/ingredients/search")  // 식재료 검색
    public ResponseDto<?> findIngredient(@RequestBody IngredientRequestDto requestDto, HttpServletRequest request,
                                         @PageableDefault(size = 5) Pageable pageable){
        if(bucket.tryConsume(1)) {
            return ingredientService.findIngredient(requestDto.getFood_name(),request,pageable);
        }else{
            return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }

    @PostMapping("/api/ingredient")  // 식재료 작성
    public ResponseDto<?> saveMyIngredient(@RequestBody IngredientRequestDto requestDto, HttpServletRequest request) throws ParseException {
        if(bucket.tryConsume(1)) {
            return ingredientService.saveMyIngredient(requestDto,request);
        }else{
            return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }

    @GetMapping("/api/ingredient") //식재료 전체 조회회
    public ResponseDto<?> getAllMyIngredient(HttpServletRequest request) throws ParseException {
        if(bucket.tryConsume(1)) {
            return ingredientService.getAllMyIngredient(request);
        }else{
            return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }

    @GetMapping("/api/ingredients") // 저장소별 식재료 조회
    public ResponseDto<?> getMyIngredient(@RequestParam("storage") String storage, HttpServletRequest request) throws ParseException {
        if(bucket.tryConsume(1)) {
            return ingredientService.getMyIngredient(storage,request);
        }else{
            return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }

    @GetMapping("/api/ingredients/detail") // 카테고리별 식재료 조회
    public ResponseDto<?> getMyCategoryIngredient(@RequestParam("category") String category,HttpServletRequest request) throws ParseException {
        if(bucket.tryConsume(1)) {
            return ingredientService.getMyCategoryIngredient(category,request);
        }else{
            return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }


    @GetMapping("/api/ingredients/warning") // 임박 식재료 조회
    public ResponseDto<?> getMyWarningIngredient(HttpServletRequest request) throws ParseException {
        if(bucket.tryConsume(1)) {
            return ingredientService.getMyWarningIngredient(request);
        }else{
            return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }

    @DeleteMapping("/api/ingredient") //나의 냉장고 식재료 삭제
    public ResponseDto<?> deleteMyIngredient(@RequestParam("id") Long myIngredientId,HttpServletRequest request){
        if(bucket.tryConsume(1)) {
            return ingredientService.deleteMyIngredient(myIngredientId,request);
        }else{
            return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }

    @GetMapping("/api/statistics/state") // 우리집 냉장고 상태표시
    public ResponseDto<?> MyRefrigeratorState(){
        if(bucket.tryConsume(1)) {
            IngredientsRatioResponseDto stateResponseDto =ingredientService.MyRefrigeratorState();
            return ResponseDto.success(stateResponseDto, "냉장고 상태 제공에 성공하였습니다.");
        }else{
            return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }

    @GetMapping("/api/statistics/category") // (통계)제품류 나눠서 보여주기
    public ResponseDto<?> ingredientsByCategory() {
        if(bucket.tryConsume(1)) {
            IngredientsRatioResponseDto categoryResponseDto = ingredientService.ingredientsByCategory();
            return ResponseDto.success(categoryResponseDto,"성공");
        }else{
            return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }
}
