package com.search.ThreadTest;

import java.util.concurrent.*;

public class ThreadTest {
    //线程池
    public static ExecutorService service = Executors.newFixedThreadPool(10);
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        /**
         * 1. 继承Thread
         *         System.out.println("main...start...");
         *         Tread01 tread01 = new Tread01();
         *         tread01.start();
         *         System.out.println("main...end....");
         * 2. 实现Runnable接口
         *         System.out.println("main...start...");
         *         Runnable01 runnable01 = new Runnable01();
         *         new Thread(runnable01).start();
         *         System.out.println("main...end....");
         * 3. 实现Callable接口 + FutureTask 可以拿到返回结果，可以处理异常
         *         FutureTask<Integer> futureTask = new FutureTask<>(new Callable01());
         *         new Thread(futureTask).start();
         *         //阻塞等待整个线程执行完成，获取返回结果
         *         Integer integer = futureTask.get();
         * 3. 线程池
         *      给线程池提交任务
         *      service.execute(new Runnable01());
         *      1.创建
         *          1.Executors
         *          2.
         *
         * 区别:
         *      1和2 没有返回值 3 有返回值
         *      1 2 3 都不能控制资源
         *      4可以控制资源，性能稳定。
         */
        System.out.println("main...start...");
        //我们以后在业务代码里面，以上三种都不同，讲所有的线程异步任务都交给线程池
//        new Thread(()-> System.out.println("hello")).start();
        /**
         * 7大参数
         * 参数1 corePoolSize 核心线程数，一直存在除非设置了【allowCoreThreadTimeOut】
         * 参数2 maximumPoolSize 最大线程数
         * 参数3 keepAliveTime 如果当前线程数量大于core数量之外的线程，自动释放空闲线程
         * 参数4 unit 时间单位，
         * 参数5 BlockingQueue<Runnable> workQueue 阻塞队列，如果任务多，多出的任务放到队列
         * 参数6 threadFactory 线程创建工厂
         * 参数7 handler 如果队列满了，按章我们指定策略拒绝执行任务
         *
         * 工作顺序：
         * 1.线程池创建，准备号core数量的核心线程，准备接收任务
         * 1.1 如果线程占满了，新任务放到队列
         * 1.2 如果阻塞满了，就直接开新线程执行，最大只能开到MAX的线程
         * 1.3 max满了就用RejectedExcutionHandler拒绝任务
         * 1.4 max都执行完成，有空闲，在指定时间以后，自动回收核心之外的线程
         */
//        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 200, 10, TimeUnit.SECONDS, new LinkedBlockingDeque<>(100000), Executors.defaultThreadFactory(),new ThreadPoolExecutor.AbortPolicy());

//        Executors.newCachedThreadPool(); //带缓存的线程池，灵活回收空闲线程，核心线程为0
//
//        Executors.newFixedThreadPool(); //固定线程，都不回收
//
//        Executors.newScheduledThreadPool(); //定时任务的线程池
//
//        Executors.newSingleThreadExecutor(); //单线程的线程池，挨个执行


        //复杂多线程
//        CompletableFuture.runAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10/2;
//            System.out.println("运行结果：" + i);
//        },service);

        //方法完成后感知
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 0;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, service).whenComplete((res,excption)->{
//            System.out.println("异步任务完成了结果是" + res + "异常是" + excption );
//        }).exceptionally(throwable -> {
//            return 10;
//        });
//        Integer integer = future.get();


        /**
         * 方法执行完成后的处理
         */
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("运行结果：" + i);
//            return i;
//        },service).handle((res,throwable) -> {
//            if(res != null){
//                return res * 2;
//            }
//            if(throwable != null){
//                return 0;
//            }
//            return 0;
//        });
//        Integer integer = future.get();

        /**
         * 线程串行化
         * 1. thenRun 不能获取到上一步的执行结果
         *  .thenRunAsync(() -> {
         *      System.out.println("任务2启动...");
         *  },service);
         * 2.thenAcceptAsync 能接受上一步结果，但是无返回值
         * 3.thenApplyAsync 能接受上一步结果，有返回值
         */
//        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, service).thenApplyAsync(res -> {
//            System.out.println("任务2启动了。。" + res);
//            return "hello" + res;
//        }, service);

        /**
         * 两个完成并获取结果
         */

//        CompletableFuture<Object> future1 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务1：" + Thread.currentThread().getId());
//            int i = 10 / 4;
//            System.out.println("任务1结果：" + i);
//            return i;
//        }, service);
//
//        CompletableFuture<Object> future2 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务2：" + Thread.currentThread().getId());
//            try {
//                Thread.sleep(3000);
//                System.out.println("任务2结束");
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            return "hello";
//        }, service);

//        future1.runAfterBothAsync(future2,() ->{
//            System.out.println("任务3开始");
//        },service);

        //void accept(T t, U u);
//        future1.thenAcceptBothAsync(future2,(f1,f2)->{
//            System.out.println("任务3开始。之前结果" + f1 + "-->" + f2);
//        },service);

//        CompletableFuture<String> future = future1.thenCombineAsync(future2, (f1, f2) -> {
//            return f1 + ":" + f2 + " ->haha";
//        }, service);


        /**
         * 两个任务有一个完成，就执行任务3
         * runAfterEitherAsync 不感知结果，自己没有返回值
         * acceptEitherAsync 感知结果，无返回值
         * applyToEitherAsync 感知结果，有返回值
         */
//        future1.runAfterEitherAsync(future2,()->{
//            System.out.println("任务3开始..之前的结果：");
//        },service);

//        future1.acceptEitherAsync(future2,(res)->{
//            System.out.println("任务3开始..之前的结果：" +res);
//        },service);

//        CompletableFuture<String> future = future1.applyToEitherAsync(future2, res -> {
//            System.out.println("任务3开始..之前的结果：" + res);
//            return res.toString() + "哈哈";
//        }, service);

        CompletableFuture<String> futureImg = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品图片信息");
            return "heelo.jpg";
        },service);

        CompletableFuture<String> futureAttr = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品属性");
            return "黑色+256G";
        },service);

        CompletableFuture<String> futureDesc = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品介绍");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "华为";
        },service);

        CompletableFuture<Object> anyOf = CompletableFuture.anyOf(futureImg, futureAttr, futureDesc);
        anyOf.get();
//        System.out.println("main...end...." + futureImg.get() + "=>" + futureAttr.get() + "=>" + futureDesc.get());
        System.out.println("main...end...." + anyOf.get());
    }

    public static class Tread01 extends Thread{
        @Override
        public void run(){
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
        }
    }

    public static class Runnable01 implements Runnable {
        @Override
        public void run() {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
        }
    }

    public static class Callable01 implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
            return i;
        }
    }
}
