package com.futao.practice.chatroom.bio;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Set;

/**
 * @author futao
 * @date 2020/7/2
 */
public class IOUtils {

    private static final Logger logger = LoggerFactory.getLogger(IOUtils.class);

    /**
     * 从输入流中读取字符串
     *
     * @param is 输入流
     * @return 读取到的字符串
     * @throws IOException
     */
    public static String readString(InputStream is) throws IOException {
        //使用带有缓冲区的BufferInputStream以提高读取性能
        BufferedInputStream bufferedInputStream = new BufferedInputStream(is);
        //从缓冲区中一次读取的数据
        byte[] buffer = new byte[1024 * 4];
        //读取的字符串
        StringBuilder fullMessage = new StringBuilder();
        //当前循环读取到的字节个数
        int curBufferSize;
        //循环将数据写入缓冲区buffer，并返回读取到的字节个数。当前数据读取完毕会返回-1。
        // curBufferSize这个参数的作用有两个
        //  1. 判断是否读取到了流的末尾(==-1?)
        //  2. 缓冲区字节数组buffer可能并没有写满，只写了curBufferSize，那么我们只需要将字节数组中前面curBufferSize个字节转换成字符串就行。
        while ((curBufferSize = bufferedInputStream.read(buffer)) != -1) {
            //将buffer中的数据转换成字符串，从buffer的第0个字节开始，读取curBufferSize个字节
            fullMessage.append(new String(buffer, 0, curBufferSize));
        }
        return fullMessage.toString();
    }


    /**
     * 从输入流中读取指定大小的字节数据并转换成字符串
     *
     * @param inputStream 输入流
     * @return 读取到的字符串
     * @throws IOException
     */
    public static String messageReceiver(InputStream inputStream) throws IOException {
        //本次传输的数据量的大小
        int curMessageLength = inputStream.read();
        byte[] contentBytes = new byte[curMessageLength];
        //读取指定长度的字节
        inputStream.read(contentBytes);
        return new String(contentBytes);
    }


    /**
     * 判断客户端是否下线，并且将需要下线的客户端下线
     *
     * @param message         消息
     * @param socket          客户端Socket
     * @param clientSocketSet 当前接入的客户端Socket集合
     * @return 是否退出
     * @throws IOException
     */
    public static boolean isQuit(String message, Socket socket, Set<Socket> clientSocketSet) throws IOException {
        boolean isQuit = StringUtils.isBlank(message) || Constants.KEY_WORD_QUIT.equals(message);
        if (isQuit) {
            clientSocketSet.remove(socket);
            int port = socket.getPort();
            socket.close();
            logger.debug("客户端[{}]下线", port);
        }
        return isQuit;
    }

    /**
     * 转发消息
     *
     * @param curSocketPort   当前发送消息的客户端Socket的端口
     * @param message         需要转发的消息
     * @param clientSocketSet 当前接入的客户端Socket集合
     */
    public static void forwardMessage(int curSocketPort, String message, Set<Socket> clientSocketSet) {
        if (StringUtils.isBlank(message)) {
            return;
        }
        for (Socket socket : clientSocketSet) {
            if (socket.isClosed() || socket.getPort() == curSocketPort) {
                continue;
            }
            if (socket.getPort() != curSocketPort) {
                try {
                    OutputStream outputStream = socket.getOutputStream();
                    byte[] messageBytes = message.getBytes(Constants.CHARSET);
                    outputStream.write(messageBytes.length);
                    //将字符串编码之后写入客户端
                    outputStream.write(messageBytes);
                    //刷新缓冲区
                    outputStream.flush();
                } catch (IOException e) {
                    logger.error("消息转发失败", e);
                }
            }
        }
    }
}
