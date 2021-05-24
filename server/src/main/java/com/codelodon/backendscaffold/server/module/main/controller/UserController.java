package com.codelodon.backendscaffold.server.module.main.controller;

import com.codelodon.backendscaffold.common.controller.BaseController;
import com.codelodon.backendscaffold.common.entity.*;
import com.codelodon.backendscaffold.common.util.TokenUtil;
import com.codelodon.backendscaffold.dto.main.model.*;
import com.codelodon.backendscaffold.server.module.main.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.Valid;
import javax.validation.groups.ConvertGroup;
import javax.validation.groups.Default;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/user")
@Component
@ModuleAnno(name = "用户管理")
public class UserController extends BaseController<User, Long> {
    final private Logger logger = LoggerFactory.getLogger(UserController.class);
    final private UserRepo userRepo;
    final private RoleRepo roleRepo;
    final private UserFunctionsRepo userFunctionsRepo;
    final private UserOperationsRepo userOperationsRepo;
    final private FunctionRepo functionRepo;

    @PersistenceContext
    EntityManager em;

    @Autowired
    public UserController(UserRepo userRepo, RoleRepo roleRepo, UserFunctionsRepo userFunctionsRepo, UserOperationsRepo userOperationsRepo, FunctionRepo functionRepo) {
        super(userRepo);
        this.roleRepo = roleRepo;
        this.userRepo = userRepo;
        this.userFunctionsRepo = userFunctionsRepo;
        this.userOperationsRepo = userOperationsRepo;
        this.functionRepo = functionRepo;
    }

    @Override
    @Transactional
    public Response create(User user) {
        Role r = user.getRole();
        long roleId = r.getId();

        // 根据角色名，查询出角色关联的操作权限
        Optional<Role> optionalRole = roleRepo.findById(roleId);
        if (optionalRole.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new BaseResponse(false, "角色不存在", null)).build();
        }

        Role role = optionalRole.get();

        // 保存对应功能权限到用户信息中
        String functionIds = role.getRoleFunctions().getFunctionIds();
        UserFunctions userFunctions = new UserFunctions();
        userFunctions.setFunctionIds(functionIds);

        user.setUserFunctions(userFunctions);

        // 保存对应操作权限到用户信息中
        String operationIds = role.getRoleOperations().getOperationIds();
        UserOperations userOperations = new UserOperations();
        userOperations.setOperationIds(operationIds);

        user.setUserOperations(userOperations);

