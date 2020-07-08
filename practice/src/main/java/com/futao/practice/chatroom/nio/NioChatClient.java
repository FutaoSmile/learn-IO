package com.futao.practice.chatroom.nio;

import com.futao.practice.chatroom.bio.Constants;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 基于NIO实现的群聊客户端
 *
 * @author futao
 * @date 2020/7/8
 */
@Slf4j
public class NioChatClient {

    /**
     * 启动客户端
     */
    public void start() {
        try {
            // 创建客户端通道
            SocketChannel socketChannel = SocketChannel.open();
            // 将通道设置为非阻塞
            socketChannel.configureBlocking(false);

            // 创建多路复用器
            Selector selector = Selector.open();

            // 将客户端通道注册到多路复用器，并监听可读事件
            socketChannel.register(selector, SelectionKey.OP_CONNECT);

            // 尝试连接到聊天服务器
            socketChannel.connect(new InetSocketAddress("localhost", Constants.SERVER_PORT));

            while (true) {
                // 阻塞等待通道上的事件触发。返回触发的通道的数量
                int eventCountTriggered = selector.select();
                if (eventCountTriggered <= 0) {
                    continue;
                }
                // 获取到所有触发的事件
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                // 遍历事件进行处理
                for (SelectionKey selectionKey : selectionKeys) {
                    // 处理事件
                    selectionKeyHandler(selectionKey, selector);
                }
                // 清除事件记录
                selectionKeys.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void selectionKeyHandler(SelectionKey selectionKey, Selector selector) {

        if (selectionKey.isConnectable()) {
            //触发的是成功接入服务器的事件
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            try {
                // 判断是否成功连接到服务器
                if (socketChannel.isConnectionPending()) {
                    // 成功建立连接
                    socketChannel.finishConnect();
                    log.debug("成功接入聊天服务器");

                    // 将通道设置成非阻塞
                    socketChannel.configureBlocking(false);
                    // 将通道注册到多路复用器，并监听可读事件
                    socketChannel.register(selector, SelectionKey.OP_READ);

                    // 创建缓冲区，用于处理将用户输入的数据写入通道
                    ByteBuffer byteBuffer = ByteBuffer.allocate(4 * 1024);
                    // 在新线程中处理用户输入
                    new Thread(() -> {
                        while (true) {
                            //先清空缓冲区中的数据
                            byteBuffer.clear();
                            // 获取用户输入的文本
                            String message = new Scanner(System.in).nextLine();
                            // 将数据写入缓冲区
                            byteBuffer.put(message.getBytes(Constants.CHARSET));
                            // 将缓冲区设置为读模式
                            byteBuffer.flip();
                            try {
                                // 当缓冲区中还有数据
                                while (byteBuffer.hasRemaining()) {
                                    //将数据写入通道
                                    socketChannel.write(byteBuffer);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (selectionKey.isReadable()) {
            // 触发的是可读事件
            // 获取到可读事件的通道
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            //创建缓冲区
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 4);
            try {
                //将通道上的数据写入缓冲区(返回0或者-1说明读到了末尾)
                while (socketChannel.read(byteBuffer) > 0) {
                }
                log.info("接收到数据:[{}]", Constants.CHARSET.decode(byteBuffer));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        new NioChatClient().start();
    }
}
