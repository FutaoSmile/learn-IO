package com.futao.learn.imooc.chatroom;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * @author futao
 * @date 2020/5/6
 */
public class Const implements Serializable {
    public static final String EXIT_KEY_WORD = "quit";
    public static final int SERVER_PORT = 8888;


    public static void main(String[] args) throws InterruptedException {
        Set<String> integers = new HashSet<String>() {
            @Override
            public synchronized boolean add(String s) {
                return super.add(s);
            }

            @Override
            public synchronized boolean remove(Object o) {
                return super.remove(o);
            }
        };


        CountDownLatch countDownLatch = new CountDownLatch(10);

        for (int j = 0; j < 10; j++) {
            new Thread(() -> {
                for (int i = 0; i < 100; i++) {
                    integers.add(Thread.currentThread().getId() + ":" + i);
                }
                countDownLatch.countDown();
            }).start();
        }


        countDownLatch.await();
//        Thread.sleep(3500L);
        System.out.println(integers.size());
    }

}
