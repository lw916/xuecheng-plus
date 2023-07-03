package com.xuecheng.base.exception;

/**
 *
 * @author Wayne
 * @description 自定义异常
 * @date 2023/7/1
 */

public class XueChengPlusException extends RuntimeException {

    // Idea构造函数快捷键 alt + insert

    private String errMessage;
    private String errCode;

    public XueChengPlusException() {
        super();
    }

    public XueChengPlusException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
        this.errCode = null;
    }

    public XueChengPlusException(String errMessage, String errCode) {
        super(errMessage);
        this.errMessage = errMessage;
        this.errCode = errCode;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public String getErrCode(){
        return errCode;
    }

    // 重载方法用通用错误类型
    public static void cast(CommonError commonError){
        throw new XueChengPlusException(commonError.getErrMessage());
    }
    public static void cast(String errMessage){
        throw new XueChengPlusException(errMessage);
    }

    public static void cast(String errMessage, String errCode){
        throw new XueChengPlusException(errMessage, errCode);
    }

}
