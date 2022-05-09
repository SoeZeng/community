package com.nowcoder.community.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KaptchaConfig {

    @Bean //声明一个Bean，这个Bean会被Spring容器所管理
    //实例化Kaptcha的核心接口Producer
    public Producer kaptchaProducer() {
        Properties properties = new Properties();
        properties.setProperty("kaptcha.image.width","100");
        properties.setProperty("kaptcha.image.height","40");
        properties.setProperty("kaptcha.textproducer.font.size","32");
        properties.setProperty("kaptcha.textproducer.font.color","0,0,0");
        properties.setProperty("kaptcha.textproducer.char.string","0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"); //验证码内容
        properties.setProperty("kaptcha.textproducer.char.length","4");  //验证码的长度
        properties.setProperty("kaptcha.noise.impl","com.google.code.kaptcha.impl.NoNoise"); //采用的干扰类



        DefaultKaptcha kaptcha = new DefaultKaptcha();

        //将传入的参数封装到Config对象里,Config依赖于Properties对象
        Config config = new Config(properties);
        kaptcha.setConfig(config);
        return kaptcha;
    }


}
