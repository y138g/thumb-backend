package com.itgr.thumbbackend.constant;

/**
 * 点赞常量类
 */
public interface ThumbConstant {

    /**
     * 用户点赞 hash key
     */
    String USER_THUMB_KEY_PREFIX = "thumb:";

    /**
     * 临时点赞key前缀
     */
    String TEMP_THUMB_KEY_PREFIX = "thumb:temp:%s";

    /**
     * 未点赞，防止本地缓存和 redis 缓存数据不一致
     */
    Long UN_THUMB_CONSTANT = 0L;
}
