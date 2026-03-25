package com.mylog.common.response;

import com.mylog.common.exception.ErrorCode;
import java.time.LocalDateTime;
import java.lang.Exception;
import lombok.Getter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

@Getter
public class ErrorResponse extends BaseResponse {
  private final ErrorDetail error;

  public record ErrorDetail(String code, String message) {}

  private ErrorResponse(String code, String message) {
    super(false, LocalDateTime.now());
    this.error = new ErrorDetail(code, message);
  }

  public static ErrorResponse from(ErrorCode code) {
    return new ErrorResponse(code.getCode(), code.getMessage());
  }

  public static ErrorResponse of(String code, String message) {
    return new ErrorResponse(code, message);
  }

  public static ErrorResponse denied(AccessDeniedException ex) {
    ErrorCode code = ErrorCode.ACCESS_DENIED;
    return new ErrorResponse(code.getCode(), ex.getMessage());
  }

  public static ErrorResponse entry(AuthenticationException ex) {
    ErrorCode code = ErrorCode.UNAUTHORIZED_USER;
    return new ErrorResponse(code.getCode(), ex.getMessage());
  }

  public static ErrorResponse unknown(Exception ex) {
    ErrorCode code = ErrorCode.UNKNOWN_ERROR;
    return new ErrorResponse(code.getCode(), ex.getMessage());
  }



}
