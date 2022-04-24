package com.raptor.factory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class BeanFactory {

    private static Properties properties;
    private static Map<String, Object> cache = new HashMap<>();

    static {
        properties = new Properties();
        try {
            properties.load(BeanFactory.class.getClassLoader().getResourceAsStream("factory.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 1、强依赖/紧耦合，编译之后无法修改，没有扩展性。
     * 2、弱依赖/松耦合，编译之后仍然可以修改，让程序具有更好的扩展性。
     * 自己放弃了创建对象的权限，将创建对象的权限交给了BeanFactory，
     * 这种将控制权交给别人的思想，就是控制反转 IoC。
     */
    public static Object getDao(String beanName) {
        //判断缓存中是否存在bean
        boolean containsKey = cache.containsKey(beanName);
        if (!containsKey) {
            synchronized (BeanFactory.class) {
                try {
                    String value = properties.getProperty(beanName); //反射机制创建对象 try {Class clazz = Class.forName(value);
                    Class clazz = Class.forName(value);
                    Object object = clazz.getConstructor(null).newInstance(null);
                    cache.put(beanName, object);
                    return object;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return cache.get(beanName);
    }
}

