package com.bspoljaric.backend.model;

public class Currency extends AbstractCode {

    private int numericCode;

    public Currency(String name, String code, int numericCode) {
        super(name, code);
        this.numericCode = numericCode;
    }

    public Currency(String code) {
        super(code);
    }

    public Currency() {

    }

    public int getNumericCode() {
        return numericCode;
    }

    public void setNumericCode(int numericCode) {
        this.numericCode = numericCode;
    }
}
