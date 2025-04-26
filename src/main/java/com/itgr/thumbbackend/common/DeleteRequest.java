package com.itgr.thumbbackend.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author ：y138g
 * 删除请求包装类
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    @Serial
    private static final long serialVersionUID = 1L;
}
