package com.raptor.service.impl;

import com.raptor.dao.HelloDao;
import com.raptor.factory.BeanFactory;
import com.raptor.service.HelloService;

import java.util.List;

public class HelloServiceImpl implements HelloService {

    //单例bean测试
    public HelloServiceImpl() {
        for (int i = 0; i < 10; i++) {
            System.out.println(BeanFactory.getDao("helloDao"));
        }
    }

    private HelloDao helloDao = (HelloDao) BeanFactory.getDao("helloDao");

    @Override
    public List<String> findAll() {
        return this.helloDao.findAll();
    }
}
