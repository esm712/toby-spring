package com.hokkom.dao;

import domain.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {

    private ConnectionMaker connectionMaker;

    public UserDao(ConnectionMaker connectionMaker) {
        this.connectionMaker = connectionMaker;
    }

    public void add(User user) throws ClassNotFoundException, SQLException {
        // 1. DB 연결을 위한 Connection을 가져온다.
        Connection c = connectionMaker.makeConnection();

        // 2. SQL을 담을 Statement를 만든다.
        PreparedStatement ps = c.prepareStatement("insert into users(id, name, password) values(?,?,?)");
        ps.setString(1, user.getId());
        ps.setString(2, user.getName());
        ps.setString(3, user.getPassword());

        // 3. Statement를 실행한다.
        ps.executeUpdate();

        // 4. 조회의 경우 쿼리의 실행결과를 ResultSet로 받아서 저장할 오브젝트에 옯겨준다.

        // 5. 작업 중 생성된 Connection, Statement, ResultSet 같은 리소스는 작업을 마친 후 반드시 닫아준다.
        ps.close();
        c.close();
    }

    public User get(String id) throws ClassNotFoundException, SQLException {
        // 1. DB 연결을 위한 Connection을 가져온다.
        Connection c = connectionMaker.makeConnection();

        // 2. SQL을 담을 Statement를 만든다.
        PreparedStatement ps = c.prepareStatement("select * from users where id = ?");
        ps.setString(1, id);

        // 3. Statement를 실행한다.
        ResultSet rs = ps.executeQuery();

        // 4. 조회의 경우 쿼리의 실행결과를 ResultSet로 받아서 저장할 오브젝트에 옯겨준다.
        rs.next();
        User user = new User();
        user.setId(rs.getString("id"));
        user.setName(rs.getString("name"));
        user.setPassword(rs.getString("password"));

        // 5. 작업 중 생성된 Connection, Statement, ResultSet 같은 리소스는 작업을 마친 후 반드시 닫아준다.
        rs.close();
        ps.close();
        c.close();
        return user;
    }

}
