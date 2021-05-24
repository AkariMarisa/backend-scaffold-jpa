package com.codelodon.backendscaffold.common.handler;

import com.codelodon.backendscaffold.common.entity.BaseResponse;
import org.springframework.dao.DataIntegrityViolationException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.sql.SQLIntegrityConstraintViolationException;

public class CustomSQLIntegrityConstraintViolationException implements ExceptionMapper<DataIntegrityViolationException> {
    @Override
    public Response toResponse(DataIntegrityViolationException exception) {
        Throwable root = exception.getRootCause();
        String message;
        if (root instanceof SQLIntegrityConstraintViolationException) { // 约束异常
            String errorMessage = root.getLocalizedMessage();

            if (errorMessage.contains("Duplicate entry")) { // 唯一约束

                String[] words = errorMessage.split(" ");
                String v = words[2];
                message = String.format("%s 违反唯一约束, 请勿添加重复数据", v);

            } else if (errorMessage.contains("foreign key constraint fails")) { // 外键约束

                String[] columns = errorMessage.split("FOREIGN KEY")[1].split("REFERENCES");
                String foreignKey = columns[0].replaceAll("\\(", "")
                        .replaceAll("\\)", "");

                String tableName = columns[1];
                tableName = tableName.substring(0, tableName.length() - 1);

                message = String.format("上送数据违反外键%s关联表字段%s 的约束, 请检查关联字段是否存在", foreignKey, tableName);

            } else { // 主键约束

                message = "上送数据违反主键约束";

            }

        } else { // 其他错误
            message = "服务端发生未知异常，请联系管理员";
        }
        return Response
                .status(Response.Status.METHOD_NOT_ALLOWED)
                .entity(new BaseResponse(false, message, null))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
