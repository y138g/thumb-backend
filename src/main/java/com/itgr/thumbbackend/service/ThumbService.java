package com.itgr.thumbbackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itgr.thumbbackend.model.dto.thumb.DoThumbRequest;
import com.itgr.thumbbackend.model.empty.Thumb;

/**
 * @author ygking
 * @description 针对表【thumb】的数据库操作Service
 * @createDate 2025-04-26 18:22:16
 */
public interface ThumbService extends IService<Thumb> {

    /**
     * 点赞
     *
     * @param doThumbRequest 点赞请求
     * @return 是否成功
     */
    Boolean doThumb(DoThumbRequest doThumbRequest);

    /**
     * 取消点赞
     *
     * @param doThumbRequest 取消点赞请求
     * @return 是否成功
     */
    Boolean unDoThumb(DoThumbRequest doThumbRequest);
}
