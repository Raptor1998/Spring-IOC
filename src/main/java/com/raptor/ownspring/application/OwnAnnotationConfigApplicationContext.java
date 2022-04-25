package com.raptor.ownspring.application;

import com.raptor.ownspring.BeanPostProcessor;
import com.raptor.ownspring.annotation.*;
import com.raptor.ownspring.aware.BeanNameAware;
import com.raptor.ownspring.definition.BeanDefinition;
import com.raptor.ownspring.initializing.InitializingBean;
import com.raptor.ownspring.utils.MyTools;
import com.sun.org.apache.xml.internal.security.Init;
import sun.reflect.generics.scope.Scope;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class OwnAnnotationConfigApplicationContext {

    private Map<String, Object> cache = new HashMap<>();
    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

    private List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    public OwnAnnotationConfigApplicationContext(String packageName) {
        //找到对应的包，加载对应的class
        Set<BeanDefinition> beanDefinitions = findBeanDefinition(packageName);

        //根据类名和class   创建bean
        createObject(beanDefinitions);

        //自动装在
        autowireObject(beanDefinitions);
    }

    public void autowireObject(Set<BeanDefinition> beanDefinitions) {
        Iterator<BeanDefinition> iterator = beanDefinitions.iterator();
        while (iterator.hasNext()) {
            BeanDefinition beanDefinition = iterator.next();
            Class beanClass = beanDefinition.getBeanClass();
            Field[] declaredFields = beanClass.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                OwnAutowired ownAutowiredAnnotation = declaredField.getAnnotation(OwnAutowired.class);
                if (ownAutowiredAnnotation != null) {
                    OwnQualifier ownQualifierAnnotation = declaredField.getAnnotation(OwnQualifier.class);
                    if (ownQualifierAnnotation != null) {
                        //有auto  也有  Qualifier
                        //根据bean名称
                        try {
                            String beanName = ownQualifierAnnotation.value();
                            //属性bean
                            Object o = cache.get(beanName);
                            String fieldName = declaredField.getName();
                            String methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                            Method method = beanClass.getMethod(methodName, declaredField.getType());
                            //需要设置  属性的 类
                            Object o1 = cache.get(beanDefinition.getBeanName());

                            method.invoke(o1, o);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        //只有autowire
                        //根据类型
                        Class<?> aClass = declaredField.getType();
                        OwnComponent annotation = aClass.getAnnotation(OwnComponent.class);
                        if (annotation != null) {
                            String beanName = annotation.value();
                            if ("" .equals(beanName)) {
                                String className = aClass.getName().replaceAll(aClass.getPackage().getName() + ".", "");
                                beanName = className.substring(0, 1).toLowerCase() + className.substring(1);
                            }
                            try {
                                Object o = cache.get(beanName);
                                String fieldName = declaredField.getName();
                                String methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                                Method method = beanClass.getMethod(methodName, declaredField.getType());
                                Object o1 = cache.get(beanDefinition.getBeanName());
                                System.out.println(o.getClass());
                                method.invoke(o1, o);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    public Object getBean(String beanName) {
        if (beanDefinitionMap.containsKey(beanName)) {
            System.out.println(beanName);
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScope().equals("singleton")) {
                return cache.get(beanName);
            } else {
                return createBean(beanDefinition);
            }
        } else {
            throw new RuntimeException(beanName + " not exist");
        }

    }

    public void createObject(Set<BeanDefinition> beanDefinitions) {
        Iterator<BeanDefinition> iterator = beanDefinitions.iterator();
        while (iterator.hasNext()) {
            BeanDefinition beanDefinition = iterator.next();
            Object object = createBean(beanDefinition);
            //放入缓存
            cache.put(beanDefinition.getBeanName(), object);
        }
    }

    private Object createBean(BeanDefinition beanDefinition) {
        Class beanClass = beanDefinition.getBeanClass();
        String beanName = beanDefinition.getBeanName();
        try {
            //创建对象
            Object object = beanClass.getConstructor().newInstance();
            //属性赋值
            Field[] declaredFields = beanClass.getDeclaredFields();
            for (Field declaredField : declaredFields) {
//                    System.out.println(declaredField);
                OwnValue annotation = declaredField.getAnnotation(OwnValue.class);
                //不等于null  有注解
                if (annotation != null) {
                    String value = annotation.value();
                    //属性名
                    String fieldName = declaredField.getName();
                    String methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                    Method method = beanClass.getMethod(methodName, declaredField.getType());
                    //完成数据类型转换
                    Object val = null;
                    switch (declaredField.getType().getName()) {
                        case "java.lang.Integer":
                            val = Integer.parseInt(value);
                            break;
                        case "java.lang.String":
                            val = value;
                            break;
                        case "java.lang.Float":
                            val = Float.parseFloat(value);
                            break;
                    }
                    method.invoke(object, val);
                }
            }
            //aware回调
            if (object instanceof BeanNameAware) {
                ((BeanNameAware) object).setBeanName(beanName);
            }
            for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
                object = beanPostProcessor.postProcessBeforeInitialization(object, beanName);
            }
            
            //初始化
            if (object instanceof InitializingBean) {
                ((InitializingBean) object).afterPropertiesSet();
            }

            //bean post processor
            for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
                object = beanPostProcessor.postProcessAfterInitialization(object, beanName);
            }

            return object;
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new RuntimeException(beanName + " created error");
    }

    public Set<BeanDefinition> findBeanDefinition(String packageName) {
        //获取当前包所有的类
        //遍历所有的类
        //奖这写类封装成 BeanDefinition，装在到集合中
        Set<BeanDefinition> set = new HashSet<>();
        Set<Class<?>> classes = MyTools.getClasses(packageName);
        Iterator<Class<?>> iterator = classes.iterator();
        while (iterator.hasNext()) {
            Class<?> aClass = iterator.next();
            OwnComponent annotation = aClass.getAnnotation(OwnComponent.class);
            if (annotation != null) {

                //如果是特殊的processor
                if (BeanPostProcessor.class.isAssignableFrom(aClass)) {
                    try {
                        BeanPostProcessor beanPostProcessor = (BeanPostProcessor) aClass.getDeclaredConstructor().newInstance();
                        beanPostProcessors.add(beanPostProcessor);
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }


                //有注解   目标类找到
//                System.out.println(aClass+" 有注解");
                //封装一个BeanDefinition
                String beanName = annotation.value();
                if ("" .equals(beanName)) {
                    //没写别名
                    //获取到的类名是全类名   可以通过获取包名然后替换    同时将首字母更换为小写
                    //之更改第一个字母为小写即可
                    //beanName = aClass.getName().replace(classNamePre, "").toLowerCase();
                    String className = aClass.getName().replaceAll(aClass.getPackage().getName() + ".", "");
                    beanName = className.substring(0, 1).toLowerCase() + className.substring(1);
                }
                BeanDefinition beanDefinition = new BeanDefinition(beanName, aClass);
                //如果有bean的作用域
                if (aClass.isAnnotationPresent(OwnScope.class)) {
                    OwnScope scopeAnnotation = aClass.getDeclaredAnnotation(OwnScope.class);
                    beanDefinition.setScope(scopeAnnotation.value());
                    if (scopeAnnotation.value().equals("singleton")) {
                        set.add(beanDefinition);
                    }
                } else {
                    //默认是单例bean
                    beanDefinition.setScope("singleton");
                    //单例bean等会直接创建对象
                    set.add(beanDefinition);
                }
                //所有的原材料记录一下
                beanDefinitionMap.put(beanDefinition.getBeanName(), beanDefinition);
            }
        }
        return set;
    }

    private Class configClass;

    public OwnAnnotationConfigApplicationContext(Class<?> appConfigClass) {
        this.configClass = appConfigClass;

        //解析配置类     扫描包的路径
        OwnComponentScan componentScanAnnotation = (OwnComponentScan) configClass.getDeclaredAnnotation(OwnComponentScan.class);
        if (componentScanAnnotation != null) {
            String path = componentScanAnnotation.value();
//            System.out.println(path);
            //获取原材料
            Set<BeanDefinition> beanDefinitions = findBeanDefinition(path);
            createObject(beanDefinitions);
            autowireObject(beanDefinitions);
        }
    }
}
