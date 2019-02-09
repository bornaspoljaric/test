package com.bspoljaric.backend;

import com.bspoljaric.backend.model.Currency;
import com.bspoljaric.backend.model.ExchangeRate;
import com.bspoljaric.backend.service.TransferApi;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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

        final Currency usd = new Currency("American Dollar", "USD", 840);
        final Currency eur = new Currency("Euro", "EUR", 978);
        final Currency rub = new Currency("Russian Ruble", "RUB", 643);
        final Currency gbp = new Currency("British Pound", "GBP", );
        final Currency chf = new Currency("Swiss Franc", "CHF", );

        // Server
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        Server jettyServer = new Server(8080);
        jettyServer.setHandler(context);

        ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
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

    private static void initializeDatabase() {
        Connection conn = null;
        Statement stmt = null;
        try {
            // STEP 1: Register JDBC driver
            Class.forName(JDBC_DRIVER);

            LOGGER.info("Initalizing database.");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            stmt = conn.createStatement();

            final String currencySql = "CREATE TABLE IF NOT EXISTS CURRENCY (ID INTEGER not NULL, NUM_CODE VARCHAR(4) not NULL, CODE VARCHAR(4) not NULL, PRIMARY KEY ( ID ))";
            final String exchangeRateSQL = "CREATE TABLE IF NOT EXISTS EXCHANGERATE (ID INTEGER not NULL, CUR_ID INTEGER not NULL,  BUY_RATE DECIMAL(7,4),  SELL_RATE DECIMAL(7,4), PRIMARY KEY ( ID ), FOREIGN KEY (CUR_ID) references CURRENCY(ID))";
            final String accountSQL = "CREATE TABLE IF NOT EXISTS ACCOUNT(ID INTEGER not NULL, IBAN VARCHAR(30) not NULL, CUR_ID INTEGER not NULL, AMOUNT DECIMAL(20,2) NOT NULL, PRIMARY KEY ( ID ), FOREIGN KEY (CUR_ID) references CURRENCY(ID))";
            final String transactionSQL = "CREATE TABLE IF NOT EXISTS TRANSACTION(ID INTEGER not NULL, ACC_FROM_ID INTEGER not NULL, ACC_TO_ID INTEGER not NULL, CUR_ID INTEGER not NULL, AMOUNT DECIMAL(20,2) NOT NULL, TRX_STATUS INTEGER not NULL, PRIMARY KEY ( ID ), FOREIGN KEY (ACC_FROM_ID) references ACCOUNT(ID), FOREIGN KEY (ACC_TO_ID) references ACCOUNT(ID))";
            final String transactionHistorySQL = "CREATE TABLE IF NOT EXISTS TRANSACTION_H(ID INTEGER not NULL, TRX_ID INTEGER NOT NULL, TRX_ACTION INTEGER NOT NULL, PRIMARY KEY ( ID ), FOREIGN KEY (TRX_ID) references TRANSACTION(ID))";

            stmt.addBatch(currencySql);
            stmt.addBatch(exchangeRateSQL);
            stmt.addBatch(accountSQL);
            stmt.addBatch(transactionSQL);
            stmt.addBatch(transactionHistorySQL);

            stmt.executeBatch();

            LOGGER.info("Database initalized.");
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            LOGGER.severe(se.getMessage());
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
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
