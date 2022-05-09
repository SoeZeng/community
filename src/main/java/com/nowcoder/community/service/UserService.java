package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.Thymeleaf;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    //激活码中要包含域名+项目名，需注入
    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contentPath;

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    public Map<String,Object> register(User user) {
        Map<String,Object> map = new HashMap<>();

        //空值处理
        if(user == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if(StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg","账号不能为空！");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg","密码不能为空！");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg","邮箱不能为空！");
            return map;
        }

        //验证账号
        User u = userMapper.selectByName(user.getUsername());
        if(u != null) {
            map.put("usernameMsg","该账号已存在");
            return map;
        }

        //验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if(u != null) {
            map.put("emailMsg","该邮箱已被注册");
            return map;
        }

        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.MD5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        //设置随机头像
        user.setHeaderUrl(String.format("https://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        //当调用insert语句之后，Mybatis使用了自动生成id的机制，所以会自动获取id对其进行回填
        userMapper.insertUser(user);

        //发送激活邮件
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        // http://localhost:8080/community/activation/101/code
        String url = domain + contentPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(),"激活账号",content);

        return map;
    }

    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if(user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId,1);
            return ACTIVATION_SUCCESS;
        }else {
            return ACTIVATION_FAILURE;
        }
    }


    public Map<String, Object> login(String username, String password, int expiredSeconds) {

        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if(StringUtils.isBlank(username)) {
            map.put("usernameMsg","账号不能为空！");
            return map;
        }
        if(StringUtils.isBlank(password)) {
            map.put("passwordMsg","密码不能为空！");
            return map;
        }

        // 验证账号
        User user = userMapper.selectByName(username);
        if(user == null) {
            map.put("usernameMsg","该账号不存在！");
            return map;
        }

        // 验证状态
        if(user.getStatus() == 0) {
            map.put("usernameMsg","该账号未激活！");
            return map;
        }

        // 验证密码
        // 首先对传入的的明文密码按照同样的规则加密
        password = CommunityUtil.MD5(password + user.getSalt());
//        System.out.println(password);
        if(!user.getPassword().equals(password)) {
            map.put("passwordMsg","密码不正确！");
            return map;
        }

        // 生成登录凭证，服务器记录登录状态并给客户端发放凭证用于记录
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket) {
        loginTicketMapper.updateStatus(ticket,1);
    }

    public LoginTicket findLoginTicket(String ticket) {
        return loginTicketMapper.selectByTicket(ticket);
    }

    public int updateHeader(int userId, String headerUrl) {
        return userMapper.updateHeader(userId,headerUrl);
    }


    public Map<String, Object> updatePassword(int userId, String oldPassword, String newPassword) {
        Map<String, Object> map = new HashMap<>();

        if(StringUtils.isBlank(oldPassword)) {
            map.put("oldPasswordMsg","原密码不能为空！");
            return map;
        }

        if(StringUtils.isBlank(newPassword)) {
            map.put("newPasswordMsg","新密码不能为空！");
            return map;
        }

        User user = userMapper.selectById(userId);

        oldPassword = CommunityUtil.MD5(oldPassword + user.getSalt());
        if(!user.getPassword().equals(oldPassword)) {
            map.put("oldPasswordMsg","原密码不正确！");
            return map;
        }

        newPassword = CommunityUtil.MD5(newPassword + user.getSalt());
        map.put("updateCompleted",userMapper.updatePassword(userId,newPassword));
        return map;
    }

}
