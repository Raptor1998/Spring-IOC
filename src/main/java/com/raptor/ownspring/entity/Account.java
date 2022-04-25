package com.raptor.ownspring.entity;

import com.raptor.ownspring.annotation.*;
import com.raptor.ownspring.aware.BeanNameAware;
import com.raptor.ownspring.initializing.InitializingBean;
import lombok.Data;

//@Data
@OwnComponent
@OwnScope("singleton")
public class Account implements BeanNameAware, InitializingBean {

    private String beanName;

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
        System.out.println("account中的回调,beanName:" + beanName);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("account中的初始化后");
    }

    private Integer id;
    @OwnValue("raptor")
    private String name;
    @OwnValue("23")
    private Integer age;
    @OwnAutowired
//    @OwnQualifier(value = "orderTest")
    private Order order;

    public Account() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", order=" +
                '}';
    }


    public void test(){
        System.out.println("account test !");
    }
}
