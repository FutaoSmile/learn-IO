package com.futao.learn.imooc.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author futao
 * @date 2020/6/30.
 */
public class BIOWebServer {


    public static final String resp = "HTTP/1.1 200 OK\r\n" +
            "Content-Type: text/html;charset=utf-8\r\n" +
            "Vary: Accept-Encoding\r\n\r\n";

    public static final String pathname = System.getProperty("user.dir") + "/src/main/resources/index.html";

    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(8888);
        while (true) {
            Socket socket = serverSocket.accept();
            InputStream inputStream = socket.getInputStream();
            byte[] bytes = new byte[1024];
            int read = inputStream.read(bytes);
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < read; i++) {
                stringBuffer.append((char) bytes[i]);
            }
            System.out.println(socket.getPort() + ":" + stringBuffer.toString());

            OutputStream outputStream = socket.getOutputStream();

            outputStream.write(resp.getBytes());

            byte[] writeCache = new byte[1024];
            FileInputStream fis = new FileInputStream(new File(pathname));
            int len;
            while ((len = fis.read(writeCache)) != -1) {
                outputStream.write(writeCache, 0, len);
            }
            socket.close();
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println(BIOWebServer.class.getName());
        System.out.println(BIOWebServer.class.getSimpleName());
        new BIOWebServer().start();
    }
}
