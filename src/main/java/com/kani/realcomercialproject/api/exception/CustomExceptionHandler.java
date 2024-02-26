package com.kani.realcomercialproject.api.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Log4j2
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {
   @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> exception(Exception exception, WebRequest webRequest) throws Exception {
       log.error("Exception: ", exception);
       return handleException(exception, webRequest);
   }
}
