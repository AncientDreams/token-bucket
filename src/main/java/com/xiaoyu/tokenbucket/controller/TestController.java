package com.xiaoyu.tokenbucket.controller;

import com.xiaoyu.tokenbucket.limit.Bucket;
import com.xiaoyu.tokenbucket.limit.TokenBucket;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *
 * </p>
 *
 * @author ZhangXianYu
 * @since 2023-02-27 10:26
 */
@RestController
public class TestController {

    private final TokenBucket tokenBucket;

    public TestController(@Qualifier(value = "redisTokenBucket") TokenBucket tokenBucket) {
        this.tokenBucket = tokenBucket;
    }

    /**
     * 测试接口
     *
     * 可根据自己的场景自定义 拦截
     * 1. 拦截器拦截所有接口并且限流
     * 2.拦截器根据url可配置化限流，针对接口的限流方式。拦截器获取到uri后再读取配置中接口的参数创建
     * bucket对象，然后调用bucket
     *
     * bucket实例的key理应是接口uri
     */
    @RequestMapping("/test")
    public String get() throws Exception {
        Bucket bucket = new Bucket();
        bucket.setBucketMaxCapacity(2);
        bucket.setKey("/test");
        bucket.setPutSpeed(1);
        tokenBucket.createTokenBucket(bucket);
        return tokenBucket.getToken(bucket.getKey()) ? "success" : "fail";
    }
}
