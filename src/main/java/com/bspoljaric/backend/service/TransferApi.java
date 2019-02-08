package com.bspoljaric.backend.service;

import com.bspoljaric.backend.dto.Transfer;
import com.bspoljaric.backend.util.ApiError;

import javax.print.attribute.standard.Media;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;

@Path("/transfer")
public class TransferApi {
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response transfer(final Transfer transfer) {

        // Define params to eliminate multiple calls
        final String accountTo = transfer.getAccTo();
        final String accountFrom = transfer.getAccFrom();
        final BigDecimal amount = transfer.getAmount();
        // Do null validations
        if (accountTo == null || accountTo.trim().length() == 0) {
            return Response.serverError().entity(new ApiError(Response.Status.BAD_REQUEST.getStatusCode(), "Account to cannot be null")).build();
        }

        if (accountFrom == null || accountFrom.trim().length() == 0) {
            return Response.serverError().entity(new ApiError(Response.Status.BAD_REQUEST.getStatusCode(), "Account from cannot be null")).build();
        }

        if (amount == null) {
            return Response.serverError().entity(new ApiError(Response.Status.BAD_REQUEST.getStatusCode(), "Amount cannot be null")).build();
        }

        // TODO: Check if accounts exist, return 404, return 500 if db not working
        return Response.ok("Transaction processed!", MediaType.APPLICATION_JSON).build();
    }
}
