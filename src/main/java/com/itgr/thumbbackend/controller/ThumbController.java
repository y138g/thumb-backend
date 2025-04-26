package com.itgr.thumbbackend.controller;

import com.itgr.thumbbackend.common.BaseResponse;
import com.itgr.thumbbackend.common.ResultUtils;
import com.itgr.thumbbackend.model.dto.thumb.DoThumbRequest;
import com.itgr.thumbbackend.service.ThumbService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("thumb")
public class ThumbController {

    @Resource
    private ThumbService thumbService;

    /**
     * 点赞
     *
     * @param doThumbRequest 点赞请求
     * @return 点赞结果
     */
    @PostMapping("/do")
    public BaseResponse<Boolean> doThumb(@RequestBody DoThumbRequest doThumbRequest) {
        Boolean success = thumbService.doThumb(doThumbRequest);
        return ResultUtils.success(success);
    }

    /**
     * 取消点赞
     *
     * @param doThumbRequest 点赞请求
     * @return 取消点赞结果
     */
    @PostMapping("/undo")
    public BaseResponse<Boolean> unDoThumb(@RequestBody DoThumbRequest doThumbRequest) {
        Boolean success = thumbService.unDoThumb(doThumbRequest);
        return ResultUtils.success(success);
    }
}
