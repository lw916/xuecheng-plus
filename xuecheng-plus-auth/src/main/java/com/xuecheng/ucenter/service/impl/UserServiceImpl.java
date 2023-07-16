package com.xuecheng.ucenter.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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

    @Autowired
    ApplicationContext applicationContext; // 注入Spring容器

    // 传入的请求认证的参数是AuthParamsDto
    @Override
    // 重写userDetail去数据库拿用户名和密码
    public UserDetails loadUserByUsername(String str) throws UsernameNotFoundException{
        AuthParamsDto authParamsDto = null;
        // 将传入的jsonchuan转为AuthParamsDto
        try{
            authParamsDto = JSON.parseObject(str, AuthParamsDto.class);
        }catch (Exception e){
            throw new RuntimeException("请求认证的参数不符合");
        }
        // 获取认证方式
        /**
         * @description 该方法用于确认使用什么方法去认证 统一认证
         */
        String authType = authParamsDto.getAuthType();
        // 根据认证方式从Spring容器中取出指定Bean
        String beanName = authType + "_authservice";
        AuthService authService = applicationContext.getBean(beanName, AuthService.class);// @Todo 理解下
        // 调用方法
        XcUserExt xcUserExt = authService.execute(authParamsDto);
        // 分装用户数据为UserDetails
        // 查到了则匹配用户密码，最终封装成UserDetails放出
        return this.getUserPrincipal(xcUserExt);
    }

    // 从用户表获取用户的信息
    /**
     * @description 查询用户信息
     * @param user  用户id，主键
     * @return com.xuecheng.ucenter.model.po.XcUser 用户信息
     * @author Mr.M
     * @date 2022/9/29 12:19
     */
    public UserDetails getUserPrincipal(XcUserExt user){
        //用户权限,如果不加报Cannot pass a null GrantedAuthority collection
        String[] authorities = {"p1"};
        String password = user.getPassword();
        //为了安全在令牌中不放密码
        user.setPassword(null);
        //将user对象转json
        String userString = JSON.toJSONString(user);
        //创建UserDetails对象
        return User.withUsername(userString).password(password).authorities(authorities).build();
    }




}
