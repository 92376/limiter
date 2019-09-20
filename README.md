# README

## 1. 引入依赖

```xml
<!-- 令牌桶依赖 -->
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>4.4.1</version>
</dependency>
<!-- redis依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<!-- 自定义yml属性提示 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-configuration-processor</artifactId>
    <optional>true</optional>
</dependency>
<!-- lombok -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

## 2. application.yml配置

```yaml
rate:
  limit:
    enable: true
    burst-capacity: 6
    replenish-rate: 2
    # memory与redis
#    impl: redis
```

## 3. redis_limit.lua脚本

>  从spring cloud gateway 2.1.2.RELEASE拷贝

```lua
local tokens_key = KEYS[1]
local timestamp_key = KEYS[2]
--redis.log(redis.LOG_WARNING, "tokens_key " .. tokens_key)

local rate = tonumber(ARGV[1])
local capacity = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local requested = tonumber(ARGV[4])

local fill_time = capacity/rate
local ttl = math.floor(fill_time*2)

--redis.log(redis.LOG_WARNING, "rate " .. ARGV[1])
--redis.log(redis.LOG_WARNING, "capacity " .. ARGV[2])
--redis.log(redis.LOG_WARNING, "now " .. ARGV[3])
--redis.log(redis.LOG_WARNING, "requested " .. ARGV[4])
--redis.log(redis.LOG_WARNING, "filltime " .. fill_time)
--redis.log(redis.LOG_WARNING, "ttl " .. ttl)

local last_tokens = tonumber(redis.call("get", tokens_key))
if last_tokens == nil then
  last_tokens = capacity
end
--redis.log(redis.LOG_WARNING, "last_tokens " .. last_tokens)

local last_refreshed = tonumber(redis.call("get", timestamp_key))
if last_refreshed == nil then
  last_refreshed = 0
end
--redis.log(redis.LOG_WARNING, "last_refreshed " .. last_refreshed)

local delta = math.max(0, now-last_refreshed)
local filled_tokens = math.min(capacity, last_tokens+(delta*rate))
local allowed = filled_tokens >= requested
local new_tokens = filled_tokens
local allowed_num = 0
if allowed then
  new_tokens = filled_tokens - requested
  allowed_num = 1
end

--redis.log(redis.LOG_WARNING, "delta " .. delta)
--redis.log(redis.LOG_WARNING, "filled_tokens " .. filled_tokens)
--redis.log(redis.LOG_WARNING, "allowed_num " .. allowed_num)
--redis.log(redis.LOG_WARNING, "new_tokens " .. new_tokens)

redis.call("setex", tokens_key, ttl, new_tokens)
redis.call("setex", timestamp_key, ttl, now)

return { allowed_num, new_tokens }

```

## 4. Filter使用示例

>  **注意: ** 依赖的核心实现代码在config包下

```java
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
```

## 5. 测试接口

```java
@RestController
@RequestMapping("/limit")
public class LimitController {

    @GetMapping("test")
    public String test() {

        return "test";
    }

}
```

## 6. 使用jmeter测试

>  3秒内请求20次

```http
GET http://localhost:8080/limit/test
```

>  memory输出结果(填充速度 >>> 匀速)

```
允许5
允许4
允许3
允许2
允许2
允许1
允许0
允许0
限制0
限制0
限制0
允许0
限制0
限制0
允许0
限制0
限制0
允许0
限制0
限制0
```

>  redis输出结果(填充速度 >>> 一次性)

```
允许5
允许3
允许4
允许4
允许3
允许2
允许1
允许0
限制0
允许1
允许0
限制0
限制0
限制0
限制0
允许1
允许0
限制0
限制0
限制0
```

