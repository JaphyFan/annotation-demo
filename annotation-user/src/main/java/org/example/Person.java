package org.example;

import org.example.DemoBuilder;

/**
 * @author Japhy
 * @since 2021/1/17 01:53
 */
public class Person {

    private String name;

    private int age;

    public String getName() {
        return name;
    }

    @DemoBuilder
    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    @DemoBuilder
    public void setAge(int age) {
        this.age = age;
    }
}
