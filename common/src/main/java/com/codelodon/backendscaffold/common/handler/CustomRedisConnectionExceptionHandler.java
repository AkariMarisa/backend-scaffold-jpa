package com.codelodon.backendscaffold.common.handler;

import com.codelodon.backendscaffold.common.entity.BaseResponse;
import io.lettuce.core.RedisConnectionException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class CustomRedisConnectionExceptionHandler implements ExceptionMapper<RedisConnectionException> {
    @Override
    public Response toResponse(RedisConnectionException exception) {
        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new BaseResponse(false, "Redis Server connection error.", null))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
