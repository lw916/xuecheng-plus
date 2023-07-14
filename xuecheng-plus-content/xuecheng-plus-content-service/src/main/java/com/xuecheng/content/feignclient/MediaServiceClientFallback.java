package com.xuecheng.content.feignclient;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class MediaServiceClientFallback implements MediaServiceClient{

    // fallback 让服务不可用的情况下跳到该备份办法
    // 但不知道为什么会出错， 无法拿到熔断异常

    @Override
    public String upload(MultipartFile filedata, String objectName) throws IOException {

        return null;
    }

}
