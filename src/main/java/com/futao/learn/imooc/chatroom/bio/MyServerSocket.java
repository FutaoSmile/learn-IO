package com.futao.learn.imooc.chatroom.bio;

import com.futao.learn.imooc.chatroom.bio.MyClientSocket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * 服务端ServerSocket
 *
 * @author futao
 * @date 2020/5/6
 */
public class MyServerSocket {

    public static final int SERVER_PORT = 8888;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("服务端启动，端口:" + serverSocket.getLocalPort());
            while (true) {
                //线程将阻塞，直到有客户端进行连接
                Socket socket = serverSocket.accept();
                System.out.println("接收到客户端请求." + socket.getPort());
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

                String msg = null;
                while ((msg = reader.readLine()) != null) {
                    //读取客户端发送的消息
                    System.out.println("[客户端" + socket.getPort() + "]：" + msg);
                    //发送响应
                    writer.write("来自服务端的响应:" + msg + "\n");
                    //刷新缓冲区，保证数据全部发送
                    writer.flush();

                    //退出系统
                    if (MyClientSocket.QUIT_KEY_WORD.equals(msg)) {
                        System.out.println("客户端[" + socket.getPort() + "]断开连接");
                        break;
                    }
                }

                reader.close();
                writer.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
