package com.futao.practice.chatroom.aio;

import com.futao.practice.chatroom.bio.Constants;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 异步非阻塞
 *
 * @author futao
 * @date 2020/7/13
 */
@Slf4j
public class ChatServer {

    // TODO: 2020/7/13 AIO就是基于通知和事件回调 eg: Future.get()或者CompletionHandler回调

    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(10);

    public void start() {
        try {
            // 资源组
            AsynchronousChannelGroup asynchronousChannelGroup = AsynchronousChannelGroup.withThreadPool(THREAD_POOL);

            AsynchronousServerSocketChannel asynchronousServerSocketChannel = AsynchronousServerSocketChannel.open(asynchronousChannelGroup);
            asynchronousServerSocketChannel.bind(new InetSocketAddress("localhost", Constants.SERVER_PORT));
            log.debug("服务器启动成功");

            while (true) {
                asynchronousServerSocketChannel.accept(new Object(), new CompletionHandler<AsynchronousSocketChannel, Object>() {
                    @Override
                    public void completed(AsynchronousSocketChannel result, Object attachment) {
//                        log.debug("客户端[{}]接入成功",result.);
                    }

                    @Override
                    public void failed(Throwable exc, Object attachment) {

                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
