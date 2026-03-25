package com.mylog.common.exception;

import com.mylog.common.response.ErrorResponse;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // 서비스 동작시 거의 대부분의 에러
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> businessExceptionHandler(BusinessException ex) {
    ErrorCode errorCode = ex.getCode();
    return ResponseEntity.status(errorCode.getStatus()).body(ErrorResponse.from(errorCode));
  }

  // 처리되지 않은 오류
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> missingExceptionHandler(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.unknown(ex));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex) {
    String message = getErrorMessage(ex);
    String code = ErrorCode.VALIDATION_FAILED.getCode();
    return ResponseEntity.status(ex.getStatusCode()).body(ErrorResponse.of(code, message));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException ex) {
    String message = getErrorMessage(ex);
    String code = ErrorCode.TYPE_MISMATCH.getCode();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.of(code, message));
  }

  private static String getErrorMessage(MethodArgumentTypeMismatchException ex) {
    return String.format("'%s'의 타입이 맞지 않습니다.", ex.getName());
  }

  private static String getErrorMessage(MethodArgumentNotValidException ex) {
    String where = ex.getBindingResult().getFieldErrors().get(0).getField();

    String how = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();

    return String.format("'%s' 검증실패,  %s.", where, how);
  }
}
