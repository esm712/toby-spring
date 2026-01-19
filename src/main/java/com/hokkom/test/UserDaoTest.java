package com.hokkom.test;

import com.hokkom.dao.ConnectionMaker;
import com.hokkom.dao.DConnectionMaker;
import com.hokkom.dao.DaoFactory;
import com.hokkom.dao.UserDao;
import com.hokkom.domain.User;

import java.sql.SQLException;

public class UserDaoTest {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        UserDao dao = new DaoFactory().userDao();

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
