package com.example.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scripting.support.ResourceScriptSource;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * redis 限速配置
 * copy from ...
 * spring-cloud-gateway-core
 * 2.1.2.RELEASE
 *
 * @author wujing
 * @since 2019/9/18 10:15
 */
@Slf4j
@Configuration
public class RedisLimiter {

    @Autowired
    private RedisTemplate<String, List> redisTemplate;

    @Bean
    public RedisTemplate<String, List> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, List> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setKeySerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    static List<String> getKeys(String id) {
        String prefix = "request_rate_limiter.{" + id;
        String tokenKey = prefix + "}.tokens";
        String timestampKey = prefix + "}.timestamp";
        return Arrays.asList(tokenKey, timestampKey);
    }

    static RedisScript<List> redisScript() {
        DefaultRedisScript<List> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis_limit.lua")));
        redisScript.setResultType(List.class);
        return redisScript;
    }

    @SuppressWarnings("unchecked")
    public RateLimitResult allowed(Long replenishRate, Long burstCapacity, String key) {

        List<Long> execute = redisTemplate.execute(redisScript(), getKeys(key),
                replenishRate, burstCapacity, Instant.now().getEpochSecond(), 1);

        if (execute != null) {
            return new RateLimitResult(execute.get(0) == 1L, execute.get(1));
        }
        return new RateLimitResult(false, -1L);
    }

}
