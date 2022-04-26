package com.raptor.ownspring.entity;

import com.raptor.ownspring.BeanPostProcessor;
import com.raptor.ownspring.annotation.OwnComponent;
import com.raptor.ownspring.entity.Account;
import com.raptor.ownspring.entity.Order;
import com.raptor.ownspring.inte.IAop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
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

        if (beanName.equals("aopTest")) {
            Object proxyInstance = Proxy.newProxyInstance(BeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("初始化之后的代理逻辑");
                    return method.invoke(bean,args);
                }
            });
            return proxyInstance;
        }else if (beanName.equals("orderTest")){
            System.out.println("order 增强测试");
            //有问题   代理对象返回类型不对
            //Object proxyInstance = Proxy.newProxyInstance(BeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
            //    @Override
            //    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            //        ((Order)bean).setPrice(1000f);
            //        return method.invoke(bean,args);
            //    }
            //});
            //return proxyInstance;
        }

        return bean;
    }
}
