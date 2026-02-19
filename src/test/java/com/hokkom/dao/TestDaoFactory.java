package com.hokkom.dao;

import com.hokkom.service.*;
import com.learningtest.MessageFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.mail.MailSender;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class TestDaoFactory {

    @Bean
    public UserDao userDao() {
        return new UserDaoJdbc(dataSource());
    }

    @Bean
    public UserServiceImpl userServiceImpl() {
        return new UserServiceImpl(userDao(), mailSender());
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    public MailSender mailSender() {
        return new DummyMailSender();
    }

    @Bean
    public DataSource dataSource() {
        // Use H2 in-memory database for testing
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1"); // DB_CLOSE_DELAY=-1 keeps the database open until the JVM exits
        dataSource.setUsername("sa");
        dataSource.setPassword("");

        // Initialize the database schema
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("create-users.sql")); // Assuming create-users.sql is in src/test/resources or src/main/resources
        DatabasePopulatorUtils.execute(populator, dataSource);

        return dataSource;
    }

    @Bean
    public MessageFactoryBean message() {
        MessageFactoryBean factoryBean = new MessageFactoryBean();
        factoryBean.setText("Factory Bean");

        return factoryBean;
    }

    @Bean
    public TxProxyFactoryBean userService() {
        TxProxyFactoryBean factoryBean = new TxProxyFactoryBean();
        factoryBean.setTarget(userServiceImpl());
        factoryBean.setTransactionManager(transactionManager());
        factoryBean.setPattern("upgradeLevels");
        factoryBean.setServiceInterface(UserService.class);
        return factoryBean;
    }
}
