package com.bspoljaric.backend.service;

import com.bspoljaric.backend.model.Transaction;

import java.sql.SQLException;
import java.util.List;

public interface TransactionService {

    List<Transaction> findAll() throws SQLException;
}
