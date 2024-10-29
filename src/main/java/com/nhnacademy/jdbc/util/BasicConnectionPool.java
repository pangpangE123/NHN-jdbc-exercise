package com.nhnacademy.jdbc.util;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;

public class BasicConnectionPool {

    private final String driverClassName;
    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final int maximumPoolSize;
    private final Queue<Connection> connections;

    public BasicConnectionPool(String driverClassName, String jdbcUrl, String username, String password, int maximumPoolSize) {

        this.driverClassName = driverClassName;
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.maximumPoolSize = maximumPoolSize;
        connections = new LinkedList<>();

        checkDriver();
        initialize();
    }

    private void checkDriver() {
        //todo#1 driverClassName에 해당하는 class가 존재하는지 check합니다.
        //존재하지 않는다면 RuntimeException 예외처리.
        Class clazz = null;
        try {
            clazz = Class.forName(driverClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver Not Found");
        }
    }

    private void initialize() {
        //todo#2 maximumPoolSize만큼 Connection 객체를 생성해서 Connection Pool에 등록합니다.
        for (int i = 0; i < maximumPoolSize; i++) {

            try {
                // 드라이버 매니저에서 연결을 가지고오기
                Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
                // offer : add와 다르게 용량이 꽉찼을때 execption을 던지지 않고 special value를 반환함
                connections.offer(connection);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public Connection getConnection() throws InterruptedException {
        //todo#3 Connection Pool에 connection이 존재하면 반환하고 비어있다면 Connection Pool에 Connection이 존재할 때 까지 대기합니다.
        synchronized (this) {
            while (connections.isEmpty()) {
                wait();
            }
        }

        return connections.poll();
    }

    public void releaseConnection(Connection connection) {
        //todo#4 작업을 완료한 Connection 객체를 Connection Pool에 반납 합니다.
        synchronized (this) {
            connections.offer(connection);
            notifyAll();
        }
    }

    public int getUsedConnectionSize() {
        //todo#5 현재 사용중인 Connection 객체 수를 반환합니다.
        return maximumPoolSize - connections.size();
    }

    public void distory() throws SQLException {
        //todo#6 Connection Pool에 등록된 Connection을 close 합니다.
        for (Connection connection : connections) {
            if (!connection.isClosed()) {
                connection.close();
            }
        }
    }
}
