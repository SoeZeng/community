package com.nowcoder.community.controller;

import com.nowcoder.community.service.AlphaService;
import com.nowcoder.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@Controller
@RequestMapping("/alpha") //规定访问路径
public class AlphaController {

    @Autowired
    private AlphaService alphaService;

    @RequestMapping("/hello")
    @ResponseBody //返回字符串
    public String sayhello() {
        return "hello Spring Boot";
    }

    @RequestMapping("/data")
    @ResponseBody
    public String getData() {
        return alphaService.find();
    }

    @RequestMapping("/http")
//    @ResponseBody
    public void http(HttpServletRequest request, HttpServletResponse response) {
        /*获取请求数据*/
        //请求行
        System.out.println(request.getMethod());
        System.out.println(request.getServletPath());
        //请求消息头
        Enumeration<String> enumeration = request.getHeaderNames();
        while(enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            String value = request.getHeader(name);
            System.out.println(name + ": " + value);
        }
        System.out.println(request.getParameter("code"));

        /*返回响应数据*/
        //设置返回类型
        response.setContentType("text/html;charset=utf-8");
        //获取输出流
        try(
                PrintWriter writer = response.getWriter(); //jdk7新语法，自动在finally中关闭writer
        ) {

            writer.write("<h1>牛客网</h1>");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //GET请求

    //students?current=3&limit=50
    @RequestMapping(path = "/students",method = RequestMethod.GET)
    @ResponseBody
    public String getStudents(
            @RequestParam(name = "current",required = false,defaultValue = "1") int current,
            @RequestParam(name = "limit",required = false,defaultValue = "10") int limit) {
        System.out.println(current);
        System.out.println(limit);
        return "some students";
    }

    //student/123
    @RequestMapping(path = "/student/{id}", method = RequestMethod.GET)
    @ResponseBody
    public String getStudent(@PathVariable("id") int id) {
        System.out.println(id);
        return "a student";
    }


    //POST请求
    @RequestMapping(path = "/student", method = RequestMethod.POST)
    @ResponseBody
    public String saveStudent(String name, int age) {
        System.out.println(name);
        System.out.println(age);
        return "success";
    }

    //响应HTML数据
    @RequestMapping(path = "/teacher", method = RequestMethod.GET)
    public ModelAndView getTeacher() {
        ModelAndView mav = new ModelAndView();
        mav.addObject("name","张三");
        mav.addObject("age","30");
        mav.setViewName("/demo/view");
        return mav;
    }

    @RequestMapping(path = "/school", method = RequestMethod.GET)
    public String getSchool(Model model) {
        model.addAttribute("name","SUEP");
        model.addAttribute("age","70");
        return "demo/view";
    }

    //响应JSON数据（异步请求）
    //Java对象 -> JSON字符串 -> JS对象
    @RequestMapping(path = "/emp", method = RequestMethod.GET)
    @ResponseBody
    public Map<String,Object> getEmp() {
        Map<String,Object> emp = new HashMap<>();
        emp.put("name","张三");
        emp.put("age","23");
        emp.put("salary","8000");
        return emp;

    }

    @RequestMapping(path = "/emps", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String,Object>> getEmps() {
        List<Map<String,Object>> list = new ArrayList<>();
        Map<String,Object> emp = new HashMap<>();
        emp.put("name","张三");
        emp.put("age","23");
        emp.put("salary","8000");
        list.add(emp);

        emp = new HashMap<>();
        emp.put("name","李四");
        emp.put("age","23");
        emp.put("salary","8000");
        list.add(emp);

        emp = new HashMap<>();
        emp.put("name","王五");
        emp.put("age","23");
        emp.put("salary","8000");
        list.add(emp);
        return list;

    }

    //cookie示例
    //模拟浏览器访问服务器，第一次请求时服务器将cookie发送给浏览器
    //浏览器再次访问服务器，请求头中包含cookie
    @RequestMapping(path = "/cookie/set", method = RequestMethod.GET)
    @ResponseBody
    //创建完cookie后要将cookie存到response里，在响应的时候才能自动携带给浏览器
    public String setCookie(HttpServletResponse response) {
        //创建cookie
        //Cookie没有无参构造器，必须传入参数，且参数类型都是字符串，一个cookie只能存一对key-value
        Cookie cookie = new Cookie("code", CommunityUtil.generateUUID());
        //设置cookie生效范围：声明在哪些路径上有效
        cookie.setPath("/community/alpha"); //访问该路径及其子路径才有效
        //设置cookie生存时间，默认存到内存重启后消失，设置生效时间后会存到硬盘中，超过时间才失效
        cookie.setMaxAge(60*10);
        //发送到响应头
        response.addCookie(cookie);

        return "set cookie";

    }

    @RequestMapping(path = "/cookie/get", method = RequestMethod.GET)
    @ResponseBody
    //将key为"code"的cookie赋给参数code
    public String getCookie(@CookieValue("code") String code) {
        System.out.println(code);
        return "get cookie";
    }

    //session示例,session会通过cookie给浏览器传送一个session id
    @RequestMapping(path = "/session/set",method = RequestMethod.GET)
    @ResponseBody
    //服务器/Spring MVC会自动创建session并注入
    public String setSession(HttpSession session) {
        session.setAttribute("id",1);
        session.setAttribute("name","Test");
        return "set session";

    }

    @RequestMapping(path = "/session/get",method = RequestMethod.GET)
    @ResponseBody
    //服务器/Spring MVC会自动创建session并注入
    public String getSession(HttpSession session) {
        System.out.println(session.getAttribute("id"));
        System.out.println(session.getAttribute("name"));
        return "get session";

    }

    // ajax示例
    @RequestMapping(path = "/ajax", method = RequestMethod.POST)
    @ResponseBody
    public String testAjax(String name, int age) {
        System.out.println(name);
        System.out.println(age);
        return CommunityUtil.getJSONString(0,"操作成功！");
    }

}
