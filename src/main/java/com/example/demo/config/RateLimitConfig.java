package com.example.demo.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

/**
 * 限速配置类
 *
 * @author wujing
 * @since 2019/9/18 17:00
 */
@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfig {

    private final RedisLimiter redisLimit;

    private final RateLimitProperties rateLimitProperties;

    public RateLimitConfig(RedisLimiter redisLimit, RateLimitProperties rateLimitProperties) {
        this.redisLimit = redisLimit;
        this.rateLimitProperties = rateLimitProperties;
    }

    /**
     * 限速是否开启，与限速实现方式
     *
     * @param key 限速依据
     * @return 限速结果
     */
    public RateLimitResult getLimitResult(String key) {

        long replenishRate = rateLimitProperties.getReplenishRate();
        long burstCapacity = rateLimitProperties.getBurstCapacity();

        RateLimitResult result = null;

        if (rateLimitProperties.isEnable()) {
            if (Objects.equals(rateLimitProperties.getImpl(), RateLimitProperties.REDIS)) {
                result = redisLimit.allowed(replenishRate, burstCapacity, key);
            } else {
                result = MemoryLimiter.allowed(replenishRate, burstCapacity, key);
            }
        }
        return result;
    }

}
