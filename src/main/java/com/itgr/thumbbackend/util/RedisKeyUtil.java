package com.itgr.thumbbackend.util;

import com.itgr.thumbbackend.constant.ThumbConstant;

public class RedisKeyUtil {

    /**
     * 获取用户点赞的key
     *
     * @param userId 用户id
     * @return 前缀+userId：thumb:{userId}
     */
    public static String getUserThumbKey(Long userId) {
        return ThumbConstant.USER_THUMB_KEY_PREFIX + userId;
    }

    /**
     * 获取临时点赞的key
     *
     * @param time 时间戳
     * @return 前缀+时间戳：thumb:{time}
     */
    public static String getTempThumbKey(String time) {
        return ThumbConstant.TEMP_THUMB_KEY_PREFIX.formatted(time);
    }
}

