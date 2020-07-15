package com.futao.practice.chatroom.aio;

import com.futao.practice.chatroom.bio.Constants;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author futao
 * @date 2020/7/15
 */
@Slf4j
public class AioChatClient {

    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(4);

    private static final ByteBuffer WRITE_BUF = ByteBuffer.allocate(1024 * 4);

    public void start() {
        try {
            AsynchronousChannelGroup group = AsynchronousChannelGroup.withThreadPool(THREAD_POOL);
            AsynchronousSocketChannel asynchronousSocketChannel = AsynchronousSocketChannel.open(group);
            Future<Void> connectFuture = asynchronousSocketChannel.connect(new InetSocketAddress("localhost", Constants.SERVER_PORT));
            // 等待结果
            connectFuture.get();
            log.debug("成功接入聊天室");

            // 处理用户输入
            new Thread(() -> {
                String message = new Scanner(System.in).nextLine();
                WRITE_BUF.clear();
                WRITE_BUF.put(message.getBytes(Constants.CHARSET));
                WRITE_BUF.flip();
                asynchronousSocketChannel.write(WRITE_BUF, new AioAttachment(), new CompletionHandler<Integer, AioAttachment>() {
                    @Override
                    public void completed(Integer result, AioAttachment attachment) {
                        log.debug("消息发送成功");
                    }

                    @Override
                    public void failed(Throwable exc, AioAttachment attachment) {
                        log.debug("消息发送失败");
                    }
                });
                // 或者使用这种写法也是可以的
                // Future<Integer> write = asynchronousSocketChannel.write(WRITE_BUF);
                // Integer integer = write.get();

            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new AioChatClient().start();
    }
}
