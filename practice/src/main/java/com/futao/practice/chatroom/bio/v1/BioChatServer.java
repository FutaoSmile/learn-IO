package com.futao.practice.chatroom.bio.v1;

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

/**
 * @author futao
 * @date 2020/7/2
 */
public class BioChatServer {

    private static final Logger logger = LoggerFactory.getLogger(BioChatServer.class);

    /**
     * 启动服务器
     */
    public void start() {
        //创建服务端ServerSocket，并监听端口
        try (ServerSocket serverSocket = new ServerSocket(Constants.SERVER_PORT)) {
            logger.debug("========== 基于BIO的聊天室在[{}]端口启动成功 ==========", Constants.SERVER_PORT);
            //循环accept()监听
            while (true) {
                //accept()将阻塞，直到有客户端Socket接入。并在服务端创建一个Socket与其对应
                Socket socket = serverSocket.accept();
                logger.debug("客户端[{}]成功接入", socket.getPort());
                //将获取到的客户端连接交给子线程去处理，不影响主线程继续监听，等待下一个客户端连接
                new Thread(() -> {
                    try (
                            //用于从客户端读取数据
                            InputStream inputStream = socket.getInputStream();
                            //用于将数据写给客户端
                            OutputStream outputStream = socket.getOutputStream()
                    ) {
                        //从输入流中读取数据
                        String fullMessage = IOUtils.readString(inputStream);
                        logger.info("接收到客户端【{}】发来的消息[{}]", socket.getPort(), fullMessage);
                        //响应
                        outputStream.write(String.format("echo from server: <%s>", fullMessage).getBytes(Constants.CHARSET));
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

    public static void main(String[] args) {
        new BioChatServer().start();
    }
}
