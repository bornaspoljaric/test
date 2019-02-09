package com.bspoljaric.backend.model;

public enum TransactionAction {
    CREATE(1),
    PROCESS(2);

    public final Integer value;

    private TransactionAction(final Integer value) {
        this.value = value;
    }

}
