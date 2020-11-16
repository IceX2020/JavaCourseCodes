package java0.conc0303;

import java.util.concurrent.CyclicBarrier;

/**
 * 本周作业：（必做）思考有多少种方式，在main函数启动一个新线程或线程池，
 * 异步运行一个方法，拿到这个方法的返回值后，退出主线程？
 * 写出你的方法，越多越好，提交到github。
 *
 * 一个简单的代码参考：
 */
public class Homework03_02 {
    private static int result;
    private static int flag = 0;

    public static void main(String[] args)  throws InterruptedException{
        long start=System.currentTimeMillis();
        // 在这里创建一个线程或线程池，
        // 异步执行 下面方法
        CyclicBarrier cyclicBarrier = new CyclicBarrier(1, new Runnable() {
            @Override
            public void run() {
                result = sum();
                System.out.println("异步计算结果为："+result);
                flag = 1;
            }
        });
        for (int i = 0; i < 1; i++) {
            new Thread(new Homework03_02.readNum(i,cyclicBarrier)).start();
        }

        // 确保拿到resul并输出
        while(flag == 1) {
            System.out.println("异步计算结果为：" + result);
            System.out.println("使用时间："+ (System.currentTimeMillis()-start) + " ms");
        }
        // 然后退出main线程
    }
    
    private static int sum() {
        return fibo(36);
    }
    
    private static int fibo(int a) {
        if ( a < 2) 
            return 1;
        return fibo(a-1) + fibo(a-2);
    }

    static class readNum implements Runnable{
        private int id;
        private CyclicBarrier cyc;
        public readNum(int id, CyclicBarrier cyc){
            this.id = id;
            this.cyc = cyc;
        }
        @Override
        public void run() {
            synchronized (this){
                System.out.println("id:"+id+","+Thread.currentThread().getName());
                try {
                    //cyc.await();
                    System.out.println("线程组任务" + id + "结束，其他任务继续");
                    cyc.await();   // 注意跟CountDownLatch不同，这里在子线程await
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
