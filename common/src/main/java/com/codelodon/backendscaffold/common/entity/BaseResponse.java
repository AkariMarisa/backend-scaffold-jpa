package com.codelodon.backendscaffold.common.entity;

import lombok.Data;

@Data
public class BaseResponse {
    /**
     * 处理状态
     */
    private Boolean state;

    /**
     * 处理结果详细信息
     */
    private String message;

    /**
     * 响应数据
     */
    private Object data;

    public BaseResponse(Boolean state, String message, Object data) {
        this.state = state;
        this.message = message;
        this.data = data;
    }
}
