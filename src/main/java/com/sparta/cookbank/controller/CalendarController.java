package com.sparta.cookbank.controller;

import com.sparta.cookbank.ResponseDto;
import com.sparta.cookbank.domain.calendar.dto.CalendarRequestDto;
import com.sparta.cookbank.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;

@RequiredArgsConstructor
@RestController
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping("/api/calendar/day") // 특정한 날 조회
    public ResponseDto<?> getSpecificDayDiet(@RequestParam("day") String day, HttpServletRequest request){
        return calendarService.getSpecificDayDiet(day,request);
    }

    @PostMapping("/api/calendar")    // 캘린더 생성
    public ResponseDto<?> createSpecificDayDiet(@RequestBody CalendarRequestDto requestDto,HttpServletRequest request){
        return calendarService.createSpecificDayDiet(requestDto,request);
    }

    @PutMapping("/api/calendar/{id}")   // 캘린더 수정
    public ResponseDto<?> updateSpecificDayDiet(@PathVariable Long id,
                                                HttpServletRequest request,
                                                @RequestBody CalendarRequestDto requestDto){
        return calendarService.updateSpecificDayDiet(id, requestDto, request);
    }

    @DeleteMapping("/api/calendar/{id}")
    public ResponseDto<?> deleteSpecificDayDiet(@PathVariable Long id,HttpServletRequest request){
        return calendarService.deleteSpecificDayDiet(id,request);
    }

    @GetMapping("/api/calendar/week")
    public ResponseDto<?> getSpecificWeekDiet(@RequestParam("day") String day,HttpServletRequest request ) throws ParseException {
        return calendarService.getSpecificWeekDiet(day, request);
    }

    @GetMapping("/api/calendar/all")
    public ResponseDto<?> getSpecificMonthDiet(HttpServletRequest request) throws ParseException {
        return calendarService.getSpecificMonthDiet(request);
    }
}
