package com.codelodon.backendscaffold.server.handler;

import com.codelodon.backendscaffold.common.handler.AuthorizationHandler;
import com.codelodon.backendscaffold.common.util.TokenUtil;
import com.codelodon.backendscaffold.dto.main.entity.HttpMethod;
import com.codelodon.backendscaffold.dto.main.entity.UserType;
import com.codelodon.backendscaffold.dto.main.model.Operation;
import com.codelodon.backendscaffold.dto.main.model.User;
import com.codelodon.backendscaffold.server.module.main.dao.OperationRepo;
import com.codelodon.backendscaffold.server.module.main.dao.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ResourceInfo;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class MyAuthorizationHandler implements AuthorizationHandler {
    final private Logger logger = LoggerFactory.getLogger(MyAuthorizationHandler.class);

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private OperationRepo operationRepo;

    @Override
    @Transactional
    public boolean handler(ContainerRequestContext requestContext, ResourceInfo resourceInfo) {
        String authorization = requestContext.getHeaderString("Authorization");
        String token = authorization.split("Bearer ")[1];
        TokenUtil tokenUtil = TokenUtil.getInstance();
        String decodedToken = tokenUtil.decodeToken(token);
        String userId = decodedToken.split("@")[0];

        Optional<User> optionalUser = userRepo.findById(Long.parseLong(userId));
        if (optionalUser.isEmpty()) {
            logger.warn("Token 中获取的用户信息，在服务端不存在");
            return false;
        }

        User u = optionalUser.get();

        if (UserType.SALES_APP.equals(u.getType()) ||
                UserType.PRODUCE_APP.equals(u.getType())) {
            // App 端用户有所有权限
            // FIXME 如果有用 App 端用户身份恶意请求资源的情况，则需要针对其需要的资源再进行控制
            return true;
        }

        String requestMethod = requestContext.getMethod();
        String fullClassName = resourceInfo.getResourceClass().getCanonicalName();
        String methodName = resourceInfo.getResourceMethod().getName();

        HttpMethod type = HttpMethod.valueOf(requestMethod);

        List<Operation> operations = operationRepo.findForAuth(type, fullClassName, methodName);
        if (null == operations || operations.size() <= 0) {
            logger.warn(String.format("请求 [ %s ] 接口 [ Class: %s, Method: %s ] 在当前系统中不存在", requestMethod, fullClassName, methodName));
            return false;
        }

        Operation operation = operations.get(0);

        String operationId = operation.getId().toString();

        String[] userOperationIds = u.getUserOperations().getOperationIds().split(",");
        Optional<String> id = Arrays.stream(userOperationIds).filter(i -> i.equals(operationId)).findAny();
        if (id.isEmpty()) {
            logger.warn(String.format("用户 [ ID: %s, username: %s ] 没有 [ %s ] 接口权限 [ Class: %s, Method: %s ]", userId, u.getUsername(), requestMethod, fullClassName, methodName));
            return false;
        }

        return true;
    }
}
