package com.futao.practice.chatroom.bio.v2;

import com.futao.practice.chatroom.bio.Constants;
import com.futao.practice.chatroom.bio.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * 常规思路实现的基于BIO的聊天室
 *
 * @author futao
 * @date 2020/7/2
 */
public class BioChatClient {

    private static final Logger logger = LoggerFactory.getLogger(BioChatClient.class);

    /**
     * 启动客户端
     */
    private void start() {
        try (
                //尝试连接到服务器
                Socket socket = new Socket("localhost", Constants.SERVER_PORT);
                //获取到输入流
                InputStream inputStream = socket.getInputStream();
                //获取到输出流
                OutputStream outputStream = socket.getOutputStream()
        ) {
            logger.debug("========== 成功连接到聊天服务器 ==========");
            //开启新的线程来处理用户的输入
            new Thread(() -> {
                try {
                    while (true) {
                        //获取用户输入的数据
                        String message = new Scanner(System.in).nextLine();
                        //将数据写入输出流中
                        outputStream.write(message.getBytes(Constants.CHARSET));
                        //刷新缓冲区
                        outputStream.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            //主线程一直从输入流中读取数据
            while (true) {
                logger.info("接收到消息:[{}]", IOUtils.readString(inputStream));
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new BioChatClient().start();
    }

}
