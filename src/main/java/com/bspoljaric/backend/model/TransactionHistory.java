package com.bspoljaric.backend.model;

import java.io.Serializable;

public class TransactionHistory implements Serializable {

    private Serializable      id;
    private Transaction       transaction;
    private TransactionAction transactionAction;

    public TransactionHistory(Transaction transaction, TransactionAction transactionAction) {
        this.transaction = transaction;
        this.transactionAction = transactionAction;
    }

    public TransactionHistory() {

    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public TransactionAction getTransactionAction() {
        return transactionAction;
    }

    public void setTransactionAction(TransactionAction transactionAction) {
        this.transactionAction = transactionAction;
    }
}
