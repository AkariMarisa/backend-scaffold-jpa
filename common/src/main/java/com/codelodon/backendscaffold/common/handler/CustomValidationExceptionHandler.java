package com.codelodon.backendscaffold.common.handler;


import com.codelodon.backendscaffold.common.entity.BaseResponse;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.Set;

public class CustomValidationExceptionHandler implements ExceptionMapper<ValidationException> {
    @Override
    public Response toResponse(ValidationException exception) {
        if (exception instanceof ConstraintViolationException) {
            Set<ConstraintViolation<?>> l = ((ConstraintViolationException) exception).getConstraintViolations();
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<?> c : l) {
                sb.append(c.getMessage());
                sb.append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new BaseResponse(false, sb.toString(), null))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } else {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(new BaseResponse(false, exception.getMessage(), null))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }
}
