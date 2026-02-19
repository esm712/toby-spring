package com.learningtest;


import com.hokkom.dao.TestDaoFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestDaoFactory.class})
class FactoryBeanTest {

    @Autowired
    ApplicationContext context;

    @Test
    public void getMessageFactoryBean() {
        Object message = context.getBean("message");
        assertThat(message).isInstanceOf(Message.class);
        assertThat(((Message)message).getText()).isEqualTo("Factory Bean");
    }

    @Test
    public void getFactoryBean() {
        Object factory = context.getBean("&message");
        assertThat(factory).isInstanceOf(MessageFactoryBean.class);
    }
}