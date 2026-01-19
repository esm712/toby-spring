package com.hokkom.dao;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ConnectionBuilder;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class CountingDataSource implements DataSource {
    private int counter = 0;
    private DataSource realDataSource;

    public CountingDataSource(DataSource realDataSource) {
        this.realDataSource = realDataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        this.counter++; // 연결 요청 시 카운트 증가
        return realDataSource.getConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        this.counter++;
        return realDataSource.getConnection(username, password);
    }

    public int getCounter() {
        return counter;
    }

    // --- 나머지 메서드들은 realDataSource에 위임 ---
    @Override public <T> T unwrap(Class<T> iface) throws SQLException { return realDataSource.unwrap(iface); }
    @Override public boolean isWrapperFor(Class<?> iface) throws SQLException { return realDataSource.isWrapperFor(iface); }
    @Override public PrintWriter getLogWriter() throws SQLException { return realDataSource.getLogWriter(); }
    @Override public void setLogWriter(PrintWriter out) throws SQLException { realDataSource.setLogWriter(out); }
    @Override public void setLoginTimeout(int seconds) throws SQLException { realDataSource.setLoginTimeout(seconds); }
    @Override public int getLoginTimeout() throws SQLException { return realDataSource.getLoginTimeout(); }
    @Override public ConnectionBuilder createConnectionBuilder() throws SQLException {return realDataSource.createConnectionBuilder(); }
    @Override public Logger getParentLogger() throws SQLFeatureNotSupportedException { return realDataSource.getParentLogger(); }
}
