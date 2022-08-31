package com.sparta.cookbank;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponseDto<T> {
    private boolean result;
    private T content;
    private Status status;

    public static <T> ResponseDto<T> success(T data, String message) {
        return new ResponseDto<>(true, data, new Status("200",message));
    }

    public static <T> ResponseDto<T> fail(String code, String message) {
        return new ResponseDto<>(false, null, new Status(code, message));
    }

    @Getter
    @AllArgsConstructor
    static class Status {
        private String code;
        private String message;
    }

    
}
