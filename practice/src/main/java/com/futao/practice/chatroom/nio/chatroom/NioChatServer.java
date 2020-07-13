package com.futao.practice.chatroom.nio.chatroom;

import com.futao.practice.chatroom.bio.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 基于NIO实现的聊天室服务端
 *
 * @author futao
 * @date 2020/7/8
 */
@Slf4j
public class NioChatServer {

    /**
     * 用于处理通道上的事件的线程池（可选的）
     */
    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(10);

    /**
     * 启动聊天室
     */
    public void start() {
        try {
            //服务端Socket通道
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            //将通道设置成非阻塞
            serverSocketChannel.configureBlocking(false);
            //绑定主机与监听端口
            serverSocketChannel.bind(new InetSocketAddress("localhost", Constants.SERVER_PORT));

            //多路复用器
            Selector selector = Selector.open();

            //将服务端通道注册到多路复用器上，并设置监听事件接入事件
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            log.debug("{} 基于NIO的聊天室在[{}]端口启动成功 {}", StringUtils.repeat("=", 30), Constants.SERVER_PORT, StringUtils.repeat("=", 30));

            while (true) {
                // 触发了事件的通道数量，该方法会阻塞
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

    /**
     * 事件处理器
     *
     * @param selectionKey 触发的事件信息
     * @param selector     多路复用器
     */
    private void selectionKeyHandler(SelectionKey selectionKey, Selector selector) {
        if (selectionKey.isAcceptable()) {
            //如果触发的是SocketChannel接入事件
            try {
                // ServerSocketChannel上触发的客户端SocketChannel接入
                SocketChannel socketChannel = ((ServerSocketChannel) selectionKey.channel()).accept();
                log.debug("客户端[{}]成功接入聊天服务器", socketChannel.socket().getPort());
                // 将客户端SocketChannel通道设置成非阻塞
                socketChannel.configureBlocking(false);
                // 将客户端通道注册到多路复用器，并监听这个通道上发生的可读事件
                socketChannel.register(selector, SelectionKey.OP_READ);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (selectionKey.isReadable()) {
            // 触发的是可读事件
            // 获取到可读事件的客户端通道
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            //创建缓冲区
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 4);
            try {
                // 读取通道上的数据写入缓冲区(返回0或者-1说明读到了末尾)
                while (socketChannel.read(byteBuffer) > 0) {
                }
                //切换为读模式
                byteBuffer.flip();
                // 接收到的消息
                String message = String.valueOf(Constants.CHARSET.decode(byteBuffer));
                log.info("接收到来自客户端[{}]的数据:[{}]", socketChannel.socket().getPort(), message);
                // 是否退出
                quit(message, selector, selectionKey);
                // 消息转发
                forwardMessage(message, selector, selectionKey);
                // 清除缓冲区的数据
                byteBuffer.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 客户端退出
     *
     * @param message      消息
     * @param selector     多路复用器
     * @param selectionKey 触发的selectionKey
     */
    public void quit(String message, Selector selector, SelectionKey selectionKey) {
        if (StringUtils.isBlank(message) || Constants.KEY_WORD_QUIT.equals(message)) {
            int port = ((SocketChannel) selectionKey.channel()).socket().getPort();
            // 客户端下线
            selectionKey.cancel();
            log.debug("客户端[{}]下线", port);
            // 因为发生了监听事件和channel的变更，所以需要通知selector重新整理selector所监听的事件
            selector.wakeup();
        }
    }

    /**
     * 转发消息
     *
     * @param message         需要转发的消息
     * @param selector        多路复用器
     * @param curSelectionKey 当前触发的selectionKey
     */
    public void forwardMessage(String message, Selector selector, SelectionKey curSelectionKey) {
        // 创建缓冲区
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 4);
        // 数据写入缓冲区
        byteBuffer.put(message.getBytes(Constants.CHARSET));

        // 切换为读模式
        byteBuffer.flip();
        // 在首尾进行标记，因为需要给每个客户端发送同样的数据，需要重复读取
        byteBuffer.mark();
        // 当前注册在多路复用器上的SelectionKey集合
        Set<SelectionKey> keys = selector.keys();
        for (SelectionKey key : keys) {
            // 消息不能转发给自己 and 只转发给客户端SocketChannel
            if (curSelectionKey.equals(key) || !(key.channel() instanceof SocketChannel)) {
                continue;
            }
            // 客户端SocketChannel
            SocketChannel socketChannel = (SocketChannel) key.channel();
            // 如果缓冲区中还有数据就一直写
            while (byteBuffer.hasRemaining()) {
                try {
                    // 数据写入通道
                    socketChannel.write(byteBuffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // 重置到上次mark的地方，即首位
            byteBuffer.reset();
        }
        // 清除缓冲区的数据
        byteBuffer.clear();
    }


    public static void main(String[] args) {
        new NioChatServer().start();
    }
}
