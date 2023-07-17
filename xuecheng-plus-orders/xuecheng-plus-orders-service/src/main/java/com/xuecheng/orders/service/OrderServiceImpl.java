package com.xuecheng.orders.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.utils.IdWorkerUtils;
import com.xuecheng.base.utils.QRCodeUtil;
import com.xuecheng.orders.mapper.XcOrdersGoodsMapper;
import com.xuecheng.orders.mapper.XcOrdersMapper;
import com.xuecheng.orders.mapper.XcPayRecordMapper;
import com.xuecheng.orders.model.dto.AddOrderDto;
import com.xuecheng.orders.model.dto.PayRecordDto;
import com.xuecheng.orders.model.po.XcOrders;
import com.xuecheng.orders.model.po.XcOrdersGoods;
import com.xuecheng.orders.model.po.XcPayRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService{
    @Autowired
    XcOrdersMapper ordersMapper;

    @Autowired
    XcOrdersGoodsMapper ordersGoodsMapper;

    @Autowired
    XcPayRecordMapper payRecordMapper;

    @Value("${pay.qrcodeurl}")
    String qrcodeUrl;

    @Transactional
    @Override
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto) {

        //添加商品订单
        // 幂等性判断，同一个选课记录只能有一个订单
        XcOrders xcOrders = saveXcOrders(userId, addOrderDto);

        //添加支付交易记录
        // OrderId 唯一
        // outPayNo：支付系统的支付流水号 唯一
        XcPayRecord payRecord = createPayRecord(xcOrders);

        //生成二维码
        QRCodeUtil qrCodeUtil = new QRCodeUtil();
        String qrCode = "";
        try {
            String url = String.format(qrcodeUrl, payRecord.getPayNo());
            qrCode = qrCodeUtil.createQRCode(url, 200, 200);
        }catch (IOException exception){
            XueChengPlusException.cast("生成二维码失败");
        }
        PayRecordDto payRecordDto = new PayRecordDto();
        BeanUtils.copyProperties(payRecord, payRecordDto);
        payRecordDto.setQrcode(qrCode);
        return payRecordDto;
    }

    @Transactional
    public XcOrders saveXcOrders(String userId, AddOrderDto addOrderDto){
        //幂等性处理, 同一个选课记录只能有一个订单
        XcOrders order = getOrderByBusinessId(addOrderDto.getOutBusinessId());
        if(order!=null){
            return order;
        }
        order = new XcOrders();
        //生成订单号 用雪花算法
        long orderId = IdWorkerUtils.getInstance().nextId();
        order.setId(orderId);
        order.setTotalPrice(addOrderDto.getTotalPrice());
        order.setCreateDate(LocalDateTime.now());
        order.setStatus("600001");//未支付
        order.setUserId(userId);
        order.setOrderType(addOrderDto.getOrderType());
        order.setOrderName(addOrderDto.getOrderName());
        order.setOrderDetail(addOrderDto.getOrderDetail());
        order.setOrderDescrip(addOrderDto.getOrderDescrip());
        order.setOutBusinessId(addOrderDto.getOutBusinessId());//选课记录id
        // 插入订单主表
        int insert = ordersMapper.insert(order);
        if(insert <= 0) XueChengPlusException.cast("插入订单失败");
        // 订单明细Json串 把Json串转成XcOrdersGoods（字段和XCORDERGOODS对的上）的List
        String orderDetailJson = addOrderDto.getOrderDetail();
        List<XcOrdersGoods> xcOrdersGoodsList = JSON.parseArray(orderDetailJson, XcOrdersGoods.class);
        xcOrdersGoodsList.forEach(goods->{
            XcOrdersGoods xcOrdersGoods = new XcOrdersGoods();
            BeanUtils.copyProperties(goods,xcOrdersGoods);
            xcOrdersGoods.setOrderId(orderId);//订单号
            int insert1 = ordersGoodsMapper.insert(xcOrdersGoods);// 插入Goods表
            if(insert1 <= 0) XueChengPlusException.cast("插入订单记录失败");
        });
        return order;
    }
    
    // 创建支付记录
    public XcPayRecord createPayRecord(XcOrders orders){
        // outPayNo：支付系统的支付流水号 唯一
        // 如果订单不存在不能添加支付记录
        Long orderId = orders.getId();
        XcOrders xcOrders = ordersMapper.selectById(orderId);
        if(xcOrders==null){
            XueChengPlusException.cast("订单不存在");
        }
        // 如果已支付不能重复支付
        if(orders.getStatus().equals("601002")){
            XueChengPlusException.cast("订单已支付");
        }
        XcPayRecord payRecord = new XcPayRecord();
        //生成支付交易流水号 雪花算法
        long payNo = IdWorkerUtils.getInstance().nextId();
        payRecord.setPayNo(payNo);
        payRecord.setOrderId(orders.getId());//商品订单号
        payRecord.setOrderName(orders.getOrderName());
        payRecord.setTotalPrice(orders.getTotalPrice());
        payRecord.setCurrency("CNY");
        payRecord.setCreateDate(LocalDateTime.now());
        payRecord.setStatus("601001");//未支付
        payRecord.setUserId(orders.getUserId());
        int insert = payRecordMapper.insert(payRecord);
        if(insert <= 0) XueChengPlusException.cast("插入记录支付失败");
        return payRecord;

    }


    //根据业务id查询订单
    private XcOrders getOrderByBusinessId(String businessId) {
        // outBusinessId是选课表（外部服务）的ID
        return ordersMapper.selectOne(new LambdaQueryWrapper<XcOrders>().eq(XcOrders::getOutBusinessId, businessId));
    }



}
