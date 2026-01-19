package com.hokkom.test;

import com.hokkom.dao.DaoFactory;
import com.hokkom.dao.UserDao;
import com.hokkom.domain.User;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.sql.SQLException;

public class UserDaoTest {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        // 방식 1. DaoFactory 직접 생성
        DaoFactory factory = new DaoFactory();
        UserDao dao1 = factory.userDao();
        UserDao dao2 = factory.userDao();

        System.out.println(dao1);
        System.out.println(dao2);

        // 방식 2. 스프링 애플리케이션 컨텍스트 방식 생성
        ApplicationContext context = new AnnotationConfigApplicationContext(DaoFactory.class);
        UserDao dao3 = context.getBean("userDao", UserDao.class);
        UserDao dao = context.getBean("userDao", UserDao.class);

        System.out.println(dao3);
        System.out.println(dao);

        User user = new User();
        user.setId("lsm");
        user.setName("이승민");
        user.setPassword("123456");

        dao.add(user);

        System.out.println(user.getId() + " 등록 성공");

        User user2 = dao.get(user.getId());
        System.out.println(user2.getName());
        System.out.println(user2.getPassword());

        System.out.println(user2.getId() + " 조회 성공");
    }
}
