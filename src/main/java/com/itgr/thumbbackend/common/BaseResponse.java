package com.itgr.thumbbackend.common;

import com.itgr.thumbbackend.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * @param <T>
 * @author ：y138g
 * 通用响应类
 */
@Data
public class BaseResponse<T> implements Serializable {

    private int code;

    private T data;

    private String message;

    /**
     * 成功响应
     *
     * @param code    状态码
     * @param data    数据
     * @param message 响应信息
     */
    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    /**
     * 成功响应
     *
     * @param code 状态码
     * @param data 数据
     */
    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    /**
     * 错误响应
     *
     * @param errorCode 错误码
     */
    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}
