package com.plarpc.test;

public class TestImpl implements Test{
    public Integer hello() {
        System.out.println("hello");
        return 0;
    }

    @Override
    public Integer helloString(String name) {
        System.out.println("hello " + name);
        return 0;
    }

    @Override
    public Integer square(Integer a) {
        return a * a;
    }

    @Override
    public Integer sum(Integer a, Integer b) {
        return a + b;
    }
}