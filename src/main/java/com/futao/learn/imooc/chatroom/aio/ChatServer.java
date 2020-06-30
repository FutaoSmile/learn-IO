package com.futao.learn.imooc.chatroom.aio;

import com.futao.learn.imooc.chatroom.Const;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author futao
 * @date 2020/6/24
 */
public class ChatServer {


    private static final Logger LOGGER = LoggerFactory.getLogger(ChatServer.class);

    private AsynchronousServerSocketChannel asynchronousServerSocketChannel;

    private static final Charset CHAR_SET = StandardCharsets.UTF_8;

    private Set<AsynchronousSocketChannel> connectedClients = new HashSet<AsynchronousSocketChannel>() {
        @Override
        public synchronized boolean add(AsynchronousSocketChannel asynchronousSocketChannel) {
            return super.add(asynchronousSocketChannel);
        }

        @Override
        public synchronized boolean remove(Object o) {
            AsynchronousSocketChannel asynchronousSocketChannel = (AsynchronousSocketChannel) o;
            try {
                LOGGER.info("客户端[{}]已断开连接", ((InetSocketAddress) asynchronousSocketChannel.getRemoteAddress()).getPort());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return super.remove(o);
        }
    };

    public void start() throws IOException {
        AtomicInteger atomicInteger = new AtomicInteger();
        try {
            asynchronousServerSocketChannel = AsynchronousServerSocketChannel.open(AsynchronousChannelGroup.withThreadPool(Executors.newCachedThreadPool(new ThreadFactory() {
                @Override
                public Thread newThread(@NotNull Runnable r) {
                    return new Thread(r, "线程-" + atomicInteger.incrementAndGet());
                }
            })))
                    .bind(new InetSocketAddress(Const.SERVER_PORT));

            while (true) {
                asynchronousServerSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
                    @Override
                    public void completed(AsynchronousSocketChannel clientSocketChannel, Object attachment) {
                        //成功建立连接
                        if (asynchronousServerSocketChannel.isOpen()) {
                            //继续监听下一次连接
                            asynchronousServerSocketChannel.accept(null, this);
                        }
                        //得到客户端socketChannel
                        if (clientSocketChannel != null && clientSocketChannel.isOpen()) {
                            // 将新用户添加到在线用户列表
                            connectedClients.add(clientSocketChannel);
                            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                            //参数1:将clientChannel的数据写入byteBuffer缓冲区，参数2:将这个缓冲区传递给另外一个handler
                            clientSocketChannel.read(byteBuffer, byteBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                                @Override
                                public void completed(Integer result, ByteBuffer attachment) {
                                    //数据读取完成
                                    if (attachment != null) {
                                        if (result <= 0) {
                                            //客户端异常 将用户从在线用户列表中移除
                                            connectedClients.remove(clientSocketChannel);
                                        } else {
                                            attachment.flip();
                                            String msg = String.valueOf(CHAR_SET.decode(byteBuffer));
                                            try {
                                                LOGGER.info("接收到用户[{}]发来的消息:[{}]", ((InetSocketAddress) clientSocketChannel.getRemoteAddress()).getPort(), msg);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            if (Const.EXIT_KEY_WORD.equals(msg)) {
                                                //退出
                                                connectedClients.remove(clientSocketChannel);
                                            }
                                            //转发消息
                                            connectedClients.forEach(client -> {
                                                try {
                                                    ByteBuffer buffer = CHAR_SET.encode(((InetSocketAddress) clientSocketChannel.getRemoteAddress()).getPort() + ":" + msg);
                                                    // TODO: 2020/6/27 completedHandler
                                                    client.write(buffer);
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            });
                                        }
                                    }
                                }

                                @Override
                                public void failed(Throwable exc, ByteBuffer attachment) {
                                    LOGGER.error("客户端数据读取失败", exc);
                                }
                            });
                        }
                    }

                    @Override
                    public void failed(Throwable exc, Object attachment) {
                        LOGGER.error("建立连接失败", exc);
                    }
                });
                System.in.read();
            }
        } finally {
            asynchronousServerSocketChannel.close();
        }
    }
}
