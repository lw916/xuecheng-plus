package com.xuecheng.content.feignclient;

import com.xuecheng.content.config.MultipartSupportConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @description 媒资管理服务远程接口
 * @author Mr.M
 * @date 2022/9/20 20:29
 * @version 1.0
 */
// 让Feign支持Multipart File
// Feign 远程调用做法
// 指定熔断配置到配置文件中 —— 将要调用的接口拷贝过来（接口！！！) —— 指定服务：media-api 来自于远程注册（比如nacos）的appName —— 加注解调用@EnableFeignClients（。。。）
//使用fallback定义降级类是无法拿到熔断异常,使用FallbackFactory可以拿到熔断的异常信息
@FeignClient(value = "media-api",configuration = MultipartSupportConfig.class)
public interface MediaServiceClient {

    @RequestMapping(value = "/media/upload/coursefile",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String upload(@RequestPart("filedata") MultipartFile filedata,
                         @RequestParam(value="objectName", required=false) String objectName) throws IOException;

}
