package com.futao.learn.imooc.filecopy;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author futao
 * @date 2020/6/29.
 */
public class MainTest {

    public static void main(String[] args) throws IOException {
        InputStream is = new ByteArrayInputStream("123131231sadasdsa".getBytes(StandardCharsets.UTF_8));
        byte[] bytes = new byte[1024];
        int totalSize = is.read(bytes);

        System.out.println(new String(bytes, StandardCharsets.UTF_8));


        // TODO: 2020/6/29 为什么要这样写，打印出来的是乱码
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < totalSize; i++) {
            sb.append((char) bytes[i]);
        }
        System.out.println(sb.toString());

    }
}
