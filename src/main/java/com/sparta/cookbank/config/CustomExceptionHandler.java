package com.sparta.cookbank.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sparta.cookbank.ResponseDto;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        return ResponseDto.fail("400",errorMessage);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDto<?> handleRuntimeException(RuntimeException exception){
        String errorMessage = exception.getMessage();
        return ResponseDto.fail("400",errorMessage);
    }
    
    @ExceptionHandler(JsonProcessingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseDto<?> handleJsonProcessingException(JsonProcessingException exception){
        String errorMessage = "readTree Fail" + exception.getMessage();
        return ResponseDto.fail("400",errorMessage);
    }

    //시간초과 예외 만들기
    @ExceptionHandler(ExpiredJwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseDto<?> handleExpiredJwtException(ExpiredJwtException exception){
        String errorMessage = "토큰이 만료되었습니다.";
        return ResponseDto.fail("401",errorMessage);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseDto<?> handleUsernameNotFoundException(UsernameNotFoundException exception) {
        String errorMessage = exception.getMessage();
        return ResponseDto.fail("401",errorMessage);
    }

    @ExceptionHandler(IOException.class)
    public ResponseDto<?> handleIOException(IOException exception) {
        String errorMessage = exception.getMessage();
        return ResponseDto.fail("400",errorMessage);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    protected ResponseDto<?> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException e) {
        String errorMessage = e.getMessage();
        return ResponseDto.success("4123", errorMessage);
    }
}
