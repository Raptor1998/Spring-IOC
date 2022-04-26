package com.raptor.originspring.test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Test {
    public static void main(String[] args) {
        //加载IoC容器
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext("com.raptor.originspring.entity");
        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
        System.out.println(applicationContext.getBeanDefinitionCount());
        for (String beanDefinitionName : beanDefinitionNames) {
            System.out.println(beanDefinitionName);
            System.out.println(applicationContext.getBean(beanDefinitionName));
        }
    }
}
