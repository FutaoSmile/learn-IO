package com.futao.learn.imooc.chatroom.bio.threads;

import com.futao.learn.imooc.chatroom.Const;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author futao
 * @date 2020/5/6
 */
@Slf4j
public class Client {

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();


    public static void main(String[] args) {
        try (Socket socket = new Socket("127.0.0.1", Const.SERVER_PORT)) {
            EXECUTOR_SERVICE.execute(() -> {
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String msg;
                    while (!socket.isInputShutdown() && (msg = bufferedReader.readLine()) != null) {
                        log.info("接收到消息:{}", msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            while (true) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                String line = reader.readLine();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                writer.write(line + "\n");
                writer.flush();

                if (Const.EXIT_KEY_WORD.equals(line)) {
                    EXECUTOR_SERVICE.shutdown();
                    writer.close();
                    reader.close();
                    break;
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
