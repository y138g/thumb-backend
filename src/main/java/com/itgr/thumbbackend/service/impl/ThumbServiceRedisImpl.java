package com.itgr.thumbbackend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itgr.thumbbackend.constant.RedisLuaScriptConstant;
import com.itgr.thumbbackend.exception.ErrorCode;
import com.itgr.thumbbackend.exception.ThrowUtils;
import com.itgr.thumbbackend.mapper.ThumbMapper;
import com.itgr.thumbbackend.model.dto.thumb.DoThumbRequest;
import com.itgr.thumbbackend.model.empty.Thumb;
import com.itgr.thumbbackend.model.enums.LuaStatusEnum;
import com.itgr.thumbbackend.service.ThumbService;
import com.itgr.thumbbackend.util.RedisKeyUtil;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Objects;

@Service("ThumbServiceRedis")
@Slf4j
@RequiredArgsConstructor
public class ThumbServiceRedisImpl extends ServiceImpl<ThumbMapper, Thumb> implements ThumbService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Boolean doThumb(DoThumbRequest doThumbRequest) {
        ThrowUtils.throwIf(doThumbRequest == null || doThumbRequest.getBlogId() == null,
                ErrorCode.NOT_FOUND_ERROR, "参数错误");
        long loginUserId = StpUtil.getLoginIdAsLong();
        Long blogId = doThumbRequest.getBlogId();
        // Redis Key
        String timeSlice = getTimeSlice();
        String tempThumbKey = RedisKeyUtil.getTempThumbKey(timeSlice);
        String userThumbKey = RedisKeyUtil.getUserThumbKey(loginUserId);
        // 执行 Lua 脚本
        Long result = redisTemplate.execute(
                RedisLuaScriptConstant.THUMB_SCRIPT,
                Arrays.asList(tempThumbKey, userThumbKey),
                loginUserId,
                blogId
        );
        ThrowUtils.throwIf(Objects.equals(LuaStatusEnum.FAIL.getValue(), result),
                ErrorCode.OPERATION_ERROR, "用户已点赞");
        return LuaStatusEnum.SUCCESS.getValue().equals(result);
    }

    @Override
    public Boolean unDoThumb(DoThumbRequest doThumbRequest) {
        ThrowUtils.throwIf(doThumbRequest == null || doThumbRequest.getBlogId() == null,
                ErrorCode.NOT_FOUND_ERROR, "参数错误");
        long loginUserId = StpUtil.getLoginIdAsLong();
        Long blogId = doThumbRequest.getBlogId();
        // Redis Key
        String timeSlice = getTimeSlice();
        String tempThumbKey = RedisKeyUtil.getTempThumbKey(timeSlice);
        String userThumbKey = RedisKeyUtil.getUserThumbKey(loginUserId);
        // 执行 Lua 脚本
        Long result = redisTemplate.execute(
                RedisLuaScriptConstant.UNTHUMB_SCRIPT,
                Arrays.asList(tempThumbKey, userThumbKey),
                loginUserId,
                blogId
        );
        ThrowUtils.throwIf(Objects.equals(LuaStatusEnum.FAIL.getValue(), result),
                ErrorCode.OPERATION_ERROR, "用户未点赞");
        return LuaStatusEnum.SUCCESS.getValue().equals(result);
    }

    @Override
    public Boolean hasThumb(Long blogId, Long userId) {
        return redisTemplate.opsForHash().hasKey(RedisKeyUtil.getUserThumbKey(userId), blogId.toString());
    }

    /**
     * 获取当前时间片
     *
     * @return 时间片
     */
    private String getTimeSlice() {
        DateTime date = DateUtil.date();
        // 整秒，统计一段时间的点赞数量
        return DateUtil.format(date, "HH:mm:") + (DateUtil.second(date) / 10) * 10;
    }
}
