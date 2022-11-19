package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStreamImpl;
import javax.jws.WebParam;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Controller
public class LoginController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${server.servlet.context-path}")
    private String contextPath;


    @RequestMapping(path = "/register",method = RequestMethod.GET)
    public String getRegisterPage() {

        return "/site/register";
    }

    // 给浏览器返回一个html，html中会包含一个图片的路径，
    // 浏览器会根据这个路径再次访问服务器获取图片，所以需要在单独写一个请求向浏览器返回图片，
    // 这个请求会在"/site/login"模板中引用它的路径
    @RequestMapping(path = "/login",method = RequestMethod.GET)
    public String getLoginPage() {

        return "/site/login";
    }

    @RequestMapping(path = "/register",method = RequestMethod.POST)
    public String register(Model model, User user) {
        Map<String, Object> map = userService.register(user);
        //注册成功后跳转到首页
        if(map == null || map.isEmpty()) {
            model.addAttribute("msg","注册成功，我们已经向您的邮箱发送了一封激活邮件，请尽快激活！");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "site/register";
        }
    }

    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    //@PathVariable接收请求路径中占位符的值
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        if(result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg","激活成功，您的账号已经可以正常使用了！");
            model.addAttribute("target","/login");
        }else if(result == ACTIVATION_REPEAT) {
            model.addAttribute("msg","无效操作，该账号已经激活过了！");
            model.addAttribute("target","/index");
        }else {
            model.addAttribute("msg","激活失败，您提供的激活码不正确！");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }

    // 获取图片，需要用Response对象手动向浏览器输出
    @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
    // 生成验证码之后，服务端需要保存，当浏览器再次访问服务器时确认验证码是否正确，
    // 该验证码不能存在浏览器端，容易被盗取
    // 第一次请求时生成，登录时再使用 -> session
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/) {
        // 生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        // 将验证码存入session
        // session.setAttribute("kaptcha",text);

        // 验证码的归属
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60); // 生存时间
        cookie.setPath(contextPath); // 生效范围
        response.addCookie(cookie);

        // 将验证码存入Redis
        String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(kaptchaKey, text, 60, TimeUnit.SECONDS);

        // 将图片输出给浏览器
        response.setContentType("image/png");
        // response给浏览器做响应，需要获取它的输出流
        try {
            ServletOutputStream os = response.getOutputStream();
            ImageIO.write(image, "png",os);
        } catch (IOException e) {
            logger.error("响应验证码失败：" + e.getMessage());

        }
    }

    @RequestMapping(path = "/login",method = RequestMethod.POST)
    // 如果某个参数是一个实体(Bean)，Spring MVC会自动将其注入Model，页面就可以直接获得该实体类对象的数据；
    // 但如果是基本数据类型，不会将其注入Model，则可以 -> ①手动注入Model ②这些参数是存在于Request对象中的，可通过Request.getParameter得到
    public String login(String username, String password, String code, boolean rememberme,
                        Model model,/* HttpSession session,*/ HttpServletResponse response,
                        @CookieValue("kaptchaOwner") String kaptchaOwner) {
        // 检查验证码
        // String kaptcha = (String) session.getAttribute("kaptcha");
        String kaptcha = null;
        if(StringUtils.isNotBlank(kaptchaOwner)) {
            String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);
        }

        if(StringUtils.isBlank(kaptcha) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg","验证码不正确！");
            return "/site/login";
        }

        // 检查账号，密码
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if(map.containsKey("ticket")) {
            // 将ticket发送给客户端
            Cookie cookie = new Cookie("ticket",map.get("ticket").toString());
            cookie.setPath(contextPath); // cookie的有效路径
            cookie.setMaxAge(expiredSeconds); // cookie的有效时间
            response.addCookie(cookie);

            // 重定向到首页
            return "redirect:/index";
        }else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {

        userService.logout(ticket);
        return "redirect:/login";
    }

}
