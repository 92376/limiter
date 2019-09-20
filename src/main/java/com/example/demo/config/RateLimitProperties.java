package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 限速配置
 *
 * @author wujing
 * @since 2019/9/18 15:49
 */
@ConfigurationProperties("rate.limit")
public class RateLimitProperties {

    public static final String MEMORY = "memory";

    public static final String REDIS = "redis";

    /**
     * 初始令牌容量
     */
    private long burstCapacity = 20;

    /**
     * 每秒令牌填充数量
     */
    private long replenishRate = 10;

    /**
     * 开启限速
     */
    private boolean enable = false;

    /**
     * 限速实现方式: memory(内存); redis
     */
    private String impl = MEMORY;

    public long getBurstCapacity() {
        return burstCapacity;
    }

    public void setBurstCapacity(long burstCapacity) {
        this.burstCapacity = burstCapacity;
    }

    public long getReplenishRate() {
        return replenishRate;
    }

    public void setReplenishRate(long replenishRate) {
        this.replenishRate = replenishRate;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String getImpl() {
        return impl;
    }

    public void setImpl(String impl) {
        this.impl = impl;
    }
}
