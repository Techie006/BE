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
    private String recipe_name;
    private MealCategory category;
    private String day;
}
