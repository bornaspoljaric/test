package com.bspoljaric.backend.service;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseService {

    void initializeDatabase() throws SQLException;

    Connection getConnection() throws ClassNotFoundException, SQLException;


}
