package com.raptor.ownspring.test;

import com.raptor.ownspring.application.OwnAnnotationConfigApplicationContext;
import com.raptor.ownspring.config.AppConfig;
import com.raptor.ownspring.entity.Account;

public class AppConfigTest {
    public static void main(String[] args) {
        OwnAnnotationConfigApplicationContext ownAnnotationConfigApplicationContext = new OwnAnnotationConfigApplicationContext(AppConfig.class);
        for (int i = 0; i < 1; i++) {
            Object test = ownAnnotationConfigApplicationContext.getBean("orderTest");
            System.out.println(test);
        }
    }
}
