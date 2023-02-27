package com.xiaoyu.tokenbucket.limit;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Redis 令牌桶
 * </p>
 *
 * @author ZhangXianYu
 * @since 2023-02-25 15:30
 */
@Service
public class RedisTokenBucket extends AbstactTokenBucket {

    private final StringRedisTemplate redisTemplate;

    public RedisTokenBucket(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private final String hashKey = "RedisTokenBucket";

    public boolean decreaseCapacity(String key) {
        boolean lock = lock(hashKey + key);
        try {
            if (!lock) {
                return false;
            }
            Bucket bucket = this.getNowCapacity(key);
            if (bucket == null) {
                return false;
            }
            redisTemplate.opsForHash().put(hashKey, bucket.getKey(), JSON.toJSONString(bucket));
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (lock) {
                unlock(hashKey + key);
            }
        }
    }

    @Override
    public void createTokenBucket(Bucket bucket) {
        Object tokenBucketStr = redisTemplate.opsForHash().get(hashKey, bucket.getKey());
        if (tokenBucketStr == null) {
            redisTemplate.opsForHash().put(hashKey, bucket.getKey(), JSON.toJSONString(bucket));
            return;
        }
        // 变更配置
        Bucket redisBucket = JSONObject.parseObject(tokenBucketStr.toString(), Bucket.class);
        if (redisBucket.getBucketMaxCapacity() != bucket.getBucketMaxCapacity()
                || redisBucket.getPutSpeed() != bucket.getPutSpeed()) {
            redisBucket.setBucketMaxCapacity(bucket.getBucketMaxCapacity());
            redisBucket.setPutSpeed(bucket.getPutSpeed());
            boolean lock = lock(hashKey + bucket.getKey());
            try {
                if (lock) {
                    redisTemplate.opsForHash().put(hashKey, bucket.getKey(), JSON.toJSONString(redisBucket));
                }
            } finally {
                if (lock) {
                    unlock(hashKey + bucket.getKey());
                }
            }

        }
    }


    @Override
    public Bucket getTokenBucket(String key) throws Exception {
        Object tokenBucket = redisTemplate.opsForHash().get(hashKey, key);
        if (tokenBucket == null) {
            throw new Exception("not find bucket config,key:" + key);
        }
        return JSONObject.parseObject(tokenBucket.toString(), Bucket.class);
    }


    /**
     * 加锁
     *
     * @param key 锁名称
     * @return 加锁成功返回true，加锁失败返回false，未拿到锁
     */
    private boolean lock(String key) {
        int timeOut = 5000;
        try {
            long start = System.currentTimeMillis();
            for (; ; ) {
                boolean retryResult = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, String.valueOf(System.currentTimeMillis())));
                if (retryResult) {
                    redisTemplate.expire(key, 5, TimeUnit.SECONDS);
                    return true;
                }
                if ((start + timeOut) > System.currentTimeMillis()) {
                    System.out.println("超时未拿到执行锁");
                    //超时
                    return false;
                }
                Thread.sleep(100);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 解锁
     *
     * @param key 锁名称
     */
    private void unlock(String key) {
        redisTemplate.opsForValue().getOperations().delete(key);
    }
}
