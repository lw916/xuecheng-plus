package com.xuecheng.system.config;

import org.springframework.web.filter.CorsFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 *
 * @author Wayne
 * @description 跨域头设置
 * @date 2023/6/30
 */

@Configuration
public class GlobalCorsConfig {

    @Bean
    public CorsFilter corsFilter(){

        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("*"); // 允许所有跨域访问
        config.setAllowCredentials(true); // 允许跨域发送cookie
        config.addAllowedHeader("*"); // 放行全部原始头信息
        config.addAllowedMethod("*"); // 允许所有方法跨域调用
        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        corsConfigurationSource.registerCorsConfiguration("/**", config);
        return new CorsFilter(corsConfigurationSource);
    }

}
