package com.learningtest;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class HelloUppercaseTest {
    @Test
    public void simpleProxy() {
        // 다이나믹 프록시 사용 전
        // Hello proxiedHello = new HelloUppercase(new HelloTarget());
        // 다이나믹 프록시 사용 후
        Hello proxiedHello = (Hello) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[] { Hello.class },
                new UppercaseHandler(new HelloTarget())
                );
        assertThat(proxiedHello.sayHello("Toby")).isEqualTo("HELLO TOBY");
        assertThat(proxiedHello.sayHi("Toby")).isEqualTo("HI TOBY");
        assertThat(proxiedHello.sayThankYou("Toby")).isEqualTo("THANK YOU TOBY");
    }
}