package ru.ntzw.com.dt.server.model.sql;

import ru.ntzw.com.dt.server.Main;
import ru.ntzw.com.dt.server.model.Disposable;
import ru.ntzw.com.dt.server.model.Initializable;

import java.sql.*;

public class SimpleSqlService implements SqlService, Initializable, Disposable {

    private final String url, username, password;
    private Connection connection;

    public SimpleSqlService(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public void init() throws Exception {
        this.connection = DriverManager.getConnection(url, username, password);
    }

    @Override
    public void insert(String table, String... values) throws SQLException {
        StringBuilder val = new StringBuilder();
        for(String s : values) {
            val.append("\"").append(s).append("\",");
        }
        String update = String.format("INSERT INTO %s VALUES (%s);", table, val.substring(0, val.length() - 1));
        Main.log.debug("SQL update: " + update);
        Statement statement = connection.createStatement();
        statement.executeUpdate(update);
    }

    @Override
    public void dispose() throws Exception {
        if(connection != null) {
            connection.close();
        }
    }
}
