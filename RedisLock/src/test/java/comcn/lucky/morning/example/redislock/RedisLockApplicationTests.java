package comcn.lucky.morning.example.redislock;

import comcn.lucky.morning.example.redislock.service.RedisLockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class RedisLockApplicationTests {
    @Autowired
    private RedisLockService redisLockService;

    int storage = 100000;

    @Test
    public void testLock() throws InterruptedException {
        String key = "lock:key";
        redisLockService.lock(key);
        System.out.println("加锁成功");
        System.out.println("休眠10s再解锁");
        Thread.sleep(10000);
        if (redisLockService.unlock(key)) {
            System.out.println("解锁成功");
        } else {
            System.out.println("解锁失败！");
        }
    }

    @Test
    public void bugGoodsNoLock() throws InterruptedException {
        int maxCoreSize = 1000;
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
    void bugGoodsOnLock() throws InterruptedException {
        String redisKeyPre = "lock:redis:";
        String key = redisKeyPre + "1";
        int maxCoreSize = 1000;
        int maxCount = storage;
        ThreadPoolExecutor executor = new ThreadPoolExecutor(maxCoreSize, maxCoreSize, 1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(maxCount));
        CountDownLatch latch = new CountDownLatch(maxCount);

        for (int index = 0; index < maxCount; index++) {
            executor.execute(() -> {
                try {
                    redisLockService.lock(key);

                    storage = storage - 1;
                    System.out.println("减库存成功：" + storage);
                    if (!redisLockService.unlock(key)) {
                        System.out.println("解锁失败！");
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        System.out.println("执行完毕,当前库存：" + storage);
    }

}
