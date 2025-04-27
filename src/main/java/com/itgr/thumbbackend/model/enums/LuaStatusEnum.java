package com.itgr.thumbbackend.model.enums;

import lombok.Getter;

/**
 * Lua 脚本执行结果枚举类
 */
@Getter
public enum LuaStatusEnum {

    // 执行成功
    SUCCESS(1L),

    // 执行失败
    FAIL(-1L),
    ;

    private final Long value;

    LuaStatusEnum(Long value) {
        this.value = value;
    }
}
