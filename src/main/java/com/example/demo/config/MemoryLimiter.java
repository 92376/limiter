package com.example.demo.config;

import io.github.bucket4j.*;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存限速
 * 依赖 bucket4j-core
 *
 * @author wujing
 * @since 2019/9/18 16:01
 */
public class MemoryLimiter {

    private MemoryLimiter() {}

    private static final Map<String, Bucket> BUCKET_MAP = new ConcurrentHashMap<>();

    public static RateLimitResult allowed(Long replenishRate, Long burstCapacity, String key) {

        Bucket bucket = BUCKET_MAP.computeIfAbsent(key, k -> {
            Refill refill = Refill.greedy(replenishRate, Duration.ofSeconds(1));
            Bandwidth limit = Bandwidth.classic(burstCapacity, refill);
            return Bucket4j.builder().addLimit(limit).build();
        });

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        return new RateLimitResult(probe.isConsumed(), probe.getRemainingTokens());
    }

}
