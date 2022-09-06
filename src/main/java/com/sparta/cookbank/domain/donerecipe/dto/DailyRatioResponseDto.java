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
public class DailyRatioResponseDto {
    private List<List<Long>> today;
    private List<List<Long>> yesterday;
}
