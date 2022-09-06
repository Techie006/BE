package com.sparta.cookbank.domain.donerecipe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutrientsRatioResponseDto {
    private List<String> days;
    private long[] carbohydrates;
    private long[] proteins;
    private long[] fats;
    private long[] sodium;
}
