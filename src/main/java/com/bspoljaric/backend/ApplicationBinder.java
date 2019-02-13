package com.bspoljaric.backend;

import com.bspoljaric.backend.service.DatabaseService;
import com.bspoljaric.backend.service.TransactionService;
import com.bspoljaric.backend.service.impl.DatabaseServiceImpl;
import com.bspoljaric.backend.service.impl.TransactionServiceImpl;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class ApplicationBinder extends AbstractBinder {
    @Override
    protected void configure() {
        bind(new TransactionServiceImpl()).to(TransactionService.class);
        bind(new DatabaseServiceImpl()).to(DatabaseService.class);
    }
}
