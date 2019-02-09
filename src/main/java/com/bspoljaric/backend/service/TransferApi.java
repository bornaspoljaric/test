package com.bspoljaric.backend.service;

import com.bspoljaric.backend.Application;
import com.bspoljaric.backend.dto.Transfer;
import com.bspoljaric.backend.model.Account;
import com.bspoljaric.backend.model.Currency;
import com.bspoljaric.backend.model.Transaction;
import com.bspoljaric.backend.util.ApiError;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.logging.Logger;

import static com.bspoljaric.backend.Application.*;

@Path("/transfer")
public class TransferApi {
    final static Logger LOGGER = Logger.getLogger(TransferApi.class.getName());

    @POST
    @Path("/create")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response transfer(final Transfer transfer) {

        // Define params to eliminate multiple calls
        final String accountToDto = transfer.getAccTo();
        final String accountFromDto = transfer.getAccFrom();
        final BigDecimal amount = transfer.getAmount();

        Account accountFrom;
        Account accountTo;
        final Transaction transaction = null;

        // Do null validations
        if (accountToDto == null || accountToDto.trim().length() == 0) {
            return Response.serverError().entity(new ApiError(Response.Status.BAD_REQUEST.getStatusCode(), "Recipient account to cannot be null")).build();
        }

        if (accountFromDto == null || accountFromDto.trim().length() == 0) {
            return Response.serverError().entity(new ApiError(Response.Status.BAD_REQUEST.getStatusCode(), "Sending account from cannot be null")).build();
        }

        if (amount == null) {
            return Response.serverError().entity(new ApiError(Response.Status.BAD_REQUEST.getStatusCode(), "Amount cannot be null")).build();
        }

        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName(JDBC_DRIVER);

            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();

            final String selectAccount = "SELECT IBAN, CUR_ID, AMOUNT FROM ACCOUNT WHERE IBAN = ? AND CUR_ID = ?";
            final String selectLocalAccount = "SELECT IBAN, CUR_ID, AMOUNT FROM ACCOUNT WHERE IBAN = ? AND CUR_ID = 2";
            final String selectCountAccount = "SELECT COUNT(*) IBAN, CUR_ID, AMOUNT FROM ACCOUNT WHERE IBAN = ? as COUNT";
            final String selectCurrency = "SELECT ID, CUR_NUM, CUR_CODE, CUR_NAME FROM CURRENCY WHERE CUR_ID = ?";
            final String selectExchangeRate = "SELECT SELL_RATE FROM EXCHANGERATE WHERE CUR_ID = ?";

            final PreparedStatement pstmtAcc = conn.prepareStatement(selectAccount);
            pstmtAcc.setString(1, transfer.getAccTo());
            pstmtAcc.setInt(2, transfer.getCurrencyId());

            final PreparedStatement pstmtAccToLocal = conn.prepareStatement(selectLocalAccount);
            pstmtAccToLocal.setString(1, transfer.getAccTo());

            final PreparedStatement pstmtCount = conn.prepareStatement(selectCountAccount);
            pstmtCount.setString(1, transfer.getAccTo());

            final ResultSet rsCount = pstmtCount.executeQuery();
            while (rsCount.next()) {
                int count = rsCount.getInt("total");
                // Account does not exist, return error
                if (count == 0) {
                    return Response.serverError().entity(new ApiError(Response.Status.NOT_FOUND.getStatusCode(), "Recipient account cannot be found")).build();
                } // Check if account has the same currency, if not, use local currency
                else {
                    final ResultSet rsAccTo = pstmtAcc.executeQuery();
                    // Currency account does not, use local currency ( EUR )
                    if (!rsAccTo.next()) {
                        ResultSet rsAccToLocal = pstmtAccToLocal.executeQuery();
                        Currency trxCurrency = getCurrency(conn, selectCurrency, 2);
                        accountTo = calculateAccount(accountTo, trxCurrency, rsAccToLocal);
                    } else {
                        Currency trxCurrency = getCurrency(conn, selectCurrency, transfer.getCurrencyId());
                        accountTo = calculateAccount(accountTo, trxCurrency, rsAccTo);
                    }
                }
            }

            final PreparedStatement pstmtAccFrom = conn.prepareStatement(selectAccount);
            pstmtAccFrom.setString(1, transfer.getAccFrom());
            pstmtAccFrom.setInt(2, transfer.getCurrencyId());

            final ResultSet rsAccTo = pstmtCount.executeQuery();
            // Check if acc exists
            if (!rsAccTo.next()) {
                return Response.serverError().entity(new ApiError(Response.Status.NOT_FOUND.getStatusCode(), "Recipient account cannot be found")).build();
            } else {
                // If it exists, map to Java object
                while (rsAccTo.next()) {
                    final ResultSet rsAccFrom = pstmtAcc.executeQuery();
                    final Currency trxCurrency = getCurrency(conn, selectCurrency, transfer.getCurrencyId());
                    accountFrom = calculateAccount(accountFrom, trxCurrency, rsAccFrom);
                }
            }

            // IF currencys are not equal, use exchange rate
            if (!accountFrom.getCurrency().equals(accountTo.getCurrency())) {
                final PreparedStatement pstmtExchange = conn.prepareStatement(selectExchangeRate);
                pstmtAcc.setInt(1, transfer.getCurrencyId());
                ResultSet rsExchange = pstmtExchange.executeQuery();
                // check if rate exists
                if (!rsExchange.next()) {
                    return Response.serverError().entity(new ApiError(Response.Status.NOT_FOUND.getStatusCode(), "Exchange Rate cannot be found")).build();
                } else {
                    while (rsExchange.next()) {
                        BigDecimal rate = rsExchange.getBigDecimal("SELL_RATE");
                        amount = amount.divide(rate).setScale(2, RoundingMode.HALF_UP);
                    }
                }

            }
            transaction.setAccountFrom(accountFrom);
            transaction.setAccountTo(accountTo);
            transaction.setCurrency(accountFrom.getCurrency());
            transaction.setAmount(amount);

            // Execute transaction
            // Add to account of recipient
            // Deduct from account of payer
            
            stmt.close();
            conn.close();
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
            closeConnection(conn, stmt);
        }
        return Response.ok("Transaction processed!", MediaType.APPLICATION_JSON).build();
    }

    private Currency getCurrency(Connection conn, String selectCurrency, int currencyId) throws SQLException {
        final PreparedStatement pstmtCurrency = conn.prepareStatement(selectCurrency);
        pstmtCurrency.setInt(1, currencyId);
        ResultSet rsCurrency = pstmtCurrency.executeQuery();
        Currency trxCurrency = new Currency();
        while (rsCurrency.next()) {
            trxCurrency.setId(rsCurrency.getInt("ID"));
            trxCurrency.setCode(rsCurrency.getString("CUR_CODE"));
            trxCurrency.setNumericCode(rsCurrency.getInt("NUM_CODE"));
            trxCurrency.setName(rsCurrency.getString("CUR_NAME"));
        }
        return trxCurrency;
    }

    private Account calculateAccount(Account account, Currency trxCurrency, ResultSet rsAcc) throws SQLException {
        while (rsAcc.next()) {
            account.setId(rsAcc.getInt("ID"));
            account.setIban(rsAcc.getString("IBAN"));
            account.setAmount(rsAcc.getBigDecimal("AMOUNT"));
            account.setCurrency(trxCurrency);
        }
        return account;
    }

}
