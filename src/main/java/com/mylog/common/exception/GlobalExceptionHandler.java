package com.mylog.common.exception;


import com.mylog.common.response.CommonResult;
import com.mylog.common.response.ResponseService;
import com.mylog.common.enums.ResultCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CDuplicatedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CommonResult handleDuplicatedException(CDuplicatedException ex){
        return ResponseService.getFailResult(ResultCode.DATA_DUPLICATED);
    }

    @ExceptionHandler(CMissingDataException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CommonResult handleMissingDataException(CMissingDataException ex){
        return ResponseService.getFailResult(ResultCode.DATA_MISSED);
    }

    @ExceptionHandler(CInvalidDataException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CommonResult handleInvalidDataException(CInvalidDataException ex){
        return ResponseService.getFailResult(ResultCode.DATA_INVALID);
    }

    @ExceptionHandler(CUnAuthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public CommonResult handleUnAuthorizedException(CUnAuthorizedException ex){
        return ResponseService.getFailResult(ResultCode.UNAUTHORIZED_ACCESS);
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
