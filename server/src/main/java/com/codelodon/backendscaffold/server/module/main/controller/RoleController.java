package com.codelodon.backendscaffold.server.module.main.controller;

import com.codelodon.backendscaffold.common.controller.BaseController;
import com.codelodon.backendscaffold.common.entity.BaseResponse;
import com.codelodon.backendscaffold.common.entity.ModuleAnno;
import com.codelodon.backendscaffold.common.entity.OperationAnno;
import com.codelodon.backendscaffold.dto.main.model.Function;
import com.codelodon.backendscaffold.dto.main.model.Role;
import com.codelodon.backendscaffold.dto.main.model.RoleFunctions;
import com.codelodon.backendscaffold.dto.main.model.RoleOperations;
import com.codelodon.backendscaffold.server.module.main.dao.FunctionRepo;
import com.codelodon.backendscaffold.server.module.main.dao.RoleFunctionsRepo;
import com.codelodon.backendscaffold.server.module.main.dao.RoleOperationsRepo;
import com.codelodon.backendscaffold.server.module.main.dao.RoleRepo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/role")
@Component
@ModuleAnno(name = "角色管理")
public class RoleController extends BaseController<Role, Long> {
    final private Logger logger = LoggerFactory.getLogger(RoleController.class);
    final private RoleRepo roleRepo;
    final private RoleOperationsRepo roleOperationsRepo;
    final private RoleFunctionsRepo roleFunctionsRepo;
    final private FunctionRepo functionRepo;

    @Autowired
    public RoleController(RoleRepo roleRepo, RoleOperationsRepo roleOperationsRepo, RoleFunctionsRepo roleFunctionsRepo, FunctionRepo functionRepo) {
        super(roleRepo);
        this.roleRepo = roleRepo;
        this.roleOperationsRepo = roleOperationsRepo;
        this.roleFunctionsRepo = roleFunctionsRepo;
        this.functionRepo = functionRepo;
    }

    @Override
    public Response create(Role role) {
        RoleOperations roleOperations = new RoleOperations();
        roleOperations.setOperationIds("");
        RoleOperations nro = roleOperationsRepo.save(roleOperations);
        role.setRoleOperations(nro);

        RoleFunctions roleFunctions = new RoleFunctions();
        roleFunctions.setFunctionIds("");
        RoleFunctions nrf = roleFunctionsRepo.save(roleFunctions);
        role.setRoleFunctions(nrf);

        return super.create(role);
    }

    @Override
    @Transactional
    public Response modify(Long id, Role role) {
        Optional<Role> optionalRole = roleRepo.findById(id);
        if (optionalRole.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new BaseResponse(false, "数据库查无此记录", null)).build();
        }

        Role or = optionalRole.get();
        or.setName(role.getName());

        // 修改角色信息
        roleRepo.updateInfo(or);

