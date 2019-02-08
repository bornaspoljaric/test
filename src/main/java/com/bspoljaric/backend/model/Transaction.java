package com.bspoljaric.backend.model;

import java.io.Serializable;

public class Transaction implements Serializable {

    private Serializable id;

    private String       account;

    public Transaction() {
    }

    public Transaction(Serializable id, String account) {
        this.id = id;
        this.account = account;
    }

    public Serializable getId() {
        return id;
    }

    public void setId(Serializable id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }
}
