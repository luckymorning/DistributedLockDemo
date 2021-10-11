package comcn.lucky.morning.example.redislock;

import comcn.lucky.morning.example.redislock.service.RedisLockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class RedisLockApplicationTests {
    @Autowired
    private RedisLockService redisLockService;

    @Test
    void contextLoads() throws InterruptedException {
        String redisKeyPre = "lock:redis:";
        String key = redisKeyPre + "1";
        if (redisLockService.lock(key)) {
            System.out.println("加锁成功");
        } else {
            System.out.println("加锁失败");
        }
        System.out.println("休眠10秒后进行解锁操作");
        Thread.sleep(10000);
        if (redisLockService.unlock(key)) {
            System.out.println("解锁成功");
        } else {
            System.out.println("解锁失败");
        }
    }

}
