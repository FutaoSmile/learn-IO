package com.futao.practice.chatroom.bio.v1;

import com.futao.practice.chatroom.bio.Constants;
import com.futao.practice.chatroom.bio.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
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
                //输出流
                OutputStream outputStream = socket.getOutputStream()
        ) {
            logger.debug("========== 成功连接到聊天服务器 ==========");
            //获取到用户输入的字符串
            String userInputStr = new Scanner(System.in).nextLine();
            //将字符串转换成字节，写入输出流
            outputStream.write(userInputStr.getBytes(Constants.CHARSET));
            //刷新缓冲区
            outputStream.flush();
            //【重要】关闭输出流，通知服务器客户端消息已经发送完毕
            socket.shutdownOutput();
            //读取服务端的响应
            logger.info("接收到消息:[{}]", IOUtils.readString(inputStream));
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
