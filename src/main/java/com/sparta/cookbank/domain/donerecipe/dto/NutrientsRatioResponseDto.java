package com.sparta.cookbank.domain.donerecipe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutrientsRatioResponseDto {
    private List<LocalDate> days;
    private List<Long> carbohydrates;
    private List<Long> proteins;
    private List<Long> fats;
    private List<Long> sodium;
}
