package com.raptor.ownspring.test;

import com.raptor.ownspring.application.OwnAnnotationConfigApplicationContext;

public class AnnotationTest {
    public static void main(String[] args) {
        //工具类测试
        //获取一个包下的类
        OwnAnnotationConfigApplicationContext ownAnnotationConfigApplicationContext = new OwnAnnotationConfigApplicationContext("com.raptor.ownspring.entity");
        Object account = ownAnnotationConfigApplicationContext.getBean("account");
        System.out.println(account);

        Object orderTest = ownAnnotationConfigApplicationContext.getBean("orderTest");
        System.out.println(orderTest);
    }
}
