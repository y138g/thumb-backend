package com.itgr.thumbbackend.listener.thumb;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itgr.thumbbackend.listener.thumb.msg.ThumbEvent;
import com.itgr.thumbbackend.mapper.BlogMapper;
import com.itgr.thumbbackend.model.empty.Thumb;
import com.itgr.thumbbackend.service.impl.ThumbServiceMQImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.MessageId;
import org.apache.pulsar.client.api.SubscriptionType;
import org.apache.pulsar.common.schema.SchemaType;
import org.springframework.pulsar.annotation.PulsarListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 点赞消息消费者
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ThumbConsumer {

    // 注入点赞服务，这种注入方式保证了线程安全
    private final ThumbServiceMQImpl thumbService;

    private final BlogMapper blogMapper;

    /**
     * 消费死信队列
     *
     * @param message 死信
     */
    @PulsarListener(topics = "thumb-dlq-topic")
    public void consumerDlq(Message<ThumbEvent> message) {
        MessageId messageId = message.getMessageId();
        log.info("dlq message: {}", messageId);
        log.info("消息 {} 已入库", messageId);
        log.info("已通知相关人员 {} 处理消息 {} ", "帅哥", messageId);
    }

    /**
     * 批量消费
     *
     * @param messages 消息列表
     */
    @PulsarListener(
            subscriptionName = "thumb-subscription", // 订阅名称
            topics = "thumb-topic", // 主题
            schemaType = SchemaType.JSON, // 消息类型
            batch = true, // 批量消费
            consumerCustomizer = "thumbConsumerConfig", // 消费者配置
            negativeAckRedeliveryBackoff = "negativeAckRedeliveryBackoff", // 引用 NACK 重试策略
            ackTimeoutRedeliveryBackoff = "ackTimeoutRedeliveryBackoff", // 应用 ACK 超时重试策略
            subscriptionType = SubscriptionType.Shared, // 死信仅适用于 shared 类型
            deadLetterPolicy = "deadLetterPolicy" // 引用死信队列策略
    )
    @Transactional(rollbackFor = Exception.class)
    public void processBatch(List<Message<ThumbEvent>> messages) {
        log.info("批量消费点赞消息: {}", messages.size());

        // 模拟异常，需要注释掉
//        messages.forEach(message -> log.info("message.getMessageId() = {}", message.getMessageId()));
//        ThrowUtils.throwIf(true, ErrorCode.SYSTEM_ERROR, "ThumbConsumer processBatch failed");

        // 线程安全的hashmap
        Map<Long, Long> countMap = new ConcurrentHashMap<>();
        List<Thumb> thumbs = new ArrayList<>();

        // 并行处理消息
        LambdaQueryWrapper<Thumb> wrapper = new LambdaQueryWrapper<>();
        // 使用原子封装类做标记
        AtomicReference<Boolean> needRemove = new AtomicReference<>(false);

        // 提取事件
        List<ThumbEvent> events = messages.stream()
                .map(Message::getValue)
                // 过滤无效信息
                .filter(Objects::nonNull)
                .toList();

        // 按（userId，blogId）分组，并获取每个分组的最新事件
        Map<Pair<Long, Long>, ThumbEvent> latestEvent = events.stream().collect(Collectors.groupingBy(
                e -> Pair.of(e.getUserId(), e.getBlogId()),
                Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> {
                            list.sort(Comparator.comparing(ThumbEvent::getEventTime));
                            if (list.size() % 2 == 0) return null;
                            return list.getLast();
                        }
                )
        ));

        latestEvent.forEach((userBlogPair, event) -> {
            if (event == null) return;
            ThumbEvent.EventType finalAction = event.getType();

            // 根据事件类型进行操作
            if (finalAction == ThumbEvent.EventType.INCR) {
                countMap.merge(event.getBlogId(), 1L, Long::sum);
                Thumb thumb = new Thumb();
                thumb.setBlogId(event.getBlogId());
                thumb.setUserId(event.getUserId());
                thumbs.add(thumb);
            } else {
                needRemove.set(true);
                wrapper.or()
                        .eq(Thumb::getUserId, event.getUserId())
                        .eq(Thumb::getBlogId, event.getBlogId());
                countMap.merge(event.getBlogId(), -1L, Long::sum);
            }
        });

        if (needRemove.get()) thumbService.remove(wrapper);
        batchUpdateBlogs(countMap);
        batchSaveThumbs(thumbs);
    }

    /**
     * 批量更新点赞数
     *
     * @param countMap 点赞数map
     */
    public void batchUpdateBlogs(Map<Long, Long> countMap) {
        if (!countMap.isEmpty()) blogMapper.batchUpdateThumbCount(countMap);
    }

    /**
     * 批量保存点赞记录
     *
     * @param thumbs 点赞记录列表
     */
    public void batchSaveThumbs(List<Thumb> thumbs) {
        if (!thumbs.isEmpty()) thumbService.saveBatch(thumbs, 500);
    }
}
