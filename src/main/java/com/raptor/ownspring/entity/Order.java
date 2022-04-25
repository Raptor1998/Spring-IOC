package com.raptor.ownspring.entity;

import com.raptor.ownspring.annotation.OwnComponent;
import com.raptor.ownspring.annotation.OwnValue;
import com.raptor.ownspring.inte.IAop;
import lombok.Data;

@Data
@OwnComponent("orderTest")
public class Order{
    @OwnValue("qsad")
    private String orderId;
    @OwnValue("1000.2")
    private Float price;
//    @OwnAutowired
//    private Account account;
}
