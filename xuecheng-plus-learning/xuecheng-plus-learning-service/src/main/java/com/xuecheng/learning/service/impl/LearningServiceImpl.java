package com.xuecheng.learning.service.impl;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.content.model.po.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.feignclient.MediaServiceClient;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.service.LearningService;
import com.xuecheng.learning.service.MyCourseTableService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LearningServiceImpl implements LearningService {

    @Autowired
    ContentServiceClient contentServiceClient;

    @Autowired
    MyCourseTableService myCourseTableService;

    @Autowired
    MediaServiceClient mediaServiceClient;


    @Override
    // 根据用户ID判断有没有这门可的权限
    // 根据课程媒资ID去获取路径
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId) {

        // 查询课程信息
        CoursePublish coursePublish = contentServiceClient.getCoursepublish(courseId);
        if(coursePublish == null){
            XueChengPlusException.cast("该课程不存在");
        }
        // 根据课程teachPlanId查该课程是否可以试学，如果可以返回视频路径
        // 从课程发布信息中也可以直接拿数据
        // teachplan拿不到isPreview 还是要远程调用
//        String teachplan = coursePublish.getTeachplan();
//        System.out.println(teachplan.substring(1, teachplan.length() - 1));
//        TeachplanDto teachplanDto = JSON.parseObject(teachplan.substring(1, teachplan.length() - 1), TeachplanDto.class);
//        String isPreview = teachplanDto.getIsPreview();
//        if(isPreview.equals("1")){
//            // 支持试学
//            return mediaServiceClient.getPlayUrlByMediaId(mediaId);
//        }
        // 校验学习资格
        // 判断登录状态
        if(StringUtils.isNotEmpty(userId)){
            // 判断是否选课， 根据选课情况判断学习资格
            //学习资格状态 [{"code":"702001","desc":"正常学习"},{"code":"702002","desc":"没有选课或选课后没有支付"},{"code":"702003","desc":"已过期需要申请续期或重新支付"}]
            XcCourseTablesDto xcCourseTablesDto = myCourseTableService.getLearningStatus(userId, courseId);
            String learnStatus = xcCourseTablesDto.getLearnStatus();
            if(learnStatus.equals("702001")){
                return mediaServiceClient.getPlayUrlByMediaId(mediaId);
            }else if(learnStatus.equals("702003")){
                RestResponse.validfail("您的选课已过期需要申请续期或重新支付");
            }else{
                RestResponse.validfail("没有选课或选课后没有支付");
            }
        }else{
            // 如果没有登陆
            // 确认课程收费规则
            String charge = coursePublish.getCharge();
            if(charge.equals("201000")){
                // 免费课程可以直接学习
                return mediaServiceClient.getPlayUrlByMediaId(mediaId);
            }
        }
        return RestResponse.validfail("请购买课程后继续学习");
    }
}


