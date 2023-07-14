package com.xuecheng.content.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
@Slf4j
public class MediaServiceClientFallbackFactory implements FallbackFactory<MediaServiceClient> {

    // 拿到熔断时的异常信息
    @Override
    public MediaServiceClient create(Throwable throwable) {

        // 当发生了熔断，上传服务器调用此方法降级逻辑
        return new MediaServiceClient() {
            @Override
            public String upload(MultipartFile filedata, String objectName) throws IOException {
                log.debug("远程调用出现错误，发生熔断的原因：{}", throwable.toString(), throwable);
                return null;
            }
        };
    }
}
