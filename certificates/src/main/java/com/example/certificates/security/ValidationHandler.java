package com.example.certificates.security;

import com.example.certificates.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ValidationHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        List<Map<String, String>> errors = new ArrayList<>();

        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            Map<String, String> errorsMap= new HashMap<>();
            errorsMap.put("message", "Field " + fieldName + " " + errorMessage);
            errors.add(
                    errorsMap
            );
        });

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);

    }
    @ExceptionHandler(value
            = UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse
    handleUserAlreadyExistsException(UserAlreadyExistsException ex)
    {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(value
            = InvalidRepeatPasswordException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse
    handleInvalidRepeatPasswordException(InvalidRepeatPasswordException ex)
    {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(value
            = EndIssuerException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse
    handleEndIssuerException(EndIssuerException ex)
    {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(value
            = InvalidCertificateEndDateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse
    handleInvalidCertificateEndDateException(InvalidCertificateEndDateException ex)
    {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(value
            = InvalidIssuerException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse
    handleInvalidIssuerException(InvalidIssuerException ex)
    {
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(value
            = NonExistingCertificateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse
    handleNonExistingCertificateException(NonExistingCertificateException ex)
    {
        return new ErrorResponse(ex.getMessage());
    }


    @ExceptionHandler(value
            = InvalidCertificateTypeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public @ResponseBody ErrorResponse
    handleInvalidCertificateTypeException(InvalidCertificateTypeException ex)
    {
        return new ErrorResponse(ex.getMessage());
    }




    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(Exception ex, WebRequest request) {
        return new ResponseEntity("Access Denied", HttpStatus.FORBIDDEN);
    }



}
