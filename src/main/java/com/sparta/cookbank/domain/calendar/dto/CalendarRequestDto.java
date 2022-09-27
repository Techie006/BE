package com.sparta.cookbank.domain.calendar.dto;

import com.sparta.cookbank.domain.calendar.enums.MealCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalendarRequestDto {
    private Long recipe_id;
    private MealCategory category;
    private String day;
}
