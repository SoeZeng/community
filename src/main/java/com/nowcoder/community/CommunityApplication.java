package com.nowcoder.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication //此注解表示的类表明该类是一个配置文件
public class CommunityApplication {

	public static void main(String[] args) {
		//启动Tomcat
		//自动创建Spring容器
		SpringApplication.run(CommunityApplication.class, args);
	}

}
