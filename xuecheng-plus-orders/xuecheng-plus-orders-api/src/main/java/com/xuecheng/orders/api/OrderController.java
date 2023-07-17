package com.xuecheng.orders.api;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.service.OrderService;
import com.xuecheng.orders.util.SecurityUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Api(value = "订单支付接口", tags = "订单支付接口")
@Slf4j
@Controller
public class OrderController {

    @Autowired
    OrderService orderService;

    @ApiOperation("生成支付二维码")
    @PostMapping("/generatepaycode")
    @ResponseBody
    public PayRecordDto generatePayCode(@RequestBody AddOrderDto addOrderDto) {

        SecurityUtil.XcUser user = SecurityUtil.getUser();
        if(user == null) XueChengPlusException.cast("确认用户登陆情况");
        String userId = user.getId();

        // AddOrderDto 订单相关信息
        // 调用Service完成创建/插入订单信息 / 插入支付记录 / 生成支付二维码
        return orderService.createOrder(userId, addOrderDto);
    }

}
