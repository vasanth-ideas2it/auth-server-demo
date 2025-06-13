package com.i2i.AuthServer.exceptionHandling;



import com.i2i.AuthServer.model.ErrorModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorModel> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorModel  errorModel=ErrorModel.builder().errorCode(11).description(ex.getMessage()).build();
        return new ResponseEntity<>(errorModel, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorModel> handleGeneralException(Exception ex) {
        log.error("Error occurred "+ex);
        ErrorModel  errorModel=ErrorModel.builder().errorCode(12).description(ex.getMessage()).build();
        return new ResponseEntity<>(errorModel, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorModel> handleUserNotFoundException(Exception ex) {
        ErrorModel  errorModel=ErrorModel.builder().errorCode(15).description(ex.getMessage()).build();
        return new ResponseEntity<>(errorModel, HttpStatus.EXPECTATION_FAILED);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ErrorModel>> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<ErrorModel> errorModelList=new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->

                    errorModelList.add(ErrorModel.builder().errorCode(20).description(error.getDefaultMessage()).build())

        );
        return new ResponseEntity<>(errorModelList, HttpStatus.EXPECTATION_FAILED);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorModel> handleAuthenticationException(Exception ex)
    {
        ErrorModel  errorModel=ErrorModel.builder().errorCode(25).description(ex.getMessage()).build();
        return new ResponseEntity<>(errorModel, HttpStatus.UNAUTHORIZED);
    }



}
