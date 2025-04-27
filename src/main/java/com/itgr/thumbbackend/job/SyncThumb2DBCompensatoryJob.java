package com.itgr.thumbbackend.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.itgr.thumbbackend.constant.ThumbConstant;
import com.itgr.thumbbackend.util.RedisKeyUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * 定时任务补偿机制：
 * 当定时任务超过10s后，会执行下一个定时任务，导致前一个定时任务未完成，故需要有补偿机制
 */
@Component
@Slf4j
public class SyncThumb2DBCompensatoryJob {

    @Resource
    private SyncThumb2DBJob syncThumb2DBJob;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Scheduled(cron = "0 0 2 * * *")
    public void run() {
        // 记录开始补偿数据
        log.info("开始补偿数据");
        // 获取所有以临时缩略图key开头的key
        Set<String> thumbKeys = redisTemplate.keys(RedisKeyUtil.getTempThumbKey("") + "*");
        // 需要处理的数据集合
        Set<String> needHandleDateSet = new HashSet<>();
        // 断言thumbKeys不为空
        assert thumbKeys != null;
        // 遍历thumbKeys
        thumbKeys.stream()
                // 过滤掉为空的key
                .filter(ObjUtil::isNotNull)
                // 将key中的日期部分添加到needHandleDateSet中
                .forEach(thumbKey -> needHandleDateSet.add(thumbKey
                        .replace(ThumbConstant.TEMP_THUMB_KEY_PREFIX.formatted(""), "")));
        // 如果没有需要补偿的数据，记录日志并返回
        if (CollUtil.isEmpty(needHandleDateSet)) {
            log.info("没有需要补偿的数据");
            return;
        }
        // 遍历needHandleDateSet，调用syncThumb2DBJob.syncThumb2DBDate方法进行数据补偿
        for (String date : needHandleDateSet) syncThumb2DBJob.syncThumb2DBDate(date);
        // 记录临时数据补偿完成
        log.info("临时数据补偿完成");
    }
}
