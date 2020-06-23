package com.futao.learn.imooc.chatroom.nio;

import com.futao.learn.imooc.chatroom.Const;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * @author futao
 * @date 2020/6/22
 */
@Slf4j
public class ChatServer {

    /**
     * 服务器端通道
     */
    private ServerSocketChannel serverSocketChannel;

    private Selector selector;
    /**
     * 读缓冲区
     */
    private ByteBuffer readByteBuffer = ByteBuffer.allocate(1024);
    /**
     * 写缓冲区
     */
    private ByteBuffer writeByteBuffer = ByteBuffer.allocate(1024);
    /**
     * 字符编码
     */
    private static final Charset CHAR_SET = StandardCharsets.UTF_8;

    public void start() throws IOException {
        try {
            //创建服务端通道ServerSocketChannel
            serverSocketChannel = ServerSocketChannel.open();
            //设置成非阻塞
            serverSocketChannel.configureBlocking(false);
            //设置ServerSocket监听的端口
            serverSocketChannel.socket().bind(new InetSocketAddress(Const.SERVER_PORT));

            //创建Selector
            selector = Selector.open();
            //让selector监听serverSocketChannel上发生的accept事件
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            log.info("聊天服务器已启动，正在监听[{}]端口", Const.SERVER_PORT);


            //存在触发事件
            while (true) {
                //如果没有相关的事件触发，则select()处于阻塞状态
                int triggerChannelCount = selector.select();
                if (triggerChannelCount == 0) {
                    continue;
                }
                //获取到当前selector上触发的所有事件
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey key : selectionKeys) {
                    //处理所触发的事件
                    handleSelectionKey(key);
                }
                //需要手动清除已经处理完的事件，否则下一次还将读取到已经处理过的事件
                selectionKeys.clear();
            }
        } finally {
            //只需要关闭selector即可，selector会自动关闭注册在selector上的channel
            selector.close();
            //serverSocketChannel.close();
        }
    }

    /**
     * 处理事件
     *
     * @param selectionKey
     * @throws IOException
     */
    public void handleSelectionKey(SelectionKey selectionKey) throws IOException {

        //触发的是Accept事件，该事件是在ServerSocket上触发的
        if (selectionKey.isAcceptable()) {
            //获取通道
            ServerSocketChannel channel = (ServerSocketChannel) selectionKey.channel();
            //获取到客户端的SocketChannel
            SocketChannel socketChannel = channel.accept();
            //设置成非阻塞调用
            socketChannel.configureBlocking(false);
            //将客户端socketChannel注册到Selector上，并监听该通道上的可读事件
            socketChannel.register(selectionKey.selector(), SelectionKey.OP_READ);
            log.info("客户端[{}]成功连接服务器", socketChannel.socket().getPort());
        } else
            //触发的是read事件
            if (selectionKey.isReadable()) {
                //该事件是在客户端SocketChannel上触发的，获取到该Socket
                SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

                //清除缓冲区的历史数据
                readByteBuffer.clear();

                //将数据写入缓冲区
                while (socketChannel.read(readByteBuffer) > 0) {
                }
                //切换读写模式
                readByteBuffer.flip();
                //从缓冲区中读取数据客户端发送的数据
                String msg = String.valueOf(CHAR_SET.decode(readByteBuffer));
                log.info("接收到客户端[{}]发来消息[{}]", socketChannel.socket().getPort(), msg);

                if (StringUtils.isBlank(msg)) {
                    //客户端异常，=====>测试发现，如果客户端被直接关闭，会出现isReadable()事件一直触发的问题
                    log.info("客户端[{}]异常，即将剔除", 1);
                    //断开当前注册的selector上的触发当前事件的channel连接
                    int clientPort = ((SocketChannel) selectionKey.channel()).socket().getPort();
                    selectionKey.cancel();
                    log.info("客户端[{}]下线", clientPort);
                    //因为发生了监听事件和channel的变更，所以需要通知selector重新整理selector所监听的事件
                    selector.wakeup();
                }

                //判断是否是退出
                if (Const.EXIT_KEY_WORD.equals(msg)) {
                    //断开当前注册的selector上的触发当前事件的channel连接
                    int clientPort = ((SocketChannel) selectionKey.channel()).socket().getPort();
                    selectionKey.cancel();
                    log.info("客户端[{}]下线", clientPort);
                    //因为发生了监听事件和channel的变更，所以需要通知selector重新整理selector所监听的事件
                    selector.wakeup();
                }

                //转发消息

                //当前注册的所有SelectionKey
                Set<SelectionKey> keys = selector.keys();
                for (SelectionKey key : keys) {
                    //通道是有效的，且是客户端Socket，并且不是当前发送消息的通道
                    if (key.isValid() && key.channel() instanceof SocketChannel && key.channel() != selectionKey.channel()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        //清除历史数据
                        writeByteBuffer.clear();
                        //将数据写入缓冲区
                        writeByteBuffer.put(CHAR_SET.encode(msg + "[from " + socketChannel.socket().getPort() + "]"));
                        //切换读写模式
                        writeByteBuffer.flip();
                        //如果缓冲区中还有数据
                        while (writeByteBuffer.hasRemaining()) {
                            //将数据写入客户端channel
                            channel.write(writeByteBuffer);
                        }
                    }
                }
            }

    }

    public static void main(String[] args) throws IOException {
        ChatServer chatServer = new ChatServer();
        chatServer.start();

    }
}
