package comcn.lucky.morning.example.redislock.service.impl;

import comcn.lucky.morning.example.redislock.service.RedisLockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * RedisLockServiceImpl
 *
 * @author wangchen
 * @group comcn.lucky.morning.example.redislock.service.impl
 * @date 2021/10/11 17:43
 */
@Service
public class RedisLockServiceImpl implements RedisLockService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean lock(String key) {
        long threadId = Thread.currentThread().getId();
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, this.getNameByThreadId(threadId), 30000, TimeUnit.MILLISECONDS));
    }

    @Override
    public boolean unlock(String key) {
        long threadId = Thread.currentThread().getId();
        String rName = redisTemplate.opsForValue().get(key);
        if (rName == null) {
            throw new RuntimeException("锁不存在");
        }
        String name = this.getNameByThreadId(threadId);
        if (name.equals(rName)) {
            return Boolean.TRUE.equals(redisTemplate.delete(key));
        }
        return false;
    }

    @Override
    public String getNameByThreadId(long threadId) {
        return TARGET + ":" + threadId;
    }
}
