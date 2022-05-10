package com.nowcoder.community.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

//@Component
//@Aspect
public class AlphaAspect {

    // *：1th表示所有返回值，2nd表示所有业务组件，3td表示所有方法；..：表示所有参数
    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")
    public void pointcut() {

    }

    @Before("pointcut()") // 连接点的开头
    public void before() {
        System.out.println("before");
    }

    @After("pointcut()") // 连接点的末尾
    public void after() {
        System.out.println("after");
    }

    @AfterReturning("pointcut()") // 有了返回值以后
    public void afterReturning() {
        System.out.println("afterReturning");
    }

    @AfterThrowing("pointcut()") // 抛异常以后
    public void afterThrowing() {
        System.out.println("afterThrowing");
    }

    @Around("pointcut()") // 前后都织入逻辑
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("around before");
        // 调用目标组件的方法
        Object obj = joinPoint.proceed();
        System.out.println("around after");
        return obj;
    }


}
