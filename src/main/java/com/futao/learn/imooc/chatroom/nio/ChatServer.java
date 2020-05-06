package com.futao.learn.imooc.chatroom.nio;

import com.futao.learn.imooc.chatroom.Const;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author futao
 * @date 2020/5/6
 */
@Slf4j
public class ChatServer {
    /**
     * 已经连接的客户端
     * 用port来标识每一个客户端
     */
    private static final Map<Integer, Writer> CONNECTED_CLIENTS = new HashMap<>();

    private static final ExecutorService EXECUTORS = Executors.newCachedThreadPool();

    /**
     * 添加客户端-客户端上线
     *
     * @param clientSocket
     */
    public void addClient(Socket clientSocket) {
        int port = clientSocket.getPort();
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            CONNECTED_CLIENTS.put(port, bufferedWriter);
            log.info(">>>>>>>>>>>>> 客户端[{}]上线", port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 客户端下线
     *
     * @param clientSocket
     */
    public void removeClient(Socket clientSocket) {
        int port = clientSocket.getPort();
        Writer writer = CONNECTED_CLIENTS.get(port);
        if (writer != null) {
            try {
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            CONNECTED_CLIENTS.remove(port);
            log.info("<<<<<<<<<< 客户端[{}]下线", port);
        }
    }

    /**
     * 转发消息给当前连接到服务器的所有用户
     *
     * @param sender
     * @param msg
     */
    public void forwardMsg(Socket sender, String msg) {
        CONNECTED_CLIENTS.forEach((port, client) -> {
            int senderPort = sender.getPort();
            if (senderPort != port) {
                //不发送给自己
                try {
                    client.write(msg + "\t\t<from>" + senderPort + "\n");
                    client.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(Const.SERVER_PORT)) {
            log.info("服务器启动成功...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                EXECUTORS.execute(() -> {
                    //上线
                    addClient(clientSocket);
                    BufferedReader bufferedReader = null;
                    try {
                        bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    while (true) {
                        try {
                            String msg = bufferedReader.readLine();
                            forwardMsg(clientSocket, msg);
                            if (Const.EXIT_KEY_WORD.equals(msg)) {
                                //退出
                                removeClient(clientSocket);
                                bufferedReader.close();
                                clientSocket.close();
                                break;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ChatServer().startServer();
    }

}
