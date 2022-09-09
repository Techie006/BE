package com.sparta.cookbank.domain.calendar.dto;

import com.sparta.cookbank.domain.calendar.enums.MealCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarResponseDto {
    private Long id;
    private String recipe_name;
    private String time;
    private String day;
    private boolean liked;
    private String  category;
    private Long calorie;
    private String method;
}
