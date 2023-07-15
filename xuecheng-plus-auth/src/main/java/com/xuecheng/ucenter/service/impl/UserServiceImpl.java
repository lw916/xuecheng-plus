package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.po.XcUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Slf4j
@Component
public class UserServiceImpl implements UserDetailsService {

    @Autowired
    XcUserMapper xcUserMapper;

    // 传入的请求认证的参数是AuthParamsDto
    @Override
    // 重写userDetail去数据库拿用户名和密码
    public UserDetails loadUserByUsername(String user) throws UsernameNotFoundException{

        AuthParamsDto authParamsDto = null;
        // 将传入的jsonchuan转为AuthParamsDto
        try{
            authParamsDto = JSON.parseObject(user, AuthParamsDto.class);
        }catch (Exception e){
            throw new RuntimeException("请求认证的参数不符合");
        }
        String userName = authParamsDto.getUsername();
        // 根据username账户查询
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, userName));
        // 查询用户不存在返回null，SpringSecurity自动抛出异常
        if(xcUser == null) return null;
        // 查到了则匹配用户密码，最终封装成UserDetails放出
        String password = xcUser.getPassword();
        String[] authorities = {"test"};
        // 用户信息转json
        xcUser.setPassword(null);
        String userJson = JSON.toJSONString(xcUser); // 扩展用户信息到jwt令牌中让信息更容易获取
        return User.withUsername(userJson).password(password).authorities(authorities).build();
    }

    // 从用户表获取用户的信息



}
