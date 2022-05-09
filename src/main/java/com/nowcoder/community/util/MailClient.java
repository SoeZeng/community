package com.nowcoder.community.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.security.auth.Subject;

@Component
public class MailClient{

    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public void sendMail(String to, String subject, String content) {

        try {
            //create的对象是个空的，只是一个模板
            MimeMessage message = mailSender.createMimeMessage();

            //使用帮助类构建更详细的内容
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(from);
            //设置收件人
            helper.setTo(to);
            helper.setSubject(subject);
            //不加参数true默认该文本为普通文本，加参数表示允许支持html文件
            helper.setText(content,true);
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            logger.error("发送邮件失败" + e.getMessage());
        }
    }

}

