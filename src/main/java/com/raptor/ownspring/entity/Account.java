package com.raptor.ownspring.entity;

import com.raptor.ownspring.annotation.OwnAutowired;
import com.raptor.ownspring.annotation.OwnComponent;
import com.raptor.ownspring.annotation.OwnQualifier;
import com.raptor.ownspring.annotation.OwnValue;
import lombok.Data;

@Data
@OwnComponent
public class Account {
    private Integer id;
    @OwnValue("raptor")
    private String name;
    @OwnValue("23")
    private Integer age;
    @OwnAutowired
//    @OwnQualifier(value = "orderTest")
    private Order order;

}
