package comcn.lucky.morning.example.redislock;

import comcn.lucky.morning.example.redislock.service.RedisLockService;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class RedisLockApplicationTests {
    private static int storage = 1000;
    private static int maxCoreSize = 1000;

    @Resource(name = "redisLockSpringServiceImpl")
    private RedisLockService redisLockSpringServiceImpl;

    @Resource(name = "redisLockLuaServiceImpl")
    private RedisLockService redisLockLuaServiceImpl;

    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void execute() {
        redisLockLuaServiceImpl.lock("lock");
    }

    @Test
    public void testLock() {
        String key = "lock:key";
        redisLockLuaServiceImpl.lock(key);
        System.out.println("加锁成功");
        if (redisLockLuaServiceImpl.unlock(key)) {
            System.out.println("解锁成功");
        } else {
            System.out.println("解锁失败！");
        }
    }

    @Test
    public void bugGoodsNoLock() throws InterruptedException {
        int maxCount = storage;
        ThreadPoolExecutor executor = new ThreadPoolExecutor(maxCoreSize, maxCoreSize, 1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(maxCount));
        CountDownLatch latch = new CountDownLatch(maxCount);

        for (int index = 0; index < maxCount; index++) {
            executor.execute(() -> {
                storage = storage - 1;
                System.out.println("减库存成功：" + storage);
                latch.countDown();
            });
        }
        latch.await();
        System.out.println("执行完毕,当前库存：" + storage);
    }

    @Test
    void bugGoodsOnCustomLock() throws InterruptedException {
        String redisKeyPre = "lock:redis:";
        String key = redisKeyPre + "1";
        int maxCount = storage;
        ThreadPoolExecutor executor = new ThreadPoolExecutor(maxCoreSize, maxCoreSize, 1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(maxCount));
        CountDownLatch latch = new CountDownLatch(maxCount);

        long start = System.currentTimeMillis();
        for (int index = 0; index < maxCount; index++) {
            executor.execute(() -> {
                try {
                    redisLockLuaServiceImpl.lock(key);
                    storage = storage - 1;
                    System.out.println("减库存成功：" + storage);
                } finally {
                    latch.countDown();
                    if (!redisLockLuaServiceImpl.unlock(key)) {
                        System.out.println("解锁失败！");
                    }
                }
            });
        }
        latch.await();
        System.out.println("执行完毕,当前库存：" + storage);
        System.out.println("耗时：" + (System.currentTimeMillis() - start));
    }

    @Test
    public void bugGoodsOnRedissonLock() throws InterruptedException {
        String redisKeyPre = "lock:redis:";
        String key = redisKeyPre + "1";
        int maxCount = storage;
        ThreadPoolExecutor executor = new ThreadPoolExecutor(maxCoreSize, maxCoreSize, 1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(maxCount));
        CountDownLatch latch = new CountDownLatch(maxCount);

        long start = System.currentTimeMillis();
        for (int index = 0; index < maxCount; index++) {
            executor.execute(() -> {
                RLock lock = null;
                try {
                    lock = redissonClient.getLock(key);
                    lock.lock();
                    storage = storage - 1;
                    System.out.println("减库存成功：" + storage);
                } finally {
                    if (lock != null && lock.isLocked()) {
                        lock.unlock();
                    }
                    latch.countDown();
                }
            });
        }
        latch.await();
        System.out.println("执行完毕,当前库存：" + storage);
        System.out.println("耗时：" + (System.currentTimeMillis() - start));
    }

}
