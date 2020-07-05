package com.futao.practice.chatroom.bio.v5;

import com.futao.practice.chatroom.bio.Constants;
import com.futao.practice.chatroom.bio.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author futao
 * @date 2020/7/6
 */
public class BioChatClient {

    private static final Logger logger = LoggerFactory.getLogger(BioChatClient.class);

    private static final ExecutorService SINGLE_THREAD_EXECUTOR = Executors.newSingleThreadExecutor();

    /**
     * 启动客户端
     */
    public void start() {
        try {  //尝试连接到聊天服务器
            Socket socket = new Socket("localhost", Constants.SERVER_PORT);
            logger.debug("========== 成功连接到聊天服务器 ==========");

            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            //从输入流中读取数据
            SINGLE_THREAD_EXECUTOR.execute(() -> {
                try {
                    while (true) {
                        String message = IOUtils.messageReceiver(inputStream);
                        logger.info("接收到服务端消息:[{}]", message);
                    }
                } catch (IOException e) {
                    logger.error("发生异常", e);
                }
            });

            while (true) {
                //获取用户输入的数据
                String message = new Scanner(System.in).nextLine();
                if (StringUtils.isBlank(message)) {
                    break;
                }
                //将内容转换为字节数组
                byte[] contentBytes = message.getBytes(Constants.CHARSET);
                //内容字节数组的大小
                int length = contentBytes.length;
                //第一个字节写入本次传输的数据量的大小
                outputStream.write(length);
                //写入真正需要传输的内容
                outputStream.write(contentBytes);
                //刷新缓冲区
                outputStream.flush();

                if (Constants.KEY_WORD_QUIT.equals(message)) {
                    //客户端退出
                    SINGLE_THREAD_EXECUTOR.shutdownNow();
                    inputStream.close();
                    outputStream.close();
                    socket.close();
                    break;
                }
            }
        } catch (IOException e) {
            logger.error("发生异常", e);
        }
    }

    public static void main(String[] args) {
        new BioChatClient().start();
    }

}
