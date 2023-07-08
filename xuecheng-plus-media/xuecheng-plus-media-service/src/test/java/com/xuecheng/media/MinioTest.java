package com.xuecheng.media;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import io.minio.errors.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Mr.M
 * @version 1.0
 * @description 测试minio的sdk
 * @date 2023/2/17 11:55
 */
public class MinioTest {

    MinioClient minioClient =
            MinioClient.builder()
                    .endpoint("http://192.168.101.65:9000")
                    .credentials("minioadmin", "minioadmin")
                    .build();

    @Test
    public void test_upload() throws Exception {

        //通过扩展名得到媒体资源类型 mimeType
        //利用j256.simplemagic.ContentType去查扩展名设置文件类型
        //根据扩展名取出mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".mp4");
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mimeType，字节流
        if(extensionMatch!=null){
            mimeType = extensionMatch.getMimeType();
        }

        //上传文件的参数信息
        UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                .bucket("testbucket")//桶
                .filename("D:\\1.mp4") //指定本地文件路径
//                .object("1.mp4")//对象名 在桶下存储该文件
                .object("/1.mp4")//对象名 放在子目录下
                .contentType(mimeType)//设置媒体文件类型
                .build();

        //上传文件
        minioClient.uploadObject(uploadObjectArgs);

    }
    //删除文件
    @Test
    public void test_delete() throws Exception {

        //RemoveObjectArgs
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder().bucket("testbucket").object("/1.mp4").build();

        //删除文件
        minioClient.removeObject(removeObjectArgs);

    }

    //查询文件 从minio中下载
    @Test
    public void test_getFile() throws Exception {

        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket("testbucket").object("/1.mp4").build();
        //查询远程服务获取到一个流对象
        FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
        //指定输出流
        FileOutputStream outputStream = new FileOutputStream(new File("D:\\1a.mp4"));
        IOUtils.copy(inputStream,outputStream);

        //校验文件的完整性对文件的内容进行md5
        FileInputStream fileInputStream1 = new FileInputStream(new File("D:\\1.mp4"));
        String source_md5 = DigestUtils.md5Hex(fileInputStream1);
        FileInputStream fileInputStream = new FileInputStream(new File("D:\\1a.mp4"));
        String local_md5 = DigestUtils.md5Hex(fileInputStream);
        if(source_md5.equals(local_md5)){
            System.out.println("下载成功");
        }

    }


    @Test
    // 将分块文件上传到minio
    public void uploadChunk() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        for (int i = 0; i < 22; i++) {
            // 传文件
            //通过扩展名得到媒体资源类型 mimeType
            //利用j256.simplemagic.ContentType去查扩展名设置文件类型
            //根据扩展名取出mimeType
            ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".mp4");
            String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;//通用mimeType，字节流
            if(extensionMatch!=null){
                mimeType = extensionMatch.getMimeType();
            }

            //上传文件的参数信息
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket("testbucket")//桶
                    .filename("E:\\chunk\\"+i) //指定本地文件路径
//                .object("1.mp4")//对象名 在桶下存储该文件
                    .object("chunk/"+i)//对象名 放在子目录下
                    .contentType(mimeType)//设置媒体文件类型
                    .build();

            //上传文件
            minioClient.uploadObject(uploadObjectArgs);
            System.out.println("上传分块"+i+"成功");
        }

    }

    @Test
    // 调用minio接口方法去合并文件
    public void testMerge() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        // 获取指定分块文件信息
        List<ComposeSource> sourceList;

//        for (int i = 0; i < 2; i++) {
//            sourceList.add(ComposeSource.builder().bucket("testbucket").object("chunk"+i).build());
//        }

        // 用Stream流映射
        sourceList = Stream.iterate(0, i -> ++i).limit(22).map(i -> ComposeSource.builder().bucket("testbucket").object("chunk/"+i).build()).collect(Collectors.toList());


        // 指定合并的信息和合并后的命名
        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket("testbucket")
                .object("merge01.mp4")
                .sources(sourceList)
                .build();// 使用源文件;
        // 合并文件
        // 报错 size 1048576 must be greater than 5242880 minio默认分块文件大小为5m
        minioClient.composeObject(composeObjectArgs);
        System.out.println("合并成功");
    }









}
