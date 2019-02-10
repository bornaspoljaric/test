package com.bspoljaric.backend;

import com.bspoljaric.backend.model.Currency;
import com.bspoljaric.backend.service.TransferApi;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.DriverManager;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Application {

    // JDBC driver name and database URL
    public static final String      JDBC_DRIVER     = "org.h2.Driver";
    public static final String      DB_URL          = "jdbc:h2:~/test";

    // Database credentials
    public static final String      USER            = "";
    public static final String      PASS            = "";

    // init Logger
    final static Logger             LOGGER          = Logger.getLogger(Application.class.getName());
    private static final BigDecimal SELL_MULTIPLIER = new BigDecimal(0.95);
    private static final BigDecimal BUY_MULTIPLIER  = new BigDecimal(1.05);
    private static final int        SCALE           = 4;
    private static final double     EUR_TO_USD      = 1.13355;
    private static final double     EUR_TO_RUB      = 74.20949;
    private static final double     EUR_TO_GBP      = 0.87529;
    private static final double     EUR_TO_CHF      = 1.13569;

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
        try {
            // STEP 1: Register JDBC driver
            Class.forName(JDBC_DRIVER);

            LOGGER.info("Initalizing database.");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();

            createTables(stmt);
            insertCurrencyAndExchange(conn);
            insertAccounts(conn);

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
            closeConnection(conn, stmt);
        }
    }

    private static void createTables(Statement stmt) throws SQLException {

        // Clean DB
        stmt.execute("DROP ALL OBJECTS");

        final String currencySql = "CREATE TABLE IF NOT EXISTS CURRENCY (ID BIGINT auto_increment not NULL, CUR_NUM VARCHAR(4) not NULL, CUR_CODE VARCHAR(4) not NULL, CUR_NAME VARCHAR(30) not NULL, PRIMARY KEY ( ID ))";
        final String exchangeRateSQL = "CREATE TABLE IF NOT EXISTS EXCHANGERATE (ID BIGINT auto_increment not NULL, CUR_ID INTEGER not NULL,  BUY_RATE DECIMAL(7,4),  SELL_RATE DECIMAL(7,4), PRIMARY KEY ( ID ), FOREIGN KEY (CUR_ID) references CURRENCY(ID))";
        final String accountSQL = "CREATE TABLE IF NOT EXISTS ACCOUNT(ID BIGINT auto_increment not NULL, IBAN VARCHAR(30) not NULL, CUR_ID INTEGER not NULL, AMOUNT DECIMAL(20,2) NOT NULL, PRIMARY KEY ( ID ), FOREIGN KEY (CUR_ID) references CURRENCY(ID))";
        final String transactionSQL = "CREATE TABLE IF NOT EXISTS TRANSACTION(ID BIGINT auto_increment not NULL, ACC_FROM_ID INTEGER not NULL, ACC_TO_ID INTEGER not NULL, CUR_ID INTEGER not NULL, AMOUNT DECIMAL(20,2) NOT NULL, TRX_STATUS INTEGER not NULL, PRIMARY KEY ( ID ), FOREIGN KEY (ACC_FROM_ID) references ACCOUNT(ID), FOREIGN KEY (ACC_TO_ID) references ACCOUNT(ID), FOREIGN KEY (CUR_ID) references CURRENCY(ID))";
        final String transactionHistorySQL = "CREATE TABLE IF NOT EXISTS TRANSACTION_H(ID BIGINT auto_increment not NULL, TRX_ID INTEGER NOT NULL, TRX_ACTION INTEGER NOT NULL, PRIMARY KEY ( ID ), FOREIGN KEY (TRX_ID) references TRANSACTION(ID))";

        stmt.addBatch(currencySql);
        stmt.addBatch(exchangeRateSQL);
        stmt.addBatch(accountSQL);
        stmt.addBatch(transactionSQL);
        stmt.addBatch(transactionHistorySQL);

        stmt.executeBatch();
    }

    private static void insertCurrencyAndExchange(Connection conn) throws SQLException {
        final PreparedStatement pstmtCurrency;
        final PreparedStatement pstmtExchange;
        final List<Currency> currencyList = new ArrayList<>();
        currencyList.add(new Currency("American Dollar", "USD", 840));
        currencyList.add(new Currency("Euro", "EUR", 978));
        currencyList.add(new Currency("Russian Ruble", "RUB", 643));
        currencyList.add(new Currency("British Pound", "GBP", 826));
        currencyList.add(new Currency("Swiss Franc", "CHF", 756));

        final String currencyInsertSQL = "Insert into CURRENCY (ID, CUR_NUM, CUR_CODE, CUR_NAME) VALUES (?, ?, ?, ?)";
        final String exchangeRateInsertSQL = "Insert into EXCHANGERATE (CUR_ID, BUY_RATE, SELL_RATE) VALUES (?, ?, ?)";

        pstmtCurrency = conn.prepareStatement(currencyInsertSQL);
        pstmtExchange = conn.prepareStatement(exchangeRateInsertSQL);
        conn.setAutoCommit(false);
        for (int i = 0; i < currencyList.size(); i++) {
            final Currency currency = currencyList.get(i);
            pstmtCurrency.setInt(1, i + 1);
            pstmtCurrency.setInt(2, currency.getNumericCode());
            pstmtCurrency.setString(3, currency.getCode());
            pstmtCurrency.setString(4, currency.getName());
            pstmtCurrency.executeUpdate();

            pstmtExchange.setInt(1, i + 1);
            pstmtExchange.setBigDecimal(2, calculateBuy(currency.getCode()));
            pstmtExchange.setBigDecimal(3, calculateSell(currency.getCode()));
            pstmtExchange.executeUpdate();
        }
        conn.commit();
        conn.setAutoCommit(true);
    }

    private static void insertAccounts(Connection conn) throws SQLException {
        final PreparedStatement pstmtAccounts;
        final String accountInsertSQL = "Insert into ACCOUNT (IBAN, CUR_ID, AMOUNT) VALUES (?, ?, ?)";

        pstmtAccounts = conn.prepareStatement(accountInsertSQL);
        conn.setAutoCommit(false);
        final String ibanNum1 = "HR0523600003116069505";
        final String ibanNum2 = "HR0823600003239587990";
        for (int i = 0; i < 2; i++) {
            final String iban;
            for (int j = 0; j < 5; j++) {
                // Create all currencies with first IBAN, create local only for second IBAN
                if (i == 0) {
                    pstmtAccounts.setString(1, ibanNum1);
                    pstmtAccounts.setInt(2, j + 1);
                    pstmtAccounts.setBigDecimal(3, new BigDecimal(5000));
                    pstmtAccounts.executeUpdate();
                } else {
                    pstmtAccounts.setString(1, ibanNum2);
                    pstmtAccounts.setInt(2, 2);
                    pstmtAccounts.setBigDecimal(3, new BigDecimal(5000));
                    pstmtAccounts.executeUpdate();
                    break;
                }
            }
            conn.commit();
        }
        conn.setAutoCommit(true);
    }

    private static BigDecimal calculateSell(String code) {
        return calculateSellOrBuyRate(code, SELL_MULTIPLIER);
    }

    private static BigDecimal calculateBuy(final String code) {
        return calculateSellOrBuyRate(code, BUY_MULTIPLIER);
    }

    private static BigDecimal calculateSellOrBuyRate(String code, BigDecimal multiplier) {
        if ("USD".equals(code)) {
            return new BigDecimal(EUR_TO_USD).multiply(multiplier).setScale(SCALE, RoundingMode.HALF_UP);
        } else if ("RUB".equals(code)) {
            return new BigDecimal(EUR_TO_RUB).multiply(multiplier).setScale(SCALE, RoundingMode.HALF_UP);
        } else if ("GBP".equals(code)) {
            return new BigDecimal(EUR_TO_GBP).multiply(multiplier).setScale(SCALE, RoundingMode.HALF_UP);
        } else if ("CHF".equals(code)) {
            return new BigDecimal(EUR_TO_CHF).multiply(multiplier).setScale(SCALE, RoundingMode.HALF_UP);
        } else {
            return BigDecimal.ONE; // EUR is local Value
        }
    }

    public static void closeConnection(Connection conn, Statement stmt) {
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException se) {
            LOGGER.severe(se.getMessage());
        }
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException se) {
            LOGGER.severe(se.getMessage());
        }
    }
}
