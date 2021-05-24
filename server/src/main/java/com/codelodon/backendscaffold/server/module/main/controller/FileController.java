package com.codelodon.backendscaffold.server.module.main.controller;

import com.codelodon.backendscaffold.common.entity.BaseResponse;
import com.codelodon.backendscaffold.common.entity.ModuleAnno;
import com.codelodon.backendscaffold.common.entity.OperationAnno;
import com.codelodon.backendscaffold.dto.main.model.File;
import com.codelodon.backendscaffold.server.config.SimpleFileServerConfig;
import com.codelodon.backendscaffold.server.module.main.dao.FileRepo;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.security.PermitAll;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Path("/file")
@ModuleAnno(name = "文件管理")
public class FileController {
    private final Logger logger = LoggerFactory.getLogger(FileController.class);

    final private FileRepo fileRepository;
    final private SimpleFileServerConfig simpleFileServerConfig;

    @Autowired
    public FileController(FileRepo fileRepository, SimpleFileServerConfig simpleFileServerConfig) {
        this.fileRepository = fileRepository;
        this.simpleFileServerConfig = simpleFileServerConfig;
    }

    /**
     * 获取文件详情
     *
     * @param fileId 文件ID
     * @return 文件详情
     */
    @GET
    @Path("/{fileId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @OperationAnno(name = "获取文件详情", httpMethod = "GET", needAuth = false)
    public Response getById(@PathParam("fileId") Long fileId) {
        Optional<File> f = fileRepository.findById(fileId);
        if (f.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new BaseResponse(false, "文件不存在", null)).build();
        }

        return Response.ok(new BaseResponse(true, "查询成功", f.get())).build();
    }

    /**
     * 下载文件
     *
     * @param fileId 文件ID
     * @return 文件
     */
    @GET
    @Path("/{fileId}/download")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @PermitAll
    @OperationAnno(name = "下载文件", httpMethod = "GET", needAuth = false)
    public Response download(@PathParam("fileId") Long fileId) {
        Optional<File> f = fileRepository.findById(fileId);
        if (f.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new BaseResponse(false, "文件不存在", null)).build();
        }

        File fileInfo = f.get();
        StreamingOutput fileStream = output -> {
            java.nio.file.Path path = Paths.get(simpleFileServerConfig.getBaseDir() + java.io.File.separator + fileInfo.getName());
            byte[] data = Files.readAllBytes(path);
            output.write(data);
            output.flush();
        };

        return Response
                .ok(fileStream, MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename = " + fileInfo.getFilename())
                .build();
    }

    /**
     * 上传文件，并保存文件详情
     *
     * @param file            文件
     * @param fileDisposition 表单文件信息
     * @return 保存后的文件详情
     */
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    @OperationAnno(name = "上传文件", httpMethod = "POST", needAuth = false)
    public Response uploadFile(@FormDataParam("file") InputStream file,
                               @FormDataParam("file") FormDataContentDisposition fileDisposition) {
        String fileName = fileDisposition.getFileName();
        fileName = new String(fileName.getBytes(StandardCharsets.ISO_8859_1)); // FIXME 解决文件名乱码问题，现在这种解决方式太暴力了，估计以后还会出现这种问题

        // 截取后缀名
        int iDot = fileName.lastIndexOf(".");
        String subFix = "";
        if (iDot > -1) {
            // 存在后缀名
            subFix = fileName.substring(iDot);
        }

        // 生成一个UUID作为新的文件名，防止文件重名覆盖
        String uuidFilename = UUID.randomUUID().toString().replaceAll("-", "") + subFix;

        java.nio.file.Path path = FileSystems.getDefault().getPath(simpleFileServerConfig.getBaseDir() + java.io.File.separator + uuidFilename);
        long size;
        try {
            size = file.available();
            Files.copy(file, path);
        } catch (IOException e) {
            logger.error("将文件保存到本地时发生异常", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new BaseResponse(false, "将文件保存到本地时发生异常", null)).build();
        }

        File f = new File();
        f.setName(uuidFilename);
        f.setFilename(fileName);
        f.setSize(size);
        String fileType = fileDisposition.getType();
        f.setFileType(fileType);
        fileRepository.save(f);

        return Response.ok(new BaseResponse(true, "创建成功", f)).build();
    }
}
