package com.futao.practice.chatroom.bio.v4;

import com.futao.practice.chatroom.bio.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author futao
 * @date 2020/7/2
 */
public class BioChatClient {

    private static final Logger logger = LoggerFactory.getLogger(BioChatClient.class);

    /**
     * 开启这个线程的目的是，当用户输入了退出指令，需要通知监听响应的线程也结束，
     * 否则如果监听响应的线程还处于阻塞状态的话，客户端应用是无法停止的
     */
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public void start() {
        try {
            Socket socket = new Socket("localhost", Constants.SERVER_PORT);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), Constants.CHARSET));
            OutputStream outputStream = socket.getOutputStream();

            logger.debug("========== 成功连接到聊天服务器 ==========");
            new Thread(() -> {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in, Constants.CHARSET));
                while (true) {
                    try {
                        String userInputStr = bufferedReader.readLine();
                        //需要加上换行符
                        outputStream.write((userInputStr + "\n").getBytes(Constants.CHARSET));
                        outputStream.flush();
                        if (Constants.KEY_WORD_QUIT.equals(userInputStr)) {
                            reader.close();
                            outputStream.close();
                            socket.close();
                            executorService.shutdownNow();
                            break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                logger.debug("========== 退出聊天 ==========");
            }).start();


            executorService.execute(() -> {
                //线程一直监听服务端发送的消息
                String message;
                try {
                    while (!socket.isInputShutdown() && (message = reader.readLine()) != null) {
                        logger.info("接收到消息:[{}]", message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
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
