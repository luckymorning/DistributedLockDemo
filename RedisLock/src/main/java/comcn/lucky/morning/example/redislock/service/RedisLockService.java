package comcn.lucky.morning.example.redislock.service;

import cn.hutool.core.util.RandomUtil;

import java.util.UUID;

/**
 * RedisLockService
 *
 * @author wangchen
 * @group comcn.lucky.morning.example.redislock.service
 * @date 2021/10/11 17:42
 */
public interface RedisLockService {
    String TARGET = UUID.randomUUID().toString();

    int TIME_OUT = 30000;

    int MIN_PER = 5;

    int DEFAULT_PER_CHECK_TIME = 10;

    /**
     * 加锁
     *
     * @param key 锁名
     */
    void lock(String key);

    /**
     * 加锁
     *
     * @param key          锁名
     * @param perCheckTime 加锁等待间隔
     */
    void lock(String key, int perCheckTime);

    /**
     * 解锁
     *
     * @param key 锁名
     * @return 是否成功
     */
    boolean unlock(String key);

    /**
     * 通过线程ID获取标记值
     *
     * @param threadId 线程ID
     * @return 名称
     */
    default String getNameByThreadId(long threadId) {
        return TARGET + ":" + threadId;
    }

    default int calePerCheckTime(int perCheckTime) {
        if (perCheckTime >= MIN_PER) {
            int optimizeValue = (int) (perCheckTime * 0.1);
            if (optimizeValue < 1) {
                optimizeValue = 1;
                perCheckTime = perCheckTime + 1;
            }
            perCheckTime = RandomUtil.randomInt(perCheckTime - optimizeValue, perCheckTime + optimizeValue);
        }
        return perCheckTime;
    }
}
