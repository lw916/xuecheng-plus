package com.xuecheng.ucenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.feignclient.CheckCodeClient;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @description 账户密码认证方式
 */
@Service("password_authservice")// 一个接口多个类型
public class PasswordAuthServiceImpl implements AuthService {

    @Resource
    XcUserMapper xcUserMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    CheckCodeClient checkCodeClient;

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto) {
        // 获取用户名
        String userName = authParamsDto.getUsername();
        // @Todo 校验验证码
        String checkCode = authParamsDto.getCheckcode();
        String checkCodeKey = authParamsDto.getCheckcodekey();
        if(StringUtils.isEmpty(checkCode) || StringUtils.isEmpty(checkCodeKey)){
            throw new RuntimeException("请输入验证码");
        }
        // 远程调用验证码服务去校验
        Boolean verify = checkCodeClient.verify(checkCodeKey, checkCode);
        if(verify == null || !verify){
            throw new RuntimeException("验证码错误");
        }

        // 账户是否存在
        XcUser user = xcUserMapper.selectOne(new LambdaQueryWrapper<XcUser>().eq(XcUser::getUsername, userName));
        if(user == null){
            throw new RuntimeException("账户不存在！");
        }
        // 密码是否正确
        String password = user.getPassword();
        String user_input_password = authParamsDto.getPassword();
        boolean matches = passwordEncoder.matches(user_input_password, password);
        if(!matches) {
            throw new RuntimeException("密码错误！");
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(user, xcUserExt);
        return xcUserExt;
    }
}
