package com.raptor.ownspring.entity;

import com.raptor.ownspring.BeanPostProcessor;
import com.raptor.ownspring.annotation.OwnComponent;
import com.raptor.ownspring.entity.Account;
import com.raptor.ownspring.entity.Order;

/**
 *
 *
 * 为了扫描方便，放在此处
 * 对spring而言，扫描类的时候
 * 发现类不但有component  还是先了processor 会与普通bean不同处理
 */
@OwnComponent
public class OwnBeanPost implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {

        if (beanName.equals("account")) {
            System.out.println("bean的初始化前");
            ((Account) bean).test();
            ((Account) bean).setName("铠甲勇士");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("bean的初始化后");

        if (beanName.equals("orderTest")) {
            System.out.println(((Order)bean).getPrice());
        }

        return bean;
    }
}
