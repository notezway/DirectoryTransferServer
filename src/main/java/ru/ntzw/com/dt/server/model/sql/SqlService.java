package ru.ntzw.com.dt.server.model.sql;

import java.sql.SQLException;

public interface SqlService {

    void insert(String table, String... values) throws SQLException;
}
