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
public class CalendarListResponseDto {
    private boolean empty;
    private List<CalendarResponseDto> meals;
    private String day;
}
