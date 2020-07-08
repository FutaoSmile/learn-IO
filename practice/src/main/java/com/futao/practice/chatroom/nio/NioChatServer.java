package com.futao.practice.chatroom.nio;

import com.futao.practice.chatroom.bio.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

/**
 * 基于NIO实现的聊天室
 *
 * @author futao
 * @date 2020/7/8
 */
@Slf4j
public class NioChatServer {

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
                //客户端SocketChannel接入
                SocketChannel socketChannel = ((ServerSocketChannel) selectionKey.channel()).accept();
                log.debug("客户端[{}]成功接入聊天服务器", socketChannel.socket().getPort());
                //将客户端SocketChannel通道设置成非阻塞
                socketChannel.configureBlocking(false);
                //将客户端通道注册到多路复用器，并监听这个通道上发生的可读事件
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
                log.info("接收到来自客户端[{}]的数据:[{}]", socketChannel.socket().getPort(), Constants.CHARSET.decode(byteBuffer));
                //转发消息
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        new NioChatServer().start();
    }
}
