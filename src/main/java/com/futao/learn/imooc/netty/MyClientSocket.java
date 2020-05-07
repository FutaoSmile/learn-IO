package com.futao.learn.imooc.netty;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * 客户端 Socket
 *
 * @author futao
 * @date 2020/5/6
 */
public class MyClientSocket {

    public static final String QUIT_KEY_WORD = "quit";

    public static void main(String[] args) {
        try (Socket socket = new Socket("127.0.0.1", MyServerSocket.SERVER_PORT)) {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while (true) {
                BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
                String input = consoleReader.readLine();


                writer.write(input + "\n");
                writer.flush();


                String msg = reader.readLine();
                System.out.println("接收到服务端的消息；" + msg);

                if (QUIT_KEY_WORD.equals(input)) {
                    writer.close();
                    reader.close();
                    socket.close();
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
