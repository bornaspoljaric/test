package com.bspoljaric.backend.dto;

import java.math.BigDecimal;

public class Transfer {

    private String     accFrom;

    private String     accTo;

    private int        currencyId;

    private BigDecimal amount;

    public Transfer(String accFrom, String accTo, int currencyId, BigDecimal amount) {
        this.accFrom = accFrom;
        this.accTo = accTo;
        this.currencyId = currencyId;
        this.amount = amount;
    }

    public Transfer() {
    }

    public String getAccFrom() {
        return accFrom;
    }

    public void setAccFrom(String accFrom) {
        this.accFrom = accFrom;
    }

    public String getAccTo() {
        return accTo;
    }

    public void setAccTo(String accTo) {
        this.accTo = accTo;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public int getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(int currencyId) {
        this.currencyId = currencyId;
    }
}
