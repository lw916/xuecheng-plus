package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
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

    @Override
    // 重写userDetail去数据库拿用户名和密码
    public UserDetails loadUserByUsername(String user) throws UsernameNotFoundException{

        // 根据username账户查询
        XcUser xcUser = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, user));
        // 查询用户不存在返回null，SpringSecurity自动抛出异常
        if(xcUser == null) return null;
        // 查到了则匹配用户密码，最终封装成UserDetails放出
        String password = xcUser.getPassword();
        String[] authorities = {"test"};
        return User.withUsername(user).password(password).authorities(authorities).build();
    }


}
