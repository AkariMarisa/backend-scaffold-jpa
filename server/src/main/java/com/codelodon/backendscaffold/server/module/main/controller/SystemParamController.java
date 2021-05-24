package com.codelodon.backendscaffold.server.module.main.controller;

import com.codelodon.backendscaffold.common.controller.BaseController;
import com.codelodon.backendscaffold.common.entity.ModuleAnno;
import com.codelodon.backendscaffold.common.entity.OperationAnno;
import com.codelodon.backendscaffold.dto.main.model.SystemParam;
import com.codelodon.backendscaffold.server.module.main.dao.SystemParamRepo;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/systemParam")
@ModuleAnno(name = "系统参数管理")
public class SystemParamController extends BaseController<SystemParam, Long> {
    final private SystemParamRepo systemParamRepo;

    @Autowired
    public SystemParamController(SystemParamRepo systemParamRepo) {
        super(systemParamRepo);
        this.systemParamRepo = systemParamRepo;
    }

    @Override
    @PermitAll
    @OperationAnno(name = "按ID查询", httpMethod = "GET", needAuth = false)
    public Response getOne(Long aLong) {
        return super.getOne(aLong);
    }
}
