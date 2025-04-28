package com.itgr.thumbbackend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itgr.thumbbackend.constant.RedisLuaScriptConstant;
import com.itgr.thumbbackend.exception.ErrorCode;
import com.itgr.thumbbackend.exception.ThrowUtils;
import com.itgr.thumbbackend.listener.thumb.msg.ThumbEvent;
import com.itgr.thumbbackend.mapper.ThumbMapper;
import com.itgr.thumbbackend.model.dto.thumb.DoThumbRequest;
import com.itgr.thumbbackend.model.empty.Thumb;
import com.itgr.thumbbackend.model.enums.LuaStatusEnum;
import com.itgr.thumbbackend.service.ThumbService;
import com.itgr.thumbbackend.util.RedisKeyUtil;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.pulsar.core.PulsarTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service("thumbService")
@Slf4j
@RequiredArgsConstructor
@Primary
public class ThumbServiceMQImpl extends ServiceImpl<ThumbMapper, Thumb> implements ThumbService {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private PulsarTemplate<ThumbEvent> pulsarTemplate;

    @Override
    public Boolean doThumb(DoThumbRequest doThumbRequest) {
        ThrowUtils.throwIf(doThumbRequest == null || doThumbRequest.getBlogId() == null,
                ErrorCode.NOT_FOUND_ERROR, "参数错误");
        Long blogId = doThumbRequest.getBlogId();
        long loginUserId = StpUtil.getLoginIdAsLong();
        String userThumbKey = RedisKeyUtil.getUserThumbKey(loginUserId);
        // 执行 Lua 脚本
        Long result = redisTemplate.execute(
                RedisLuaScriptConstant.THUMB_SCRIPT_MQ,
                List.of(userThumbKey),
                blogId
        );
        ThrowUtils.throwIf(Objects.equals(result, LuaStatusEnum.FAIL.getValue()),
                ErrorCode.OPERATION_ERROR, "点赞失败");
        // 构造点赞事件消息
        ThumbEvent thumbEvent = ThumbEvent.builder()
                .blogId(blogId)
                .userId(loginUserId)
                .type(ThumbEvent.EventType.INCR)
                .eventTime(LocalDateTime.now())
                .build();
        // 异步发送点赞事件消息
        pulsarTemplate.sendAsync("thumb-topic", thumbEvent).exceptionally(ex -> {
            redisTemplate.opsForHash().delete(userThumbKey, blogId, true);
            log.error("点赞事件消息发送失败：userId{}，blogId={}", loginUserId, blogId, ex);
            return null;
        });
        return true;
    }

    @Override
    public Boolean unDoThumb(DoThumbRequest doThumbRequest) {
        ThrowUtils.throwIf(doThumbRequest == null || doThumbRequest.getBlogId() == null,
                ErrorCode.NOT_FOUND_ERROR, "参数错误");
        Long blogId = doThumbRequest.getBlogId();
        long loginUserId = StpUtil.getLoginIdAsLong();
        String userThumbKey = RedisKeyUtil.getUserThumbKey(loginUserId);
        // 执行 Lua 脚本
        Long result = redisTemplate.execute(
                RedisLuaScriptConstant.UNTHUMB_SCRIPT_MQ,
                List.of(userThumbKey),
                blogId
        );
        ThrowUtils.throwIf(Objects.equals(result, LuaStatusEnum.FAIL.getValue()),
                ErrorCode.OPERATION_ERROR, "取消点赞失败");
        // 构造取消点赞事件消息
        ThumbEvent thumbEvent = ThumbEvent.builder()
                .userId(loginUserId)
                .blogId(blogId)
                .type(ThumbEvent.EventType.DECR)
                .eventTime(LocalDateTime.now())
                .build();
        // 异步发送取消点赞事件消息
        pulsarTemplate.sendAsync("thumb-topic", thumbEvent).exceptionally(ex -> {
            redisTemplate.opsForHash().put(userThumbKey, blogId, true);
            log.error("取消点赞事件消息发送失败：userId{}，blogId={}", loginUserId, blogId, ex);
            return null;
        });
        return true;
    }

    @Override
    public Boolean hasThumb(Long blogId, Long userId) {
        return redisTemplate.opsForHash().hasKey(RedisKeyUtil.getUserThumbKey(userId), blogId.toString());
    }
}
