package comcn.lucky.morning.example.redislock.service;

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

    /**
     * 加锁
     *
     * @param key 锁名
     * @return 是否成功
     */
    boolean lock(String key);

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
    String getNameByThreadId(long threadId);
}
