package com.futao.learn.imooc.nio.filecopy;

import java.io.File;

/**
 * @author futao
 * @date 2020/5/7
 */
public interface FileCopyRunner {
    /**
     * 复制文件
     *
     * @param source 源文件
     * @param target 目标文件
     */
    void copyFile(File source, File target);
}
