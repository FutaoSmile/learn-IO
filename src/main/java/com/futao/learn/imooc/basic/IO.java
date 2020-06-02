package com.futao.learn.imooc.basic;


import com.futao.learn.imooc.chatroom.Const;

import java.io.*;
import java.nio.file.Files;

/**
 * 默认缓冲区大小8 * 1024B.
 * 为了更好地使用内置缓冲区的磁盘，同样建议把缓冲区大小设置成1024的整数倍。
 *
 * @author futao
 * @date 2020/6/2
 */
public class IO {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        //字节流字符流读取/写入文件
        file();

        //JavaObj序列化
//        obj();

        //使用管道，实现在两个线程之间的通信
//        pip();

        //字节流转换成字符流
//        charFromByte();

        data();
    }

    private static void data() throws IOException {
        String pathname = "/Users/futao/src/backend/java/imooc/learn-imooc-netty/src/main/java/com/futao/learn/imooc/basic";
        File file = new File(pathname + "/txt.json");
        if (!file.exists()) {
            file.createNewFile();
        }
        DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
        System.out.println(dataInputStream.readInt());

    }


    //从字节流中整合字符流
    private static void charFromByte() throws IOException {
        String pathname = "/Users/futao/src/backend/java/imooc/learn-imooc-netty/src/main/java/com/futao/learn/imooc/basic";
        File file = new File(pathname + "/obj.txt");
        if (!file.exists()) {
            file.createNewFile();
        }

        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(bufferedOutputStream);
        outputStreamWriter.write("使用OutputStreamWriter字符流封装OutputStream字节流");
        //通过调用flush()方法，可以把缓冲区内的数据刷新到磁盘(或者网络，以及其他任何形式的目标媒介)中。
        outputStreamWriter.flush();
        bufferedOutputStream.close();
        outputStreamWriter.close();


        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
        InputStreamReader inputStreamReader = new InputStreamReader(bufferedInputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        char[] bufferCache = new char[3];
        while (bufferedReader.read(bufferCache) != -1) {
            System.out.println("读取:" + new String(bufferCache));
        }

    }


    //需要显示的关闭管道，否则会报错`Write end dead`
    public static void pip() throws IOException {
        PipedReader pipedReader = new PipedReader();
        PipedWriter pipedWriter = new PipedWriter();
        pipedReader.connect(pipedWriter);

        new Thread(() -> {
            try {
                Thread.sleep(1000L);

                pipedWriter.write("我是你爸爸!!!");
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                try {
                    pipedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();


        //请记得，当使用两个相关联的管道流时，务必将它们分配给不同的线程。read()方法和write()方法调用时会导致流阻塞，这意味着如果你尝试在一个线程中同时进行读和写，可能会导致线程死锁。

        new Thread(() -> {
            try {
                char[] cbuf = new char[1];
                while (pipedReader.read(cbuf) != -1) {
                    System.out.println("读取到的数据为:" + new String(cbuf));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    pipedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static void obj() throws IOException, ClassNotFoundException {
        String pathname = "/Users/futao/src/backend/java/imooc/learn-imooc-netty/src/main/java/com/futao/learn/imooc/basic";
        File file = new File(pathname + "/obj.txt");
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
        objectOutputStream.writeObject(new Const());
        objectOutputStream.close();

        fileOutputStream.close();
        objectOutputStream.close();


        ObjectInputStream objectInputStream = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
        Const aConst = (Const) objectInputStream.readObject();
        System.out.println(aConst);
        objectInputStream.close();
    }


    private static void file() throws IOException {
        String pathname = "/Users/futao/src/backend/java/imooc/learn-imooc-netty/src/main/java/com/futao/learn/imooc/basic";
        File file = new File(pathname + "/txt.json");
        if (!file.exists()) {
            file.createNewFile();
        }

        //字节流   InputStream/OutputStream
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        byte[] buffedCache = new byte[1];

        while (bufferedInputStream.read(buffedCache) != -1) {
            System.out.println("本次读取到的数据为:" + new String(buffedCache));
        }


        //字符流   Reader/Writer

        //向文件中写入数据
        FileWriter writer = new FileWriter(file, false);
        //使用BufferedWriter进行封装，缓冲区
        BufferedWriter bufferedWriter = new BufferedWriter(writer);
        //写入数据
        bufferedWriter.write("123");
        //数据刷入
        //通过调用flush()方法，可以把缓冲区内的数据刷新到磁盘(或者网络，以及其他任何形式的目标媒介)中。
        bufferedWriter.flush();
        //关闭流
        bufferedWriter.close();
        writer.close();
    }

}
