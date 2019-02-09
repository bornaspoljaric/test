package com.bspoljaric.backend.model;

public enum TransactionStatus {
    CREATED(1), //
    PROCESSED(2), //
    REJECTED(3); //

    public final Integer value;

    private TransactionStatus(final Integer value) {
        this.value = value;
    }

}