        return Response.ok().entity(new BaseResponse(true, "修改成功", null)).build();
    }

    // 获取角色对应功能ID数组
    @Path("/{id}/functionIds")
    @GET
    @OperationAnno(name = "获取角色对应菜单功能ID数组", httpMethod = "GET")
    public Response getFunctionIds(@PathParam("id") final Long id) {
        Optional<Role> optionalRole = roleRepo.findById(id);
        if (optionalRole.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new BaseResponse(false, "数据库查无此记录", null)).build();
        }

        Role r = optionalRole.get();

        return Response.ok().entity(new BaseResponse(true, "查询成功", r.getRoleFunctions().getFunctionIds().split(","))).build();
    }

    // 角色授权接口
    @Path("/{id}/addFunction/{functionIds}")
    @PUT
    @Transactional
    @OperationAnno(name = "角色授权", httpMethod = "PUT")
    public Response addFunction(@PathParam("id") final Long id, @PathParam("functionIds") final String functionIds) {
        Optional<Role> optionalRole = roleRepo.findById(id);
        if (optionalRole.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new BaseResponse(false, "数据库查无此记录", null)).build();
        }

        String[] functionIdStrArr = functionIds.split(",");
        List<Long> functionIdLongList = new ArrayList<>(functionIdStrArr.length);
        for (String idStr : functionIdStrArr) {
            if (StringUtils.isNotEmpty(idStr)) {
                functionIdLongList.add(Long.valueOf(idStr));
            }
        }

        Role r = optionalRole.get();
        RoleFunctions rf = r.getRoleFunctions();
        StringBuilder roleFunctionIdsStr = new StringBuilder(rf.getFunctionIds());
        String[] roleFunctionIds = roleFunctionIdsStr.toString().split(",");

        RoleOperations ro = r.getRoleOperations();
        StringBuilder operationIds = new StringBuilder(ro.getOperationIds());
        String[] roleOperationIds = operationIds.toString().split(",");

        for (Long functionIdL : functionIdLongList) {
            Optional<Function> optionalFunction = functionRepo.findById(functionIdL);
            if (optionalFunction.isEmpty()) {
                logger.info(String.format("[功能ID: %d] 功能不存在, 跳过此条", functionIdL));
                continue;
            }

            //----------更新角色对应功能----------
            String functionIdStr = functionIdL.toString();
            Optional<String> existFunctionId = Arrays.stream(roleFunctionIds).filter(i -> i.equals(functionIdStr)).findAny();

            if (existFunctionId.isPresent()) {
                logger.info(String.format("[功能ID: %d] 用户已绑定对应功能, 不再重复操作", functionIdL));
                continue;
            }

            roleFunctionIdsStr.append(functionIdStr);
            roleFunctionIdsStr.append(",");
            //----------结束更新----------

            //----------更新角色对应操作----------
            Function f = optionalFunction.get();
            Long operationId = f.getOperation().getId();

            String operationIdStr = operationId.toString();
            Optional<String> existOperationId = Arrays.stream(roleOperationIds).filter(i -> i.equals(operationIdStr)).findAny();

            // 因为后台的一个操作可能对应网页上的多个功能, 则如果添加的功能对应的操作已经绑定到用户, 就不需要重复添加了
            if (existOperationId.isPresent()) {
                continue;
            }

            operationIds.append(operationIdStr);
            operationIds.append(",");
            //----------结束更新----------
        }
        rf.setFunctionIds(roleFunctionIdsStr.toString());
        roleFunctionsRepo.save(rf);

        ro.setOperationIds(operationIds.toString());
        roleOperationsRepo.save(ro);

        return Response.ok().entity(new BaseResponse(true, "修改成功", null)).build();
    }

    // 角色取消授权接口
    @Path("/{id}/removeFunction/{functionIds}")
    @PUT
    @Transactional
    @OperationAnno(name = "角色取消授权接口", httpMethod = "PUT")
    public Response removeFunction(@PathParam("id") final Long id, @PathParam("functionIds") final String functionIds) {
        Optional<Role> optionalRole = roleRepo.findById(id);
        if (optionalRole.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new BaseResponse(false, "数据库查无此记录", null)).build();
        }

        String[] functionIdStrArr = functionIds.split(",");
        List<Long> functionIdLongList = new ArrayList<>(functionIdStrArr.length);
        for (String idStr : functionIdStrArr) {
            if (StringUtils.isNotEmpty(idStr)) {
                functionIdLongList.add(Long.valueOf(idStr));
            }
        }

        Role r = optionalRole.get();

        RoleFunctions rf = r.getRoleFunctions();
        String roleFunctionIdsStr = rf.getFunctionIds();
        String[] roleFunctionIds = roleFunctionIdsStr.split(",");
        List<String> roleFunctionIdsList = Arrays.asList(roleFunctionIds);
        roleFunctionIdsList = new ArrayList<>(roleFunctionIdsList);

        RoleOperations ro = r.getRoleOperations();
        String operationIds = ro.getOperationIds();
        String[] roleOperationIds = operationIds.split(",");
        List<String> roleOperationIdsList = Arrays.asList(roleOperationIds);
        roleOperationIdsList = new ArrayList<>(roleOperationIdsList);

        for (Long functionIdL : functionIdLongList) {
            Optional<Function> optionalFunction = functionRepo.findById(functionIdL);
            if (optionalFunction.isEmpty()) {
                logger.info(String.format("[功能ID: %d] 功能不存在, 跳过此条", functionIdL));
                continue;
            }

            //----------更新角色对应功能----------
            roleFunctionIdsList.remove(functionIdL.toString());
            //----------结束更新----------

            //----------更新角色对应操作----------
            Function f = optionalFunction.get();
            Long operationId = f.getOperation().getId();

            roleOperationIdsList.remove(operationId.toString());
            //----------结束更新----------
        }

        rf.setFunctionIds(StringUtils.join(roleFunctionIdsList.toArray(), ",") + ',');
        roleFunctionsRepo.save(rf);

        ro.setOperationIds(StringUtils.join(roleOperationIdsList.toArray(), ",") + ',');
        roleOperationsRepo.save(ro);

        return Response.ok().entity(new BaseResponse(true, "修改成功", null)).build();
    }
}
