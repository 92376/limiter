package com.example.demo.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 限速结果
 *
 * @author wujing
 * @since 2019/9/18 14:32
 */
@Getter
@ToString
@AllArgsConstructor
public class RateLimitResult {

    /**
     * 是否允许
     */
    private Boolean allowed;

    /**
     * 桶中令牌剩余
     */
    private Long tokensLeft;

}