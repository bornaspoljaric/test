package com.bspoljaric.backend.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class ExchangeRate implements Serializable {

    private Serializable id;

    private Currency     currency;

    private BigDecimal   buyRate;

    private BigDecimal   sellRate;

    public ExchangeRate(Currency currency, BigDecimal buyRate, BigDecimal sellRate) {
        this.currency = currency;
        this.buyRate = buyRate;
        this.sellRate = sellRate;
    }

    public Serializable getId() {
        return id;
    }

    public void setId(Serializable id) {
        this.id = id;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public BigDecimal getBuyRate() {
        return buyRate;
    }

    public void setBuyRate(BigDecimal buyRate) {
        this.buyRate = buyRate;
    }

    public BigDecimal getSellRate() {
        return sellRate;
    }

    public void setSellRate(BigDecimal sellRate) {
        this.sellRate = sellRate;
    }
}
