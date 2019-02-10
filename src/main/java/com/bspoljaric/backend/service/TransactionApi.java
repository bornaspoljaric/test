package com.bspoljaric.backend.service;

import com.bspoljaric.backend.model.Account;
import com.bspoljaric.backend.model.Currency;
import com.bspoljaric.backend.model.Transaction;
import com.bspoljaric.backend.util.ApiError;
import com.google.gson.Gson;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.bspoljaric.backend.Application.DB_URL;
import static com.bspoljaric.backend.Application.JDBC_DRIVER;
import static com.bspoljaric.backend.Application.PASS;
import static com.bspoljaric.backend.Application.USER;

@Path("/transactions")
public class TransactionApi {

    final static Logger LOGGER = Logger.getLogger(TransactionApi.class.getName());

    @GET
    public Response transactions() throws SQLException {

        final List<Transaction> transactionList = new ArrayList<>();
        final String selectTransactions = "SELECT TRX.AMOUNT, A.IBAN, B.IBAN, C.CUR_CODE FROM TRANSACTION TRX " + "INNER JOIN ACCOUNT as A on TRX.ACC_FROM_ID = A.ID "
                + "INNER JOIN ACCOUNT as B on TRX.ACC_TO_ID = B.ID " + "INNER JOIN CURRENCY as C on TRX.CUR_ID = C.ID";
        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName(JDBC_DRIVER);

            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            stmt = conn.createStatement();
            ResultSet rsTrx = stmt.executeQuery(selectTransactions);
            while (rsTrx.next()) {
                final Transaction transaction = new Transaction();
                transaction.setAmount(rsTrx.getBigDecimal(1));
                transaction.setAccountFrom(new Account(rsTrx.getString(2)));
                transaction.setAccountTo(new Account(rsTrx.getString(3)));
                transaction.setCurrency(new Currency(rsTrx.getString(4)));
                transactionList.add(transaction);
            }

        } catch (SQLException se) {
            LOGGER.severe(se.getMessage());
            conn.rollback();
            return Response.serverError().entity(new ApiError(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Server has internal problems, please try again later"))
                    .build();
        } catch (Exception e) {
            LOGGER.severe(e.getMessage());
            conn.rollback();
            return Response.serverError().entity(new ApiError(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Server has internal problems, please try again later"))
                    .build();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return Response.ok().type(MediaType.APPLICATION_JSON).entity(new Gson().toJson(transactionList)).build();
    }
}
