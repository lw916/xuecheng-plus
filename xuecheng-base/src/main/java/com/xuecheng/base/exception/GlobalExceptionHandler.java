package com.xuecheng.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author Wayne
 * @description 
 * @date 2023/7/1
 */

@Slf4j
@RestControllerAdvice
//@ControllerAdvice
public class GlobalExceptionHandler {

    // 自定义异常方法
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(XueChengPlusException.class)
    public RestErrorResponse customException(XueChengPlusException exception){

        // 记录异常
        log.error("系统异常{}", exception.getErrMessage(), exception);

        // 解析异常信息
        String errMessage = exception.getErrMessage();
        RestErrorResponse restErrorResponse = new RestErrorResponse(errMessage);
        return restErrorResponse;

    }

    // 系统异常方法
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public RestErrorResponse customException(Exception exception){

        // 记录异常
        log.error("系统异常{}", exception.getMessage(), exception);

        // 解析异常信息
        // 系统异常统一为执行过程异常，默认错误
        RestErrorResponse restErrorResponse = new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());
        return restErrorResponse;

    }


}
