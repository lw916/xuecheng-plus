package com.xuecheng.media;

import com.baomidou.mybatisplus.extension.api.R;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BigFileTest {

    // 测试文件分块
    @Test
    public void testChunk() throws IOException {

        // 源文件
        File sourceFile = new File("E:\\test.mp4");
        // 分块存储路径
        String chunkFilePath= "E:\\chunk\\";
        // 分块大小，使用官方合并API需要至少5M
        int chunkSize = 1024 * 1024 * 5;
        // 分块文件个数
        int chunkNum = (int)Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        // 使用源文件读数据，向分块文件写数据
        RandomAccessFile raf_r = new RandomAccessFile(sourceFile, "r");
        // 读文件缓冲区;
        byte[] bytes = new byte[1024];
        for(int i = 0; i < chunkNum; i++){
            File chunkFile = new File(chunkFilePath + i);
            // 分块文件写入流
            RandomAccessFile raf_rw = new RandomAccessFile(chunkFile, "rw");
            int len = -1;
            while((len = raf_r.read(bytes))!= -1){
                raf_rw.write(bytes, 0, len);
                // 当分块文件大小大于设定的大小则中止
                if(chunkFile.length() >= chunkSize){
                    break;
                }
            }
            raf_rw.close();
        }
        raf_r.close();
    }


    // 测试文件合并
    @Test
    public void testMerge() throws IOException {
        // 块文件目录
        File chunkFolder = new File("E:\\chunk\\");
        // 源文件
        File sourceFile = new File("E:\\test.mp4");
        // 合并后的文件
        File mergeFile = new File("E:\\合并.mp4");
        // 提出所有的分块文件
        File[] files = chunkFolder.listFiles();
        // 将数组转成List
        List<File> filesList = Arrays.asList(files);

        // 排序，从0开始排序让分块文件有顺序
        filesList.sort(new Comparator<File>() {
            @Override
            // 升序
            // 降序 反过来减
            public int compare(File o1, File o2) {
                return Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName());
            }
        });

        // 缓冲区
        byte[] bytes = new byte[1024];
        // 合并文件写的流
        RandomAccessFile raf_rw= new RandomAccessFile(mergeFile, "rw");

        // 遍历分块文件写入
        filesList.forEach(file -> {
            try {
                RandomAccessFile raf_r = new RandomAccessFile(file,"r");
                int len = -1;
                while((len=raf_r.read(bytes))!= -1){
                    raf_rw.write(bytes, 0, len);
                }
                raf_r.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        // 使用完就关闭
        try {
            raf_rw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 合并文件完成检验md5
        FileInputStream mergeFileInput = new FileInputStream(mergeFile);
        FileInputStream sourceFileInput = new FileInputStream(sourceFile);
        String md5merge = DigestUtils.md5Hex(mergeFileInput);
        String md5source = DigestUtils.md5Hex(sourceFileInput);
        if(md5source.equals(md5merge)) System.out.println("文件合并成功");
        else System.out.println("失败");


    }



}
