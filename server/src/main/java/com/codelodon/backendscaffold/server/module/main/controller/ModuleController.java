package com.codelodon.backendscaffold.server.module.main.controller;

import com.codelodon.backendscaffold.common.controller.BaseController;
import com.codelodon.backendscaffold.common.entity.ModuleAnno;
import com.codelodon.backendscaffold.dto.main.model.Module;
import com.codelodon.backendscaffold.server.module.main.dao.ModuleRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/module")
@ModuleAnno(name = "模块管理")
public class ModuleController extends BaseController<Module, Long> {
    final private Logger logger = LoggerFactory.getLogger(ModuleController.class);

    final private ModuleRepo moduleRepo;

    @Autowired
    public ModuleController(ModuleRepo moduleRepo) {
        super(moduleRepo);
        this.moduleRepo = moduleRepo;
    }

}
