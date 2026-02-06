package com.hokkom.dao;

import com.hokkom.domain.User;
import org.springframework.dao.EmptyResultDataAccessException;

import javax.sql.DataSource;
import java.sql.*;

public class UserDao {

    private JdbcContext jdbcContext;
    private DataSource dataSource;

    public UserDao() {}

    public UserDao(DataSource dataSource) {
        this.jdbcContext = new JdbcContext();
        this.jdbcContext.setDataSource(dataSource);
        this.dataSource = dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.jdbcContext = new JdbcContext();
        this.jdbcContext.setDataSource(dataSource);
        this.dataSource = dataSource;
    }

    public void add(final User user) throws SQLException {
        this.jdbcContext.workWithStatementStrategy(
                c -> {
                    PreparedStatement ps = c.prepareStatement("insert into users(id, name, password) values (?, ?, ?)");
                    ps.setString(1, user.getId());
                    ps.setString(2, user.getName());
                    ps.setString(3, user.getPassword());
                    return ps;
                }
        );
    }

    public User get(String id) throws SQLException {
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        User user = null;
        try {
            c = dataSource.getConnection();

            ps = c.prepareStatement("select * from users where id = ?");
            ps.setString(1, id);

            rs = ps.executeQuery();

            user = null;
            if (rs.next()){
                user = new User();
                user.setId(rs.getString("id"));
                user.setName(rs.getString("name"));
                user.setPassword(rs.getString("password"));
            }

            if(user == null) throw new EmptyResultDataAccessException(1);

            return user;
        } catch (SQLException e) {
            throw e;
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {}
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {}
            }
            if (c != null) {
                try {
                    c.close();
                } catch (SQLException e) {}
            }
        }
    }

    public void deleteAll() throws SQLException {
        this.jdbcContext.workWithStatementStrategy(
                c -> {
                    PreparedStatement ps = c.prepareStatement("delete from users");
                    return ps;
                }
        );
    }

    public int getCount() throws SQLException {
        Connection c = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            c = dataSource.getConnection();

            ps = c.prepareStatement("select count(*) from users");

            rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw e;
        } finally {
            if (rs != null) {
                try{
                    rs.close();
                } catch (SQLException e){}
            }
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e){}
            }
            if (c != null) {
                try {
                    c.close();
                } catch (SQLException e){}
            }
        }
    }


    public void jdbcContextWithStatement(StatementStrategy stmt) throws SQLException {
        Connection c = null;
        PreparedStatement ps = null;

        try {
            c = dataSource.getConnection();
            ps = stmt.makePreparedStatement(c);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw e;
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {}
            }
            if (c != null) {
                try {
                    c.close();
                } catch (SQLException e) {}
            }
        }
    }
}
