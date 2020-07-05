package com.futao.practice.chatroom.bio.v3.test;

import com.futao.practice.chatroom.bio.v3.BioChatClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author futao
 * @date 2020/7/5
 */
public class ClientRunner {

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(50);
        for (int i = 0; i < 50; i++) {
            executorService.execute(() -> {
                new BioChatClient().start();
            });
        }
    }
}
