package com.itgr.thumbbackend.job;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.StrPool;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itgr.thumbbackend.mapper.BlogMapper;
import com.itgr.thumbbackend.model.empty.Thumb;
import com.itgr.thumbbackend.model.enums.ThumbTypeEnum;
import com.itgr.thumbbackend.service.ThumbService;
import com.itgr.thumbbackend.util.RedisKeyUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 定时任务将 Redis 中临时点赞数据同步到数据库
 */
@Component
@Slf4j
public class SyncThumb2DBJob {

    @Resource
    private ThumbService thumbService;

    @Resource
    private BlogMapper blogMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Scheduled(fixedRate = 10000)
    @Transactional(rollbackFor = Exception.class)
    public void run() {
        log.info("定时任务开始执行");
        DateTime nowDate = DateUtil.date();
        // 秒数为0～9，则回溯到上一分钟的50秒
        int second = (DateUtil.second(nowDate) / 10 - 1) * 10;
        if (second == -10) {
            second = 50;
            nowDate = DateUtil.offsetMinute(nowDate, -1);
        }
        String date = DateUtil.format(nowDate, "HH:mm:") + second;
        syncThumb2DBDate(date);
        log.info("定时任务执行结束，当前时间：{}", date);
    }

    /**
     * 根据日期同步临时点赞数据到数据库
     *
     * @param date 日期
     */
    public void syncThumb2DBDate(String date) {
        // 获取临时点赞key
        String tempThumbKey = RedisKeyUtil.getTempThumbKey(date);
        // 获取所有临时点赞map
        Map<Object, Object> allTempThumbMap = redisTemplate.opsForHash().entries(tempThumbKey);
        boolean thumbMapEmpty = allTempThumbMap.isEmpty();
        // 创建博客点赞数量map
        Map<Long, Long> blogThumbCountMap = new HashMap<>();
        // 如果临时点赞map为空，则直接返回
        if (thumbMapEmpty) return;
        // 创建点赞列表
        ArrayList<Thumb> thumbList = new ArrayList<>();
        // 是否需要删除
        boolean needRemove = false;
        // 创建点赞查询wrapper
        LambdaQueryWrapper<Thumb> wrapper = new LambdaQueryWrapper<>();
        // 遍历临时点赞map
        for (Object userIdBlogIdObj : allTempThumbMap.keySet()) {
            // 获取用户id和博客id
            String userIdBlogId = (String) userIdBlogIdObj;
            String[] userIdAndBlogId = userIdBlogId.split(StrPool.COLON);
            Long userId = Long.valueOf(userIdAndBlogId[0]);
            Long blogId = Long.valueOf(userIdAndBlogId[1]);
            // 获取点赞类型
            Integer thumbType = Integer.valueOf(allTempThumbMap.get(userIdBlogId).toString());
            if (thumbType == ThumbTypeEnum.INCR.getValue()) {
                // 创建点赞对象
                Thumb thumb = new Thumb();
                // 设置用户id和博客id
                thumb.setUserId(userId);
                thumb.setBlogId(blogId);
                // 添加到点赞列表
                thumbList.add(thumb);
            } else if (thumbType == ThumbTypeEnum.DECR.getValue()) {
                // 需要删除
                needRemove = true;
                // 添加到查询wrapper
                wrapper.or().eq(Thumb::getUserId, userId).eq(Thumb::getBlogId, blogId);
            } else {
                if (thumbType != ThumbTypeEnum.NON.getValue())
                    log.warn("数据异常：{}", userId + StrPool.C_COMMA + blogId + StrPool.C_COMMA + thumbType);
                continue;
            }
            // 更新博客点赞数量map
            blogThumbCountMap.put(blogId, blogThumbCountMap.getOrDefault(blogId, 0L) + thumbType);
        }
        // 批量保存点赞列表
        thumbService.saveBatch(thumbList);
        // 如果需要删除，删除点赞
        if (needRemove) thumbService.remove(wrapper);
        // 如果博客点赞数量map不为空，批量更新博客点赞数量
        if (!blogThumbCountMap.isEmpty()) blogMapper.batchUpdateThumbCount(blogThumbCountMap);
        // 启动虚拟线程，异步删除临时点赞key
        Thread.startVirtualThread(() -> {
            redisTemplate.delete(tempThumbKey);
        });
    }
}
