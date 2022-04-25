package com.raptor.loader;

import java.io.File;
import java.net.URL;

public class ClassLoaderTest {
    public static void main(String[] args) {
        //类加载器
        ClassLoader classLoader = ClassLoaderTest.class.getClassLoader();
        URL resource = classLoader.getResource("com/raptor/service/impl");
        File file = new File(resource.getFile());
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
//                获取到了class文件的文件地址
//                System.out.println(f);
                String fName = f.getAbsolutePath();
                if (fName.endsWith(".class")) {
                    String className = fName.substring(fName.indexOf("com"), fName.indexOf(".class"));
                    className = className.replace("\\", ".");
                    try {
                        Class<?> aClass = classLoader.loadClass(className);
                        System.out.println(aClass);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }
}