        return super.create(user);
    }

    @Override
    @Transactional
    public Response modify(Long id, User user) {
        Optional<User> optionalUser = userRepo.findById(id);
        if (optionalUser.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new BaseResponse(false, "数据库查无此记录", null)).build();
        }

        // 修改用户个人信息
        userRepo.updateInfo(user);

        // 清空对应 Token
        TokenUtil tokenUtil = TokenUtil.getInstance();
        tokenUtil.deleteToken(id.toString());

        return Response.ok().entity(new BaseResponse(true, "修改成功", null)).build();
    }

    @Override
    @Transactional
    public Response delete(Long id) {

        Optional<User> optionalUser = userRepo.findById(id);
        if (optionalUser.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new BaseResponse(false, "数据库查无此记录", null)).build();
        }

        User u = optionalUser.get();

        userRepo.deleteById(id);
        userOperationsRepo.deleteById(u.getUserOperations().getId());
        userFunctionsRepo.deleteById(u.getUserFunctions().getId());

        return Response.ok().entity(new BaseResponse(true, "删除成功", null)).build();
    }

    /**
     * 修改用户密码
     *
     * @param id   用户ID
     * @param user 用户信息
     * @return 修改后的用户信息
     */
    @Path("/{id}/changePassword")
    @PUT
    @Transactional
    @OperationAnno(name = "修改用户密码", httpMethod = "PUT")
    public Response changePassword(@PathParam("id") final Long id, @Valid @ConvertGroup(from = Default.class, to = ChangePasswordGroup.class) User user) {
        Optional<User> optionalUser = userRepo.findById(id);
        if (optionalUser.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new BaseResponse(false, "数据库查无此记录", null)).build();
        }

        userRepo.updatePassword(id, user.getPassword());

        // 清空对应 Token
        TokenUtil tokenUtil = TokenUtil.getInstance();
        tokenUtil.deleteToken(id.toString());

        return Response.ok().entity(new BaseResponse(true, "修改成功", null)).build();
    }

    /**
     * 修改用户可用性状态
     *
     * @param id   用户ID
     * @param user 用户信息
     * @return 修改后的用户信息
     */
    @Path("/{id}/changeUserUsable")
    @PUT
    @Transactional
    @OperationAnno(name = "修改用户可用性状态", httpMethod = "PUT")
    public Response changeUserUsable(@PathParam("id") final Long id, @Valid @ConvertGroup(from = Default.class, to = UserUsableGroup.class) User user) {
        Optional<User> optionalUser = userRepo.findById(id);
        if (optionalUser.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new BaseResponse(false, "数据库查无此记录", null)).build();
        }

        userRepo.updateUsability(id, user.getIsEnabled());

        // 清空对应 Token
        TokenUtil tokenUtil = TokenUtil.getInstance();
        tokenUtil.deleteToken(id.toString());

        return Response.ok().entity(new BaseResponse(true, "修改成功", null)).build();
    }

    @Path("/{id}/functionIds")
    @GET
    @OperationAnno(name = "获取用户对应菜单功能ID数组", httpMethod = "GET")
    public Response getFunctionIds(@PathParam("id") final Long id) {
        Optional<User> optionalUser = userRepo.findById(id);
        if (optionalUser.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new BaseResponse(false, "数据库查无此记录", null)).build();
        }

        User u = optionalUser.get();

        return Response.ok().entity(new BaseResponse(true, "查询成功", u.getUserFunctions().getFunctionIds().split(","))).build();
    }

    @Path("/{id}/functions")
    @GET
    @OperationAnno(name = "获取用户对应菜单功能", httpMethod = "GET")
    public Response getFunctions(@PathParam("id") final Long id) {
        Optional<User> optionalUser = userRepo.findById(id);
        if (optionalUser.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new BaseResponse(false, "数据库查无此记录", null)).build();
        }

        User u = optionalUser.get();

        String functionIds = u.getUserFunctions().getFunctionIds();

        String[] idsInStr = functionIds.split(",");
        List<Long> ids = new ArrayList<>(idsInStr.length);

        for (String idStr : idsInStr) {
            if (StringUtils.isNotEmpty(idStr)) {
                ids.add(Long.valueOf(idStr));
            }
        }

        Iterable<Function> functions = ids.size() > 0 ? functionRepo.findAllById(ids) : null;

        return Response.ok().entity(new BaseResponse(true, "查询成功", functions)).build();
    }

    // 用户授权接口
    @Path("/{id}/addFunction/{functionIds}")
    @PUT
    @Transactional
    @OperationAnno(name = "用户授权", httpMethod = "PUT")
    public Response addFunction(@PathParam("id") final Long id, @PathParam("functionIds") final String functionIds) {
        Optional<User> optionalUser = userRepo.findById(id);
        if (optionalUser.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new BaseResponse(false, "数据库查无此记录", null)).build();
        }

        String[] functionIdStrArr = functionIds.split(",");
        List<Long> functionIdLongList = new ArrayList<>(functionIdStrArr.length);
        for (String idStr : functionIdStrArr) {
            if (StringUtils.isNotEmpty(idStr)) {
                functionIdLongList.add(Long.valueOf(idStr));
            }
        }

        User u = optionalUser.get();
        UserFunctions uf = u.getUserFunctions();
        StringBuilder userFunctionIdsStr = new StringBuilder(uf.getFunctionIds());
        String[] userFunctionIds = userFunctionIdsStr.toString().split(",");

        UserOperations uo = u.getUserOperations();
        StringBuilder operationIds = new StringBuilder(uo.getOperationIds());
        String[] userOperationIds = operationIds.toString().split(",");

        for (Long functionIdL : functionIdLongList) {
            Optional<Function> optionalFunction = functionRepo.findById(functionIdL);
            if (optionalFunction.isEmpty()) {
                logger.info(String.format("[功能ID: %d] 功能不存在, 跳过此条", functionIdL));
                continue;
            }

            //----------更新用户对应功能----------
            String functionIdStr = functionIdL.toString();
            Optional<String> existFunctionId = Arrays.stream(userFunctionIds).filter(i -> i.equals(functionIdStr)).findAny();

            if (existFunctionId.isPresent()) {
                logger.info(String.format("[功能ID: %d] 用户已绑定对应功能, 不再重复操作", functionIdL));
                continue;
            }

            userFunctionIdsStr.append(functionIdStr);
            userFunctionIdsStr.append(",");
            //----------结束更新----------

            //----------更新用户对应操作----------
            Function f = optionalFunction.get();
            Long operationId = f.getOperation().getId();

            String operationIdStr = operationId.toString();
            Optional<String> existOperationId = Arrays.stream(userOperationIds).filter(i -> i.equals(operationIdStr)).findAny();

            // 因为后台的一个操作可能对应网页上的多个功能, 则如果添加的功能对应的操作已经绑定到用户, 就不需要重复添加了
            if (existOperationId.isPresent()) {
                continue;
            }

            operationIds.append(operationIdStr);
            operationIds.append(",");
            //----------结束更新----------
        }

        uf.setFunctionIds(userFunctionIdsStr.toString());
        userFunctionsRepo.save(uf);

        uo.setOperationIds(operationIds.toString());
        userOperationsRepo.save(uo);

        return Response.ok().entity(new BaseResponse(true, "修改成功", null)).build();
    }

    // 用户取消授权接口
    @Path("/{id}/removeFunction/{functionIds}")
    @PUT
    @Transactional
    @OperationAnno(name = "用户取消授权", httpMethod = "PUT")
    public Response removeFunction(@PathParam("id") final Long id, @PathParam("functionIds") final String functionIds) {
        Optional<User> optionalUser = userRepo.findById(id);
        if (optionalUser.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new BaseResponse(false, "数据库查无此记录", null)).build();
        }

        String[] functionIdStrArr = functionIds.split(",");
        List<Long> functionIdLongList = new ArrayList<>(functionIdStrArr.length);
        for (String idStr : functionIdStrArr) {
            if (StringUtils.isNotEmpty(idStr)) {
                functionIdLongList.add(Long.valueOf(idStr));
            }
        }

        User u = optionalUser.get();
        UserFunctions uf = u.getUserFunctions();
        String userFunctionIdsStr = uf.getFunctionIds();
        String[] userFunctionIds = userFunctionIdsStr.split(",");
        List<String> userFunctionIdsList = Arrays.asList(userFunctionIds);
        userFunctionIdsList = new ArrayList<>(userFunctionIdsList);

        UserOperations uo = u.getUserOperations();
        String operationIds = uo.getOperationIds();
        String[] userOperationIds = operationIds.split(",");
        List<String> userOperationIdsList = Arrays.asList(userOperationIds);
        userOperationIdsList = new ArrayList<>(userOperationIdsList);

        for (Long functionIdL : functionIdLongList) {
            Optional<Function> optionalFunction = functionRepo.findById(functionIdL);
            if (optionalFunction.isEmpty()) {
                logger.info(String.format("[功能ID: %d] 功能不存在, 跳过此条", functionIdL));
                continue;
            }

            //----------更新用户对应功能----------
            userFunctionIdsList.remove(functionIdL.toString());
            //----------结束更新----------

            //----------更新用户对应操作----------
            Function f = optionalFunction.get();
            Long operationId = f.getOperation().getId();

            userOperationIdsList.remove(operationId.toString());
            //----------结束更新----------
        }

        uf.setFunctionIds(StringUtils.join(userFunctionIdsList.toArray(), ",") + ',');
        userFunctionsRepo.save(uf);

        uo.setOperationIds(StringUtils.join(userOperationIdsList.toArray(), ",") + ',');
        userOperationsRepo.save(uo);

        return Response.ok().entity(new BaseResponse(true, "修改成功", null)).build();
    }
}
