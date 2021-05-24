package com.codelodon.backendscaffold.server.module.main.controller;

import com.codelodon.backendscaffold.common.controller.BaseController;
import com.codelodon.backendscaffold.common.entity.ModuleAnno;
import com.codelodon.backendscaffold.dto.main.model.SubSystem;
import com.codelodon.backendscaffold.server.module.main.dao.SubSystemRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/subSystem")
@ModuleAnno(name = "子系统管理")
public class SubSystemController extends BaseController<SubSystem, Long> {
    final private Logger logger = LoggerFactory.getLogger(SubSystemController.class);

    final private SubSystemRepo subSystemRepo;

    @Autowired
    public SubSystemController(SubSystemRepo subSystemRepo) {
        super(subSystemRepo);
        this.subSystemRepo = subSystemRepo;
    }
}
