package com.learningtest.jdk;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ReflectionTest {
    @Test
    public void invokeMehtod() throws Exception {
        String name = "Spring";

        // length()
        assertThat(name.length()).isEqualTo(6);

        Method lengthMethod = String.class.getMethod("length");
        assertThat((Integer) lengthMethod.invoke(name)).isEqualTo(6);

        // charAt()
        assertThat(name.charAt(0)).isEqualTo('S');

        Method charAtMethod = String.class.getMethod("charAt", int.class);
        assertThat((Character) charAtMethod.invoke(name,0)).isEqualTo('S');
    }
}
