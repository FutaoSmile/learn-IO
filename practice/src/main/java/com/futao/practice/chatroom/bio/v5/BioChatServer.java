package com.futao.practice.chatroom.bio.v5;

import com.futao.practice.chatroom.bio.Constants;
import com.futao.practice.chatroom.bio.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author futao
 * @date 2020/7/6
 */
public class BioChatServer {

    private static final Logger logger = LoggerFactory.getLogger(BioChatServer.class);

    /**
     * 可同时接入的客户端数量
     */
    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(10);


    /**
     * 当前接入的客户端
     */
    private static final Set<Socket> CLIENT_SOCKET_SET = new HashSet<Socket>() {
        @Override
        public synchronized boolean add(Socket o) {
            return super.add(o);
        }

        @Override
        public synchronized boolean remove(Object o) {
            return super.remove(o);
        }
    };

    /**
     * 启动客户端
     */
    public void start() {
        try {
            //启动服务器，监听端口
            ServerSocket serverSocket = new ServerSocket(Constants.SERVER_PORT);
            logger.debug("========== 基于BIO的聊天室在[{}]端口启动成功 ==========", Constants.SERVER_PORT);
            while (true) {
                //监听客户端接入事件
                Socket socket = serverSocket.accept();
                THREAD_POOL.execute(() -> {
                    CLIENT_SOCKET_SET.add(socket);
                    int port = socket.getPort();
                    logger.debug("客户端[{}]成功接入聊天服务器", port);
                    try {
                        InputStream inputStream = socket.getInputStream();
                        OutputStream outputStream = socket.getOutputStream();

                        while (true) {
                            //获取到客户端发送的消息
                            String message = IOUtils.messageReceiver(inputStream);
                            logger.info("接收到客户端[{}]发送的消息:[{}]", port, message);
                            //客户端是否退出
                            boolean isQuit = IOUtils.isQuit(message, socket, CLIENT_SOCKET_SET);
                            if (isQuit) {
                                socket.close();
                                break;
                            } else {
                                //消息转发
                                IOUtils.forwardMessage(port, message, CLIENT_SOCKET_SET);
                            }
                        }
                    } catch (IOException e) {
                        logger.error("发生异常", e);
                    }
                });
            }
        } catch (IOException e) {
            logger.error("发生异常", e);
        }
    }


    public static void main(String[] args) {
        new BioChatServer().start();
    }
}
