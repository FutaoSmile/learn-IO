package com.futao.practice.chatroom.nio.demo;

import com.futao.practice.chatroom.bio.Constants;
import org.apache.commons.lang3.StringUtils;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.zip.ZipOutputStream;

/**
 * ByteBuffer的代码演示
 *
 * @author futao
 * @date 2020/7/7
 */
public class ByteBufferDemo {
    public static void main(String[] args) {
        // ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024 * 4);
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 4);

        // Q: 初始化之后，这三个值分别是多少呢？
        System.out.println("position: " + byteBuffer.position());
        System.out.println("capacity: " + byteBuffer.capacity());
        System.out.println("limit: " + byteBuffer.limit());

        //向ByteBuffer写入数据
        byteBuffer.put("hello, 喜欢天文的pony站长~".getBytes(Constants.CHARSET));

        // Q: 向ByteBuffer中写入数据之后，哪些值会发生变化呢？
        System.out.println(StringUtils.repeat("=", 10) + "写入数据之后" + StringUtils.repeat("=", 10));
        System.out.println("position: " + byteBuffer.position());
        System.out.println("capacity: " + byteBuffer.capacity());
        System.out.println("limit: " + byteBuffer.limit());

        //将ByteBuffer从写模式调整为读模式
        byteBuffer.flip();

        //将ByteBuffer从写模式调整为读模式之后，ByteBuffer的哪些值会发生变化？
        System.out.println(StringUtils.repeat("=", 10) + "调整ByteBuffer为读模式之后" + StringUtils.repeat("=", 10));
        System.out.println("position: " + byteBuffer.position());
        System.out.println("capacity: " + byteBuffer.capacity());
        System.out.println("limit: " + byteBuffer.limit());

        //从ByteBuffer中读取一个字节
        byteBuffer.get();
        // Q: 从ByteBuffer中读取一个字节之后，哪些值会发生变化?
        System.out.println(StringUtils.repeat("=", 10) + "从ByteBuffer中读取一个字节之后" + StringUtils.repeat("=", 10));
        System.out.println("position: " + byteBuffer.position());
        System.out.println("capacity: " + byteBuffer.capacity());
        System.out.println("limit: " + byteBuffer.limit());

        // 记录一个标记
        byteBuffer.mark();
        // 继续往下读取
        byteBuffer.get();
        System.out.println(StringUtils.repeat("=", 10) + "记录一个标记之后继续往下读取" + StringUtils.repeat("=", 10));
        System.out.println("position: " + byteBuffer.position());
        System.out.println("capacity: " + byteBuffer.capacity());
        System.out.println("limit: " + byteBuffer.limit());

        //重置position到上一次mark()的标记位置
        byteBuffer.reset();
        System.out.println(StringUtils.repeat("=", 10) + "reset之后" + StringUtils.repeat("=", 10));
        System.out.println("position: " + byteBuffer.position());
        System.out.println("capacity: " + byteBuffer.capacity());
        System.out.println("limit: " + byteBuffer.limit());

        System.out.println("byteBuffer中是否还有数据:" + byteBuffer.hasRemaining());
        //拷贝未读取的数据到缓冲区最前面
        byteBuffer.compact();
        System.out.println(StringUtils.repeat("=", 10) + "compact之后" + StringUtils.repeat("=", 10));
        System.out.println("position: " + byteBuffer.position());
        System.out.println("capacity: " + byteBuffer.capacity());
        System.out.println("limit: " + byteBuffer.limit());

        //逻辑上清空数据=>实际上只是指针的变化
        byteBuffer.clear();
        System.out.println(StringUtils.repeat("=", 10) + "clear之后" + StringUtils.repeat("=", 10));
        System.out.println("position: " + byteBuffer.position());
        System.out.println("capacity: " + byteBuffer.capacity());
        System.out.println("limit: " + byteBuffer.limit());
    }
}
