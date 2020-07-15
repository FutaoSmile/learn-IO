package com.futao.practice.chatroom.nio.httpserver;

import com.futao.practice.chatroom.bio.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

/**
 * NIO实现HTTP服务器
 *
 * @author futao
 * @date 2020/7/10
 */
@Slf4j
public class NioHttpServer {

    private static final ByteBuffer READ_BUFFER = ByteBuffer.allocate(1024 * 4);

    /**
     * 静态资源路径
     */
    private static final String STATIC_RESOURCE_PATH = System.getProperty("user.dir") + "/practice/src/main/resources/pages/";

    /**
     * 响应的基础信息
     */
    public static final String BASIC_RESPONSE = "HTTP/1.1 200 OK\r\n" +
            "Content-Type: text/html;charset=utf-8\r\n" +
            "Vary: Accept-Encoding\r\n";

    /**
     * 回车换行符
     */
    private static final String carriageReturn = "\r\n";


    public void start() {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.bind(new InetSocketAddress("localhost", Constants.SERVER_PORT));

            Selector selector = Selector.open();

            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);


            while (true) {
                int eventCountTriggered = selector.select();
                if (eventCountTriggered == 0) {
                    continue;
                }
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey selectionKey : selectionKeys) {
                    handleSelectKey(selectionKey, selector);
                }
                selectionKeys.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void handleSelectKey(SelectionKey selectionKey, Selector selector) {
        if (selectionKey.isAcceptable()) {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
            try {
                SocketChannel socketChannel = serverSocketChannel.accept();
                socketChannel.configureBlocking(false);
                socketChannel.register(selector, SelectionKey.OP_READ);
                log.debug("客户端[{}]接入", socketChannel.socket().getPort());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (selectionKey.isReadable()) {
            READ_BUFFER.clear();
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            try {
                while (socketChannel.read(READ_BUFFER) > 0) {
                }
                READ_BUFFER.flip();
                String requestMessage = String.valueOf(Constants.CHARSET.decode(READ_BUFFER));
                log.info("接收到浏览器发来的数据:\n{} === request print end...", requestMessage);
                if (StringUtils.isBlank(requestMessage)) {
                    selectionKey.cancel();
                    selector.wakeup();
                }

                String requestUri = NioHttpServer.getRequestUri(requestMessage);
                staticHandler(requestUri, socketChannel);
                selectionKey.cancel();
                selector.wakeup();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取请求的资源地址
     *
     * @param request
     * @return
     */
    private static String getRequestUri(String request) {
        //GET /index.html HTTP/1.1
        int firstBlank = request.indexOf(" ");
        String excludeMethod = request.substring(firstBlank + 2);
        return excludeMethod.substring(0, excludeMethod.indexOf(" "));
    }


    /**
     * 静态资源处理器
     *
     * @return
     */
    public boolean staticHandler(String page, SocketChannel socketChannel) throws IOException {
        //资源的绝对路径
        String filePath = NioHttpServer.STATIC_RESOURCE_PATH + page;
        boolean fileExist = false;
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            log.debug("静态资源[{}]存在", page);
            fileExist = true;
            //读取文件内容
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));

            ByteBuffer buffer = ByteBuffer.allocate(4 * 1024);

            buffer.put(BASIC_RESPONSE.getBytes(Constants.CHARSET));
            buffer.put(("Server: futaoServerBaseNIO/1.1" + NioHttpServer.carriageReturn).getBytes(Constants.CHARSET));
            buffer.put(("content-length: " + bytes.length + NioHttpServer.carriageReturn).getBytes(Constants.CHARSET));
            buffer.put(NioHttpServer.carriageReturn.getBytes(Constants.CHARSET));
            buffer.put(bytes);
            buffer.flip();

            while (buffer.hasRemaining()) {
                socketChannel.write(buffer);
            }
        }
        return fileExist;
    }

    public static void main(String[] args) {
        new NioHttpServer().start();
    }
}
