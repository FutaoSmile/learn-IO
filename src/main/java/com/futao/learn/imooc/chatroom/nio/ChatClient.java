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
import java.util.Scanner;
import java.util.Set;

/**
 * @author futao
 * @date 2020/6/22
 */
@Slf4j
public class ChatClient {


    private SocketChannel socketChannel;

    private Selector selector;

    private ByteBuffer readByteBuffer = ByteBuffer.allocate(1024);

    private ByteBuffer writeByteBuffer = ByteBuffer.allocate(1024);

    private static final Charset CHAR_SET = StandardCharsets.UTF_8;

    public void start() throws IOException {
        selector = Selector.open();

        socketChannel = SocketChannel.open();

        socketChannel.configureBlocking(false);

        socketChannel.connect(new InetSocketAddress("localhost", 8888));
        //成功建立连接
        socketChannel.register(selector, SelectionKey.OP_CONNECT);

        while (true) {
            int triggerChannelCount = 0;
            try {
                triggerChannelCount = selector.select();
            } catch (ClosedSelectorException e) {
                log.info("用户正常退出...");
                break;
            }
            if (triggerChannelCount == 0) {
                continue;
            }
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            for (SelectionKey selectionKey : selectionKeys) {
                if (selectionKey.isConnectable()) {
                    log.info("成功连接到服务器");
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    if (channel.isConnectionPending()) {
                        channel.finishConnect();
                        //处理用户的输入
                        new Thread(() -> {
                            while (true) {
                                Scanner scanner = new Scanner(System.in);
                                String msg = scanner.nextLine();
                                writeByteBuffer.clear();
                                writeByteBuffer.put(CHAR_SET.encode(msg));
                                writeByteBuffer.flip();
                                try {
                                    while (writeByteBuffer.hasRemaining()) {
                                        channel.write(writeByteBuffer);
                                    }
                                    //下线
                                    if (Const.EXIT_KEY_WORD.equals(msg)) {
                                        selector.close();
                                        break;
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }
                    socketChannel.register(selector, SelectionKey.OP_READ);
                } else if (selectionKey.isReadable()) {
                    SocketChannel channel = (SocketChannel) selectionKey.channel();
                    while (channel.read(readByteBuffer) > 0) {
                    }
                    readByteBuffer.flip();
                    String msg = String.valueOf(CHAR_SET.decode(readByteBuffer));
                    if (StringUtils.isBlank(msg)) {
                        //服务器异常，=====>测试发现，如果服务器被直接关关闭，会出现isReadable()事件一直触发的问题
                        selector.close();
                        log.info("服务器异常，系统结束");
                    }
                    log.info("接收到消息:[{}]", msg);
                    readByteBuffer.clear();
                }
            }
            selectionKeys.clear();
        }
    }


    public static void main(String[] args) throws IOException {
        ChatClient chatClient = new ChatClient();
        try {
            chatClient.start();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
