package com.codelodon.backendscaffold.common.config;

import com.codelodon.backendscaffold.common.filter.AuthenticationFilter;
import com.codelodon.backendscaffold.common.filter.AuthorizationFilter;
import com.codelodon.backendscaffold.common.handler.*;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.logging.Level;
import java.util.logging.Logger;

public class BaseResourceConfig extends ResourceConfig {

    public BaseResourceConfig() {
        // Jersey 对 MultiPart 的支持
        register(MultiPartFeature.class);
        register(new LoggingFeature(Logger.getLogger(LoggingFeature.DEFAULT_LOGGER_NAME),
                Level.INFO, LoggingFeature.Verbosity.PAYLOAD_ANY, 10000));

        // 注册默认异常处理器
        register(CustomExceptionHandler.class);
        register(CustomNotFoundExceptionHandler.class);
        register(CustomNotAllowedExceptionHandler.class);
        register(CustomValidationExceptionHandler.class);
        register(CustomRedisConnectionExceptionHandler.class);
        register(CustomSQLIntegrityConstraintViolationException.class);

        // 注册 Object Mapper
        register(ObjectMapperContextResolver.class);
    }

    public void enableDefaultAuthenticationFilter() {
        // 注册默认鉴权过滤器
        register(AuthenticationFilter.class);
        register(AuthorizationFilter.class);
    }
}
