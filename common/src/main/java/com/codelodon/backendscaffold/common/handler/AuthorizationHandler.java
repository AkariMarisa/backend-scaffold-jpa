package com.codelodon.backendscaffold.common.handler;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;

public interface AuthorizationHandler {
    boolean handler(ContainerRequestContext requestContext, ResourceInfo resourceInfo);
}
