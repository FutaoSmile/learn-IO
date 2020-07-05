package com.futao.practice.chatroom.bio;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author futao
 * @date 2020/7/2
 */
public class IOUtils {

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
}
