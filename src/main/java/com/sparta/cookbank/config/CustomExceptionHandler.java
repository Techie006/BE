package com.sparta.cookbank.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sparta.cookbank.ResponseDto;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.io.IOException;

@RestControllerAdvice     // 글로벌로 적용된다.
public class CustomExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDto<?> handleIllegalArgumentException(IllegalArgumentException exception) {
        String errorMessage = exception.getMessage();
        return ResponseDto.fail("221",errorMessage);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDto<?> handleRuntimeException(RuntimeException exception){
        String errorMessage = exception.getMessage();
        return ResponseDto.fail("222",errorMessage);
    }
    
    @ExceptionHandler(JsonProcessingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDto<?> handleJsonProcessingException(JsonProcessingException exception){
        String errorMessage = "readTree Fail" + exception.getMessage();
        return ResponseDto.fail("223",errorMessage);
    }

    //시간초과 예외 만들기
    @ExceptionHandler(ExpiredJwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseDto<?> handleExpiredJwtException(ExpiredJwtException exception){
        String errorMessage = "토큰이 만료되었습니다.";
        return ResponseDto.fail("224",errorMessage);
    }


    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseDto<?> handleUsernameNotFoundException(UsernameNotFoundException exception) {
        String errorMessage = exception.getMessage();
        return ResponseDto.fail("225",errorMessage);
    }

    @ExceptionHandler(IOException.class)
    public ResponseDto<?> handleIOException(IOException exception) {
        String errorMessage = exception.getMessage();
        return ResponseDto.fail("226",errorMessage);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    protected ResponseDto<?> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException e) {
        return ResponseDto.fail("227", "사진은 20MB까지 등록이 가능합니다.");
    }
}

