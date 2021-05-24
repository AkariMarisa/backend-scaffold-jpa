package com.codelodon.backendscaffold.common.handler;

import com.codelodon.backendscaffold.common.entity.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class CustomExceptionHandler implements ExceptionMapper<Throwable> {
    final private Logger logger = LoggerFactory.getLogger(CustomExceptionHandler.class);

    @Override
    public Response toResponse(Throwable exception) {
        // TODO 需要根据以后业务不断完善
        logger.error("服务器发生未知异常", exception);
        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new BaseResponse(false, "服务端发生未知异常，请联系管理员", null))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
