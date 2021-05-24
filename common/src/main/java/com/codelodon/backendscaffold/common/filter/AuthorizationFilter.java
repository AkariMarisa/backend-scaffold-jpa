package com.codelodon.backendscaffold.common.filter;

import com.codelodon.backendscaffold.common.entity.BaseResponse;
import com.codelodon.backendscaffold.common.handler.AuthorizationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Priority;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Method;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class AuthorizationFilter implements ContainerRequestFilter {
    final private Logger logger = LoggerFactory.getLogger(AuthorizationFilter.class);
    @Context
    ResourceInfo resourceInfo;
    @Autowired(required = false)
    private AuthorizationHandler authorizationHandler;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        logger.info("请求到达第二层过滤器");

        if (requestContext.getMethod().equals(HttpMethod.OPTIONS)) { // 不需要对 OPTIONS 请求鉴权
            return;
        }

        final Method method = resourceInfo.getResourceMethod();

        if (method.isAnnotationPresent(DenyAll.class)) { // @DenyAll 注解的方法直接拒绝请求
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON).entity(new BaseResponse(false, "禁止访问", null)).build());
        } else if (!method.isAnnotationPresent(PermitAll.class)) { // 默认鉴权所有非 @DenyAll 和 @PermitAll 的方法
            if (null != authorizationHandler) { // 只有注入了 AuthorizationHandler 的实现类，才执行认证

                boolean canPass = authorizationHandler.handler(requestContext, resourceInfo);
                if (!canPass) {
                    requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(new BaseResponse(false, "未授权禁止访问", null)).build());
                }
            }
        }
    }
}
