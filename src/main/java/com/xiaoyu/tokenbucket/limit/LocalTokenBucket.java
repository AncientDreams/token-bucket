package com.xiaoyu.tokenbucket.limit;


import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * 本地令牌桶
 * </p>
 *
 * @author ZhangXianYu
 * @since 2023-02-25 12:05
 */
@Service
public class LocalTokenBucket extends AbstactTokenBucket {

    /**
     * 本地令牌桶存储Map
     * key-令牌key
     * value - 令牌容量
     */
    private final Map<String, Bucket> BUCKET_MAP = new ConcurrentHashMap<>(8);


    @Override
    public synchronized boolean decreaseCapacity(String key) throws Exception {
        Bucket bucket = this.getNowCapacity(key);
        return bucket != null;
    }

    @Override
    public Bucket getTokenBucket(String key) throws Exception {
        Bucket bucket = BUCKET_MAP.get(key);
        if (bucket == null) {
            throw new Exception("not find bucket config,key:" + key);
        }
        return bucket;
    }

    @Override
    public void createTokenBucket(Bucket bucket) {
        if (!BUCKET_MAP.containsKey(bucket.getKey())) {
            BUCKET_MAP.put(bucket.getKey(), bucket);
        }
    }
}
