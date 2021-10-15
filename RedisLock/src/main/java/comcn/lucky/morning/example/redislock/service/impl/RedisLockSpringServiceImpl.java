package comcn.lucky.morning.example.redislock.service.impl;

import cn.hutool.core.thread.ThreadUtil;
import comcn.lucky.morning.example.redislock.service.RedisLockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * RedisLockSpringServiceImpl
 * 使用StringRedisTemplate.setIfAbsent 加锁 等同与 redis命令: set key value nx px milliseconds
 *
 * @author wangchen
 * @group comcn.lucky.morning.example.redislock.service.impl
 * @date 2021/10/15 15:55
 */
@Service
public class RedisLockSpringServiceImpl implements RedisLockService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void lock(String key) {
        this.lock(key, DEFAULT_PER_CHECK_TIME);
    }

    @Override
    public void lock(String key, int perCheckTime) {

        long threadId = Thread.currentThread().getId();
        String value = this.getNameByThreadId(threadId);

        while (true) {
            if (Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value, TIME_OUT, TimeUnit.MILLISECONDS))) {
                return;
            }
            perCheckTime = this.calePerCheckTime(perCheckTime);
            ThreadUtil.safeSleep(perCheckTime);
        }
    }

    @Override
    public boolean unlock(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }
}
