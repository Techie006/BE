package com.sparta.cookbank.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sparta.cookbank.ResponseDto;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice     // 글로벌로 적용된다.
public class CustomExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseDto<?> handleIllegalArgumentException(IllegalArgumentException exception) {
        String errorMessage = exception.getMessage();
        return ResponseDto.fail("400",errorMessage);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseDto<?> handleRuntimeException(RuntimeException exception){
        String errorMessage = exception.getMessage();
        return ResponseDto.fail("400",errorMessage);
    }
    
    @ExceptionHandler(JsonProcessingException.class)
    public ResponseDto<?> handleJsonProcessingException(JsonProcessingException exception){
        String errorMessage = "readTree Fail" + exception.getMessage();
        return ResponseDto.fail("400",errorMessage);
    }

    //시간초과 예외 만들기
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseDto<?> handleExpiredJwtException(ExpiredJwtException exception){
        String errorMessage = "토큰이 만료되었습니다.";
        return ResponseDto.fail("401",errorMessage);
    }
}
