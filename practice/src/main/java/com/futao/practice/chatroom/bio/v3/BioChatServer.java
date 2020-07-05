package com.futao.practice.chatroom.bio.v3;

import com.futao.practice.chatroom.bio.Constants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * 使用标记符号的方式通知消息发送完毕
 *
 * @author futao
 * @date 2020/7/2
 */
public class BioChatServer {

    private static final Logger logger = LoggerFactory.getLogger(BioChatServer.class);

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


    public void start() {
        //创建服务端ServerSocket，并监听端口
        try (ServerSocket serverSocket = new ServerSocket(Constants.SERVER_PORT)) {
            logger.debug("========== 基于BIO的聊天室在[{}]端口启动成功 ==========", Constants.SERVER_PORT);
            //循环accept()监听
            while (true) {
                //accept()将阻塞，直到有客户端Socket接入。并在服务端创建一个Socket与其对应
                Socket socket = serverSocket.accept();
                logger.debug("客户端[{}]上线", socket.getPort());
                CLIENT_SOCKET_SET.add(socket);
                //将获取到的客户端连接交给子线程去处理，不影响主线程继续监听，等待下一个客户端连接
                new Thread(() -> {
                    try {
                        //用于从客户端读取数据(将字节流转换成字符流)
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new BufferedInputStream(socket.getInputStream())));
                        while (true) {
                            //从输入流中读取数据
                            String message = bufferedReader.readLine();
                            if (StringUtils.isNotBlank(message)) {
                                logger.info("接收到客户端【{}】发来的消息[{}]", socket.getPort(), message);
                            } else {
                                isQuit(message, socket);
                            }
                            //判断是否为下线
                            boolean isQuit = isQuit(message, socket);
                            //转发消息
                            if (!isQuit) {
                                forwardMessage(socket.getPort(), String.format("<from %s>", socket.getPort()) + message);
                            } else {
                                break;
                            }
                        }
                    } catch (IOException e) {
                        logger.error("客户端异常", e);
                    }
                }).start();
            }

        } catch (IOException e) {
            logger.error("服务器启动失败", e);
            return;
        }
    }


    public boolean isQuit(String message, Socket socket) throws IOException {
        boolean isQuit = Constants.KEY_WORD_QUIT.equals(message);
        if (isQuit) {
            CLIENT_SOCKET_SET.remove(socket);
            int port = socket.getPort();
            socket.close();
            logger.debug("客户端[{}]下线", port);
        }
        return isQuit;
    }

    /**
     * 转发消息
     *
     * @param curSocketPort 当前发送消息的客户端Socket的端口
     * @param message       需要转发的消息
     */
    public void forwardMessage(int curSocketPort, String message) {
        message += "\r\n";
        if (StringUtils.isBlank(message)) {
            return;
        }
        for (Socket socket : CLIENT_SOCKET_SET) {
            if (socket.isClosed() || socket.getPort() == curSocketPort) {
                continue;
            }
            if (socket.getPort() != curSocketPort) {
                try {
                    OutputStream outputStream = socket.getOutputStream();
                    //将字符串编码之后写入客户端
                    outputStream.write(message.getBytes(Constants.CHARSET));
                    //刷新缓冲区
                    outputStream.flush();
                } catch (IOException e) {
                    logger.error("消息转发失败", e);
                }
            }
        }
    }

    public static void main(String[] args) {
        new BioChatServer().start();
    }


}
