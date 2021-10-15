package comcn.lucky.morning.example.redislock.service.impl;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import comcn.lucky.morning.example.redislock.service.RedisLockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * RedisLockServiceImpl
 * 使用lua脚本加锁解锁
 *
 * @author wangchen
 * @group comcn.lucky.morning.example.redislock.service.impl
 * @date 2021/10/11 17:43
 */
@Service
public class RedisLockLuaServiceImpl implements RedisLockService {
    /**
     * 加锁lua脚本
     */
    private static final String SCRIPT_SET_ABSENT_WITH_EXPIRE = "return redis.call('set',KEYS[1],ARGV[1],'nx','px',ARGV[2])";

    private static final DefaultRedisScript<String> REDIS_SCRIPT_SET_ABSENT_WITH_EXPIRE = new DefaultRedisScript<>(SCRIPT_SET_ABSENT_WITH_EXPIRE, String.class);

    /**
     * 解锁lua脚本 脚本中判断解锁只能被加锁者成功执行
     */
    private static final String SCRIPT_REMOVE_KEY =
            "if(redis.call('get',KEYS[1]) == ARGV[1]) " +
                    "then " +
                    "if(redis.call('del',KEYS[1])==1) then return 'OK' else return (nil) end " +
                    "else " +
                    "return (nil) " +
                    "end";

    private static final DefaultRedisScript<String> REDIS_SCRIPT_REMOVE_KEY = new DefaultRedisScript<>(SCRIPT_REMOVE_KEY, String.class);

    private static final String SUCCESS = "OK";

    private static final String ERROR = "(error)";

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
            if (this.lockLua(key, value)) {
                return;
            }

            perCheckTime = this.calePerCheckTime(perCheckTime);
            ThreadUtil.safeSleep(perCheckTime);
        }
    }

    private boolean lockLua(String key, String value) {
        String result = redisTemplate.execute(REDIS_SCRIPT_SET_ABSENT_WITH_EXPIRE, Collections.singletonList(key), value, String.valueOf(TIME_OUT));
        if (StrUtil.equals(result, SUCCESS)) {
            return true;
        } else if (StrUtil.startWith(result, ERROR)) {
            throw new RuntimeException(result);
        }
        return false;
    }

    @Override
    public boolean unlock(String key) {
        long threadId = Thread.currentThread().getId();
        String value = this.getNameByThreadId(threadId);
        String result = redisTemplate.execute(REDIS_SCRIPT_REMOVE_KEY, Collections.singletonList(key), value);
        if (StrUtil.equals(result, SUCCESS)) {
            return true;
        } else if (StrUtil.startWith(result, ERROR)) {
            throw new RuntimeException(result);
        }
        return false;
    }
}
