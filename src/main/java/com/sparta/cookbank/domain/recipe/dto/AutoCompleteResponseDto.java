package com.sparta.cookbank.domain.recipe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutoCompleteResponseDto {
    private boolean empty;
    private List<AutoCompleteDto> recipes;
}
