package com.xuecheng.content.jobhandler;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.feignclient.SearchServiceClient;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.model.dto.CourseIndex;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {

    @Resource
    CoursePublishService coursePublishService;

    @Resource
    SearchServiceClient searchServiceClient;

    @Autowired
    CoursePublishMapper coursePublishMapper;

    // 任务调度入口
    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler() throws Exception{
        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex(); // 执行器的序号，从0开始
        int shardTotal = XxlJobHelper.getShardTotal(); // 执行器总数
        // 调用抽象类来执行任务
        process(shardIndex, shardTotal, "course_publish", 30, 60);
    }

    // 执行课程发布任务的逻辑，如果此方法抛出异常说明任务执行失败
    @Override
    // 执行课程发布任务逻辑
    public boolean execute(MqMessage mqMessage) {
        // 从mqMessage拿数据
        Long courseId = Long.parseLong(mqMessage.getBusinessKey1());

        // 向elasticsearch写索引
        saveCourseIndex(mqMessage, courseId);

        // 向redis写缓存
//        saveCourseCache(mqMessage, courseId);

        // 课程静态化上传到minIO
        generateCourseHtml(mqMessage, courseId);

        // 返回结果
        return false;
    }

    // 缓存信息到Redis
    public void saveCourseCache(MqMessage mqMessage,long courseId){
        // 获取消息ID
        Long taskId = mqMessage.getId();
        // 指定当前访问的数据库（原因：多个服务的mqMessage表名称一样，必须使用抽象类下指定的数据库去链接拿到真实数据）
        MqMessageService mqMessageService = this.getMqMessageService();
        // 任务幂等性判断
        // 取出该任务的执行状态
        int stageTwo = mqMessageService.getStageOne(taskId);
        if(stageTwo > 0){
            log.debug("无需处理，课程索引插入完成");
            return;
        }

        // 开始进行课程序列化

        // 任务完成写状态为完成
        mqMessageService.completedStageOne(taskId);
    }

    // 保存课程索引信息
    private void saveCourseIndex(MqMessage mqMessage, Long courseId){
        // 获取消息ID
        Long taskId = mqMessage.getId();
        // 指定当前访问的数据库（原因：多个服务的mqMessage表名称一样，必须使用抽象类下指定的数据库去链接拿到真实数据）
        MqMessageService mqMessageService = this.getMqMessageService();
        // 任务幂等性判断
        // 取出该任务的执行状态
        int stageTwo = mqMessageService.getStageTwo(taskId);
        if(stageTwo > 0){
            log.debug("无需处理，课程索引插入完成");
            return;
        }

        // 查询课程信息，添加搜索索引
        CoursePublish coursePublish = coursePublishMapper.selectById(courseId);
        // 造CourseIndex数据
        CourseIndex courseIndex = new CourseIndex();
        BeanUtils.copyProperties(coursePublish, courseIndex);
        // 远程调用
        boolean add = searchServiceClient.add(courseIndex);
        if(!add){
            XueChengPlusException.cast("远程调用添加索引失败");
            return; // 不能直接抛出索引，需要直接return不记录数据，达到分布式事务的结果
        }

        // 任务完成写状态为完成
        mqMessageService.completedStageTwo(taskId);

    }

    // 生成静态化页面的私有方法
    private void generateCourseHtml(MqMessage mqMessage, Long courseId){
        // 获取消息ID
        Long taskId = mqMessage.getId();
        // 指定当前访问的数据库（原因：多个服务的mqMessage表名称一样，必须使用抽象类下指定的数据库去链接拿到真实数据）
        MqMessageService mqMessageService = this.getMqMessageService();
        // 做任务幂等性处理
        // 取出该任务的执行状态
        int stageOne = mqMessageService.getStageThree(taskId);
        if(stageOne > 0){
            log.debug("无需处理，课程静态化完成");
            return;
        }

        // 开始进行课程静态化
        // 包括生成Html静态文件
        File file = coursePublishService.generateCourseHtml(courseId);
        // 上传文件到Minio
        if(file != null){
            coursePublishService.uploadCourseHtml(courseId, file);
        }else{
            XueChengPlusException.cast("生成静态文件失败");
        }

        // 任务完成写状态为完成
        mqMessageService.completedStageThree(taskId);
    }
}