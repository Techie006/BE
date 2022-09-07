package com.sparta.cookbank.controller;

import com.sparta.cookbank.ResponseDto;
import com.sparta.cookbank.domain.calendar.dto.CalendarRequestDto;
import com.sparta.cookbank.service.CalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RequiredArgsConstructor
@RestController
public class CalendarController {

    private final CalendarService calendarService;

    @GetMapping("/api/calendar/day") // 특정한 날 조회
    public ResponseDto<?> getSpecificDayDiet(@RequestParam("day") String day, HttpServletRequest request){
        return calendarService.getSpecificDayDiet(day,request);
    }

    @PostMapping("/api/calendar")
    public ResponseDto<?> createSpecificDayDiet(@RequestBody CalendarRequestDto requestDto,HttpServletRequest request){
        return calendarService.createSpecificDayDiet(requestDto,request);
    }

}
