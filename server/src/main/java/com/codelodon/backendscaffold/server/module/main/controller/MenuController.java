package com.codelodon.backendscaffold.server.module.main.controller;

import com.codelodon.backendscaffold.common.controller.BaseController;
import com.codelodon.backendscaffold.common.entity.BaseResponse;
import com.codelodon.backendscaffold.common.entity.ModuleAnno;
import com.codelodon.backendscaffold.dto.main.model.Menu;
import com.codelodon.backendscaffold.server.module.main.dao.FunctionRepo;
import com.codelodon.backendscaffold.server.module.main.dao.MenuRepo;
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
@Path("/menu")
@Component
@ModuleAnno(name = "菜单管理")
public class MenuController extends BaseController<Menu, Long> {
    final private MenuRepo menuRepo;
    final private FunctionRepo functionRepo;

    @Autowired
    public MenuController(MenuRepo menuRepo, FunctionRepo functionRepo) {
        super(menuRepo);
        this.menuRepo = menuRepo;
        this.functionRepo = functionRepo;
    }

    @Override
    @Transactional
    public Response delete(Long id) {
        Optional<Menu> optionalMenu = menuRepo.findById(id);
        if (optionalMenu.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new BaseResponse(false, "数据库查无此记录", null)).build();
        }

        functionRepo.deleteByMenu(id);
        menuRepo.deleteById(id);

        return Response.ok().entity(new BaseResponse(true, "删除成功", null)).build();
    }
}
