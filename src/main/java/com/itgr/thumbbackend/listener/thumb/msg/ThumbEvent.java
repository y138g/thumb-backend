package com.itgr.thumbbackend.listener.thumb.msg;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 点赞消息事件实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThumbEvent implements Serializable {

    /**
     * 点赞用户id
     */
    private Long userId;

    /**
     * 博客id
     */
    private Long blogId;

    /**
     * 事件类型
     */
    private EventType type;

    /**
     * 事件发生时间
     */
    private LocalDateTime eventTime;

    /**
     * 事件类型枚举
     */
    public enum EventType {
        /**
         * 点赞
         */
        INCR,

        /**
         * 取消点赞
         */
        DECR
    }

    @Serial
    private static final long serialVersionUID = 1L;
}
