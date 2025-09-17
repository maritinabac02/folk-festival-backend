package com.folkfest.exception;
import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*; import java.util.Map;
@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(ApiException.class) public ResponseEntity<?> handleApi(ApiException ex){ return ResponseEntity.status(ex.getStatus()).body(Map.of("error", ex.getMessage())); }
  @ExceptionHandler(Exception.class) public ResponseEntity<?> handleOther(Exception ex){ return ResponseEntity.internalServerError().body(Map.of("error", ex.getMessage())); }
}
