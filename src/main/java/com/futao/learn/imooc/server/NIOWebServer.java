package com.futao.learn.imooc.server;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * @author futao
 * @date 2020/6/30.
 */
@Slf4j
public class NIOWebServer {

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    Selector selector;

    public void start() throws IOException {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(8888));

        selector = Selector.open();

        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);


        while (true) {
            try {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey selectionKey : selectionKeys) {
                    if (selectionKey.isAcceptable()) {
                        ServerSocketChannel curServerSocketChannel = (ServerSocketChannel) selectionKey.channel();
                        SocketChannel socketChannel = curServerSocketChannel.accept();
                        socketChannel.configureBlocking(false);

                        socketChannel.register(selector, SelectionKey.OP_READ);
                        log.info("[{}]连接成功", ((InetSocketAddress) socketChannel.getLocalAddress()).getPort());
                    } else if (selectionKey.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                        socketChannel.read(byteBuffer);
                        byteBuffer.flip();
                        String value = String.valueOf(CHARSET.decode(byteBuffer));
                        if (StringUtils.isBlank(value)) {
                            log.info("{}", ((InetSocketAddress) socketChannel.getLocalAddress()).getPort() + "下线");
                            socketChannel.close();
                            selectionKey.cancel();
                            selector.wakeup();
                        }
                        log.info("接收到客户端请求报文:\n{}", value);


                        selectionKey.cancel();
                        selector.wakeup();

                        socketChannel.configureBlocking(true);

                        Socket socket = socketChannel.socket();
                        OutputStream outputStream = socket.getOutputStream();

                        outputStream.write(BIOWebServer.resp.getBytes());

                        byte[] writeCache = new byte[1024];
                        FileInputStream fis = new FileInputStream(new File(BIOWebServer.pathname));
                        int len;
                        while ((len = fis.read(writeCache)) != -1) {
                            outputStream.write(writeCache, 0, len);
                        }
                        outputStream.flush();
                        outputStream.close();
                        socket.close();





                        /*
                         ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
                        writeBuffer.put(BIOWebServer.resp.getBytes(CHARSET));
                        FileChannel.open(Paths.get(BIOWebServer.pathname)).read(writeBuffer);
                        while (writeBuffer.hasRemaining()) {
                            socketChannel.write(writeBuffer);
                        }
                        log.info("{}", "end");
                        socketChannel.close();
                        selectionKey.cancel();
                        selector.wakeup();
                         */

                    }
                }
                selectionKeys.clear();
            } catch (Exception e) {
                log.error("...", e);
            }
        }
    }

    public static void main(String[] args) {
        try {
            new NIOWebServer().start();
        } catch (Exception e) {
            log.error("...", e);
        }
    }
}
