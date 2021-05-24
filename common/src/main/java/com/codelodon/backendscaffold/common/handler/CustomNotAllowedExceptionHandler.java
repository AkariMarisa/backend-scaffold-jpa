package com.codelodon.backendscaffold.common.handler;


import com.codelodon.backendscaffold.common.entity.BaseResponse;

import javax.ws.rs.NotAllowedException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class CustomNotAllowedExceptionHandler implements ExceptionMapper<NotAllowedException> {
    @Override
    public Response toResponse(NotAllowedException exception) {
        return Response
                .status(Response.Status.METHOD_NOT_ALLOWED)
                .entity(new BaseResponse(false, "HTTP 405 Method Not Allowed", null))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
