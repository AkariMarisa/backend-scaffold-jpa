package com.codelodon.backendscaffold.server.module.main.controller;

import com.codelodon.backendscaffold.common.controller.BaseController;
import com.codelodon.backendscaffold.common.entity.BaseResponse;
import com.codelodon.backendscaffold.common.entity.ModuleAnno;
import com.codelodon.backendscaffold.dto.main.model.Operation;
import com.codelodon.backendscaffold.server.module.main.dao.FunctionRepo;
import com.codelodon.backendscaffold.server.module.main.dao.OperationRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("/operation")
@Component
@ModuleAnno(name = "接口管理")
public class OperationController extends BaseController<Operation, Long> {
    final private Logger logger = LoggerFactory.getLogger(OperationController.class);

    final private OperationRepo operationRepo;
    final private FunctionRepo functionRepo;

    @Autowired
    public OperationController(OperationRepo operationRepo, FunctionRepo functionRepo) {
        super(operationRepo);
        this.operationRepo = operationRepo;
        this.functionRepo = functionRepo;
    }

    @Override
    @Transactional
    public Response delete(Long id) {
        Optional<Operation> optionalOperation = operationRepo.findById(id);
        if (optionalOperation.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new BaseResponse(false, "数据库查无此记录", null)).build();
        }

        functionRepo.deleteByOperation(id);
        operationRepo.deleteById(id);

        return Response.ok().entity(new BaseResponse(true, "删除成功", null)).build();
    }
}
