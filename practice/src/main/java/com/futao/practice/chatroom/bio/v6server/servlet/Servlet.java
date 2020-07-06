package com.futao.practice.chatroom.bio.v6server.servlet;

/**
 * Servlet规范接口
 *
 * @author futao
 * @date 2020/7/6
 */
public interface Servlet {
    /**
     * 业务处理程序
     *
     * @return 响应
     */
    Object service();
}
