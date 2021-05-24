package com.codelodon.backendscaffold.common.handler;

import com.codelodon.backendscaffold.common.entity.BaseResponse;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class CustomNotFoundExceptionHandler implements ExceptionMapper<NotFoundException> {
    @Override
    public Response toResponse(NotFoundException exception) {
        return Response
                .status(Response.Status.NOT_FOUND)
                .entity(new BaseResponse(false, "404 NOT FOUND", null))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
