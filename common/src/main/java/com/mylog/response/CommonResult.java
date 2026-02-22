package com.mylog.response;

import com.mylog.exception.BusinessException;
import com.mylog.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommonResult {
    private String code;
    private String message;

    public static CommonResult catching(BusinessException e){
        ErrorCode errorCode = e.getErrorCode();
        return new CommonResult(errorCode.getCode(), errorCode.getMessage());
    }
}
