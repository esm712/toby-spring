package com.hokkom.test;

import com.hokkom.dao.CountingDaoFactory;
import com.hokkom.dao.CountingDataSource;
import com.hokkom.dao.UserDao;
import com.hokkom.domain.User;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.sql.SQLException;

public class UserDaoConnectionCountingTest {

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(CountingDaoFactory.class);
        UserDao userDao = context.getBean("userDao",UserDao.class);

        //
        // DAO 사용 코드
        //
        User user1 = userDao.get("lsm");
        User user2 = userDao.get("lsm");
        User user3 = userDao.get("lsm");

        CountingDataSource cds = context.getBean("countingDataSource", CountingDataSource.class);

        System.out.println("Connection counter : " + cds.getCounter());
    }
}
