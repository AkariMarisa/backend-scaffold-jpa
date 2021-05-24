package com.codelodon.backendscaffold.common.filter;

import com.codelodon.backendscaffold.common.entity.BaseResponse;
import com.codelodon.backendscaffold.common.util.TokenUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * 通用鉴权过滤器
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {
    final private Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        logger.info("请求到达第一层过滤器");

        if (requestContext.getMethod().equals(HttpMethod.OPTIONS)) { // 不需要对 OPTIONS 请求鉴权
            return;
        }

        final Method method = resourceInfo.getResourceMethod();

        if (method.isAnnotationPresent(DenyAll.class)) { // @DenyAll 注解的方法直接拒绝请求
            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON).entity(new BaseResponse(false, "禁止访问", null)).build());
        } else if (!method.isAnnotationPresent(PermitAll.class)) { // 默认鉴权所有非 @DenyAll 和 @PermitAll 的方法
            // 验证 Token
            logger.debug("开始验证Token");
            String authorization = requestContext.getHeaderString("Authorization");
            if (StringUtils.isEmpty(authorization)) {
                requestContext.abortWith(Response.status(Response.Status.PRECONDITION_FAILED).type(MediaType.APPLICATION_JSON).entity(new BaseResponse(false, "请求Token非法", null)).build());
                return;
            }

            if (!authorization.startsWith("Bearer ")) {
                requestContext.abortWith(Response.status(Response.Status.PRECONDITION_FAILED).type(MediaType.APPLICATION_JSON).entity(new BaseResponse(false, "请求Token非法", null)).build());
                return;
            }

            String token = authorization.split("Bearer ")[1];

            // 从 Token ( JWT ) 中解析出用户信息，并查询当前是否存在这条 Token

            TokenUtil tokenUtil = TokenUtil.getInstance();
            String decodedToken = tokenUtil.decodeToken(token);
            String userId = decodedToken.split("@")[0];

            String storedToken = tokenUtil.getToken(userId);

            // 如果不存在，则禁止用户访问
            if (StringUtils.isEmpty(storedToken)) {
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(new BaseResponse(false, "Token已失效", null)).build());
                return;
            }

            // 如果请求 Token 和保存的 Token 不一致，则禁止用户访问
            if (!storedToken.equals(token)) {
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).type(MediaType.APPLICATION_JSON).entity(new BaseResponse(false, "Token已失效", null)).build());
                return;
            }

            // 如果存在，则继续访问，并且为当前 Token 续约
            tokenUtil.renewToken(userId);
        }
    }
}
