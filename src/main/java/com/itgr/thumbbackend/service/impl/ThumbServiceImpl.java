package com.itgr.thumbbackend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itgr.thumbbackend.constant.ThumbConstant;
import com.itgr.thumbbackend.exception.ErrorCode;
import com.itgr.thumbbackend.exception.ThrowUtils;
import com.itgr.thumbbackend.mapper.ThumbMapper;
import com.itgr.thumbbackend.model.dto.thumb.DoThumbRequest;
import com.itgr.thumbbackend.model.empty.Blog;
import com.itgr.thumbbackend.model.empty.Thumb;
import com.itgr.thumbbackend.model.empty.User;
import com.itgr.thumbbackend.service.BlogService;
import com.itgr.thumbbackend.service.ThumbService;
import com.itgr.thumbbackend.service.UserService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class ThumbServiceImpl extends ServiceImpl<ThumbMapper, Thumb> implements ThumbService {

    @Resource
    private UserService userService;

    @Resource
    private BlogService blogService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Boolean doThumb(DoThumbRequest doThumbRequest) {
        ThrowUtils.throwIf(doThumbRequest == null || doThumbRequest.getBlogId() == null,
                ErrorCode.NOT_FOUND_ERROR, "参数错误");
        long loginUserId = StpUtil.getLoginIdAsLong();
        User loginUser = userService.getById(loginUserId);
        // 加锁
        synchronized (loginUser.getId().toString().intern()) {
            // 编程式事务
            return transactionTemplate.execute(status -> {
                Long blogId = doThumbRequest.getBlogId();
                // 从 redis 中查询
                Boolean exists = this.hasThumb(blogId, loginUserId);
                // 从数据库中查询
//                boolean exists = this.lambdaQuery()
//                        .eq(Thumb::getUserId, loginUser.getId())
//                        .eq(Thumb::getBlogId, blogId)
//                        .exists();
                ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "用户已点赞");
                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumbCount = thumbCount + 1")
                        .update();
                Thumb thumb = new Thumb();
                thumb.setUserId(loginUser.getId());
                thumb.setBlogId(blogId);
                // 更新成功才执行
                boolean result = update && this.save(thumb);
                // 更新 redis
                if (result) redisTemplate.opsForHash().put(ThumbConstant.USER_THUMB_KEY_PREFIX + loginUserId,
                        blogId.toString(), thumb.getId());
                return result;
            });
        }
    }

    @Override
    public Boolean unDoThumb(DoThumbRequest doThumbRequest) {
        ThrowUtils.throwIf(doThumbRequest == null || doThumbRequest.getBlogId() == null,
                ErrorCode.NOT_FOUND_ERROR, "参数错误");
        long loginUserId = StpUtil.getLoginIdAsLong();
        User loginUser = userService.getById(loginUserId);
        // 加锁
        synchronized (loginUser.getId().toString().intern()) {
            // 编程式事务
            return transactionTemplate.execute(status -> {
                Long blogId = doThumbRequest.getBlogId();
                // 从 redis 中查询
                Object thumbIdObj = redisTemplate.opsForHash().
                        get(ThumbConstant.USER_THUMB_KEY_PREFIX + loginUser.getId().toString(), blogId.toString());
                // 从数据库查询
//                Thumb exists = this.lambdaQuery()
//                        .eq(Thumb::getUserId, loginUser.getId())
//                        .eq(Thumb::getBlogId, blogId)
//                        .one();
                ThrowUtils.throwIf(thumbIdObj == null, ErrorCode.OPERATION_ERROR, "用户未点赞");
                Long thumbId = Long.valueOf(thumbIdObj.toString());
                boolean update = blogService.lambdaUpdate()
                        .eq(Blog::getId, blogId)
                        .setSql("thumbCount = thumbCount - 1")
                        .update();
                boolean result = update && this.removeById(thumbId);
                // 点赞记录从 Redis 删除
                if (result) redisTemplate.opsForHash().delete(ThumbConstant.USER_THUMB_KEY_PREFIX +
                        loginUser.getId(), blogId.toString());
                return result;
            });
        }
    }

    @Override
    public Boolean hasThumb(Long blogId, Long userId) {
        return redisTemplate.opsForHash().hasKey(ThumbConstant.USER_THUMB_KEY_PREFIX + userId, blogId.toString());
    }
}
