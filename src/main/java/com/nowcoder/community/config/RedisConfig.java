package com.nowcoder.community.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    // redisTemplate要想具备数据库的能力，需要创建连接，而连接是由连接工厂实现的，所以需要将连接工厂注入到此Template中
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 要将Java数据存放到Redis数据库中，要指定一种序列化的方式
        // 设置key的序列化方式
        template.setKeySerializer(RedisSerializer.string());

        // 设置value的序列化方式
        template.setValueSerializer(RedisSerializer.json());

        // 设置hash的key的序列化方式
        template.setHashKeySerializer(RedisSerializer.string());

        // 设置hash的value的序列化方式
        template.setHashValueSerializer(RedisSerializer.json());

        // 设置完成后触发生效
        template.afterPropertiesSet();
        return template;

    }
}
