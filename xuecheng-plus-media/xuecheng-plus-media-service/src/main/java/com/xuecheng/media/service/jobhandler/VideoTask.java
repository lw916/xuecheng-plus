package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import groovy.util.logging.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * XxlJob开发示例（Bean模式）
 *
 * 开发步骤：
 *      1、任务开发：在Spring Bean实例中，开发Job方法；
 *      2、注解配置：为Job方法添加注解 "@XxlJob(value="自定义jobhandler名称", init = "JobHandler初始化方法", destroy = "JobHandler销毁方法")"，注解value值对应的是调度中心新建任务的JobHandler属性的值。
 *      3、执行日志：需要通过 "XxlJobHelper.log" 打印执行日志；
 *      4、任务结果：默认任务结果为 "成功" 状态，不需要主动设置；如有诉求，比如设置任务结果为失败，可以通过 "XxlJobHelper.handleFail/handleSuccess" 自主设置任务结果；
 *
 * @author xuxueli 2019-12-11 21:52:51
 */
@Component
@Slf4j
public class VideoTask {
    private static Logger logger = LoggerFactory.getLogger(VideoTask.class);

    @Autowired
    MediaFileProcessService mediaFileProcessService;

    @Autowired
    MediaFileService mediaFileService;

    @Value("${videoprocess.ffmpegpath}") // 从Nacos配置文件获取软件路径
    String ffmpeg_path;


    /**
     * 2、分片广播任务
     */
    @XxlJob("videoJobHandler")
    public void shardingJobHandler() throws Exception {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex(); // 执行器的序号，从0开始
        int shardTotal = XxlJobHelper.getShardTotal(); // 执行器总数

        // 根据cpu核心数向数据库要任务
        int processors = Runtime.getRuntime().availableProcessors();

        // 查询待处理任务
        List<MediaProcess> mediaProcessList = mediaFileProcessService.getMediaProcessList(shardIndex, shardTotal, processors);
        int size = mediaProcessList.size(); // 拿到的任务数量
        logger.debug("拿到了{}个视频去处理", size);
        if(size == 0){
            return;
        }
        // 开启（抢）任务加锁
        // 多线程完成任务
        // 创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(size);
        // 使用计数器
        CountDownLatch countDownLatch = new CountDownLatch(size);
        mediaProcessList.forEach(mediaProcess -> {
            // 任务加入线程池
            executorService.execute(() -> {
                try {
                    // 任务执行逻辑
                    // 获取任务ID
                    Long taskId = mediaProcess.getId();
                    // 文件的MD5
                    String fileId = mediaProcess.getFileId();
                    // 开启任务
                    boolean startTask = mediaFileProcessService.startTask(taskId);
                    // 判断任务是否被占用
                    if(!startTask){
                        logger.debug("抢任务失败，任务id:{}", taskId);
                        return;
                    }
                    // 开始任务（视频转码）
                    // 先下载视频文件
                    // 获取桶
                    String bucket = mediaProcess.getBucket();
                    String ObjectName = mediaProcess.getFilePath();
                    File file = mediaFileService.downloadFileFromMinIO(bucket,ObjectName);
                    if(file == null){
                        // 保存任务处理失败结果
                        mediaFileProcessService.saveProcessFinishStatus(taskId,"3", fileId, null, "获取转码视频失败");
                        logger.debug("获取转码视频失败，任务id:{}, bucket:{}, 路径：{}", taskId, bucket, ObjectName);
                        return;
                    }

                    // 拿到临时文件路径
                    String video_path = file.getAbsolutePath();
                    String mp4_name = fileId + ".mp4";
                    // 转换后的mp4 路径
                    // 先创建临时文件，作为转换文件
                    File transfer = null;
                    try {
                        transfer = File.createTempFile("minio",".mp4");

                    } catch (IOException e) {
                        mediaFileProcessService.saveProcessFinishStatus(taskId,"3", fileId, null, "创建转码临时文件异常");
                        logger.error("创建转码临时文件异常，{}", e.getMessage());
                        return;
                    }
                    String mp4_path = transfer.getAbsolutePath(); // 注意这个地址路径
                    //创建工具类对象
                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpeg_path,video_path,mp4_name,mp4_path);
                    //开始视频转换，成功将返回success
                    String result = videoUtil.generateMp4();
                    if(!result.equals("success")){
                        // 保存任务处理失败结果
                        mediaFileProcessService.saveProcessFinishStatus(taskId,"3", fileId, null, "转码失败");
                        logger.debug("视频转码失败，任务id:{}, bucket:{}, 路径：{}", taskId, bucket, ObjectName);
                        return;
                    }
                    // 上传视频到minIO
                    // mp4 url拼接
                    String url = getFilePathByMd5(fileId, ".mp4");
                    boolean upload = mediaFileService.addMediaFilesToMinIO(mp4_path, "video/mp4", bucket, url);
                    if(!upload){
                        // 保存任务处理失败结果
                        mediaFileProcessService.saveProcessFinishStatus(taskId,"3", fileId, null, "上传视频到Minio失败");
                        logger.debug("上传视频到Minio失败，任务id:{}, bucket:{}, 路径：{}", taskId, bucket, url);
                        return;
                    }

                    // 保存任务处理结果
                    mediaFileProcessService.saveProcessFinishStatus(taskId, "2", fileId, url, null);
                }finally {
                    // 计数器-1
                    // finally 保证所有任务有没有错误都会减一个计数器
                    countDownLatch.countDown();
                }

            });
        });
        countDownLatch.await(30, TimeUnit.MINUTES); // 阻塞 让所有的任务完成再停掉这个代码 指定一个最大限度的等待时间




    }
    // 获取路径
    private String getFilePathByMd5(String fileMd5, String fileExt){
        return fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/" + fileMd5 + fileExt;
    }


}
