package com.xuecheng.content;

import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @author Mr.M
 * @version 1.0
 * @description 测试使用feign远程上传文件
 * @date 2022/9/20 20:36
 */
@SpringBootTest
@Slf4j
public class FeignUploadTest {

    @Autowired
    MediaServiceClient mediaServiceClient;

    //远程调用，上传文件
    @Test
    public void test() throws IOException {

        // 将File类型转换为Multipart类型
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(new File("D:\\test.html"));
        String upload = mediaServiceClient.upload(multipartFile, "course/test.html");
        if(upload == null) { log.error("无法调用远程接口！"); }
    }

}
