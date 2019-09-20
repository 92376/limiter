package com.example.demo.filter;

import com.example.demo.config.RateLimitConfig;
import com.example.demo.config.RateLimitResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 过滤器
 *
 * @author wujing
 * @since 2019/9/17 17:55
 */
@Slf4j
@WebFilter("/**")
@Configuration
public class LimitFilter implements Filter {

    private final RateLimitConfig rateLimitConfig;

    public LimitFilter(RateLimitConfig rateLimitConfig) {
        this.rateLimitConfig = rateLimitConfig;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filter) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        // 限速依据
        String key = request.getRequestURI();

        RateLimitResult result = rateLimitConfig.getLimitResult(key);

        if (result == null || result.getAllowed()) {
            log.info("允许{}", result == null ? "" : result.getTokensLeft());
            filter.doFilter(servletRequest, servletResponse);
        } else {
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            log.info("限制{}", result.getTokensLeft());
        }

    }

}
