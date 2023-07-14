package com.xuecheng.messagesdk.service.jobhandler;

import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CoursePublishTask extends MessageProcessAbstract {

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
        saveCourseCache(mqMessage, courseId);

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

        // 开始进行课程序列化

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

        // 任务完成写状态为完成
        mqMessageService.completedStageThree(taskId);
    }
}
