package com.raptor.ownspring.definition;

public class BeanDefinition {
    private String beanName;
    private Class beanClass;
    private String scope;

    public BeanDefinition() {
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public BeanDefinition(String beanName, Class beanClass) {
        this.beanName = beanName;
        this.beanClass = beanClass;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public Class getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(Class beanClass) {
        this.beanClass = beanClass;
    }

    @Override
    public String toString() {
        return "BeanDefinition{" +
                "beanName='" + beanName + '\'' +
                ", beanClass=" + beanClass +
                '}';
    }
}
