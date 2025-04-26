package com.itgr.thumbbackend.exception;

import com.itgr.thumbbackend.common.BaseResponse;
import com.itgr.thumbbackend.common.ResultUtils;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author ：y138g
 * 全局异常处理类
 */
@Slf4j
@RestControllerAdvice
@Hidden
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public BaseResponse<?> exceptionHandler(BusinessException e) {
        log.error("BusinessException:", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(value = RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException:", e);
//        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage());
    }
}
