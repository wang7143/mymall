package com.atguigu.gulimall.product.web;

import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
public class IndexController {
    @Autowired
    CategoryService categoryService;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    StringRedisTemplate redisTemplate;

    //读写锁，都必须等待，读锁可以并发访问，写锁是独享锁
    @ResponseBody
    @GetMapping("/write")
    public String write() {
        RReadWriteLock lock = redissonClient.getReadWriteLock("rw-lock");
        RLock rLock = lock.writeLock();
        String s = "";
        try {
            rLock.lock();
            //改数据加写锁，读加读锁
            s = UUID.randomUUID().toString();
            Thread.sleep(30000);
            redisTemplate.opsForValue().set("writeValue",s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
        }
        return s;
    }

    @ResponseBody
    @GetMapping("/read")
    public String read(){
        RReadWriteLock lock = redissonClient.getReadWriteLock("rw-lock");
        String s = "";
        RLock rLock = lock.readLock();
        rLock.lock();
        try {
            s = redisTemplate.opsForValue().get("writeValue");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
        }
        System.out.println(s);
        return s;
    }

    @ResponseBody
    @GetMapping("/test")
    public String test() {
        //获取锁
        RLock lock = redissonClient.getLock("my-lock");

        //加锁
        lock.lock(10, TimeUnit.SECONDS);  //阻塞式等待
        try {
            System.out.println("加锁成功" + Thread.currentThread().getId());
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            System.out.println("解锁成功" + Thread.currentThread().getId());
            lock.unlock();
        }
        return "200";
    }


    @GetMapping({"/","/index.html"})
    public String indexPage(Model model){
        
        //TODO 1.查出所有1级分类
        List<CategoryEntity> categoryEntity = categoryService.getLevel1Categorys();
        model.addAttribute("categorys",categoryEntity);

        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> getCatalogJson(){

        Map<String, List<Catelog2Vo>> map = categoryService.getCatalogJson();
        return map;
    }

    /**
     * 放假，锁门
     * 1班没人了，
     * 5个班全部走完，锁大门
     */

    @GetMapping("/lockDoor")
    @ResponseBody
    public String lockDoor(){
        //闭锁
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.trySetCount(5);
        try {
            door.await();  //等待闭锁都完成
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "放假了...";
    }

    @GetMapping("/gogo/{id}")
    public String gogo(@PathVariable("id") Long id){
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.countDown();//计数减去1
        return id + "都走完了";
    }

    /**
     * 车库停车
     * 3车位
     * 信号量可以用作限流分布式
     */
    @GetMapping("/park")
    @ResponseBody
    public String park(){
        RSemaphore park = redissonClient.getSemaphore("park");
        boolean b = park.tryAcquire();//获取一个信号
        if(b){
            //获取信号成功
        }else{
            //返回提示
        }
        return "ok" + b;
    }

    @GetMapping("/go")
    @ResponseBody
    public String go(){
        RSemaphore park = redissonClient.getSemaphore("park");
        park.release(); //释放一个信号

        return "ok";
    }
}
