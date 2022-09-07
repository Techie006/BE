package com.sparta.cookbank.domain.donerecipe.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DayRatioDto {
    private Long calories;
    private List<Long> nutrients;
}
