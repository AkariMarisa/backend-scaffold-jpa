package com.codelodon.backendscaffold.server.module.main.controller;

import com.codelodon.backendscaffold.common.controller.BaseController;
import com.codelodon.backendscaffold.common.entity.ModuleAnno;
import com.codelodon.backendscaffold.dto.main.model.Function;
import com.codelodon.backendscaffold.server.module.main.dao.FunctionRepo;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/function")
@ModuleAnno(name = "菜单功能")
public class FunctionController extends BaseController<Function, Long> {
    final private FunctionRepo functionRepo;

    @Autowired
    public FunctionController(FunctionRepo functionRepo) {
        super(functionRepo);
        this.functionRepo = functionRepo;
    }
}
