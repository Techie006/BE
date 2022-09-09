package com.sparta.cookbank.domain.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarWeekResponseDto {
    private List<String> days;
    private List<List> meals;
}
