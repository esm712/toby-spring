package com.hokkom.test;

import com.hokkom.dao.DaoFactory;
import com.hokkom.dao.UserDao;
import com.hokkom.domain.User;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

import java.sql.SQLException;

public class UserDaoTest {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        // ApplicationContext context = new AnnotationConfigApplicationContext(DaoFactory.class);
        // XML 방식
        ApplicationContext context = new GenericXmlApplicationContext("applicationContext.xml");

        UserDao dao = context.getBean("userDao", UserDao.class);

        User user = new User();
        user.setId("lsm");
        user.setName("이승민");
        user.setPassword("123456");

        dao.add(user);

        System.out.println(user.getId() + " 등록 성공");

        User user2 = dao.get(user.getId());

        if(!user.getName().equals(user2.getName())){
            System.out.println("테스트 실패 (name)");
        } else if (!user.getPassword().equals(user2.getPassword())){
            System.out.println("테스트 실패 (password)");
        } else {
            System.out.println("조회 테스트 성공");
        }
    }
}
