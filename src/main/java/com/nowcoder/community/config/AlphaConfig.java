package com.nowcoder.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.SimpleDateFormat;

@Configuration
public class AlphaConfig {
    @Bean //定义第三方Bean
    public SimpleDateFormat simpleDateFormat() { //方法名即为Bean的名字
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    }
}
