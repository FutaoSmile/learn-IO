package com.futao.learn.imooc.chatroom.aio.other;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author futao
 * @date 2020/6/24
 */
public class IBMFTP {

    private static AsynchronousSocketChannel asynchronousSocketChannel;

    public static void main(String[] args) throws IOException {
        AtomicInteger atomicInteger = new AtomicInteger();
        asynchronousSocketChannel = AsynchronousSocketChannel.open(AsynchronousChannelGroup.withThreadPool(Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(@NotNull Runnable r) {
                return new Thread(r, "线程-" + atomicInteger.getAndIncrement());
            }
        })));

        //连接到服务器
        asynchronousSocketChannel.connect(new InetSocketAddress("ftp.gnu.org", 21), asynchronousSocketChannel, new CompletionHandler<Void, AsynchronousSocketChannel>() {
            @Override
            public void completed(Void result, AsynchronousSocketChannel attachment) {
                new IBMFTP().start(attachment);
            }

            @Override
            public void failed(Throwable exc, AsynchronousSocketChannel attachment) {
                exc.printStackTrace();
            }
        });
        System.in.read();
    }

    public void start(AsynchronousSocketChannel attachment) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        asynchronousSocketChannel.read(byteBuffer, byteBuffer, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer result, ByteBuffer attachment) {
                if (result > 0) {
                    System.out.println("---");
                    System.out.println(String.valueOf(StandardCharsets.UTF_8.decode(byteBuffer)));
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                exc.printStackTrace();
            }
        });
    }
}
