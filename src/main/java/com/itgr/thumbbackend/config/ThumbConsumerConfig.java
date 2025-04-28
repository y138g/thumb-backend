package com.itgr.thumbbackend.config;

import org.apache.pulsar.client.api.BatchReceivePolicy;
import org.apache.pulsar.client.api.ConsumerBuilder;
import org.apache.pulsar.client.api.DeadLetterPolicy;
import org.apache.pulsar.client.api.RedeliveryBackoff;
import org.apache.pulsar.client.impl.MultiplierRedeliveryBackoff;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.pulsar.annotation.PulsarListenerConsumerBuilderCustomizer;

import java.util.concurrent.TimeUnit;

/**
 * 批量处理策略配置
 *
 * @param <T> 泛型
 */
@Configuration
public class ThumbConsumerConfig<T> implements PulsarListenerConsumerBuilderCustomizer<T> {

    @Override
    public void customize(ConsumerBuilder<T> consumerBuilder) {
        consumerBuilder.batchReceivePolicy(
                BatchReceivePolicy.builder()
                        // 每次处理 1000 条消息
                        .maxNumBytes(1000)
                        // 设置超时时间（单位：毫秒）
                        .timeout(10000, TimeUnit.MILLISECONDS)
                        .build()
        );
    }

    /**
     * NACK 重试策略
     *
     * @return RedeliveryBackoff
     */
    @Bean
    public RedeliveryBackoff negativeAckRedeliveryBackoff() {
        return MultiplierRedeliveryBackoff.builder()
                // 初始延迟时间：1s
                .minDelayMs(1000)
                // 最大延迟时间：60s
                .maxDelayMs(60_000)
                // 每次重试延迟倍数：2倍
                .multiplier(2)
                .build();
    }

    /**
     * ACK 超时重试策略
     *
     * @return RedeliveryBackoff
     */
    @Bean
    public RedeliveryBackoff ackTimeoutRedeliveryBackoff() {
        return MultiplierRedeliveryBackoff.builder()
                // 初始延迟时间：5s
                .minDelayMs(5000)
                // 最大延迟时间：5分钟
                .maxDelayMs(300_000)
                // 每次重试延迟倍数：3倍
                .multiplier(3)
                .build();
    }

    /**
     * 死信队列策略
     *
     * @return DeadLetterPolicy
     */
    @Bean
    public DeadLetterPolicy deadLetterPolicy() {
        return DeadLetterPolicy.builder()
                // 最大重试次数
                .maxRedeliverCount(3)
                // 死信主题
                .deadLetterTopic("thumb-dlq-topic")
                .build();
    }
}
