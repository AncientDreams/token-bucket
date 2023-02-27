package com.xiaoyu.tokenbucket.limit;

/**
 * <p>
 * 令牌桶限流算法核心接口
 * </p>
 *
 * @author ZhangXianYu
 * @since 2023-02-25 14:05
 */
public interface TokenBucket {

    /**
     * 通过令牌桶key获取令牌
     *
     * @param key 令牌桶key
     * @return 是否成功获取
     * @throws Exception 令牌桶不存在抛出异常
     */
    boolean getToken(String key) throws Exception;

    /**
     * 创建令牌桶
     *
     * @param bucket 令牌桶对象
     */
    void createTokenBucket(Bucket bucket);
}
