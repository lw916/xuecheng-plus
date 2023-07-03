package com.xuecheng.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

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
        String errCode = exception.getErrCode();
        if(errCode == null){
            return new RestErrorResponse(errMessage);
        }else{
            return new RestErrorResponse(errMessage, errCode);
        }

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
        return new RestErrorResponse(CommonError.UNKOWN_ERROR.getErrMessage());

    }

    // 验证异常数据解析 （JSR303）
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public RestErrorResponse methodArgumentNotValidException(MethodArgumentNotValidException exception){

        List<String> errors = new ArrayList<>();
        BindingResult bindingResult = exception.getBindingResult();
        bindingResult.getFieldErrors().stream().forEach(item->{
            errors.add(item.getDefaultMessage());
        });

        // 将list当中的错误信息拼接
        String errorMessage = StringUtils.join(errors, ",");

        // 记录异常
        log.error("系统异常{}", exception.getMessage(), errorMessage);

        // 解析异常信息
        // 系统异常统一为执行过程异常，默认错误
        return new RestErrorResponse(errorMessage);

    }


}
