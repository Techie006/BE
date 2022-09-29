package com.sparta.cookbank.controller;

import com.sparta.cookbank.ResponseDto;
import com.sparta.cookbank.service.MappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MappingController {
    private final MappingService mappingService;

    @GetMapping("api/mapping")
    public ResponseDto<?> mapping(){
        Long cnt = mappingService.MakeRecipeIngredient();
        return ResponseDto.success(cnt,"테이블이 생성되었습니다.");
    }
}
