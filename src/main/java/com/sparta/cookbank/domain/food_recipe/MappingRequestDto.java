package com.sparta.cookbank.domain.food_recipe;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MappingRequestDto {
    Long baseId;
    List<Long> subId = new ArrayList<>();
}
