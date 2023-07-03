package com.xuecheng.base.exception;

import java.io.Serializable;

/**
 *
 * @author Wayne
 * @description 错误响应参数包装
 * @date 2023/7/1
 */

public class RestErrorResponse implements Serializable {

    private String errMessage;

    private String errCode;

    public RestErrorResponse(String errMessage){
        this.errMessage= errMessage;
    }

    public RestErrorResponse(String errMessage, String errCode){
        this.errMessage= errMessage;
        this.errCode = errCode;
    }

    public String getErrMessage() {
        return errMessage;
    }
    public String getErrCode(){
        return errCode;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }

    public void setErrCode(String errCode) { this.errCode = errCode;}
}
