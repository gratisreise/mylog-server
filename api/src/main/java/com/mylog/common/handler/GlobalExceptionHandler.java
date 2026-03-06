package com.mylog.common.handler;



import com.mylog.exception.BusinessException;
import com.mylog.response.CommonResult;
import com.mylog.response.ResponseService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CommonResult handleBusinessException(BusinessException ex){
        return ResponseService.getFailResult(ex);
    }

    @ExceptionHandler(RestClientException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CommonResult handleRestClientException(RestClientException ex){
        return ResponseService.getFailResult(ex);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CommonResult handleRuntimeException(RuntimeException ex){
        return ResponseService.getFailResult(ex);
    }

}
