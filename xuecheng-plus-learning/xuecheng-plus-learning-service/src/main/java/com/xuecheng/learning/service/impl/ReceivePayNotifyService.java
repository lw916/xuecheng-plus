package com.xuecheng.learning.service.impl;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.learning.config.PayNotifyConfig;
import com.xuecheng.learning.service.MyCourseTableService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wayne
 * @version 1.0
 * @description 接收支付结果
 * @date 2023/2/23 19:04
 */


@Slf4j
@Service
public class ReceivePayNotifyService {

    @Autowired
    MyCourseTableService myCourseTableService;

    //监听消息队列接收支付结果通知
    @RabbitListener(queues = PayNotifyConfig.PAYNOTIFY_QUEUE)
    public void receive(Message message, Channel channel){
        // 失败了不要马上重试，休眠一会
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // 获取到信息的字符串JSON
        byte[] body = message.getBody();
        String messageJson = new String(body);
        // 转成对象
        MqMessage mqMessage = JSON.parseObject(messageJson, MqMessage.class);
        log.debug("学习中心服务接收支付结果:{}", mqMessage);
        // 根据传来的数据更新选课信息支付记录 / 向我的课程表插入记录
        String chooseCourseId = mqMessage.getBusinessKey1();
        String orderType = mqMessage.getBusinessKey2();
        // 判断课程是否为收费的
        if(orderType.equals("60201")){
            boolean bool = myCourseTableService.saveChooseCourseStatus(chooseCourseId);
            if(!bool){
                //添加选课失败，抛出异常，消息重回队列
                XueChengPlusException.cast("收到支付结果，添加选课失败");
            }
        }
    }
}
