package com.codelodon.backendscaffold.server.module.main.controller;

import com.codelodon.backendscaffold.common.entity.AuthGroup;
import com.codelodon.backendscaffold.common.entity.BaseResponse;
import com.codelodon.backendscaffold.common.entity.ModuleAnno;
import com.codelodon.backendscaffold.common.entity.OperationAnno;
import com.codelodon.backendscaffold.common.util.TokenUtil;
import com.codelodon.backendscaffold.dto.main.model.Function;
import com.codelodon.backendscaffold.dto.main.model.User;
import com.codelodon.backendscaffold.server.module.main.dao.FunctionRepo;
import com.codelodon.backendscaffold.server.module.main.dao.OperationRepo;
import com.codelodon.backendscaffold.server.module.main.dao.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;

import javax.annotation.security.PermitAll;
import javax.validation.Valid;
import javax.validation.groups.ConvertGroup;
import javax.validation.groups.Default;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/auth")
@ModuleAnno(name = "授权管理")
public class AuthorizationController {
    final private UserRepo userRepo;
    final private FunctionRepo functionRepo;

    @Autowired
    public AuthorizationController(UserRepo userRepo, OperationRepo operationRepo, FunctionRepo functionRepo) {
        this.userRepo = userRepo;
        this.functionRepo = functionRepo;
    }

    /**
     * 登陆
     *
     * @param user 登陆用户信息
     * @return Token
     */
    @PermitAll
    @Path("/login")
    @POST
    @OperationAnno(name = "用户登陆", httpMethod = "POST", needAuth = false)
    public Response login(@Valid @ConvertGroup(from = Default.class, to = AuthGroup.class) final User user) {
        Optional<User> userOptional = userRepo.findByUsername(user.getUsername());
        if (userOptional.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new BaseResponse(false, "用户名或密码错误", null)).build();
        }

        User u = userOptional.get();

        boolean isMatch = new BCryptPasswordEncoder().matches(user.getPassword(), u.getPassword());
        if (!isMatch) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new BaseResponse(false, "用户名或密码错误", null)).build();
        }

        if (!u.getIsEnabled()) {
            return Response.status(Response.Status.FORBIDDEN).entity(new BaseResponse(false, "用户已被禁用", null)).build();
        }

        // 生成 Token
        TokenUtil tokenUtil = TokenUtil.getInstance();

        String userIdStr = u.getId().toString();
        String token = tokenUtil.generateToken(userIdStr + "@" + System.currentTimeMillis());

        tokenUtil.saveToken(userIdStr, token);

        // 查询出用户对应的操作列表，并放到响应中
        String functionIds = u.getUserFunctions().getFunctionIds();

        String[] idsInStr = functionIds.split(",");
        List<Long> ids = new ArrayList<>(idsInStr.length);

        for (String idStr : idsInStr) {
            if (!StringUtils.isEmpty(idStr)) {
                ids.add(Long.valueOf(idStr));
            }
        }

        Set<Function> functions = new HashSet<>((List<Function>) functionRepo.findAllById(ids));

        u.setFunctions(functions);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", u);

        return Response.ok(new BaseResponse(true, "登陆成功", result)).build();
    }

    /**
     * 注销
     *
     * @return 空
     */
    @PermitAll
    @Path("/logout/{id}")
    @POST
    @OperationAnno(name = "用户注销", httpMethod = "POST", needAuth = false)
    public Response logout(@PathParam("id") Long id) {
        Optional<User> userOptional = userRepo.findById(id);
        if (userOptional.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new BaseResponse(false, "用户不存在", null)).build();
        }

        User u = userOptional.get();

        TokenUtil tokenUtil = TokenUtil.getInstance();
        tokenUtil.deleteToken(u.getId().toString());

        return Response.ok(new BaseResponse(true, "注销成功", null)).build();
    }
}
