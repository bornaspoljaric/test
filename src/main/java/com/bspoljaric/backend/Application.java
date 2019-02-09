package com.bspoljaric.backend;

import com.bspoljaric.backend.model.Currency;
import com.bspoljaric.backend.model.ExchangeRate;
import com.bspoljaric.backend.service.TransferApi;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.sql.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Application {

    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "org.h2.Driver";
    static final String DB_URL      = "jdbc:h2:~/test";

    // Database credentials
    static final String USER        = "";
    static final String PASS        = "";

    // init Logger
    final static Logger LOGGER = Logger.getLogger(Application.class.getName());

    public static void main(String[] args) throws Exception {
        initializeDatabase();




        // Server
        final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        final Server jettyServer = new Server(8080);
        jettyServer.setHandler(context);

        final ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);

        // Tells the Jersey Servlet which REST service/class to load.
        jerseyServlet.setInitParameter("jersey.config.server.provider.classnames", TransferApi.class.getCanonicalName());

        try {
            jettyServer.start();
            jettyServer.join();
        } finally {
            jettyServer.destroy();
        }
    }

    private static void initializeDatabase() throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        PreparedStatement pstmt = null;
        try {
            // STEP 1: Register JDBC driver
            Class.forName(JDBC_DRIVER);

            LOGGER.info("Initalizing database.");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            stmt = conn.createStatement();

            conn.setAutoCommit(false);
            final String currencySql = "CREATE TABLE IF NOT EXISTS CURRENCY (ID BIGINT auto_increment not NULL, CUR_NUM VARCHAR(4) not NULL, CUR_CODE VARCHAR(4) not NULL, CUR_NAME VARCHAR(30) not NULL, PRIMARY KEY ( ID ))";
            final String exchangeRateSQL = "CREATE TABLE IF NOT EXISTS EXCHANGERATE (ID BIGINT auto_increment not NULL, CUR_ID INTEGER not NULL,  BUY_RATE DECIMAL(7,4),  SELL_RATE DECIMAL(7,4), PRIMARY KEY ( ID ), FOREIGN KEY (CUR_ID) references CURRENCY(ID))";
            final String accountSQL = "CREATE TABLE IF NOT EXISTS ACCOUNT(ID BIGINT auto_increment not NULL, IBAN VARCHAR(30) not NULL, CUR_ID INTEGER not NULL, AMOUNT DECIMAL(20,2) NOT NULL, PRIMARY KEY ( ID ), FOREIGN KEY (CUR_ID) references CURRENCY(ID))";
            final String transactionSQL = "CREATE TABLE IF NOT EXISTS TRANSACTION(ID BIGINT auto_increment not NULL, ACC_FROM_ID INTEGER not NULL, ACC_TO_ID INTEGER not NULL, CUR_ID INTEGER not NULL, AMOUNT DECIMAL(20,2) NOT NULL, TRX_STATUS INTEGER not NULL, PRIMARY KEY ( ID ), FOREIGN KEY (ACC_FROM_ID) references ACCOUNT(ID), FOREIGN KEY (ACC_TO_ID) references ACCOUNT(ID))";
            final String transactionHistorySQL = "CREATE TABLE IF NOT EXISTS TRANSACTION_H(ID BIGINT auto_increment not NULL, TRX_ID INTEGER NOT NULL, TRX_ACTION INTEGER NOT NULL, PRIMARY KEY ( ID ), FOREIGN KEY (TRX_ID) references TRANSACTION(ID))";

            stmt.addBatch(currencySql);
            stmt.addBatch(exchangeRateSQL);
            stmt.addBatch(accountSQL);
            stmt.addBatch(transactionSQL);
            stmt.addBatch(transactionHistorySQL);

            stmt.executeBatch();

            final List<Currency> currencyList = new ArrayList<>();
            currencyList.add(new Currency("American Dollar", "USD", 840));
            currencyList.add(new Currency("Euro", "EUR", 978));
            currencyList.add(new Currency("Russian Ruble", "RUB", 643));
            currencyList.add(new Currency("British Pound", "GBP", 826));
            currencyList.add(new Currency("Swiss Franc", "CHF", 756));

            final String currencyInsertSQL = "Insert into Currency (CUR_NUM, CUR_CODE, CUR_NAME) VALUES (?, ?, ?)";
            for(final Currency currency : currencyList){
                pstmt = conn.prepareStatement(currencyInsertSQL);
                pstmt.setInt(1, currency.getNumericCode());
                pstmt.setString(2, currency.getCode());
                pstmt.setString(3, currency.getName());
                pstmt.executeUpdate();
            }
            conn.commit();
            conn.setAutoCommit(true);

            LOGGER.info("Database initalized.");

            stmt.close();
            conn.close();
        } catch (SQLException se) {
            LOGGER.severe(se.getMessage());
            conn.rollback();
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
            conn.rollback();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException se2) { }
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException se) {
                LOGGER.severe(se.getMessage());
            }
        }
    }
}
