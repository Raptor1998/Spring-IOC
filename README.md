# Spring-IOC
a simple version of spring IOC and AOP Demo

**好莱坞法则：“别找我们，我们找你”**；即由IoC容器帮对象找相应的依赖对象并注入，而不是由对象主动去找。

# IOC Foundation

控制反转，把对象的创建和对象之间的调用过程，交给spring进行管理，使用IOC目的，为了降低耦合度

IoC **不是一种技术，只是一种思想**，一个重要的面向对象编程的法则，它能指导我们如何设计出松耦合、更优良的程序。**IoC是设计思想，DI是实现方式**。

# origin spring

## beanFactory

通过beanfactory实现对象的创建，当实现类大声变化时，修改配置文件即可。在使用bean的时候从容器中获取

```java
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
```

## use annotation

```java
        //加载IoC容器
        ApplicationContext applicationContext = new AnnotationConfigApplicationContext("com.raptor.originspring.entity");
        String[] beanDefinitionNames = applicationContext.getBeanDefinitionNames();
        System.out.println(applicationContext.getBeanDefinitionCount());
        for (String beanDefinitionName : beanDefinitionNames) {
            System.out.println(beanDefinitionName);
            System.out.println(applicationContext.getBean(beanDefinitionName));
        }
```

# own spring——Annotation

## implementation steps

1. 自定义一个AnnotationConfigApplicationContext，构造器中传入要扫描的包。

2. 获取这个包下的所有类。

3. 遍历这些类，找出添加了 @Component 注解的类，获取它的 Class 和对应的 beanName，封装成一个BeanDefinition，存入集合 Set，这个机会就是 IoC 自动装载的原材料。

4. 遍历 Set 集合，通过反射机制创建对象，同时检测属性有没有添加 @Value 注解，如果有还需要给属性赋值，再将这些动态创建的对象以 k-v 的形式存入缓存区。

5. 提供 getBean 等方法，通过 beanName 取出对应的bean 即可。

## declaration annotation

```java
@interface OwnAutowired
@interface OwnComponent
@interface OwnQualifier
@interface OwnValue
```

## declaration AnnotationConfigApplicationContext

```java
public class OwnAnnotationConfigApplicationContext {

    private Map<String, Object> cache = new HashMap<>();

    public OwnAnnotationConfigApplicationContext(String packageName) {
        //找到对应的包，加载对应的class
        Set<BeanDefinition> beanDefinitions = findBeanDefinition(packageName);

        //根据类名和class   创建bean
        createObject(beanDefinitions);

        //自动装在
        autowireObject(beanDefinitions);
    }
}
```

## scan declaration annotation

包扫描详见此包下的com.raptor.ownspring.utils.MyTools

```java
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
                set.add(beanDefinition);
            }
        }
        return set;
    }
```

## reflex and attribute injection

```java
public void createObject(Set<BeanDefinition> beanDefinitions) {
        Iterator<BeanDefinition> iterator = beanDefinitions.iterator();
        while (iterator.hasNext()) {
            BeanDefinition beanDefinition = iterator.next();
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

                //放入缓存
                cache.put(beanName, object);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
```

## @Autowired


```java
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
                            method.invoke(o1, o);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else {
                        //没写注解  无法注入
                        throw new RuntimeException("can't autowire");
                    }
                }
            }
        }
    }
}
```

## Test Own Spring

```java
public static void main(String[] args) {
    //工具类测试
    //获取一个包下的类
    OwnAnnotationConfigApplicationContext ownAnnotationConfigApplicationContext = new OwnAnnotationConfigApplicationContext("com.raptor.ownspring.entity");
    Object account = ownAnnotationConfigApplicationContext.getBean("account");
    System.out.println(account);

    Object orderTest = ownAnnotationConfigApplicationContext.getBean("orderTest");
    System.out.println(orderTest);
}
```

# Bean Scope

## declaration annotation

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface OwnScope {
    String value();
}
```

## create bean

```java
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
```

## get bean

```java
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
```

# own spring——AppConfig

## declaration annotation

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface OwnComponentScan {
    String value();
}
```

# own spring——simple AOP

## BeanPostProcessor

```java
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
        }

        return bean;
    }
}
```

## create postProcessor

```java
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
```

## when create bean

```java
//bean post processor
for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
    object = beanPostProcessor.postProcessAfterInitialization(object, beanName);
}
```

# reference

[Spring基础 - Spring核心之控制反转(IOC)](https://www.pdai.tech/md/spring/spring-x-framework-ioc.html)

[楠哥教你学Java之3小时搞懂Spring IoC核心源码](https://www.bilibili.com/video/BV1AV411i7VH)