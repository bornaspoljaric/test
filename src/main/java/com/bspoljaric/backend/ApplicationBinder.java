package com.bspoljaric.backend;

import com.bspoljaric.backend.service.TransactionService;
import com.bspoljaric.backend.service.impl.TransactionServiceImpl;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class ApplicationBinder extends AbstractBinder {
    @Override
    protected void configure() {
        bind(TransactionServiceImpl.class).to(TransactionServiceImpl.class);
    }
}
