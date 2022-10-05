package com.sparta.cookbank.controller;

import com.sparta.cookbank.ResponseDto;
import com.sparta.cookbank.domain.calendar.dto.CalendarRequestDto;
import com.sparta.cookbank.service.CalendarService;
import com.sparta.cookbank.service.ChatService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.time.Duration;

@RequiredArgsConstructor
@RestController
public class CalendarController {

    private final CalendarService calendarService;

    private final Bucket bucket;

    @Autowired
    public CalendarController(CalendarService calendarService){
        this.calendarService = calendarService;

        //Refill.intervally token = 1000, 1회충전시 1000개의 토큰을 충전
        //Duration.ofSeconds = 1, 1초마다 토큰을 충전
        //Duration.ofMinutes = 1, 1분마다 토큰을 충전
        //Bandwidth capacity = Bucket의 총 크기는 1000
        Bandwidth limit = Bandwidth.classic(2000, Refill.intervally(2000, Duration.ofMinutes(1)));
        this.bucket = Bucket.builder()
                .addLimit(limit)
                .build();
    }

    @GetMapping("/api/calendar/day") // 특정한 날 조회
    public ResponseDto<?> getSpecificDayDiet(@RequestParam("day") String day, HttpServletRequest request){
        if(bucket.tryConsume(1)) {
            return calendarService.getSpecificDayDiet(day,request);
        }else{
            return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }

    @PostMapping("/api/calendar")    // 캘린더 생성
    public ResponseDto<?> createSpecificDayDiet(@RequestBody CalendarRequestDto requestDto,HttpServletRequest request){
        if(bucket.tryConsume(1)) {
            return calendarService.createSpecificDayDiet(requestDto,request);
        }else{
            return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }

    @PutMapping("/api/calendar/{id}")   // 캘린더 수정
    public ResponseDto<?> updateSpecificDayDiet(@PathVariable Long id,
                                                HttpServletRequest request,
                                                @RequestBody CalendarRequestDto requestDto){
        if(bucket.tryConsume(1)) {
            return calendarService.updateSpecificDayDiet(id, requestDto, request);
        }else{
            return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }

    @DeleteMapping("/api/calendar/{id}") // 캘린더 삭제
    public ResponseDto<?> deleteSpecificDayDiet(@PathVariable Long id,HttpServletRequest request){
        if(bucket.tryConsume(1)) {
            return calendarService.deleteSpecificDayDiet(id,request);
        }else{
            return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }

    @GetMapping("/api/calendar/week") // 주별 캘린더 조회
    public ResponseDto<?> getSpecificWeekDiet(@RequestParam("day") String day,HttpServletRequest request ) throws ParseException {
        if(bucket.tryConsume(1)) {
            return calendarService.getSpecificWeekDiet(day, request);
        }else{
            return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }

    @GetMapping("/api/calendar/all") //전체조회
    public ResponseDto<?> getSpecificMonthDiet(HttpServletRequest request) throws ParseException {
        if(bucket.tryConsume(1)) {
            return calendarService.getSpecificMonthDiet(request);
        }else{
            return ResponseDto.fail("233","트레픽 요청이 너무 많습니다.");
        }
    }
}
