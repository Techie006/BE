package com.sparta.cookbank.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor // 모든 필드 값을 파라미터로 받는 생성자를 만듦
@Getter
public class ResponseDto<T> {
    private boolean result;
    private T content;
    private Error error;

    public static <T> ResponseDto<T> success(T content) {
        return new ResponseDto<>(true, content, null);
    }

    public static <T> ResponseDto<T> fail(String code, String message) {
        return new ResponseDto<>(false, null, new Error(code, message));
    }

    @Getter
    @AllArgsConstructor
    static class Error {
        private String code;
        private String message;
    }
}