package com.bspoljaric.backend.model;

import java.io.Serializable;

public class Account implements Serializable {

    private Serializable id;

    private String       iban;

    private Currency     currency;

    public Account() {
    }

    public Serializable getId() {
        return id;
    }

    public void setId(Serializable id) {
        this.id = id;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
}
