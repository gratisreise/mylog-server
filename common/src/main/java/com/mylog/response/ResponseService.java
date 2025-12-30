package com.mylog.response;



import com.mylog.enums.ResultCode;
import java.util.List;

public class ResponseService {

    public static <T> ListResult<T> getListResult(List<T> data) {
        ListResult<T> result = new ListResult<>();
        result.setData(data);
        setSuccessResult(result);
        return result;
    }

    public static <T> SingleResult<T> getSingleResult(T data) {
        SingleResult<T> result = new SingleResult<>();
        result.setData(data);
        setSuccessResult(result);
        return result;
    }

    public static CommonResult getSuccessResult() {
        CommonResult result = new CommonResult();
        setSuccessResult(result);
        return result;
    }

    // 커스텀 예외일 때 사용
    public static CommonResult getFailResult(ResultCode resultCode) {
        CommonResult result = new CommonResult();
        result.setCode(resultCode.getCode());
        result.setMessage(resultCode.getMsg());
        return result;
    }

    public static CommonResult getFailResult(Exception e) {
        CommonResult result = new CommonResult();
        result.setCode(-1);
        result.setMessage(e.getMessage());
        return result;
    }

    private static void setSuccessResult(CommonResult result) {
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMsg());
    }

    private static void setFailResult(CommonResult result) {
        result.setCode(ResultCode.FAILED.getCode());
        result.setMessage(ResultCode.FAILED.getMsg());
    }


    private ResponseService(){}
}
