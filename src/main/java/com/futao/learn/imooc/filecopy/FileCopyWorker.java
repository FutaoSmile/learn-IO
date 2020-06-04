package com.futao.learn.imooc.filecopy;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;

/**
 * byte=>B->KB->MB->GB
 *
 * @author futao
 * @date 2020/5/7
 */
@Slf4j
public class FileCopyWorker {

    public static void main(String[] args) {

        String apk = "/Users/futao/Desktop/app-debug.apk";
        String apkTarget = "/Users/futao/src/backend/java/imooc/learn-imooc-netty/app2.apk";
        String md1 = "/Users/futao/src/backend/java/imooc/learn-imooc-netty/readme.md";
        String md2 = "/Users/futao/src/backend/java/imooc/learn-imooc-netty/readme22.md";


//        noBufferStreamCopy(apk, apkTarget);
//        bufferedStreamCopy(,);

        nioBufferCopy(md1, md2);
    }

    /**
     * 使用文件输入输出流实现文件拷贝
     * 无缓存，一个字节一个字节读取与写入
     */
    public static void noBufferStreamCopy(String sourcePath, String targetPath) {
        FileCopyRunner copyRunner = new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                try (FileInputStream inputStream = new FileInputStream(source);
                     FileOutputStream outputStream = new FileOutputStream(target)) {
                    int byteData;
                    //每次只读取一个字节，直到read()函数返回-1，即文件结尾
                    while ((byteData = inputStream.read()) != -1) {
                        outputStream.write(byteData);
                        outputStream.flush();
                    }
                    log.info("文件拷贝完毕");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        copyRunner.copyFile(new File(sourcePath), new File(targetPath));
    }

    /**
     * 使用文件输入输出流与缓冲区实现文件拷贝
     *
     * @param sourcePath
     * @param targetPath
     */
    public static void bufferedStreamCopy(String sourcePath, String targetPath) {
        new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(source));
                     BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(target))) {
                    //缓冲区大小
                    byte[] buffer = new byte[1024];
                    int size;
                    //read()的返回值：如果不是文件结尾，则返回读出的字节数组大小，否则返回-1
                    while ((size = inputStream.read(buffer)) != -1) {
                        //这边需要我们手动指定写入的字节数组长度，因为如果不指定长度，则默认取的是buffer.length，可能导致文件最后几个字节都是0
                        outputStream.write(buffer, 0, size);
                        outputStream.flush();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.copyFile(new File(sourcePath), new File(targetPath));
    }


    public static void nioBufferCopy(String sourcePath, String targetPath) {
        new FileCopyRunner() {
            @Override
            public void copyFile(File source, File target) {
                HashSet<OpenOption> readOptions = new HashSet<>();
                readOptions.add(StandardOpenOption.READ);

                HashSet<OpenOption> writeOptions = new HashSet<>();
                //可读
//                writeOptions.add(StandardOpenOption.READ);
                //可写(覆盖)
//                writeOptions.add(StandardOpenOption.WRITE);
                //可写(追加)
                writeOptions.add(StandardOpenOption.APPEND);
                //如果不存在则创建
                writeOptions.add(StandardOpenOption.CREATE);


                try (FileChannel readChannel = FileChannel.open(source.toPath(), readOptions);
                     FileChannel writeChannel = FileChannel.open(target.toPath(), writeOptions)) {
                    //缓冲区(byteBuffer有写模式与读模式，要注意模式的转换)
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 8);
                    //从通道readChannel读取数据，写到buffer
                    while (readChannel.read(byteBuffer) != -1) {
                        byteBuffer.flip();

                        //如果有数据
                        while (byteBuffer.hasRemaining()) {
                            //从buffer读取数据，写到writeChannel
                            writeChannel.write(byteBuffer);
                        }

                        byteBuffer.clear();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.copyFile(new File(sourcePath), new File(targetPath));
    }
}
