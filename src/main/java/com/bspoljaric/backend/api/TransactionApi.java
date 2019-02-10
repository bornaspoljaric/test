package com.bspoljaric.backend.api;

import com.bspoljaric.backend.model.Transaction;
import com.bspoljaric.backend.service.TransactionService;
import com.bspoljaric.backend.service.impl.TransactionServiceImpl;
import com.bspoljaric.backend.util.ApiError;
import com.google.gson.Gson;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

@Path("/transactions")
public class TransactionApi {

    final static Logger    LOGGER = Logger.getLogger(TransactionApi.class.getName());

    @Inject
    TransactionServiceImpl transactionService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response transactions() throws SQLException {
        final List<Transaction> transactionList = transactionService.findAll();
        if (transactionList == null) {
            LOGGER.info("Error while finding all transactions.");
            return Response.serverError().entity(new ApiError(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Server has internal problems, please try again later"))
                    .build();
        }
        return Response.ok().type(MediaType.APPLICATION_JSON).entity(new Gson().toJson(transactionList)).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response transaction() {
        return Response.ok().type(MediaType.APPLICATION_JSON).entity(new Gson().toJson("")).build();
    }

}
