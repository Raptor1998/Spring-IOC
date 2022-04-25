package com.raptor.ownspring.entity;

import com.raptor.ownspring.annotation.OwnComponent;
import com.raptor.ownspring.annotation.OwnValue;
import com.raptor.ownspring.inte.IAop;
import lombok.Data;

@OwnComponent(value = "aopTest")
public class AopTest implements IAop {

    @OwnValue(value = "hihi")
    private String name;

    public AopTest() {
        System.out.println("自定义的初始化");
    }

    public AopTest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "AopTest{" +
                "name='" + name + '\'' +
                '}';
    }
}
