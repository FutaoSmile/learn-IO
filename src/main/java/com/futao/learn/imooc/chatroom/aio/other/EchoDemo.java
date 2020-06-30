package com.futao.learn.imooc.chatroom.aio.other;

import com.futao.learn.imooc.chatroom.Const;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * @author futao
 * @date 2020/6/23
 */
@Slf4j
public class EchoDemo {

    /**
     * 服务端异步通道
     */
    private AsynchronousServerSocketChannel asynchronousServerSocketChannel;

    public void start() throws IOException {
        asynchronousServerSocketChannel = AsynchronousServerSocketChannel.open();
        asynchronousServerSocketChannel.bind(new InetSocketAddress(Const.SERVER_PORT));
        //实现异步的两种方式:
        //1. accept()返回一个Future<>
        //2. 使用CompletionHandler
        asynchronousServerSocketChannel.<Void>accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel result, Void attachment) {
                if (asynchronousServerSocketChannel.isOpen()) {
                    //继续监听
                    asynchronousServerSocketChannel.accept(null, this);
                }
                //异步的客户端通道
                AsynchronousSocketChannel asynchronousSocketChannel = result;
                if (asynchronousSocketChannel != null && asynchronousSocketChannel.isOpen()) {
                    ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                    HashMap<String, Object> attachmentMap = new HashMap<>();
                    attachmentMap.put("type", "read");
                    attachmentMap.put("buffer", readBuffer);
                    asynchronousSocketChannel.<Map<String, Object>>read(readBuffer, attachmentMap, new CompletionHandler<Integer, Map<String, Object>>() {
                        @Override
                        public void completed(Integer result, Map<String, Object> attachment) {

                        }

                        @Override
                        public void failed(Throwable exc, Map<String, Object> attachment) {
                            //处理失败场景
                        }
                    });
                }

            }

            @Override
            public void failed(Throwable exc, Void attachment) {

            }
        });
    }


}
